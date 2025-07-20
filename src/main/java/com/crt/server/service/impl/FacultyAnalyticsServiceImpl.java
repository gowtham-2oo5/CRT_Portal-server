package com.crt.server.service.impl;

import com.crt.server.dto.AttendanceAnalyticsDTO;
import com.crt.server.dto.WeeklyTimetableDTO;
import com.crt.server.model.*;
import com.crt.server.repository.AttendanceRepository;
import com.crt.server.repository.AttendanceSessionRepository;
import com.crt.server.repository.TimeSlotRepository;
import com.crt.server.service.FacultyAnalyticsService;
import com.crt.server.service.FacultyTimetableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyAnalyticsServiceImpl implements FacultyAnalyticsService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final FacultyTimetableService facultyTimetableService;

    @Override
    public AttendanceAnalyticsDTO getAttendanceAnalytics(User faculty, UUID sectionId, String startDate, String endDate) {
        log.info("Getting attendance analytics for faculty: {}", faculty.getUsername());
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        // Get attendance sessions in date range
        List<AttendanceSession> sessions = attendanceSessionRepository
                .findByFacultyAndDateBetweenOrderByDateDesc(faculty, start, end);
        
        // Filter by section if specified
        if (sectionId != null) {
            sessions = sessions.stream()
                    .filter(session -> session.getSection().getId().equals(sectionId))
                    .collect(Collectors.toList());
        }
        
        // Calculate overall stats
        AttendanceAnalyticsDTO.OverallStatsDTO overallStats = calculateOverallStats(sessions);
        
        // Calculate section stats
        List<AttendanceAnalyticsDTO.SectionStatsDTO> sectionStats = calculateSectionStats(sessions);
        
        // Calculate daily trends
        List<AttendanceAnalyticsDTO.DailyAttendanceDTO> dailyTrends = calculateDailyTrends(sessions);
        
        // Get top and low performers
        List<AttendanceAnalyticsDTO.StudentPerformanceDTO> topPerformers = getTopPerformers(sessions, 5);
        List<AttendanceAnalyticsDTO.StudentPerformanceDTO> lowPerformers = getLowPerformers(sessions, 5);
        
        return AttendanceAnalyticsDTO.builder()
                .overallStats(overallStats)
                .sectionStats(sectionStats)
                .dailyTrends(dailyTrends)
                .topPerformers(topPerformers)
                .lowPerformers(lowPerformers)
                .build();
    }

    @Override
    public WeeklyTimetableDTO getWeeklyTimetable(User faculty, String week) {
        log.info("Getting weekly timetable for faculty: {}, week: {}", faculty.getUsername(), week);
        
        // Get all time slots for faculty
        List<TimeSlot> timeSlots = timeSlotRepository.findByInchargeFaculty(faculty);
        
        // Convert to weekly schedule with attendance status
        List<WeeklyTimetableDTO.WeeklyScheduleDTO> weeklySchedule = timeSlots.stream()
                .filter(slot -> !slot.isBreak())
                .map(slot -> {
                    // Check if attendance was submitted for today
                    boolean attendanceSubmitted = attendanceSessionRepository
                            .existsByFacultyAndTimeSlotAndDate(faculty, slot, LocalDate.now());
                    
                    // Get topic taught if attendance was submitted
                    String topicTaught = attendanceSessionRepository
                            .findByFacultyAndTimeSlotAndDate(faculty, slot, LocalDate.now())
                            .map(AttendanceSession::getTopicTaught)
                            .orElse(null);
                    
                    return WeeklyTimetableDTO.WeeklyScheduleDTO.builder()
                            .id(slot.getId().toString())
                            .day("Today") // Simplified for now
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .sectionName(slot.getSection().getName())
                            .room(slot.getRoom().toString())
                            .attendanceSubmitted(attendanceSubmitted)
                            .topicTaught(topicTaught)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Get current and next slots
        var currentSlot = facultyTimetableService.getCurrentActiveTimeSlot(faculty);
        
        return WeeklyTimetableDTO.builder()
                .weeklyTimetable(weeklySchedule)
                .currentSlot(currentSlot.map(slot -> 
                        com.crt.server.dto.TodayScheduleDTO.builder()
                                .id(slot.getId().toString())
                                .day("Today")
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .sectionName(slot.getSection().getName())
                                .room(slot.getRoom().toString())
                                .isActive(true)
                                .build()
                ).orElse(null))
                .build();
    }

    @Override
    public byte[] exportAttendanceReportCSV(User faculty, UUID sectionId, String startDate, String endDate) {
        log.info("Exporting attendance report CSV for faculty: {}", faculty.getUsername());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(baos)) {
            
            // Write CSV header
            writer.println("Date,Section,Topic Taught,Total Students,Present,Absent,Attendance %");
            
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            List<AttendanceSession> sessions = attendanceSessionRepository
                    .findByFacultyAndDateBetweenOrderByDateDesc(faculty, start, end);
            
            if (sectionId != null) {
                sessions = sessions.stream()
                        .filter(session -> session.getSection().getId().equals(sectionId))
                        .collect(Collectors.toList());
            }
            
            // Write data rows
            for (AttendanceSession session : sessions) {
                writer.printf("%s,%s,%s,%d,%d,%d,%.2f%%\n",
                        session.getDate().toString(),
                        session.getSection().getName(),
                        session.getTopicTaught(),
                        session.getTotalStudents(),
                        session.getPresentCount(),
                        session.getAbsentCount(),
                        session.getAttendancePercentage());
            }
            
            writer.flush();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error exporting CSV: {}", e.getMessage());
            throw new RuntimeException("Failed to export CSV", e);
        }
    }

    private AttendanceAnalyticsDTO.OverallStatsDTO calculateOverallStats(List<AttendanceSession> sessions) {
        if (sessions.isEmpty()) {
            return AttendanceAnalyticsDTO.OverallStatsDTO.builder()
                    .totalSessions(0)
                    .totalStudents(0)
                    .averageAttendance(0.0)
                    .presentToday(0)
                    .absentToday(0)
                    .build();
        }
        
        int totalSessions = sessions.size();
        Set<String> uniqueStudents = sessions.stream()
                .flatMap(session -> session.getSection().getStudents().stream())
                .map(student -> student.getId().toString())
                .collect(Collectors.toSet());
        
        double averageAttendance = sessions.stream()
                .mapToDouble(AttendanceSession::getAttendancePercentage)
                .average()
                .orElse(0.0);
        
        // Today's stats
        LocalDate today = LocalDate.now();
        List<AttendanceSession> todaySessions = sessions.stream()
                .filter(session -> session.getDate().equals(today))
                .collect(Collectors.toList());
        
        int presentToday = todaySessions.stream()
                .mapToInt(AttendanceSession::getPresentCount)
                .sum();
        
        int absentToday = todaySessions.stream()
                .mapToInt(AttendanceSession::getAbsentCount)
                .sum();
        
        return AttendanceAnalyticsDTO.OverallStatsDTO.builder()
                .totalSessions(totalSessions)
                .totalStudents(uniqueStudents.size())
                .averageAttendance(averageAttendance)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .build();
    }

    private List<AttendanceAnalyticsDTO.SectionStatsDTO> calculateSectionStats(List<AttendanceSession> sessions) {
        // Group sessions by section
        Map<String, List<AttendanceSession>> sessionsBySection = sessions.stream()
                .collect(Collectors.groupingBy(session -> session.getSection().getId().toString()));
        
        // Process each section in parallel
        return sessionsBySection.entrySet().parallelStream()
                .map(entry -> {
                    List<AttendanceSession> sectionSessions = entry.getValue();
                    AttendanceSession firstSession = sectionSessions.get(0);
                    Section section = firstSession.getSection();
                    
                    double averageAttendance = sectionSessions.stream()
                            .mapToDouble(AttendanceSession::getAttendancePercentage)
                            .average()
                            .orElse(0.0);
                    
                    return AttendanceAnalyticsDTO.SectionStatsDTO.builder()
                            .sectionId(section.getId().toString())
                            .sectionName(section.getName())
                            .totalStudents(section.getStrength())
                            .totalSessions(sectionSessions.size())
                            .averageAttendance(averageAttendance)
                            .TrainingName(section.getTraining().getName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AttendanceAnalyticsDTO.DailyAttendanceDTO> calculateDailyTrends(List<AttendanceSession> sessions) {
        Map<LocalDate, List<AttendanceSession>> sessionsByDate = sessions.stream()
                .collect(Collectors.groupingBy(AttendanceSession::getDate));
        
        return sessionsByDate.entrySet().parallelStream()
                .sorted(Map.Entry.<LocalDate, List<AttendanceSession>>comparingByKey().reversed())
                .limit(30) // Last 30 days
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<AttendanceSession> daySessions = entry.getValue();
                    
                    int totalSessions = daySessions.size();
                    int totalPresent = daySessions.stream()
                            .mapToInt(AttendanceSession::getPresentCount)
                            .sum();
                    int totalAbsent = daySessions.stream()
                            .mapToInt(AttendanceSession::getAbsentCount)
                            .sum();
                    
                    double percentage = (totalPresent + totalAbsent) > 0 ? 
                            (totalPresent * 100.0 / (totalPresent + totalAbsent)) : 0.0;
                    
                    return AttendanceAnalyticsDTO.DailyAttendanceDTO.builder()
                            .date(date.toString())
                            .totalSessions(totalSessions)
                            .totalPresent(totalPresent)
                            .totalAbsent(totalAbsent)
                            .attendancePercentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AttendanceAnalyticsDTO.StudentPerformanceDTO> getTopPerformers(List<AttendanceSession> sessions, int limit) {
        return getStudentPerformance(sessions)
                .stream()
                .sorted((a, b) -> Double.compare(b.getAttendancePercentage(), a.getAttendancePercentage()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<AttendanceAnalyticsDTO.StudentPerformanceDTO> getLowPerformers(List<AttendanceSession> sessions, int limit) {
        return getStudentPerformance(sessions)
                .stream()
                .sorted((a, b) -> Double.compare(a.getAttendancePercentage(), b.getAttendancePercentage()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<AttendanceAnalyticsDTO.StudentPerformanceDTO> getStudentPerformance(List<AttendanceSession> sessions) {
        Set<Student> allStudents = sessions.stream()
                .flatMap(session -> session.getSection().getStudents().stream())
                .collect(Collectors.toSet());
        
        // Process student performance in parallel
        return allStudents.parallelStream()
                .map(student -> {
                    List<TimeSlot> relevantTimeSlots = sessions.stream()
                            .map(AttendanceSession::getTimeSlot)
                            .collect(Collectors.toList());
                    
                    long totalSessions = attendanceRepository.countByStudentAndTimeSlotIn(student, relevantTimeSlots);
                    long attendedSessions = attendanceRepository.countByStudentAndStatusAndTimeSlotIn(
                            student, AttendanceStatus.PRESENT, relevantTimeSlots);
                    
                    double percentage = totalSessions > 0 ? (attendedSessions * 100.0 / totalSessions) : 0.0;
                    
                    String sectionName = sessions.stream()
                            .filter(s -> s.getSection().getStudents().contains(student))
                            .findFirst()
                            .map(s -> s.getSection().getName())
                            .orElse("Unknown");
                    
                    return AttendanceAnalyticsDTO.StudentPerformanceDTO.builder()
                            .studentId(student.getId().toString())
                            .rollNumber(student.getRegNum())
                            .name(student.getName())
                            .section(sectionName)
                            .attendancePercentage(percentage)
                            .totalSessions((int) totalSessions)
                            .attendedSessions((int) attendedSessions)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
