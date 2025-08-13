package com.crt.server.controller;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.dto.TimeSlotValidationResponseDTO;
import com.crt.server.model.TimeSlotType;
import com.crt.server.service.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-slots")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Time Slots", description = "Time slot management with slot types and conflict detection")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    // Basic CRUD Operations
    @PostMapping
    @Operation(summary = "Create a new time slot")
    public ResponseEntity<TimeSlotDTO> createTimeSlot(@RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(timeSlotService.createTimeSlot(timeSlotDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get time slot by ID")
    public ResponseEntity<TimeSlotDTO> getTimeSlot(@PathVariable Integer id) {
        return ResponseEntity.ok(timeSlotService.getTimeSlot(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update time slot")
    public ResponseEntity<TimeSlotDTO> updateTimeSlot(
            @PathVariable Integer id,
            @RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(timeSlotService.updateTimeSlot(id, timeSlotDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete time slot")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Integer id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }

    // Slot Type Filtering
    @GetMapping("/type/{slotType}")
    @Operation(summary = "Get time slots by type")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsByType(@PathVariable TimeSlotType slotType) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByType(slotType));
    }

    @GetMapping("/section/{sectionId}/type/{slotType}")
    @Operation(summary = "Get section time slots by type")
    public ResponseEntity<List<TimeSlotDTO>> getSectionTimeSlotsByType(
            @PathVariable UUID sectionId,
            @PathVariable TimeSlotType slotType) {
        return ResponseEntity.ok(timeSlotService.getSectionTimeSlotsByType(sectionId, slotType));
    }

    // Day-based Queries
    @GetMapping("/day/{dayOfWeek}")
    @Operation(summary = "Get time slots by day of week")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsByDay(@PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByDay(dayOfWeek));
    }

    @GetMapping("/faculty/{facultyId}/day/{dayOfWeek}")
    @Operation(summary = "Get faculty time slots by day")
    public ResponseEntity<List<TimeSlotDTO>> getFacultyTimeSlotsByDay(
            @PathVariable UUID facultyId,
            @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(timeSlotService.getFacultyTimeSlotsByDay(facultyId, dayOfWeek));
    }

    @GetMapping("/section/{sectionId}/day/{dayOfWeek}")
    @Operation(summary = "Get section time slots by day")
    public ResponseEntity<List<TimeSlotDTO>> getSectionTimeSlotsByDay(
            @PathVariable UUID sectionId,
            @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(timeSlotService.getSectionTimeSlotsByDay(sectionId, dayOfWeek));
    }

    // Section and Faculty Queries
    @GetMapping("/section/{sectionId}")
    @Operation(summary = "Get time slots by section")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsBySection(sectionId));
    }

    @GetMapping("/faculty/{facultyId}")
    @Operation(summary = "Get time slots by faculty")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsByFaculty(@PathVariable UUID facultyId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByFaculty(facultyId));
    }

    @GetMapping("/section/{sectionId}/active")
    @Operation(summary = "Get active time slots by section")
    public ResponseEntity<List<TimeSlotDTO>> getActiveTimeSlotsBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(timeSlotService.getActiveTimeSlotsBySection(sectionId));
    }

    @GetMapping("/faculty/{facultyId}/active")
    @Operation(summary = "Get active time slots by faculty")
    public ResponseEntity<List<TimeSlotDTO>> getActiveTimeSlotsByFaculty(@PathVariable UUID facultyId) {
        return ResponseEntity.ok(timeSlotService.getActiveTimeSlotsByFaculty(facultyId));
    }

    // Validation and Conflict Detection
    @PostMapping("/validate")
    @Operation(summary = "Validate a time slot for conflicts")
    public ResponseEntity<TimeSlotValidationResponseDTO> validateTimeSlot(@RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(timeSlotService.validateTimeSlot(timeSlotDTO));
    }

    @PostMapping("/{timeSlotId}/validate")
    @Operation(summary = "Validate a time slot update for conflicts")
    public ResponseEntity<TimeSlotValidationResponseDTO> validateTimeSlotUpdate(
            @PathVariable Integer timeSlotId,
            @RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(timeSlotService.validateTimeSlotUpdate(timeSlotId, timeSlotDTO));
    }

    @GetMapping("/conflicts")
    @Operation(summary = "Get conflicting time slots for a room and time period")
    public ResponseEntity<List<TimeSlotDTO>> getConflictingTimeSlots(
            @RequestParam UUID roomId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        return ResponseEntity.ok(timeSlotService.getConflictingTimeSlots(roomId, dayOfWeek, startTime, endTime));
    }

    @GetMapping("/check-availability")
    @Operation(summary = "Check if a time slot is available")
    public ResponseEntity<Boolean> isTimeSlotAvailable(
            @RequestParam UUID roomId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        boolean isAvailable = timeSlotService.isTimeSlotAvailable(roomId, dayOfWeek, startTime, endTime);
        return ResponseEntity.ok(isAvailable);
    }

    // Bulk Operations
    @PostMapping("/bulk")
    @Operation(summary = "Create multiple time slots")
    public ResponseEntity<List<TimeSlotDTO>> createTimeSlots(@RequestBody List<TimeSlotDTO> timeSlotDTOs) {
        return ResponseEntity.ok(timeSlotService.createTimeSlots(timeSlotDTOs));
    }

    // Faculty accessible endpoints
    @GetMapping("/faculty/{facultyId}/today")
    @PreAuthorize("hasAuthority('FACULTY') or hasAuthority('ADMIN')")
    @Operation(summary = "Get today's schedule for a faculty member")
    public ResponseEntity<List<TimeSlotDTO>> getFacultyTodaySchedule(@PathVariable UUID facultyId) {
        DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
        return ResponseEntity.ok(timeSlotService.getFacultyTimeSlotsByDay(facultyId, today));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAuthority('FACULTY') or hasAuthority('ADMIN')")
    @Operation(summary = "Get today's complete schedule")
    public ResponseEntity<List<TimeSlotDTO>> getTodaySchedule() {
        DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByDay(today));
    }
}
