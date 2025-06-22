package com.crt.server.controller;

import com.crt.server.dto.*;
import com.crt.server.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/mark")
    public ResponseEntity<List<AttendanceDTO>> markAttendance(@RequestBody MarkAttendanceDTO markAttendanceDTO) {
        return ResponseEntity.ok(attendanceService.markAttendance(markAttendanceDTO));
    }

    @PostMapping("/mark/bulk")
    public ResponseEntity<BulkAttendanceResponseDTO> markBulkAttendance(
            @RequestBody BulkAttendanceDTO bulkAttendanceDTO) {
        return ResponseEntity.ok(attendanceService.markBulkAttendance(bulkAttendanceDTO));
    }

    @GetMapping("/student/{studentId}")
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
    public ResponseEntity<List<AttendanceDTO>> getTimeSlotAttendance(
            @PathVariable Integer timeSlotId,
            @RequestParam String date) {
        return ResponseEntity.ok(attendanceService.getTimeSlotAttendance(
                timeSlotId,
                LocalDateTime.parse(date)));
    }

    @GetMapping("/report/{studentId}")
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
    public ResponseEntity<Void> archiveAttendanceRecords(
            @RequestParam int year,
            @RequestParam int month) {
        attendanceService.archiveAttendanceRecords(year, month);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/archived/student/{studentId}")
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