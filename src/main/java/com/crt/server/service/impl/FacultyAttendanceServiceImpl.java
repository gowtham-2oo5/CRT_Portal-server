package com.crt.server.service.impl;

import com.crt.server.dto.*;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.ActivityLogService;
import com.crt.server.service.FacultyAttendanceService;
import com.crt.server.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyAttendanceServiceImpl implements FacultyAttendanceService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public AttendanceSessionResponseDTO submitAttendance(User faculty, AttendanceSubmissionDTO submissionDTO) {
        log.info("Submitting attendance for faculty: {} on date: {}", faculty.getUsername(), submissionDTO.getDate());

        TimeSlot timeSlot = timeSlotRepository.findById(Integer.valueOf(submissionDTO.getTimeSlotId()))
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found"));

        Section section = sectionRepository.findById(UUID.fromString(submissionDTO.getSectionId()))
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        LocalDate date = LocalDate.parse(submissionDTO.getDate());

        if (faculty.getRole() != Role.ADMIN && !canSubmitAttendance(faculty, timeSlot, submissionDTO.getDate()))
            throw new IllegalStateException("Faculty cannot submit attendance for this time slot");


        log.info("Can submit attendance for faculty: {} on date: {} For time slot {}", faculty.getUsername(), submissionDTO.getDate(), timeSlot.getId());


        boolean isAdmin = faculty.getRole().name().equals("ADMIN");


        boolean attendanceExists = isAttendanceAlreadySubmitted(faculty, timeSlot, submissionDTO.getDate());
        if (attendanceExists) {
            if (isAdmin) {
                log.info("Admin request detected. Deleting existing attendance records for update.");

                AttendanceSession existingSession = attendanceSessionRepository.findByTimeSlotAndDate(timeSlot, date)
                        .orElse(null);

                if (existingSession != null) {

                    List<Attendance> existingAttendance = attendanceRepository.findByAttendanceSession(existingSession);
                    attendanceRepository.deleteAll(existingAttendance);


                    attendanceSessionRepository.delete(existingSession);
                    log.info("Deleted existing attendance session and {} attendance records", existingAttendance.size());
                }
            } else
                throw new IllegalStateException("Attendance already submitted for this time slot and date");

        }

        int totalStudents = submissionDTO.getAttendanceRecords().size();
        int presentCount = submissionDTO.getAttendanceRecords().stream()
                .mapToInt(record -> record.isPresent() ? 1 : 0)
                .sum();
        int absentCount = totalStudents - presentCount;

        SubmissionStatus submissionStatus = determineSubmissionStatus(timeSlot);

        AttendanceSession attendanceSession = AttendanceSession.builder()
                .faculty(faculty)
                .section(section)
                .timeSlot(timeSlot)
                .date(date)
                .topicTaught(submissionDTO.getTopicTaught())
                .totalStudents(totalStudents)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .submissionStatus(submissionStatus)
                .build();

        attendanceSession = attendanceSessionRepository.save(attendanceSession);

        for (AttendanceSubmissionDTO.StudentAttendanceRecordDTO record : submissionDTO.getAttendanceRecords()) {
            Student student = studentRepository.findById(UUID.fromString(record.getStudentId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + record.getStudentId()));

            Attendance attendance = Attendance.builder()
                    .student(student)
                    .timeSlot(timeSlot)
                    .status(record.isPresent() ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT)
                    .feedback(record.getFeedback())
                    .date(date.atStartOfDay())
                    .attendanceSession(attendanceSession)
                    .build();

            attendanceRepository.save(attendance);

            studentService.updateStudentAttendancePercentage(student.getId(), getNewAttdReport(student));
        }

        log.info("Attendance submitted successfully. Session ID: {}", attendanceSession.getId());

        activityLogService.logAttendancePosted(
                faculty,
                section,
                timeSlot,
                attendanceSession.getAbsentCount()
        );

        return AttendanceSessionResponseDTO.builder()
                .id(attendanceSession.getId().toString())
                .facultyId(faculty.getId().toString())
                .sectionId(section.getId().toString())
                .sectionName(section.getName())
                .topicTaught(attendanceSession.getTopicTaught())
                .date(attendanceSession.getDate().toString())
                .timeSlot(timeSlot.getStartTime() + "-" + timeSlot.getEndTime())
                .room(timeSlot.getRoom().toString())
                .totalStudents(attendanceSession.getTotalStudents())
                .presentCount(attendanceSession.getPresentCount())
                .absentCount(attendanceSession.getAbsentCount())
                .attendancePercentage(attendanceSession.getAttendancePercentage())
                .submittedAt(attendanceSession.getSubmittedAt())
                .submissionStatus(attendanceSession.getSubmissionStatus())
                .lateSubmissionReason(attendanceSession.getLateSubmissionReason())
                .build();
    }

    private Double getNewAttdReport(Student student) {
        long totalClasses = attendanceRepository.countAttdByStudent(student);
        long absences = attendanceRepository.countAbsencesByStudent(student);

        return totalClasses > 0 ? ((totalClasses - absences) * 100.0) / totalClasses : 0;
    }

    @Override
    public List<StudentDTO> getStudentsForSection(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        return section.getStudents().stream()
                .map(student -> StudentDTO.builder()
                        .id(student.getId().toString())
                        .rollNumber(student.getRegNum())
                        .regNum(student.getRegNum())
                        .name(student.getName())
                        .email(student.getEmail())
                        .phone(student.getPhone())
                        .crtEligibility(student.getCrtEligibility())
                        .department(student.getBranch().name())
                        .batch(student.getBatch())

                        .section(section.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getStudentsForTimeSlot(Integer timeSlotId) {
        log.debug("Getting students for time slot: {}", timeSlotId);
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found"));
        log.debug("Time slot found: {}-{} {}", timeSlot.getStartTime(), timeSlot.getEndTime(), timeSlot.getSection().getName());
        return getStudentsForSection(timeSlot.getSection().getId());
    }

    @Override
    public boolean canSubmitAttendance(User faculty, TimeSlot timeSlot, String date) {

        return timeSlot.getInchargeFaculty().getId().equals(faculty.getId());
    }

    @Override
    public boolean isAttendanceAlreadySubmitted(User faculty, TimeSlot timeSlot, String date) {
        LocalDate localDate = LocalDate.parse(date);
        return attendanceSessionRepository.existsByFacultyAndTimeSlotAndDate(faculty, timeSlot, localDate);
    }

    @Override
    @Transactional
    public AttendanceSessionDTO submitLateAttendanceReason(LateSubmissionDTO lateSubmissionDTO, User faculty) {
        log.info("Faculty {} submitting late attendance reason for session {}",
                faculty.getUsername(), lateSubmissionDTO.getSessionId());

        AttendanceSession session = attendanceSessionRepository.findById(lateSubmissionDTO.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));

        // Verify that the session belongs to the faculty
        if (!session.getFaculty().getId().equals(faculty.getId())) {
            log.warn("Faculty {} attempted to update session {} belonging to faculty {}",
                    faculty.getUsername(), session.getId(), session.getFaculty().getUsername());
            throw new AccessDeniedException("You don't have permission to update this session");
        }

        session.setSubmissionStatus(SubmissionStatus.LATE);
        session.setLateSubmissionReason(lateSubmissionDTO.getReason());

        AttendanceSession savedSession = attendanceSessionRepository.save(session);
        log.info("Updated submission status for session {} to LATE with reason: {}",
                session.getId(), lateSubmissionDTO.getReason());

        return mapToDTO(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSessionDTO> getMissedAttendanceSessions(User faculty) {
        log.info("Getting missed attendance sessions for faculty {}", faculty.getUsername());

        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // Get all time slots for the faculty
        List<TimeSlot> facultyTimeSlots = timeSlotRepository.findByInchargeFaculty(faculty);

        // Filter time slots that have passed for today
        List<TimeSlot> passedTimeSlots = facultyTimeSlots.stream()
                .filter(slot -> {
                    try {
                        // Parse time in a more robust way
                        LocalTime endTime = parseTimeString(slot.getEndTime());
                        return endTime.isBefore(currentTime);
                    } catch (Exception e) {
                        log.error("Error parsing time slot end time '{}': {}", slot.getEndTime(), e.getMessage());
                        return false;
                    }
                })
                .toList();

        log.debug("Found {} passed time slots for faculty {}", passedTimeSlots.size(), faculty.getUsername());

        // Find sessions that should have been submitted but weren't
        List<AttendanceSessionDTO> missedSessions = new ArrayList<>();

        for (TimeSlot slot : passedTimeSlots) {
            boolean sessionExists = attendanceSessionRepository.existsByFacultyAndTimeSlotAndDate(faculty, slot, today);

            if (!sessionExists) {
                log.debug("Found missed session for time slot {} at {}", slot.getId(), slot.getStartTime());

                // Create a placeholder missed session DTO
                AttendanceSessionDTO missedSession = AttendanceSessionDTO.builder()
                        .facultyId(faculty.getId())
                        .facultyName(faculty.getName())
                        .sectionId(slot.getSection().getId())
                        .sectionName(slot.getSection().getName())
                        .timeSlotId(slot.getId())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .date(today)
                        .submissionStatus(SubmissionStatus.MISSED)
                        .build();

                missedSessions.add(missedSession);
            }
        }

        return missedSessions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSessionDTO> getMissedAttendanceSessionsByDate(User faculty, LocalDate date) {
        log.info("Getting missed attendance sessions for faculty {} on date {}", faculty.getUsername(), date);

        // Get all time slots for the faculty
        List<TimeSlot> facultyTimeSlots = timeSlotRepository.findByInchargeFaculty(faculty);

        // If the date is today, only include time slots that have already passed
        if (date.equals(LocalDate.now())) {
            LocalTime currentTime = LocalTime.now();
            facultyTimeSlots = facultyTimeSlots.stream()
                    .filter(slot -> {
                        try {
                            LocalTime endTime = parseTimeString(slot.getEndTime());
                            return endTime.isBefore(currentTime);
                        } catch (Exception e) {
                            log.error("Error parsing time slot end time '{}': {}", slot.getEndTime(), e.getMessage());
                            return false;
                        }
                    })
                    .toList();
        }

        // Find sessions that should have been submitted but weren't
        List<AttendanceSessionDTO> missedSessions = new ArrayList<>();

        for (TimeSlot slot : facultyTimeSlots) {
            boolean sessionExists = attendanceSessionRepository.existsByFacultyAndTimeSlotAndDate(faculty, slot, date);

            if (!sessionExists) {
                log.debug("Found missed session for time slot {} at {} on date {}",
                        slot.getId(), slot.getStartTime(), date);

                // Create a placeholder missed session DTO
                AttendanceSessionDTO missedSession = AttendanceSessionDTO.builder()
                        .facultyId(faculty.getId())
                        .facultyName(faculty.getName())
                        .sectionId(slot.getSection().getId())
                        .sectionName(slot.getSection().getName())
                        .timeSlotId(slot.getId())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .date(date)
                        .submissionStatus(SubmissionStatus.MISSED)
                        .build();

                missedSessions.add(missedSession);
            }
        }

        return missedSessions;
    }

    private SubmissionStatus determineSubmissionStatus(TimeSlot timeSlot) {
        try {
            LocalTime endTime = parseTimeString(timeSlot.getEndTime());
            LocalTime currentTime = LocalTime.now();

            // If submission is after the end time, mark as LATE
            if (currentTime.isAfter(endTime)) {
                return SubmissionStatus.LATE;
            }

            return SubmissionStatus.ON_TIME;
        } catch (Exception e) {
            log.error("Error determining submission status: {}", e.getMessage());
            return SubmissionStatus.ON_TIME;
        }
    }

    private AttendanceSessionDTO mapToDTO(AttendanceSession session) {
        return AttendanceSessionDTO.builder()
                .id(session.getId())
                .facultyId(session.getFaculty().getId())
                .facultyName(session.getFaculty().getName())
                .sectionId(session.getSection().getId())
                .sectionName(session.getSection().getName())
                .timeSlotId(session.getTimeSlot().getId())
                .startTime(session.getTimeSlot().getStartTime())
                .endTime(session.getTimeSlot().getEndTime())
                .date(session.getDate())
                .topicTaught(session.getTopicTaught())
                .totalStudents(session.getTotalStudents())
                .presentCount(session.getPresentCount())
                .absentCount(session.getAbsentCount())
                .attendancePercentage(session.getAttendancePercentage())
                .submittedAt(session.getSubmittedAt())
                .submissionStatus(session.getSubmissionStatus())
                .lateSubmissionReason(session.getLateSubmissionReason())
                .build();
    }

    /**
     * Parse a time string in format "HH:mm" or "H:mm" to LocalTime
     *
     * @param timeString The time string to parse
     * @return LocalTime object
     */
    private LocalTime parseTimeString(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new IllegalArgumentException("Time string cannot be null or empty");
        }

        timeString = timeString.trim();

        // Handle different formats
        if (timeString.matches("\\d{1,2}:\\d{2}")) {
            String[] parts = timeString.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalTime.of(hour, minute);
        } else if (timeString.matches("\\d{1,2}")) {
            // Just hours
            int hour = Integer.parseInt(timeString);
            return LocalTime.of(hour, 0);
        } else {
            throw new IllegalArgumentException("Invalid time format: " + timeString);
        }
    }
}
