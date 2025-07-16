package com.crt.server.service.impl;

import com.crt.server.dto.AttendanceSessionResponseDTO;
import com.crt.server.dto.AttendanceSubmissionDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.FacultyAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Override
    @Transactional
    public AttendanceSessionResponseDTO submitAttendance(User faculty, AttendanceSubmissionDTO submissionDTO) {
        log.info("Submitting attendance for faculty: {} on date: {}", faculty.getUsername(), submissionDTO.getDate());
        
        // Validate inputs
        TimeSlot timeSlot = timeSlotRepository.findById(Integer.valueOf(submissionDTO.getTimeSlotId()))
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found"));
        
        Section section = sectionRepository.findById(UUID.fromString(submissionDTO.getSectionId()))
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        
        LocalDate date = LocalDate.parse(submissionDTO.getDate());
        
        // Validate faculty can submit attendance
        if (!canSubmitAttendance(faculty, timeSlot, submissionDTO.getDate())) {
            throw new IllegalStateException("Faculty cannot submit attendance for this time slot");
        }
        
        // Check for duplicate submission
        if (isAttendanceAlreadySubmitted(faculty, timeSlot, submissionDTO.getDate())) {
            throw new IllegalStateException("Attendance already submitted for this time slot and date");
        }
        
        // Calculate attendance statistics
        int totalStudents = submissionDTO.getAttendanceRecords().size();
        int presentCount = (int) submissionDTO.getAttendanceRecords().stream()
                .mapToInt(record -> record.isPresent() ? 1 : 0)
                .sum();
        int absentCount = totalStudents - presentCount;
        
        // Create attendance session
        AttendanceSession attendanceSession = AttendanceSession.builder()
                .faculty(faculty)
                .section(section)
                .timeSlot(timeSlot)
                .date(date)
                .topicTaught(submissionDTO.getTopicTaught())
                .totalStudents(totalStudents)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .build();
        
        attendanceSession = attendanceSessionRepository.save(attendanceSession);
        
        // Create individual attendance records
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
        }
        
        log.info("Attendance submitted successfully. Session ID: {}", attendanceSession.getId());
        
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
                .build();
    }

    @Override
    public List<StudentDTO> getStudentsForSection(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        
        return section.getStudents().stream()
                .map(student -> StudentDTO.builder()
                        .id(student.getId().toString())
                        .rollNumber(student.getRegNum()) // Using regNum as rollNumber
                        .name(student.getName())
                        .email(student.getEmail())
                        .section(section.getName()) // Using section name from the section
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getStudentsForTimeSlot(Integer timeSlotId) {
        System.out.println("Getting students for time slot: " + timeSlotId);
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found"));
        System.out.println("Time slot found: " + timeSlot.getStartTime() + "-" + timeSlot.getEndTime() + " " + timeSlot.getSection().getName());
        return getStudentsForSection(timeSlot.getSection().getId());
    }

    @Override
    public boolean canSubmitAttendance(User faculty, TimeSlot timeSlot, String date) {
        // Check if faculty is assigned to this time slot
        return timeSlot.getInchargeFaculty().getId().equals(faculty.getId());
    }

    @Override
    public boolean isAttendanceAlreadySubmitted(User faculty, TimeSlot timeSlot, String date) {
        LocalDate localDate = LocalDate.parse(date);
        return attendanceSessionRepository.existsByFacultyAndTimeSlotAndDate(faculty, timeSlot, localDate);
    }
}
