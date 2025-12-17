package com.crt.server.controller;

import com.crt.server.dto.*;
import com.crt.server.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/mark")
    public ResponseEntity<List<AttendanceDTO>> markAttendance(@Valid @RequestBody MarkAttendanceDTO markAttendanceDTO) {
        log.info("Marking attendance for: {}", markAttendanceDTO);

        // Set isAdminRequest flag based on user role
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        markAttendanceDTO.setIsAdminRequest(isAdmin);

        return ResponseEntity.ok(attendanceService.markAttendance(markAttendanceDTO));
    }

    @PostMapping("/mark/bulk")
    public ResponseEntity<BulkAttendanceResponseDTO> markBulkAttendance(
            @Valid @RequestBody BulkAttendanceDTO bulkAttendanceDTO) {
        log.info("Marking bulk attendance");

        // Set isAdminRequest flag based on user role
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        bulkAttendanceDTO.setIsAdminRequest(isAdmin);

        return ResponseEntity.ok(attendanceService.markBulkAttendance(bulkAttendanceDTO));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendance(
            @PathVariable UUID studentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            log.info("Getting attendance for student: {} from {} to {}", studentId, start, end);
            return ResponseEntity.ok(attendanceService.getStudentAttendance(studentId, start, end));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss or yyyy-MM-dd");
        }
    }

    @GetMapping("/time-slot/{timeSlotId}")
    public ResponseEntity<List<AttendanceDTO>> getTimeSlotAttendance(
            @PathVariable Integer timeSlotId) {
        try {
            LocalDateTime dateTime = LocalDateTime.now();
            log.info("Getting attendance for time slot: {} on date: {}", timeSlotId, dateTime);
            return ResponseEntity.ok(attendanceService.getTimeSlotAttendance(timeSlotId, dateTime));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss or yyyy-MM-dd");
        }
    }

    @GetMapping("/report/{studentId}")
    public ResponseEntity<AttendanceReportDTO> getStudentAttendanceReport(
            @PathVariable UUID studentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            log.info("Getting attendance report for student: {} from {} to {}", studentId, start, end);
            return ResponseEntity.ok(attendanceService.getStudentAttendanceReport(studentId, start, end));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss or yyyy-MM-dd");
        }
    }

    @PostMapping("/archive")
    public ResponseEntity<Void> archiveAttendanceRecords(
            @RequestParam int year,
            @RequestParam int month) {
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2000 and 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        log.info("Archiving attendance records for {}/{}", year, month);
        attendanceService.archiveAttendanceRecords(year, month);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/archived/student/{studentId}")
    public ResponseEntity<List<AttendanceDTO>> getArchivedStudentAttendance(
            @PathVariable UUID studentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            log.info("Getting archived attendance for student: {} from {} to {}", studentId, start, end);
            return ResponseEntity.ok(attendanceService.getArchivedStudentAttendance(studentId, start, end));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss or yyyy-MM-dd");
        }
    }

    @GetMapping("/archived/report/{studentId}")
    public ResponseEntity<AttendanceReportDTO> getArchivedStudentAttendanceReport(
            @PathVariable UUID studentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            log.info("Getting archived attendance report for student: {} from {} to {}", studentId, start, end);
            return ResponseEntity.ok(attendanceService.getArchivedStudentAttendanceReport(studentId, start, end));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss or yyyy-MM-dd");
        }
    }


    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<SectionAttendanceRecordDTO>> getSectionAttendanceRecords(
            @PathVariable UUID sectionId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            log.info("Getting section attendance records for section: {} from {} to {}", sectionId, start, end);
            return ResponseEntity.ok(attendanceService.getSectionAttendanceRecords(sectionId, start, end));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss or yyyy-MM-dd");
        }
    }

    /**
     * Improved date parsing method with better error handling and support for multiple formats
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        String trimmedDate = dateStr.trim();

        try {
            // Try parsing as full datetime first (ISO format)
            return LocalDateTime.parse(trimmedDate);
        } catch (DateTimeParseException e1) {
            try {
                // Try parsing as date only, then convert to datetime
                LocalDate date = LocalDate.parse(trimmedDate);
                return date.atStartOfDay();
            } catch (DateTimeParseException e2) {
                try {
                    // Try with custom formatter for "yyyy-MM-dd HH:mm:ss"
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return LocalDateTime.parse(trimmedDate, formatter);
                } catch (DateTimeParseException e3) {
                    try {
                        // Try with another common format "dd/MM/yyyy HH:mm:ss"
                        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        return LocalDateTime.parse(trimmedDate, formatter2);
                    } catch (DateTimeParseException e4) {
                        log.error("Failed to parse date string: '{}'. Supported formats: ISO (yyyy-MM-ddTHH:mm:ss), Date only (yyyy-MM-dd), yyyy-MM-dd HH:mm:ss, dd/MM/yyyy HH:mm:ss", trimmedDate);
                        throw new IllegalArgumentException("Invalid date format: '" + trimmedDate + "'. Supported formats: ISO (yyyy-MM-ddTHH:mm:ss), Date only (yyyy-MM-dd), yyyy-MM-dd HH:mm:ss, dd/MM/yyyy HH:mm:ss");
                    }
                }
            }
        }
    }

    @GetMapping("/absentees/section/{sectionId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<List<AbsenteeDTO>> getAbsenteesByDateAndSection(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable UUID sectionId) {
        return ResponseEntity.ok(attendanceService.getAbsenteesByDateAndSection(date, sectionId));
    }

    @GetMapping("/absentees/{timeSlotId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AbsenteeDTO>> getAbsenteesByTimeSlot(
            @PathVariable Integer timeSlotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        if (date == null) {
            date = LocalDateTime.now();
        }
        return ResponseEntity.ok(attendanceService.getAbsenteesByTimeSlotIdAndDate(timeSlotId, date));
    }

    @GetMapping("/time-slots/filter")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<TimeSlotFilterResponseDTO> getTimeSlotsByDayAndTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        log.info("Filtering time slots for date: {}, startTime: {}, endTime: {}", date, startTime, endTime);
        return ResponseEntity.ok(attendanceService.getTimeSlotsByDayAndTime(date, startTime, endTime));
    }

    @GetMapping("/time-slots/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<List<TimeSlotDTO>> getPendingTimeSlotsByDayAndTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        log.info("Filtering time slots for date: {}, startTime: {}, endTime: {}", date, startTime, endTime);

        return ResponseEntity.ok(attendanceService.getPendingAttendanceTimeSlots(date, startTime, endTime));
    }



    @PostMapping("/admin/override")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BulkAttendanceResponseDTO> adminOverrideAttendance(
            @Valid @RequestBody AdminAttendanceRequestDTO requestDTO) {
        log.info("Admin override attendance request for timeSlotId: {}, dateTime: {}",
                requestDTO.getTimeSlotId(), requestDTO.getDateTime());
        return ResponseEntity.ok(attendanceService.adminOverrideAttendance(requestDTO));
    }

    @GetMapping("/absentees")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AbsenteeDTO>> getAbsenteesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAbsenteesByDate(date));
    }
}
