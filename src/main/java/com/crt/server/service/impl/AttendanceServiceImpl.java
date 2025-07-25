package com.crt.server.service.impl;

import com.crt.server.config.AttendanceConfig;
import com.crt.server.dto.*;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.AttendanceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceArchiveRepository attendanceArchiveRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SectionScheduleRepository sectionScheduleRepository;
    
    @Autowired
    private AttendanceConfig attendanceConfig;

    @Override
    @Transactional
    public List<AttendanceDTO> markAttendance(MarkAttendanceDTO markAttendanceDTO) {
        System.out.println("[DEBUG] Starting markAttendance with DTO: " + markAttendanceDTO);

        // Parse the date string properly
        System.out.println("[DEBUG] Raw date string: '" + markAttendanceDTO.getDateTime() + "'");
        String rawDateTime = markAttendanceDTO.getDateTime().trim();
        String normalizedDateTime = rawDateTime.substring(0, rawDateTime.length() - 2) +
                rawDateTime.substring(rawDateTime.length() - 2).toUpperCase();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy, h:mm:ss a", Locale.ENGLISH);

        LocalDateTime date;
        try {
            date = LocalDateTime.parse(normalizedDateTime, formatter);
            System.out.println("[DEBUG] Successfully parsed date: " + date);
        } catch (DateTimeParseException e) {
            System.out.println("[DEBUG] Error parsing date: " + e.getMessage());
            System.out.println("[DEBUG] Error at index: " + e.getErrorIndex());
            System.out.println("[DEBUG] Error index character: '" + rawDateTime.charAt(e.getErrorIndex())
                    + "'");
            throw new IllegalArgumentException(
                    "Invalid date format. Expected format: d/M/yyyy, h:mm:ss am/pm");
        }

        TimeSlot timeSlot = timeSlotRepository.findById(markAttendanceDTO.getTimeSlotId())
                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));
        System.out.println("[DEBUG] Found timeSlot: " + timeSlot.getId());

        Section section = timeSlot.getSection();
        System.out.println("[DEBUG] Got section: " + section.getId());

        // Get section schedule
        SectionSchedule schedule = sectionScheduleRepository.findBySectionId(section.getId())
                .orElseThrow(() -> new EntityNotFoundException("Section schedule not found"));
        System.out.println("[DEBUG] Found schedule: " + schedule.getId());

        // Validate that the time slot is in the section's schedule
        System.out.println("[DEBUG] About to check if timeSlot is in schedule");
        System.out.println("[DEBUG] TimeSlot ID: " + timeSlot.getId());
        System.out.println("[DEBUG] TimeSlot Section ID: " + timeSlot.getSection().getId());
        System.out.println("[DEBUG] Schedule ID: " + schedule.getId());

        // First validate the time slot belongs to the correct section
        if (!timeSlot.getSection().getId().equals(section.getId())) {
            throw new IllegalStateException("Time slot does not belong to the specified section");
        }

        // Then validate it's in the schedule
        boolean timeSlotFound = timeSlotRepository.existsByIdAndScheduleId(timeSlot.getId(), schedule.getId());
        if (!timeSlotFound) {
            throw new IllegalStateException("Time slot is not part of the section's schedule");
        }
        System.out.println("[DEBUG] Validated time slot in schedule");

        // Validate that the date matches the time slot's schedule
        System.out.println("[DEBUG] Validating date: " + date);
        if (!isValidTimeSlotDate(timeSlot, date)) {
            throw new IllegalStateException("Invalid date for this time slot");
        }
        System.out.println("[DEBUG] Validated time slot date");

        // Check if attendance already exists for this time slot and date
        System.out.println("[DEBUG] Checking for existing attendance");
        List<Attendance> existingAttendance = attendanceRepository.findByTimeSlotAndDate(timeSlot, date);
        boolean isAdmin = true;//markAttendanceDTO.isAdminRequest() != null && markAttendanceDTO.isAdminRequest();
        
        if (!existingAttendance.isEmpty()) {
            if (isAdmin) {
                // For admin requests, delete existing attendance records to allow update
                System.out.println("[DEBUG] Admin request detected. Deleting existing attendance records for update.");
                attendanceRepository.deleteAll(existingAttendance);
            } else {
                // For faculty requests, show error
                throw new IllegalStateException("Attendance already marked for this time slot on " + date);
            }
        }
        System.out.println("[DEBUG] Checked for existing attendance");

        // FIX 1: Create defensive copy of students to avoid
        // ConcurrentModificationException
        List<Student> students = new ArrayList<>(section.getStudents());
        System.out.println("[DEBUG] Got " + students.size() + " students from section");

        // FIX 2: Use thread-safe collection for attendance records
        List<Attendance> attendances = Collections.synchronizedList(new ArrayList<>());
        System.out.println("[DEBUG] Created empty attendances list");

        // FIX 3: Use defensive copying and avoid direct iteration over JPA collections
        for (Student student : students) {
            System.out.println("[DEBUG] Processing student: " + student.getId());
            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setTimeSlot(timeSlot);
            attendance.setStatus(AttendanceStatus.PRESENT);
            attendance.setDate(date);
            attendances.add(attendance);
        }
        System.out.println("[DEBUG] Created " + attendances.size() + " attendance records");

        // FIX 4: Handle absent students with defensive copying
        if (markAttendanceDTO.getAbsentStudentIds() != null
                && !markAttendanceDTO.getAbsentStudentIds().isEmpty()) {
            Set<UUID> absentIds = new HashSet<>(markAttendanceDTO.getAbsentStudentIds());
            System.out.println("[DEBUG] Processing " + absentIds.size() + " absent students");

            // Use iterator to safely modify collection
            for (Attendance attendance : attendances) {
                UUID studentId = attendance.getStudent().getId();
                if (absentIds.contains(studentId)) {
                    System.out.println("[DEBUG] Flagging student " + studentId + " as absent");
                    attendance.setStatus(AttendanceStatus.ABSENT);
                }
            }
        }

        // FIX 5: Handle late students with defensive copying
        if (markAttendanceDTO.getLateStudents() != null && !markAttendanceDTO.getLateStudents().isEmpty()) {
            List<StudentAttendanceDTO> lateStudents = new ArrayList<>(markAttendanceDTO.getLateStudents());
            System.out.println("[DEBUG] Processing " + lateStudents.size() + " late students");

            Map<String, String> feedbackMap = lateStudents.stream().collect(Collectors
                    .toMap(StudentAttendanceDTO::getId, StudentAttendanceDTO::getFeedback));

            for (Attendance attendance : attendances) {
                UUID studentId = attendance.getStudent().getId();
                if (feedbackMap.containsKey(studentId.toString())) {
                    System.out.println("[DEBUG] Flagging student " + studentId + " as late");
                    attendance.setStatus(AttendanceStatus.LATE);
                    attendance.setFeedback(feedbackMap.get(studentId.toString()));
                }
            }
        }

        System.out.println("[DEBUG] About to save " + attendances.size() + " attendance records");
        List<Attendance> savedAttendances = attendanceRepository.saveAll(attendances);
        System.out.println("[DEBUG] Successfully saved " + savedAttendances.size() + " attendance records");
        
        // Update attendance percentage for each student
        for (Student student : students) {
            updateStudentAttendancePercentage(student);
        }

        return savedAttendances.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BulkAttendanceResponseDTO markBulkAttendance(BulkAttendanceDTO bulkAttendanceDTO) {
        // FIX 6: Use thread-safe collections for concurrent operations
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        List<AttendanceDTO> successfulRecords = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger totalProcessed = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            TimeSlot timeSlot = timeSlotRepository.findById(bulkAttendanceDTO.getTimeSlotId())
                    .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));
            Section section = sectionRepository.findById(timeSlot.getSection().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Section not found"));

            // Check if attendance already exists and if this is an admin request
            boolean isAdmin = bulkAttendanceDTO.getIsAdminRequest() != null && bulkAttendanceDTO.getIsAdminRequest();
            List<Attendance> existingAttendance = attendanceRepository.findByTimeSlotAndDate(timeSlot, bulkAttendanceDTO.getParsedDateTime());
            
            if (!existingAttendance.isEmpty()) {
                if (isAdmin) {
                    // For admin requests, delete existing attendance records to allow update
                    log.info("Admin request detected. Deleting {} existing attendance records for update.", existingAttendance.size());
                    attendanceRepository.deleteAll(existingAttendance);
                } else {
                    // For faculty requests, show error
                    throw new IllegalStateException("Attendance already marked for this time slot on " + bulkAttendanceDTO.getParsedDateTime());
                }
            }

            // FIX 7: Create defensive copy and use Set for O(1) lookup
            Set<Student> enrolledStudents = new HashSet<>(section.getStudents());

            // Process absent students
            if (bulkAttendanceDTO.getAbsentStudentIds() != null
                    && !bulkAttendanceDTO.getAbsentStudentIds().isEmpty()) {
                // FIX 8: Create defensive copy of input list
                List<UUID> absentStudentIds = new ArrayList<>(bulkAttendanceDTO.getAbsentStudentIds());

                // Use parallelStream for concurrent processing
                List<Attendance> absentAttendances = absentStudentIds.parallelStream().map(studentId -> {
                    totalProcessed.incrementAndGet();
                    try {
                        // Use stream().anyMatch() instead of contains() for better performance
                        boolean isEnrolled = enrolledStudents.stream()
                                .anyMatch(s -> s.getId().equals(studentId));

                        if (!isEnrolled) {
                            errors.add("Student " + studentId
                                    + " is not enrolled in this section");
                            failureCount.incrementAndGet();
                            return null;
                        }

                        Student student = studentRepository.findById(studentId).orElseThrow(
                                () -> new EntityNotFoundException("Student not found"));

                        Attendance attendance = new Attendance();
                        attendance.setStudent(student);
                        attendance.setTimeSlot(timeSlot);
                        attendance.setStatus(AttendanceStatus.ABSENT);
                        attendance.setDate(bulkAttendanceDTO.getParsedDateTime());
                        return attendance;
                    } catch (Exception e) {
                        errors.add("Error processing student " + studentId + ": "
                                + e.getMessage());
                        failureCount.incrementAndGet();
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

                List<Attendance> savedAbsentAttendances = attendanceRepository
                        .saveAll(absentAttendances);
                successfulRecords.addAll(savedAbsentAttendances.stream().map(this::convertToDTO)
                        .collect(Collectors.toList()));
                successCount.addAndGet(savedAbsentAttendances.size());
                
                // Update attendance percentage for each student
                savedAbsentAttendances.stream()
                    .map(Attendance::getStudent)
                    .distinct()
                    .forEach(this::updateStudentAttendancePercentage);
            }

            // Process late students
            if (bulkAttendanceDTO.getLateStudents() != null
                    && !bulkAttendanceDTO.getLateStudents().isEmpty()) {
                // FIX 9: Create defensive copy of late students list
                List<StudentAttendanceDTO> lateStudents = new ArrayList<>(
                        bulkAttendanceDTO.getLateStudents());

                // Use parallelStream for concurrent processing
                List<Attendance> lateAttendances = lateStudents.parallelStream().map(lateStudent -> {
                    totalProcessed.incrementAndGet();
                    try {
                        boolean isEnrolled = enrolledStudents.stream().anyMatch(
                                s -> s.getId().equals(lateStudent.getId()));

                        if (!isEnrolled) {
                            errors.add("Student " + lateStudent.getId()
                                    + " is not enrolled in this section");
                            failureCount.incrementAndGet();
                            return null;
                        }

                        if (lateStudent.getFeedback() == null
                                || lateStudent.getFeedback().trim().isEmpty()) {
                            errors.add("Feedback is required for late student "
                                    + lateStudent.getId());
                            failureCount.incrementAndGet();
                            return null;
                        }

                        Student student = studentRepository.findById(UUID.fromString(lateStudent.getId()))
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "Student not found"));

                        Attendance attendance = new Attendance();
                        attendance.setStudent(student);
                        attendance.setTimeSlot(timeSlot);
                        attendance.setStatus(AttendanceStatus.LATE);
                        attendance.setFeedback(lateStudent.getFeedback());
                        attendance.setDate(
                                LocalDateTime.parse(bulkAttendanceDTO.getDateTime()));
                        return attendance;
                    } catch (Exception e) {
                        errors.add("Error processing student " + lateStudent.getId()
                                + ": " + e.getMessage());
                        failureCount.incrementAndGet();
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

                List<Attendance> savedLateAttendances = attendanceRepository.saveAll(lateAttendances);
                successfulRecords.addAll(savedLateAttendances.stream().map(this::convertToDTO)
                        .collect(Collectors.toList()));
                successCount.addAndGet(savedLateAttendances.size());
                
                // Update attendance percentage for each student
                savedLateAttendances.stream()
                    .map(Attendance::getStudent)
                    .distinct()
                    .forEach(this::updateStudentAttendancePercentage);
            }

            return BulkAttendanceResponseDTO.builder().totalProcessed(totalProcessed.get())
                    .successCount(successCount.get()).failureCount(failureCount.get())
                    .successfulRecords(successfulRecords).errors(errors).build();

        } catch (Exception e) {
            throw new RuntimeException("Error processing bulk attendance: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public BulkAttendanceResponseDTO processBulkAttendanceFile(MultipartFile file, Integer timeSlotId,
                                                               String dateTime, Boolean isAdminRequest) {
        // FIX 10: Use thread-safe collections
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        List<AttendanceDTO> successfulRecords = Collections.synchronizedList(new ArrayList<>());
        int totalProcessed = 0;
        int successCount = 0;
        int failureCount = 0;

        try {
            TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));
            Section section = sectionRepository.findById(timeSlot.getSection().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Section not found"));
                    
            // Check if attendance already exists and if this is an admin request
            LocalDateTime parsedDateTime = LocalDateTime.parse(dateTime);
            List<Attendance> existingAttendance = attendanceRepository.findByTimeSlotAndDate(timeSlot, parsedDateTime);
            
            if (!existingAttendance.isEmpty()) {
                if (isAdminRequest != null && isAdminRequest) {
                    // For admin requests, delete existing attendance records to allow update
                    log.info("Admin request detected. Deleting {} existing attendance records for update.", existingAttendance.size());
                    attendanceRepository.deleteAll(existingAttendance);
                } else {
                    // For faculty requests, show error
                    throw new IllegalStateException("Attendance already marked for this time slot on " + dateTime);
                }
            }

            // FIX 11: Create defensive copy of enrolled students
            Set<Student> enrolledStudents = new HashSet<>(section.getStudents());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    totalProcessed++;
                    String[] parts = line.split(",");
                    if (parts.length < 2) {
                        errors.add("Invalid format in line: " + line);
                        failureCount++;
                        continue;
                    }

                    try {
                        String studentId = parts[0].trim();
                        String status = parts[1].trim();
                        String feedback = parts.length > 2 ? parts[2].trim() : "";

                        Student student = studentRepository.findById(UUID.fromString(studentId))
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "Student not found"));

                        // FIX 12: Use contains() method on Set for better performance
                        if (!enrolledStudents.contains(student)) {
                            errors.add("Student " + studentId
                                    + " is not enrolled in this section");
                            failureCount++;
                            continue;
                        }

                        AttendanceStatus attendanceStatus;
                        try {
                            attendanceStatus = AttendanceStatus.valueOf(status);
                        } catch (IllegalArgumentException e) {
                            errors.add("Invalid status '" + status + "' for student "
                                    + studentId);
                            failureCount++;
                            continue;
                        }

                        if (attendanceStatus == AttendanceStatus.LATE
                                && (feedback == null || feedback.trim().isEmpty())) {
                            errors.add("Feedback is required for late student "
                                    + studentId);
                            failureCount++;
                            continue;
                        }

                        Attendance attendance = new Attendance();
                        attendance.setStudent(student);
                        attendance.setTimeSlot(timeSlot);
                        attendance.setStatus(attendanceStatus);
                        attendance.setFeedback(feedback);
                        attendance.setDate(LocalDateTime.parse(dateTime));

                        Attendance savedAttendance = attendanceRepository.save(attendance);
                        successfulRecords.add(convertToDTO(savedAttendance));
                        successCount++;

                    } catch (Exception e) {
                        errors.add("Error processing line: " + line + " - " + e.getMessage());
                        failureCount++;
                    }
                }
            }

            // Update attendance percentage for all affected students
            Set<Student> affectedStudents = new HashSet<>();
            successfulRecords.forEach(record -> {
                UUID studentId = record.getStudentId();
                studentRepository.findById(studentId).ifPresent(affectedStudents::add);
            });
            
            affectedStudents.forEach(this::updateStudentAttendancePercentage);

            return BulkAttendanceResponseDTO.builder().totalProcessed(totalProcessed)
                    .successCount(successCount).failureCount(failureCount)
                    .successfulRecords(successfulRecords).errors(errors).build();

        } catch (Exception e) {
            throw new RuntimeException("Error processing bulk attendance file: " + e.getMessage());
        }
    }

    private boolean isValidTimeSlotDate(TimeSlot timeSlot, LocalDateTime date) {
        // If end time restriction is not enforced, always return true
        if (!attendanceConfig.isEnforceEndTimeRestriction()) {
            return true;
        }
        
        // Check if the current time is after the end time of the time slot
        LocalDateTime now = LocalDateTime.now();
        LocalTime endTime = LocalTime.parse(timeSlot.getEndTime());
        LocalTime currentTime = now.toLocalTime();
        
        // If current time is after end time, attendance submission is not allowed
        if (currentTime.isAfter(endTime)) {
            log.warn("Attendance submission attempted after end time. Current time: {}, End time: {}", 
                    currentTime, endTime);
            return false;
        }
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getStudentAttendance(UUID studentId, LocalDateTime startDate,
                                                    LocalDateTime endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        return attendanceRepository.findByStudentIdAndDateBetween(studentId, startDate, endDate).stream()
//                .sorted(Comparator.comparing(Attendance::getDate))
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getTimeSlotAttendance(Integer timeSlotId, LocalDateTime date) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));

        return attendanceRepository.
                findByTimeSlotIdAndDate(timeSlotId, date).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceReportDTO getStudentAttendanceReport(UUID studentId, LocalDateTime startDate,
                                                          LocalDateTime endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        long totalClasses = attendanceRepository.countAttendanceByStudentAndDateRange(student, startDate,
                endDate);
        long absences = attendanceRepository.countAbsencesByStudentAndDateRange(student, startDate, endDate);
        double attendancePercentage = totalClasses > 0 ? ((totalClasses - absences) * 100.0) / totalClasses : 0;

        List<AttendanceDTO> attendanceRecords = getStudentAttendance(studentId, startDate, endDate);

        return AttendanceReportDTO.builder().studentId(student.getId()).studentName(student.getName())
                .regNum(student.getRegNum()).totalClasses(totalClasses).absences(absences)
                .attendancePercentage(attendancePercentage).attendanceRecords(attendanceRecords)
                .build();
    }

    @Override
    @Transactional
    public void archiveAttendanceRecords(int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

        // FIX 13: Use batch processing to avoid loading all records into memory
        // Process records in batches to prevent OutOfMemoryError for large datasets
        int batchSize = 1000;
        int page = 0;
        long totalRecords = attendanceRepository.countByDateBetween(startDate, endDate);
        
        log.info("Starting archival process for {}/{} - Total records to archive: {}", year, month, totalRecords);
        
        if (totalRecords == 0) {
            log.info("No records found to archive for {}/{}", year, month);
            return;
        }
        
        long processedRecords = 0;
        
        while (processedRecords < totalRecords) {
            Pageable pageable = PageRequest.of(page, batchSize);
            List<Attendance> recordsToArchive = attendanceRepository.findByDateBetweenOrderById(startDate, endDate, pageable);
            
            if (recordsToArchive.isEmpty()) {
                break;
            }
            
            // Convert to archive records
            List<AttendanceArchive> archives = recordsToArchive.stream()
                    .map(this::convertToArchive)
                    .collect(Collectors.toList());
            
            // Save archives and delete original records in the same transaction
            attendanceArchiveRepository.saveAll(archives);
            attendanceRepository.deleteAll(recordsToArchive);
            
            processedRecords += recordsToArchive.size();
            page++;
            
            log.info("Archived batch {} - Progress: {}/{} records", page, processedRecords, totalRecords);
        }
        
        log.info("Completed archival process for {}/{} - Total archived: {} records", year, month, processedRecords);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getArchivedStudentAttendance(UUID studentId, LocalDateTime startDate,
                                                            LocalDateTime endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        return attendanceArchiveRepository.findByStudentIdAndDateBetween(studentId, startDate, endDate).stream()
                .map(this::convertArchiveToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceReportDTO getArchivedStudentAttendanceReport(UUID studentId, LocalDateTime startDate,
                                                                  LocalDateTime endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        long totalClasses = attendanceArchiveRepository.countAttendanceByStudentAndDateRange(student, startDate,
                endDate);
        long absences = attendanceArchiveRepository.countAbsencesByStudentAndDateRange(student, startDate,
                endDate);
        double attendancePercentage = totalClasses > 0 ? ((totalClasses - absences) * 100.0) / totalClasses : 0;

        List<AttendanceDTO> attendanceRecords = getArchivedStudentAttendance(studentId, startDate, endDate);

        return AttendanceReportDTO.builder().studentId(student.getId()).studentName(student.getName())
                .regNum(student.getRegNum()).totalClasses(totalClasses).absences(absences)
                .attendancePercentage(attendancePercentage).attendanceRecords(attendanceRecords)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionAttendanceRecordDTO> getSectionAttendanceRecords(UUID sectionId, LocalDateTime startDate,
                                                                        LocalDateTime endDate) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        // FIX 14: Create defensive copy to avoid ConcurrentModificationException
        List<Student> students = new ArrayList<>(section.getStudents());
        List<SectionAttendanceRecordDTO> records = new ArrayList<>();
        String monthTitle = startDate.getMonth().toString() + " " + startDate.getYear();

        for (Student student : students) {
            long totalClasses = attendanceRepository.countAttendanceByStudentAndDateRange(student,
                    startDate, endDate);
            long absences = attendanceRepository.countAbsencesByStudentAndDateRange(student, startDate,
                    endDate);
            double attendancePercentage = totalClasses > 0
                    ? ((totalClasses - absences) * 100.0) / totalClasses
                    : 100.0;

            records.add(SectionAttendanceRecordDTO.builder().regNum(student.getRegNum())
                    .name(student.getName()).attendancePercentage(attendancePercentage)
                    .monthTitle(monthTitle).totalClasses(totalClasses).absences(absences).build());
        }

        return records;
    }

    private AttendanceDTO convertToDTO(Attendance attendance) {
        return AttendanceDTO.fromLocalDateTime(AttendanceDTO.builder().id(attendance.getId())
                        .studentId(attendance.getStudent().getId()).timeSlotId(attendance.getTimeSlot().getId())
                        .status(attendance.getStatus()).feedback(attendance.getFeedback()).build(),
                attendance.getPostedAt(), attendance.getDate());
    }

    private AttendanceArchive convertToArchive(Attendance attendance) {
        return AttendanceArchive.builder().student(attendance.getStudent()).timeSlot(attendance.getTimeSlot())
                .status(attendance.getStatus()).feedback(attendance.getFeedback())
                .postedAt(attendance.getPostedAt()).date(attendance.getDate()).build();
    }

    private AttendanceDTO convertArchiveToDTO(AttendanceArchive archive) {
        return AttendanceDTO.fromLocalDateTime(
                AttendanceDTO.builder().id(archive.getId()).studentId(archive.getStudent().getId())
                        .timeSlotId(archive.getTimeSlot().getId()).status(archive.getStatus())
                        .feedback(archive.getFeedback()).build(),
                archive.getPostedAt(), archive.getDate());
    }
    
    /**
     * Updates the attendance percentage for a student based on all their attendance records
     * 
     * @param student The student whose attendance percentage needs to be updated
     */
    private void updateStudentAttendancePercentage(Student student) {
        // Calculate attendance from all records
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = LocalDateTime.of(2000, 1, 1, 0, 0); // Far in the past to include all records
        
        long totalClasses = attendanceRepository.countAttendanceByStudentAndDateRange(student, startDate, endDate);
        long absences = attendanceRepository.countAbsencesByStudentAndDateRange(student, startDate, endDate);
        
        // Calculate attendance percentage
        double attendancePercentage = totalClasses > 0 ? ((totalClasses - absences) * 100.0) / totalClasses : 0.0;
        
        // Update student's attendance percentage
        student.setAttendancePercentage(attendancePercentage);
        studentRepository.save(student);
        
        log.info("Updated attendance percentage for student {} (ID: {}) to {}%",
                student.getRegNum(), student.getId(), attendancePercentage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbsenteeDTO> getAbsenteesByDate(LocalDate date) {
        List<Attendance> absentees = attendanceRepository.findAbsenteesByDate(date);
        return absentees.stream()
                .map(this::mapToAbsenteeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbsenteeDTO> getAbsenteesByDateAndSection(LocalDate date, UUID sectionId) {
        // Verify section exists
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + sectionId));

        List<Attendance> absentees = attendanceRepository.findAbsenteesByDateAndSection(date, sectionId);
        return absentees.stream()
                .map(this::mapToAbsenteeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AbsenteeDTO> getAbsenteesByTimeSlotId(Integer timeSlotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId).orElseThrow(() -> new EntityNotFoundException("TimeSlot not found with id: " + timeSlotId));
        List<Attendance> absentees = attendanceRepository.getAbsenteesByTimeSlot(timeSlot);
        return absentees.stream()
                .map(this::mapToAbsenteeDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<AbsenteeDTO> getAbsenteesByTimeSlotIdAndDate(Integer timeSlotId, LocalDateTime date){
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId).orElseThrow(() -> new EntityNotFoundException("TimeSlot not found with id: " + timeSlotId));
        List<Attendance> absentees = attendanceRepository.getAbsenteesByTimeSlotAndDate(timeSlot, date);
        return absentees.stream()
                .map(this::mapToAbsenteeDTO)
                .sorted(Comparator.comparing(AbsenteeDTO::getRegNum))
                .collect(Collectors.toList());
    }

    private AbsenteeDTO mapToAbsenteeDTO(Attendance attendance) {
        return AbsenteeDTO.builder()
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getName())
                .regNum(attendance.getStudent().getRegNum())
                .sectionId(attendance.getTimeSlot().getSection().getId())
                .sectionName(attendance.getTimeSlot().getSection().getName())
                .email(attendance.getStudent().getEmail())
                .phone(attendance.getStudent().getPhone())
                .timeSlotId(attendance.getTimeSlot().getId())
                .startTime(attendance.getTimeSlot().getStartTime())
                .endTime(attendance.getTimeSlot().getEndTime())
                .date(attendance.getDate())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public TimeSlotFilterResponseDTO getTimeSlotsByDayAndTime(LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Since we don't have a direct day field in the entities, we'll get all time slots
        // and filter them based on the provided parameters
        List<TimeSlot> allTimeSlots = timeSlotRepository.findAll();
        
        // Filter time slots based on start and end time if provided
        List<TimeSlot> filteredTimeSlots = allTimeSlots.stream()
                .filter(ts -> {
                    if (startTime != null && endTime != null) {
                        LocalTime tsStart = LocalTime.parse(ts.getStartTime());
                        LocalTime tsEnd = LocalTime.parse(ts.getEndTime());
                        return !tsStart.isBefore(startTime) && !tsEnd.isAfter(endTime);
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // Convert time slots to DTOs with attendance status
        List<TimeSlotStatusDTO> timeSlotStatusDTOs = new ArrayList<>();
        int postedAttendanceCount = 0;
        
        for (TimeSlot timeSlot : filteredTimeSlots) {
            boolean attendancePosted = attendanceRepository.existsByTimeSlotAndDate(timeSlot, date);
            
            // Check if current time is past the end time of the time slot
            boolean pastEndTime = LocalTime.now().isAfter(LocalTime.parse(timeSlot.getEndTime()));
            
            TimeSlotStatusDTO dto = TimeSlotStatusDTO.builder()
                    .timeSlotId(timeSlot.getId())
                    .startTime(LocalTime.parse(timeSlot.getStartTime()))
                    .endTime(LocalTime.parse(timeSlot.getEndTime()))
                    .day(date.getDayOfWeek().toString())
                    .sectionId(timeSlot.getSection().getId())
                    .sectionName(timeSlot.getSection().getName())
                    .facultyId(timeSlot.getInchargeFaculty().getId())
                    .facultyName(timeSlot.getInchargeFaculty().getName())
                    .attendancePosted(attendancePosted)
                    .pastEndTime(pastEndTime)
                    .build();
            
            timeSlotStatusDTOs.add(dto);
            
            if (attendancePosted) {
                postedAttendanceCount++;
            }
        }
        
        // Get faculties who haven't posted attendance after end time
        List<UUID> facultyIdsWithPendingAttendance = attendanceRepository.findFacultiesWithPendingAttendance(date);
        List<FacultyDTO> facultiesWithPendingAttendance = new ArrayList<>();
        
        // Filter to only include faculties whose time slots have ended
        for (UUID facultyId : facultyIdsWithPendingAttendance) {
            // Check if any of their time slots have ended but attendance not posted
            boolean hasPastEndTimeSlots = timeSlotStatusDTOs.stream()
                    .anyMatch(ts -> ts.getFacultyId().equals(facultyId) && ts.isPastEndTime() && !ts.isAttendancePosted());
            
            if (hasPastEndTimeSlots) {
                // Get faculty details and add to the list
                User faculty = filteredTimeSlots.stream()
                        .filter(ts -> ts.getInchargeFaculty().getId().equals(facultyId))
                        .findFirst()
                        .map(TimeSlot::getInchargeFaculty)
                        .orElse(null);
                
                if (faculty != null) {
                    FacultyDTO facultyDTO = FacultyDTO.builder()
                            .id(faculty.getId().toString())
                            .name(faculty.getName())
                            .email(faculty.getEmail())
                            .phone(faculty.getPhone())
                            .build();
                    
                    facultiesWithPendingAttendance.add(facultyDTO);
                }
            }
        }
        
        return TimeSlotFilterResponseDTO.builder()
                .totalTimeSlots(filteredTimeSlots.size())
                .postedAttendanceCount(postedAttendanceCount)
                .pendingAttendanceCount(filteredTimeSlots.size() - postedAttendanceCount)
                .timeSlots(timeSlotStatusDTOs)
                .facultiesWithPendingAttendance(facultiesWithPendingAttendance)
                .build();
    }

    @Override
    @Transactional
    public BulkAttendanceResponseDTO adminOverrideAttendance(AdminAttendanceRequestDTO requestDTO) {
        log.info("Admin override attendance request received for timeSlotId: {}, dateTime: {}",
                requestDTO.getTimeSlotId(), requestDTO.getDateTime());

        TimeSlot timeSlot = timeSlotRepository.findById(requestDTO.getTimeSlotId())
                .orElseThrow(() -> new EntityNotFoundException("Time slot not found with ID: " + requestDTO.getTimeSlotId()));

        LocalDateTime attendanceDateTime = requestDTO.getParsedDateTime();

        // 1. Delete existing attendance records for this time slot and date
        List<Attendance> existingAttendance = attendanceRepository.findByTimeSlotAndDate(timeSlot, attendanceDateTime);
        if (!existingAttendance.isEmpty()) {
            attendanceRepository.deleteAll(existingAttendance);
            log.info("Deleted {} existing attendance records for timeSlotId: {} on date: {}",
                    existingAttendance.size(), timeSlot.getId(), attendanceDateTime);
        }

        // 2. Get all students in the section associated with the time slot
        Section section = timeSlot.getSection();
        Set<Student> allStudentsInSection = section.getStudents();
        log.info("Found {} students in section {} for timeSlotId: {}",
                allStudentsInSection.size(), section.getName(), timeSlot.getId());

        // 3. Prepare new attendance records
        List<Attendance> newAttendances = new ArrayList<>();
        Set<UUID> absentStudentIds = new HashSet<>(requestDTO.getAbsentStudentIds());
        Map<UUID, String> lateStudentFeedback = requestDTO.getLateStudents().stream()
                .collect(Collectors.toMap(
                        dto -> UUID.fromString(dto.getId()),
                        StudentAttendanceDTO::getFeedback
                ));

        for (Student student : allStudentsInSection) {
            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setTimeSlot(timeSlot);
            attendance.setDate(attendanceDateTime);

            if (absentStudentIds.contains(student.getId())) {
                attendance.setStatus(AttendanceStatus.ABSENT);
                attendance.setFeedback(requestDTO.getOverrideReason()); // Use override reason for absent students
            } else if (lateStudentFeedback.containsKey(student.getId())) {
                attendance.setStatus(AttendanceStatus.LATE);
                attendance.setFeedback(lateStudentFeedback.get(student.getId()));
            } else {
                attendance.setStatus(AttendanceStatus.PRESENT);
            }
            newAttendances.add(attendance);
        }

        // 4. Save new attendance records
        List<Attendance> savedAttendances = attendanceRepository.saveAll(newAttendances);
        log.info("Saved {} new attendance records for timeSlotId: {} on date: {}",
                savedAttendances.size(), timeSlot.getId(), attendanceDateTime);

        // 5. Update attendance percentage for all affected students
        allStudentsInSection.forEach(this::updateStudentAttendancePercentage);
        log.info("Updated attendance percentages for all students in section {}.", section.getName());
        
        // 6. Convert saved attendances to DTOs for response
        List<AttendanceDTO> attendanceDTOs = savedAttendances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 7. Build and return response
        return BulkAttendanceResponseDTO.builder()
                .totalProcessed(savedAttendances.size())
                .successCount(savedAttendances.size())
                .failureCount(0)
                .successfulRecords(attendanceDTOs)
                .errors(new ArrayList<>())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDTO> debugGetAllAttendanceForTimeSlotAndDate(Integer timeSlotId, LocalDateTime date) {
        log.info("DEBUG: Getting all attendance records for timeSlotId: {} on date: {}", timeSlotId, date);
        
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found with id: " + timeSlotId));
        
        log.info("DEBUG: Found timeSlot: {} for section: {}", timeSlot.getId(), timeSlot.getSection().getName());
        
        // Get all attendance records for this time slot and date (using DATE comparison)
        List<Attendance> allAttendance = attendanceRepository.findByTimeSlotIdAndDateDebug(timeSlotId, date);
        
        log.info("DEBUG: Found {} total attendance records", allAttendance.size());
        
        // Log each record for debugging
        allAttendance.forEach(attendance -> {
            log.info("DEBUG: Attendance - Student: {}, Status: {}, Date: {}, TimeSlot: {}", 
                    attendance.getStudent().getRegNum(), 
                    attendance.getStatus(), 
                    attendance.getDate(),
                    attendance.getTimeSlot().getId());
        });
        
        // Filter and log absentees specifically
        List<Attendance> absentees = allAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .collect(Collectors.toList());
        
        log.info("DEBUG: Found {} absentees out of {} total records", absentees.size(), allAttendance.size());
        
        return allAttendance.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
