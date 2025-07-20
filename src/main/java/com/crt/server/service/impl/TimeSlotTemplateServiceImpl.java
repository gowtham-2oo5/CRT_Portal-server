package com.crt.server.service.impl;

import com.crt.server.dto.TimeSlotTemplateDTO;
import com.crt.server.model.TimeSlotTemplate;
import com.crt.server.repository.TimeSlotTemplateRepository;
import com.crt.server.service.TimeSlotTemplateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TimeSlotTemplateServiceImpl implements TimeSlotTemplateService {

    @Autowired
    private TimeSlotTemplateRepository timeSlotTemplateRepository;

    @Override
    @Transactional(readOnly = true)
    public TimeSlotTemplateDTO getTimeSlotTemplate(String templateName) {
        TimeSlotTemplate template = timeSlotTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new EntityNotFoundException("Time slot template not found with name: " + templateName));
        return mapToDTO(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotTemplateDTO> getAllTimeSlotTemplates() {
        return timeSlotTemplateRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TimeSlotTemplateDTO mapToDTO(TimeSlotTemplate timeSlotTemplate) {
        return TimeSlotTemplateDTO.builder()
                .name(timeSlotTemplate.getName())
                .startTime(timeSlotTemplate.getStartTime())
                .endTime(timeSlotTemplate.getEndTime())
                .build();
    }
    
    private TimeSlotTemplate mapToEntity(TimeSlotTemplateDTO dto) {
        return TimeSlotTemplate.builder()
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    @Override
    @Transactional
    public TimeSlotTemplateDTO createTimeSlotTemplate(TimeSlotTemplateDTO timeSlotTemplateDTO) {
        log.info("Creating time slot template: {}", timeSlotTemplateDTO);
        
        // Validate input
        validateTimeSlotTemplateDTO(timeSlotTemplateDTO);
        
        // Check if template with same name already exists
        boolean nameExists = timeSlotTemplateRepository.findByName(timeSlotTemplateDTO.getName()).isPresent();
        
        if (nameExists) {
            throw new IllegalArgumentException("Time slot template with name '" + 
                    timeSlotTemplateDTO.getName() + "' already exists");
        }
        
        // Create and save the new template
        TimeSlotTemplate timeSlotTemplate = mapToEntity(timeSlotTemplateDTO);
        TimeSlotTemplate savedTemplate = timeSlotTemplateRepository.save(timeSlotTemplate);
        
        log.info("Time slot template created successfully: {}", savedTemplate);
        return mapToDTO(savedTemplate);
    }

    @Override
    @Transactional
    public TimeSlotTemplateDTO updateTimeSlotTemplate(String templateName, TimeSlotTemplateDTO timeSlotTemplateDTO) {
        log.info("Updating time slot template: {} with data: {}", templateName, timeSlotTemplateDTO);
        
        // Validate input
        validateTimeSlotTemplateDTO(timeSlotTemplateDTO);
        
        // Find the existing template
        TimeSlotTemplate existingTemplate = timeSlotTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new EntityNotFoundException("Time slot template not found with name: " + templateName));
        
        // Check if new name conflicts with another template (if name is being changed)
        if (!templateName.equals(timeSlotTemplateDTO.getName())) {
            boolean nameExists = timeSlotTemplateRepository.findByName(timeSlotTemplateDTO.getName())
                    .filter(t -> !t.getId().equals(existingTemplate.getId()))
                    .isPresent();
            
            if (nameExists) {
                throw new IllegalArgumentException("Time slot template with name '" + 
                        timeSlotTemplateDTO.getName() + "' already exists");
            }
        }
        
        // Update the template
        existingTemplate.setName(timeSlotTemplateDTO.getName());
        existingTemplate.setStartTime(timeSlotTemplateDTO.getStartTime());
        existingTemplate.setEndTime(timeSlotTemplateDTO.getEndTime());
        
        TimeSlotTemplate updatedTemplate = timeSlotTemplateRepository.save(existingTemplate);
        log.info("Time slot template updated successfully: {}", updatedTemplate);
        
        return mapToDTO(updatedTemplate);
    }

    @Override
    @Transactional
    public void deleteTimeSlotTemplate(String templateName) {
        log.info("Deleting time slot template: {}", templateName);
        
        // Find the template to delete
        TimeSlotTemplate templateToDelete = timeSlotTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new EntityNotFoundException("Time slot template not found with name: " + templateName));
        
        // Delete the template
        timeSlotTemplateRepository.delete(templateToDelete);
        log.info("Time slot template deleted successfully: {}", templateName);
    }
    
    /**
     * Validates the time slot template DTO
     * 
     * @param dto The DTO to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTimeSlotTemplateDTO(TimeSlotTemplateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Time slot template cannot be null");
        }
        
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Time slot template name cannot be empty");
        }
        
        if (dto.getStartTime() == null || dto.getStartTime().trim().isEmpty()) {
            throw new IllegalArgumentException("Start time cannot be empty");
        }
        
        if (dto.getEndTime() == null || dto.getEndTime().trim().isEmpty()) {
            throw new IllegalArgumentException("End time cannot be empty");
        }
        
        try {
            // Validate time format (HH:mm)
            java.time.LocalTime.parse(dto.getStartTime());
            java.time.LocalTime.parse(dto.getEndTime());
            
            // Ensure end time is after start time
            if (!java.time.LocalTime.parse(dto.getEndTime()).isAfter(java.time.LocalTime.parse(dto.getStartTime()))) {
                throw new IllegalArgumentException("End time must be after start time");
            }
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Use HH:mm format (e.g., 09:30)");
        }
    }
}
