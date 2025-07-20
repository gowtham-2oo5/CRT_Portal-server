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

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            // Try parsing as full datetime first
            return LocalDateTime.parse(dateStr);
        } catch (DateTimeParseException e1) {
            try {
                // Try parsing as date only, then convert to datetime
                LocalDate date = LocalDate.parse(dateStr);
                return date.atStartOfDay();
            } catch (DateTimeParseException e2) {
                // Try with custom formatter
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(dateStr, formatter);
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
            @PathVariable Integer timeSlotId
    ){
        return ResponseEntity.ok(attendanceService.getAbsenteesByTimeSlotId(timeSlotId));
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
}