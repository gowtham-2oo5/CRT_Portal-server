package com.crt.server.controller;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-slots")
@PreAuthorize( "hasAuthority('ADMIN')")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    @PostMapping
    public ResponseEntity<TimeSlotDTO> createTimeSlot( @RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(timeSlotService.createTimeSlot(timeSlotDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotDTO> getTimeSlot(@PathVariable Integer id) {
        return ResponseEntity.ok(timeSlotService.getTimeSlot(id));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsBySection(sectionId));
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsByFaculty(@PathVariable UUID facultyId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByFaculty(facultyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeSlotDTO> updateTimeSlot(
            @PathVariable Integer id,
            @RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(timeSlotService.updateTimeSlot(id, timeSlotDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Integer id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/section/{sectionId}/active")
    public ResponseEntity<List<TimeSlotDTO>> getActiveTimeSlotsBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(timeSlotService.getActiveTimeSlotsBySection(sectionId));
    }

    @GetMapping("/faculty/{facultyId}/active")
    public ResponseEntity<List<TimeSlotDTO>> getActiveTimeSlotsByFaculty(@PathVariable UUID facultyId) {
        return ResponseEntity.ok(timeSlotService.getActiveTimeSlotsByFaculty(facultyId));
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> isTimeSlotAvailable(
            @RequestParam UUID roomId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        boolean isAvailable = timeSlotService.isTimeSlotAvailable(roomId, startTime, endTime);
        return ResponseEntity.ok(isAvailable);
    }
}