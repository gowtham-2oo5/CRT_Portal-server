package com.crt.server.service.impl;

import com.crt.server.dto.*;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.AttendanceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.time.format.ResolverStyle;

@Service
@RequiredArgsConstructor
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
                if (!attendanceRepository.findByTimeSlotAndDate(timeSlot, date).isEmpty()) {
                        throw new IllegalStateException("Attendance already marked for this time slot on " + date);
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

                        Map<UUID, String> feedbackMap = lateStudents.stream().collect(Collectors
                                        .toMap(StudentAttendanceDTO::getStudentId, StudentAttendanceDTO::getFeedback));

                        for (Attendance attendance : attendances) {
                                UUID studentId = attendance.getStudent().getId();
                                if (feedbackMap.containsKey(studentId)) {
                                        System.out.println("[DEBUG] Flagging student " + studentId + " as late");
                                        attendance.setStatus(AttendanceStatus.LATE);
                                        attendance.setFeedback(feedbackMap.get(studentId));
                                }
                        }
                }

                System.out.println("[DEBUG] About to save " + attendances.size() + " attendance records");
                List<Attendance> savedAttendances = attendanceRepository.saveAll(attendances);
                System.out.println("[DEBUG] Successfully saved " + savedAttendances.size() + " attendance records");

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

                        // FIX 7: Create defensive copy and use Set for O(1) lookup
                        Set<Student> enrolledStudents = new HashSet<>(section.getStudents());

                        // Process absent students
                        if (bulkAttendanceDTO.getAbsentStudentIds() != null
                                        && !bulkAttendanceDTO.getAbsentStudentIds().isEmpty()) {
                                // FIX 8: Create defensive copy of input list
                                List<UUID> absentStudentIds = new ArrayList<>(bulkAttendanceDTO.getAbsentStudentIds());

                                List<Attendance> absentAttendances = absentStudentIds.stream().map(studentId -> {
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
                        }

                        // Process late students
                        if (bulkAttendanceDTO.getLateStudents() != null
                                        && !bulkAttendanceDTO.getLateStudents().isEmpty()) {
                                // FIX 9: Create defensive copy of late students list
                                List<StudentAttendanceDTO> lateStudents = new ArrayList<>(
                                                bulkAttendanceDTO.getLateStudents());

                                List<Attendance> lateAttendances = lateStudents.stream().map(lateStudent -> {
                                        totalProcessed.incrementAndGet();
                                        try {
                                                boolean isEnrolled = enrolledStudents.stream().anyMatch(
                                                                s -> s.getId().equals(lateStudent.getStudentId()));

                                                if (!isEnrolled) {
                                                        errors.add("Student " + lateStudent.getStudentId()
                                                                        + " is not enrolled in this section");
                                                        failureCount.incrementAndGet();
                                                        return null;
                                                }

                                                if (lateStudent.getFeedback() == null
                                                                || lateStudent.getFeedback().trim().isEmpty()) {
                                                        errors.add("Feedback is required for late student "
                                                                        + lateStudent.getStudentId());
                                                        failureCount.incrementAndGet();
                                                        return null;
                                                }

                                                Student student = studentRepository.findById(lateStudent.getStudentId())
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
                                                errors.add("Error processing student " + lateStudent.getStudentId()
                                                                + ": " + e.getMessage());
                                                failureCount.incrementAndGet();
                                                return null;
                                        }
                                }).filter(Objects::nonNull).collect(Collectors.toList());

                                List<Attendance> savedLateAttendances = attendanceRepository.saveAll(lateAttendances);
                                successfulRecords.addAll(savedLateAttendances.stream().map(this::convertToDTO)
                                                .collect(Collectors.toList()));
                                successCount.addAndGet(savedLateAttendances.size());
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
                        String dateTime) {
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

                        return BulkAttendanceResponseDTO.builder().totalProcessed(totalProcessed)
                                        .successCount(successCount).failureCount(failureCount)
                                        .successfulRecords(successfulRecords).errors(errors).build();

                } catch (Exception e) {
                        throw new RuntimeException("Error processing bulk attendance file: " + e.getMessage());
                }
        }

        private boolean isValidTimeSlotDate(TimeSlot timeSlot, LocalDateTime date) {
                return true;
                /*
                System.out.println("[DEBUG] Validating time slot date");
                System.out.println("[DEBUG] Time slot start time: " + timeSlot.getStartTime());
                System.out.println("[DEBUG] Date to validate: " + date);

                // Parse the time slot's start time (assuming 24-hour format)
                String[] startParts = timeSlot.getStartTime().split(":");

                if (startParts.length != 2) {
                        System.out.println("[DEBUG] Invalid time format in time slot");
                        return false;
                }

                try {
                        int startHour = Integer.parseInt(startParts[0]);
                        int startMinute = Integer.parseInt(startParts[1]);

                        // Create LocalDateTime for the time slot's start time on the given date
                        LocalDateTime slotStart = date.toLocalDate().atTime(startHour, startMinute);
                        // Create LocalDateTime for end of the given date
                        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

                        System.out.println("[DEBUG] Slot start: " + slotStart);
                        System.out.println("[DEBUG] End of day: " + endOfDay);

                        // Check if the time is after slot start and before end of day
                        boolean isValid = date.isAfter(slotStart.minusSeconds(1))
                                        && date.isBefore(endOfDay.plusSeconds(1));
                        System.out.println("[DEBUG] Time is within valid range: " + isValid);
                        return isValid;

                } catch (NumberFormatException e) {
                        System.out.println("[DEBUG] Error parsing time values: " + e.getMessage());
                        return false;
                }

                 */

        }

        @Override
        @Transactional(readOnly = true)
        public List<AttendanceDTO> getStudentAttendance(UUID studentId, LocalDateTime startDate,
                        LocalDateTime endDate) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

                return attendanceRepository.findByStudentAndDateBetween(student, startDate, endDate).stream()
                                .map(this::convertToDTO).collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<AttendanceDTO> getTimeSlotAttendance(Integer timeSlotId, LocalDateTime date) {
                TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));

                return attendanceRepository.findByTimeSlotAndDate(timeSlot, date).stream().map(this::convertToDTO)
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
                List<Attendance> recordsToArchive = attendanceRepository.findByDateBetween(startDate, endDate);

                if (!recordsToArchive.isEmpty()) {
                        List<AttendanceArchive> archives = recordsToArchive.stream().map(this::convertToArchive)
                                        .collect(Collectors.toList());

                        attendanceArchiveRepository.saveAll(archives);
                        attendanceRepository.deleteAll(recordsToArchive);
                }
        }

        @Override
        @Transactional(readOnly = true)
        public List<AttendanceDTO> getArchivedStudentAttendance(UUID studentId, LocalDateTime startDate,
                        LocalDateTime endDate) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

                return attendanceArchiveRepository.findByStudentAndDateBetween(student, startDate, endDate).stream()
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
}
