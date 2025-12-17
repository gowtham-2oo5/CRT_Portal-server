package com.crt.server.controller;

import com.crt.server.dto.AdminAttendanceRequestDTO;
import com.crt.server.dto.BulkAttendanceResponseDTO;
import com.crt.server.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/attendance-post")
    public ResponseEntity<BulkAttendanceResponseDTO> overrideAttendance(
            @Valid @RequestBody AdminAttendanceRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Admin override attendance for time slot: {} on date: {}", 
                request.getTimeSlotId(), request.getDateTime());
        
        // Set admin flag explicitly to true
        request.setOverriddenBy(UUID.fromString(userDetails.getUsername()));
        
        BulkAttendanceResponseDTO response = attendanceService.adminOverrideAttendance(request);
        return ResponseEntity.ok(response);
    }
}
