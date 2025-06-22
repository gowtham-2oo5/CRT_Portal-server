package com.crt.server.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.service.TimeSlotService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    @Autowired
    private TimeSlotService timeSlotService;

    @GetMapping("/time-slots")
    @PreAuthorize("hasAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<List<TimeSlotDTO>> getMyTimeSlots(@RequestParam("userId") UUID userId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByFaculty(userId));
    }

    @GetMapping("/time-slots/{facultyId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TimeSlotDTO>> getFacultyTimeSlots(@PathVariable UUID facultyId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByFaculty(facultyId));
    }
}