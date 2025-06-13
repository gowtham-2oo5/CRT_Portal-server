package com.crt.server.controller;

import com.crt.server.dto.SectionScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.service.SectionScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/section-schedules")
@RequiredArgsConstructor
public class SectionScheduleController {

    private final SectionScheduleService sectionScheduleService;

    @PostMapping
    public ResponseEntity<SectionScheduleDTO> createSchedule(@RequestBody SectionScheduleDTO scheduleDTO) {
        return ResponseEntity.ok(sectionScheduleService.createSchedule(scheduleDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionScheduleDTO> getSchedule(@PathVariable UUID id) {
        return ResponseEntity.ok(sectionScheduleService.getSchedule(id));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<SectionScheduleDTO> getScheduleBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(sectionScheduleService.getScheduleBySection(sectionId));
    }

    @GetMapping
    public ResponseEntity<List<SectionScheduleDTO>> getAllSchedules() {
        return ResponseEntity.ok(sectionScheduleService.getAllSchedules());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionScheduleDTO> updateSchedule(
            @PathVariable UUID id,
            @RequestBody SectionScheduleDTO scheduleDTO) {
        return ResponseEntity.ok(sectionScheduleService.updateSchedule(id, scheduleDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID id) {
        sectionScheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/time-slots")
    public ResponseEntity<SectionScheduleDTO> addTimeSlot(
            @PathVariable UUID scheduleId,
            @RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(sectionScheduleService.addTimeSlot(scheduleId, timeSlotDTO));
    }

    @DeleteMapping("/{scheduleId}/time-slots/{timeSlotId}")
    public ResponseEntity<SectionScheduleDTO> removeTimeSlot(
            @PathVariable UUID scheduleId,
            @PathVariable Integer timeSlotId) {
        return ResponseEntity.ok(sectionScheduleService.removeTimeSlot(scheduleId, timeSlotId));
    }

    @PutMapping("/{scheduleId}/time-slots/{timeSlotId}")
    public ResponseEntity<SectionScheduleDTO> updateTimeSlot(
            @PathVariable UUID scheduleId,
            @PathVariable Integer timeSlotId,
            @RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(sectionScheduleService.updateTimeSlot(scheduleId, timeSlotId, timeSlotDTO));
    }
}