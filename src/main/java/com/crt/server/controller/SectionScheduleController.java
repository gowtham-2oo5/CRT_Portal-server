package com.crt.server.controller;

import com.crt.server.dto.SectionScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.SectionScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/section-schedules")
@RequiredArgsConstructor
public class SectionScheduleController {

    @Autowired
    private SectionScheduleService sectionScheduleService;

    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody SectionScheduleDTO scheduleDTO) {
        try {
            SectionScheduleDTO createdSchedule = sectionScheduleService.createSchedule(scheduleDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdSchedule);
        } catch (Exception e) {
            log.error("Error creating schedule: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message("Failed to create schedule: " + e.getMessage())
                    .path("/api/section-schedules")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSchedule(@PathVariable UUID id) {
        try {
            SectionScheduleDTO schedule = sectionScheduleService.getSchedule(id);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error getting schedule by id {}: {}", id, e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message("Schedule not found with id: " + id)
                    .path("/api/section-schedules/" + id)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> getScheduleBySection(@PathVariable UUID sectionId) {
        try {
            SectionScheduleDTO schedule = sectionScheduleService.getScheduleBySection(sectionId);
            // Return null if no schedule found (not an error case)
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error getting schedule for section {}: {}", sectionId, e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                    .message("Error retrieving schedule for section: " + e.getMessage())
                    .path("/api/section-schedules/section/" + sectionId)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSchedules() {
        try {
            List<SectionScheduleDTO> schedules = sectionScheduleService.getAllSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            log.error("Error getting all schedules: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                    .message("Failed to retrieve schedules: " + e.getMessage())
                    .path("/api/section-schedules")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable UUID id,
            @RequestBody SectionScheduleDTO scheduleDTO) {
        try {
            SectionScheduleDTO updatedSchedule = sectionScheduleService.updateSchedule(id, scheduleDTO);
            return ResponseEntity.ok(updatedSchedule);
        } catch (Exception e) {
            log.error("Error updating schedule {}: {}", id, e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message("Failed to update schedule: " + e.getMessage())
                    .path("/api/section-schedules/" + id)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable UUID id) {
        try {
            sectionScheduleService.deleteSchedule(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting schedule {}: {}", id, e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message("Schedule not found with id: " + id)
                    .path("/api/section-schedules/" + id)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }

    @PostMapping("/{scheduleId}/time-slots")
    public ResponseEntity<?> addTimeSlot(
            @PathVariable UUID scheduleId,
            @RequestBody TimeSlotDTO timeSlotDTO) {
        try {
            System.out.println("Adding time slot to schedule with ID: " + scheduleId);
            System.out.println("Time slot DTO: " + timeSlotDTO);
            SectionScheduleDTO updatedSchedule = sectionScheduleService.addTimeSlot(scheduleId, timeSlotDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(updatedSchedule);
        } catch (Exception e) {
            log.error("Error adding time slot to schedule {}: {}", scheduleId, e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message("Failed to add time slot: " + e.getMessage())
                    .path("/api/section-schedules/" + scheduleId + "/time-slots")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @DeleteMapping("/{scheduleId}/time-slots/{timeSlotId}")
    public ResponseEntity<?> removeTimeSlot(
            @PathVariable UUID scheduleId,
            @PathVariable Integer timeSlotId) {
        try {
            SectionScheduleDTO updatedSchedule = sectionScheduleService.removeTimeSlot(scheduleId, timeSlotId);
            return ResponseEntity.ok(updatedSchedule);
        } catch (Exception e) {
            log.error("Error removing time slot {} from schedule {}: {}", timeSlotId, scheduleId, e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message("Failed to remove time slot: " + e.getMessage())
                    .path("/api/section-schedules/" + scheduleId + "/time-slots/" + timeSlotId)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }

    @PutMapping("/{scheduleId}/time-slots/{timeSlotId}")
    public ResponseEntity<?> updateTimeSlot(
            @PathVariable UUID scheduleId,
            @PathVariable Integer timeSlotId,
            @RequestBody TimeSlotDTO timeSlotDTO) {
        try {
            SectionScheduleDTO updatedSchedule = sectionScheduleService.updateTimeSlot(scheduleId, timeSlotId, timeSlotDTO);
            return ResponseEntity.ok(updatedSchedule);
        } catch (Exception e) {
            log.error("Error updating time slot {} in schedule {}: {}", timeSlotId, scheduleId, e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message("Failed to update time slot: " + e.getMessage())
                    .path("/api/section-schedules/" + scheduleId + "/time-slots/" + timeSlotId)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }
}
