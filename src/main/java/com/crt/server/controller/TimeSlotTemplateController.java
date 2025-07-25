package com.crt.server.controller;

import com.crt.server.dto.TimeSlotTemplateDTO;
import com.crt.server.service.TimeSlotTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/time-slot-templates")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ADMIN')")
public class TimeSlotTemplateController {

    private final TimeSlotTemplateService timeSlotTemplateService;

    @GetMapping
    public ResponseEntity<List<TimeSlotTemplateDTO>> getAllTimeSlotTemplates() {
        log.info("Getting all time slot templates");
        List<TimeSlotTemplateDTO> templates = timeSlotTemplateService.getAllTimeSlotTemplates();
        for (TimeSlotTemplateDTO template : templates) {
            System.out.println(template.getName());
        }
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{templateName}")
    public ResponseEntity<TimeSlotTemplateDTO> getTimeSlotTemplate(@PathVariable String templateName) {
        log.info("Getting time slot template: {}", templateName);
        return ResponseEntity.ok(timeSlotTemplateService.getTimeSlotTemplate(templateName));
    }

    @PostMapping
    public ResponseEntity<TimeSlotTemplateDTO> createTimeSlotTemplate(@Valid @RequestBody TimeSlotTemplateDTO timeSlotTemplateDTO) {
        log.info("Creating time slot template: {}", timeSlotTemplateDTO);
        TimeSlotTemplateDTO createdTemplate = timeSlotTemplateService.createTimeSlotTemplate(timeSlotTemplateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }

    @PutMapping("/{templateName}")
    public ResponseEntity<TimeSlotTemplateDTO> updateTimeSlotTemplate(
            @PathVariable String templateName,
            @Valid @RequestBody TimeSlotTemplateDTO timeSlotTemplateDTO) {
        log.info("Updating time slot template: {} with data: {}", templateName, timeSlotTemplateDTO);
        TimeSlotTemplateDTO updatedTemplate = timeSlotTemplateService.updateTimeSlotTemplate(templateName, timeSlotTemplateDTO);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{templateName}")
    public ResponseEntity<Void> deleteTimeSlotTemplate(@PathVariable String templateName) {
        log.info("Deleting time slot template: {}", templateName);
        timeSlotTemplateService.deleteTimeSlotTemplate(templateName);
        return ResponseEntity.noContent().build();
    }
}
