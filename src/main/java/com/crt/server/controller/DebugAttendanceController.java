package com.crt.server.controller;

import com.crt.server.dto.AttendanceDTO;
import com.crt.server.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/debug/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class DebugAttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Debug endpoint to get all attendance records for a time slot on a specific date
     * This helps troubleshoot why absentees might not be showing up
     */
    @GetMapping("/timeslot/{timeSlotId}")
    public ResponseEntity<List<AttendanceDTO>> debugGetAllAttendanceForTimeSlotAndDate(
            @PathVariable Integer timeSlotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDateTime dateTime = (date != null) ? date.atStartOfDay() : LocalDateTime.now();
        
        log.info("DEBUG ENDPOINT: Getting all attendance for timeSlotId: {} on date: {}", timeSlotId, dateTime);
        
        List<AttendanceDTO> result = attendanceService.debugGetAllAttendanceForTimeSlotAndDate(timeSlotId, dateTime);
        
        log.info("DEBUG ENDPOINT: Returning {} attendance records", result.size());
        
        return ResponseEntity.ok(result);
    }
}
