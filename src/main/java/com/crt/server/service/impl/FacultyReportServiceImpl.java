package com.crt.server.service.impl;

import com.crt.server.dto.FacultyAttendanceReportDTO;
import com.crt.server.dto.PagedResponseDTO;
import com.crt.server.dto.StudentAttendanceDetailDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.FacultyReportService;
import com.crt.server.service.FacultyTimetableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyReportServiceImpl implements FacultyReportService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final FacultyTimetableService facultyTimetableService;

    @Override
    public FacultyAttendanceReportDTO getAttendanceReports(User faculty, UUID sectionId, int limit) {
        log.info("Getting attendance reports for faculty: {}, sectionId: {}", faculty.getUsername(), sectionId);
        
        // Get faculty's assigned sections
        var assignedSections = facultyTimetableService.getAssignedSections(faculty);
        
        // Get attendance sessions for faculty
        List<AttendanceSession> allSessions = attendanceSessionRepository.findByFacultyOrderByDateDesc(faculty);
        
        // Filter by section if specified
        if (sectionId != null) {
            allSessions = allSessions.stream()
                    .filter(session -> session.getSection().getId().equals(sectionId))
                    .collect(Collectors.toList());
        }
        
        // Limit results
        final List<AttendanceSession> finalSessions = allSessions.stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        // Pre-calculate time slots for efficiency
        final List<TimeSlot> timeSlots = finalSessions.stream()
                .map(AttendanceSession::getTimeSlot)
                .collect(Collectors.toList());
        
        // Get all students from these sessions and calculate their attendance in parallel
        List<FacultyAttendanceReportDTO.StudentAttendanceReportDTO> studentReports = finalSessions.stream()
                .flatMap(session -> session.getSection().getStudents().stream())
                .distinct()
                .parallel() // Use parallel stream for concurrent processing
                .map(student -> {
                    // Calculate attendance for this student
                    long totalSessions = attendanceRepository.countByStudentAndTimeSlotIn(student, timeSlots);
                    
                    long attendedSessions = attendanceRepository.countByStudentAndStatusAndTimeSlotIn(
                            student, AttendanceStatus.PRESENT, timeSlots);
                    
                    double percentage = totalSessions > 0 ? (attendedSessions * 100.0 / totalSessions) : 0.0;
                    
                    // Get last attended date
                    LocalDateTime lastAttended = attendanceRepository
                            .findTopByStudentAndStatusOrderByDateDesc(student, AttendanceStatus.PRESENT)
                            .map(Attendance::getDate)
                            .orElse(null);
                    
                    return FacultyAttendanceReportDTO.StudentAttendanceReportDTO.builder()
                            .studentId(student.getId().toString())
                            .rollNumber(student.getRegNum())
                            .name(student.getName())
                            .section(finalSessions.stream()
                                    .filter(s -> s.getSection().getStudents().contains(student))
                                    .findFirst()
                                    .map(s -> s.getSection().getName())
                                    .orElse("Unknown"))
                            .totalSessions((int) totalSessions)
                            .attendedSessions((int) attendedSessions)
                            .attendancePercentage(percentage)
                            .lastAttended(lastAttended)
                            .build();
                })
                .collect(Collectors.toList());
        
        return FacultyAttendanceReportDTO.builder()
                .sections(assignedSections)
                .attendanceReports(studentReports)
                .build();
    }

    @Override
    public PagedResponseDTO<FacultyAttendanceReportDTO.StudentAttendanceReportDTO> getAttendanceReportsPaged(
            User faculty, UUID sectionId, int page, int size, String startDate, String endDate) {
        
        log.info("Getting paged attendance reports for faculty: {}", faculty.getUsername());
        
        // For now, implement basic pagination by getting all and slicing
        FacultyAttendanceReportDTO fullReport = getAttendanceReports(faculty, sectionId, 1000);
        List<FacultyAttendanceReportDTO.StudentAttendanceReportDTO> allReports = fullReport.getAttendanceReports();
        
        int start = page * size;
        int end = Math.min(start + size, allReports.size());
        List<FacultyAttendanceReportDTO.StudentAttendanceReportDTO> pagedContent = 
                allReports.subList(start, end);
        
        return PagedResponseDTO.<FacultyAttendanceReportDTO.StudentAttendanceReportDTO>builder()
                .content(pagedContent)
                .page(page)
                .size(size)
                .totalElements(allReports.size())
                .totalPages((int) Math.ceil((double) allReports.size() / size))
                .first(page == 0)
                .last(end >= allReports.size())
                .hasNext(end < allReports.size())
                .hasPrevious(page > 0)
                .build();
    }

    @Override
    public StudentAttendanceDetailDTO getStudentDetailReport(User faculty, UUID studentId) {
        log.info("Getting detailed report for student: {} by faculty: {}", studentId, faculty.getUsername());
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // Get all attendance records for this student in faculty's sessions
        List<AttendanceSession> facultySessions = attendanceSessionRepository.findByFacultyOrderByDateDesc(faculty);
        List<TimeSlot> facultyTimeSlots = facultySessions.stream()
                .map(AttendanceSession::getTimeSlot)
                .collect(Collectors.toList());
        
        List<Attendance> attendanceRecords = attendanceRepository
                .findByStudentAndTimeSlotInOrderByDateDesc(student, facultyTimeSlots);
        
        // Calculate summary
        long totalSessions = attendanceRecords.size();
        long attendedSessions = attendanceRecords.stream()
                .mapToLong(record -> record.getStatus() == AttendanceStatus.PRESENT ? 1 : 0)
                .sum();
        
        double percentage = totalSessions > 0 ? (attendedSessions * 100.0 / totalSessions) : 0.0;
        
        LocalDateTime lastAttended = attendanceRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.PRESENT)
                .map(Attendance::getDate)
                .findFirst()
                .orElse(null);
        
        // Build session history in parallel
        List<StudentAttendanceDetailDTO.SessionHistoryDTO> sessionHistory = attendanceRecords.parallelStream()
                .map(record -> {
                    AttendanceSession session = record.getAttendanceSession();
                    return StudentAttendanceDetailDTO.SessionHistoryDTO.builder()
                            .id(record.getId().toString())
                            .date(record.getDate().toLocalDate().toString())
                            .topicTaught(session != null ? session.getTopicTaught() : "N/A")
                            .timeSlot(record.getTimeSlot().getStartTime() + "-" + record.getTimeSlot().getEndTime())
                            .room(record.getTimeSlot().getRoom().toString())
                            .isPresent(record.getStatus() == AttendanceStatus.PRESENT)
                            .markedAt(record.getPostedAt())
                            .feedback(record.getFeedback())
                            .build();
                })
                .collect(Collectors.toList());
        
        return StudentAttendanceDetailDTO.builder()
                .student(StudentDTO.builder()
                        .id(student.getId().toString())
                        .rollNumber(student.getRegNum())
                        .name(student.getName())
                        .email(student.getEmail())
                        .section(facultySessions.stream()
                                .filter(s -> s.getSection().getStudents().contains(student))
                                .findFirst()
                                .map(s -> s.getSection().getName())
                                .orElse("Unknown"))
                        .build())
                .attendanceSummary(StudentAttendanceDetailDTO.AttendanceSummaryDTO.builder()
                        .totalSessions((int) totalSessions)
                        .attendedSessions((int) attendedSessions)
                        .attendancePercentage(percentage)
                        .lastAttended(lastAttended)
                        .build())
                .sessionHistory(sessionHistory)
                .build();
    }
}
