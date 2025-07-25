package com.crt.server.controller;

import com.crt.server.dto.BatchAttendanceRequestDTO;
import com.crt.server.dto.BatchAttendanceResponseDTO;
import com.crt.server.dto.BatchableTimeSlotResponseDTO;
import com.crt.server.dto.TimeSlotValidationResponseDTO;
import com.crt.server.service.BatchAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BatchAttendanceController {

    private final BatchAttendanceService batchAttendanceService;

    @GetMapping("/time-slots/faculty/{facultyId}/batchable")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<BatchableTimeSlotResponseDTO> getBatchableTimeSlots(
            @PathVariable UUID facultyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting batchable time slots for faculty: {} on date: {}", facultyId, date);
        BatchableTimeSlotResponseDTO response = batchAttendanceService.getBatchableTimeSlots(facultyId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/time-slots/validate-batch")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<TimeSlotValidationResponseDTO> validateBatchTimeSlots(
            @RequestParam String ids) {
        
        List<String> timeSlotIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        
        log.info("Validating batch time slots: {}", timeSlotIds);
        TimeSlotValidationResponseDTO response = batchAttendanceService.validateBatchTimeSlots(timeSlotIds);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attendance/submit-batch")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<BatchAttendanceResponseDTO> submitBatchAttendance(
            @Valid @RequestBody BatchAttendanceRequestDTO request) {
        
        log.info("Submitting batch attendance for section: {} with {} time slots", 
                request.getSectionId(), request.getTimeSlotIds().size());
        
        BatchAttendanceResponseDTO response = batchAttendanceService.submitBatchAttendance(request);
        return ResponseEntity.ok(response);
    }
}
