package com.crt.server.service.impl;

import com.crt.server.dto.BatchAttendanceRequestDTO;
import com.crt.server.dto.BatchAttendanceResponseDTO;
import com.crt.server.dto.BatchableTimeSlotResponseDTO;
import com.crt.server.dto.TimeSlotValidationResponseDTO;
import com.crt.server.model.*;
import com.crt.server.model.Role;
import com.crt.server.repository.*;
import com.crt.server.service.BatchAttendanceService;
import com.crt.server.service.CurrentUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchAttendanceServiceImpl implements BatchAttendanceService {

    private final TimeSlotRepository timeSlotRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Override
    public BatchableTimeSlotResponseDTO getBatchableTimeSlots(UUID facultyId, LocalDate date) {

        User faculty = userRepository.findById(facultyId).orElseThrow(() -> new EntityNotFoundException("Faculty not found with ID: " + facultyId));
        List<TimeSlot> facultyTimeSlots = timeSlotRepository.findByInchargeFaculty(faculty);
        

        Map<Section, List<TimeSlot>> timeSlotsBySection = facultyTimeSlots.stream()
                .collect(Collectors.groupingBy(TimeSlot::getSection));
        

        List<BatchableTimeSlotResponseDTO.BatchGroup> batchGroups = new ArrayList<>();
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Map.Entry<Section, List<TimeSlot>> entry : timeSlotsBySection.entrySet()) {
            Section section = entry.getKey();
            List<TimeSlot> timeSlots = entry.getValue();
            
            // Sort time slots by start time
            timeSlots.sort(Comparator.comparing(TimeSlot::getStartTime));
            
            List<BatchableTimeSlotResponseDTO.BatchableTimeSlot> batchableSlots = new ArrayList<>();
            
            for (TimeSlot timeSlot : timeSlots) {
                // Check if attendance has been posted for this time slot
                boolean attendancePosted = !attendanceRepository.findByTimeSlotAndDate(timeSlot, date.atStartOfDay()).isEmpty();
                
                String status = attendancePosted ? "POSTED" : "PENDING";
                
                BatchableTimeSlotResponseDTO.BatchableTimeSlot batchableSlot = BatchableTimeSlotResponseDTO.BatchableTimeSlot.builder()
                        .id(timeSlot.getId().toString())
                        .startTime(timeSlot.getStartTime().formatted(timeFormatter))
                        .endTime(timeSlot.getEndTime().formatted(timeFormatter))
                        .attendanceStatus(status)
                        .build();
                
                batchableSlots.add(batchableSlot);
            }
            
            // Only add sections that have at least one time slot
            if (!batchableSlots.isEmpty()) {
                BatchableTimeSlotResponseDTO.BatchGroup batchGroup = BatchableTimeSlotResponseDTO.BatchGroup.builder()
                        .sectionId(section.getId().toString())
                        .sectionName(section.getName())
                        .batchableSlots(batchableSlots)
                        .build();
                
                batchGroups.add(batchGroup);
            }
        }
        
        return BatchableTimeSlotResponseDTO.builder()
                .batchGroups(batchGroups)
                .build();
    }

    @Override
    public TimeSlotValidationResponseDTO validateBatchTimeSlots(List<String> timeSlotIds) {
        if (timeSlotIds == null || timeSlotIds.isEmpty()) {
            return TimeSlotValidationResponseDTO.builder()
                    .valid(false)
                    .message("Invalid time slot selection.")
                    .reason("No time slots provided.")
                    .build();
        }
        
        // Convert string IDs to integers and fetch time slots
        List<Integer> ids = timeSlotIds.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        
        List<TimeSlot> timeSlots = timeSlotRepository.findAllById(ids);
        
        // Check if all time slots were found
        if (timeSlots.size() != timeSlotIds.size()) {
            return TimeSlotValidationResponseDTO.builder()
                    .valid(false)
                    .message("Invalid time slot selection.")
                    .reason("One or more time slots not found.")
                    .build();
        }
        
        // Check if all time slots belong to the same section
        Set<UUID> sectionIds = timeSlots.stream()
                .map(ts -> ts.getSection().getId())
                .collect(Collectors.toSet());
        
        if (sectionIds.size() != 1) {
            return TimeSlotValidationResponseDTO.builder()
                    .valid(false)
                    .message("Invalid time slot selection.")
                    .reason("Selected time slots do not belong to the same section.")
                    .build();
        }
        
        // Sort time slots by start time
        timeSlots.sort(Comparator.comparing(TimeSlot::getStartTime));
        
        // Check if time slots are consecutive
        for (int i = 0; i < timeSlots.size() - 1; i++) {
            TimeSlot current = timeSlots.get(i);
            TimeSlot next = timeSlots.get(i + 1);
            
            if (!current.getEndTime().equals(next.getStartTime())) {
                return TimeSlotValidationResponseDTO.builder()
                        .valid(false)
                        .message("Invalid time slot selection.")
                        .reason("Selected time slots are not consecutive.")
                        .build();
            }
        }
        
        // Check if attendance has already been posted for any of the selected time slots for today
        LocalDate today = LocalDate.now();
        for (TimeSlot timeSlot : timeSlots) {
            if (!attendanceRepository.findByTimeSlotAndDate(timeSlot, today.atStartOfDay()).isEmpty()) {
                return TimeSlotValidationResponseDTO.builder()
                        .valid(false)
                        .message("Invalid time slot selection.")
                        .reason("Attendance already posted for time slot " + timeSlot.getId() + " for today.")
                        .build();
            }
        }

        // All validations passed, create response
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Section section = timeSlots.get(0).getSection();
        
        List<TimeSlotValidationResponseDTO.TimeSlotDetail> timeSlotDetails = timeSlots.stream()
                .map(ts -> TimeSlotValidationResponseDTO.TimeSlotDetail.builder()
                        .id(ts.getId().toString())
                        .startTime(ts.getStartTime().formatted(timeFormatter))
                        .endTime(ts.getEndTime().formatted(timeFormatter))
                        .sectionId(section.getId().toString())
                        .sectionName(section.getName())
                        .build())
                .collect(Collectors.toList());
        
        return TimeSlotValidationResponseDTO.builder()
                .valid(true)
                .message("Selected time slots are valid for batch submission.")
                .timeSlots(timeSlotDetails)
                .build();
    }

    @Override
    @Transactional
    public BatchAttendanceResponseDTO submitBatchAttendance(BatchAttendanceRequestDTO request) {
        List<BatchAttendanceResponseDTO.BatchSubmissionResult> results = new ArrayList<>();
        boolean overallSuccess = true;

        User currentUser = currentUserService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        try {
            // Parse date
            LocalDate date = LocalDate.parse(request.getDate());

            // Get section
            UUID sectionId = UUID.fromString(request.getSectionId());
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new EntityNotFoundException("Section not found with ID: " + sectionId));

            // Get all students in the section
            Set<Student> sectionStudents = section.getStudents();

            // Process each time slot
            for (String timeSlotIdStr : request.getTimeSlotIds()) {
                Integer timeSlotId = Integer.parseInt(timeSlotIdStr);

                try {
                    // Get time slot
                    TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                            .orElseThrow(() -> new EntityNotFoundException("Time slot not found with ID: " + timeSlotId));

                    // Authorization check for FACULTY
                    if (!isAdmin && !timeSlot.getInchargeFaculty().getId().equals(currentUser.getId())) {
                        throw new SecurityException("Faculty is not authorized to submit attendance for this time slot.");
                    }

                    // Check if attendance already exists for this time slot and date
                    List<Attendance> existingAttendance = attendanceRepository.findByTimeSlotAndDate(timeSlot, date.atStartOfDay());

                    if (!existingAttendance.isEmpty()) {
                        // If admin, delete existing records to allow override
                        if (isAdmin) {
                            attendanceRepository.deleteAll(existingAttendance);
                            log.info("Admin override: Deleted {} existing attendance records for time slot {} on {}",
                                    existingAttendance.size(), timeSlotId, date);
                        } else {
                            throw new IllegalStateException("Attendance already marked for this time slot on " + date);
                        }
                    }

                    // Create a map of student IDs to attendance status
                    Map<String, Boolean> studentAttendanceMap = request.getAttendanceRecords().stream()
                            .collect(Collectors.toMap(
                                    BatchAttendanceRequestDTO.AttendanceRecord::getStudentId,
                                    BatchAttendanceRequestDTO.AttendanceRecord::isPresent
                            ));

                    // Create attendance records for all students in the section
                    List<Attendance> attendanceRecords = new ArrayList<>();

                    for (Student student : sectionStudents) {
                        String studentId = student.getId().toString();
                        Boolean isPresent = studentAttendanceMap.getOrDefault(studentId, false);

                        Attendance attendance = new Attendance();
                        attendance.setStudent(student);
                        attendance.setTimeSlot(timeSlot);
                        attendance.setDate(date.atStartOfDay());
                        attendance.setStatus(isPresent ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT);

                        if (!isPresent) {
                            attendance.setFeedback("Absent - Batch attendance submission");
                        }

                        attendanceRecords.add(attendance);
                    }

                    // Save attendance records
                    attendanceRepository.saveAll(attendanceRecords);

                    // Add success result
                    results.add(BatchAttendanceResponseDTO.BatchSubmissionResult.builder()
                            .timeSlotId(timeSlotIdStr)
                            .status("success")
                            .build());

                } catch (Exception e) {
                    log.error("Error processing time slot {}: {}", timeSlotIdStr, e.getMessage());

                    // Add error result
                    results.add(BatchAttendanceResponseDTO.BatchSubmissionResult.builder()
                            .timeSlotId(timeSlotIdStr)
                            .status("error")
                            .error(e.getMessage())
                            .build());

                    overallSuccess = false;
                }
            }

            // Create response
            String message = overallSuccess
                    ? "Batch attendance submitted successfully for all time slots."
                    : "Batch attendance submitted with some errors.";

            return BatchAttendanceResponseDTO.builder()
                    .success(overallSuccess)
                    .message(message)
                    .results(results)
                    .build();

        } catch (Exception e) {
            log.error("Error processing batch attendance: {}", e.getMessage());

            return BatchAttendanceResponseDTO.builder()
                    .success(false)
                    .message("Error processing batch attendance: " + e.getMessage())
                    .results(results)
                    .build();
        }
    }
}
