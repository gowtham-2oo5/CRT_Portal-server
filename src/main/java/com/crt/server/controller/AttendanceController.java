package com.crt.server.controller;

import com.crt.server.dto.AttendanceDTO;
import com.crt.server.dto.AttendanceReportDTO;
import com.crt.server.dto.MarkAttendanceDTO;
import com.crt.server.dto.BulkAttendanceDTO;
import com.crt.server.dto.BulkAttendanceResponseDTO;
import com.crt.server.dto.SectionAttendanceRecordDTO;
import com.crt.server.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    @PreAuthorize("hasAuthority('FACULTY')")
    public ResponseEntity<List<AttendanceDTO>> markAttendance(@RequestBody MarkAttendanceDTO markAttendanceDTO) {
        return ResponseEntity.ok(attendanceService.markAttendance(markAttendanceDTO));
    }

    @PostMapping("/mark/bulk")
    @PreAuthorize("hasAuthority('FACULTY')")
    public ResponseEntity<BulkAttendanceResponseDTO> markBulkAttendance(
            @RequestBody BulkAttendanceDTO bulkAttendanceDTO) {
        return ResponseEntity.ok(attendanceService.markBulkAttendance(bulkAttendanceDTO));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendance(
            @PathVariable UUID studentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(attendanceService.getStudentAttendance(
                studentId,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate)));
    }

    @GetMapping("/time-slot/{timeSlotId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<List<AttendanceDTO>> getTimeSlotAttendance(
            @PathVariable Integer timeSlotId,
            @RequestParam String date) {
        return ResponseEntity.ok(attendanceService.getTimeSlotAttendance(
                timeSlotId,
                LocalDateTime.parse(date)));
    }

    @GetMapping("/report/{studentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<AttendanceReportDTO> getStudentAttendanceReport(
            @PathVariable UUID studentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(attendanceService.getStudentAttendanceReport(
                studentId,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate)));
    }

    @PostMapping("/archive")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> archiveAttendanceRecords(
            @RequestParam int year,
            @RequestParam int month) {
        attendanceService.archiveAttendanceRecords(year, month);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/archived/student/{studentId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AttendanceDTO>> getArchivedStudentAttendance(
            @PathVariable UUID studentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(attendanceService.getArchivedStudentAttendance(
                studentId,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate)));
    }

    @GetMapping("/archived/report/{studentId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AttendanceReportDTO> getArchivedStudentAttendanceReport(
            @PathVariable UUID studentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(attendanceService.getArchivedStudentAttendanceReport(
                studentId,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate)));
    }

    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<List<SectionAttendanceRecordDTO>> getSectionAttendanceRecords(
            @PathVariable UUID sectionId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(attendanceService.getSectionAttendanceRecords(
                sectionId,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate)));
    }
}