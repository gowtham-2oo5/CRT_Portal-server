package com.crt.server.service.impl;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.dto.TimeSlotValidationResponseDTO;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.TimeSlotService;
import com.crt.server.service.TimeSlotValidationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final SectionScheduleRepository sectionScheduleRepository;
    private final TimeSlotValidationService validationService;

    @Override
    @Transactional
    @CacheEvict(value = {"timeSlotsBySection", "timeSlotsByFaculty"}, allEntries = true)
    public TimeSlotDTO createTimeSlot(TimeSlotDTO timeSlotDTO) {
        log.debug("Creating time slot: {}", timeSlotDTO);
        
        // Validate the time slot
        TimeSlotValidationResponseDTO validation = validationService.validateTimeSlot(timeSlotDTO);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Time slot validation failed: " + validation.getMessage());
        }
        
        return createTimeSlotInternal(timeSlotDTO);
    }

    @Override
    public TimeSlotDTO getTimeSlot(Integer id) {
        log.debug("Getting time slot with id: {}", id);
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found with id: " + id));
        return convertToDTO(timeSlot);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"timeSlotsBySection", "timeSlotsByFaculty"}, allEntries = true)
    public TimeSlotDTO updateTimeSlot(Integer id, TimeSlotDTO timeSlotDTO) {
        log.debug("Updating time slot with id: {}, data: {}", id, timeSlotDTO);
        
        TimeSlot existingTimeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found with id: " + id));
        
        // Validate the update
        TimeSlotValidationResponseDTO validation = validationService.validateTimeSlotUpdate(id, timeSlotDTO);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Time slot update validation failed: " + validation.getMessage());
        }
        
        // Update fields
        updateTimeSlotFields(existingTimeSlot, timeSlotDTO);
        
        TimeSlot savedTimeSlot = timeSlotRepository.save(existingTimeSlot);
        return convertToDTO(savedTimeSlot);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"timeSlotsBySection", "timeSlotsByFaculty"}, allEntries = true)
    public void deleteTimeSlot(Integer id) {
        log.debug("Deleting time slot with id: {}", id);
        
        if (!timeSlotRepository.existsById(id)) {
            throw new EntityNotFoundException("TimeSlot not found with id: " + id);
        }
        
        timeSlotRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "timeSlotsByType", key = "#slotType")
    public List<TimeSlotDTO> getTimeSlotsByType(TimeSlotType slotType) {
        log.debug("Getting time slots by type: {}", slotType);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySlotType(slotType);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getSectionTimeSlotsByType(UUID sectionId, TimeSlotType slotType) {
        log.debug("Getting section time slots by type: sectionId={}, slotType={}", sectionId, slotType);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySectionIdAndSlotType(sectionId, slotType);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsByDay(DayOfWeek dayOfWeek) {
        log.debug("Getting time slots by day: {}", dayOfWeek);
        List<TimeSlot> timeSlots = timeSlotRepository.findByDayOfWeek(dayOfWeek);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getFacultyTimeSlotsByDay(UUID facultyId, DayOfWeek dayOfWeek) {
        log.debug("Getting faculty time slots by day: facultyId={}, dayOfWeek={}", facultyId, dayOfWeek);
        List<TimeSlot> timeSlots = timeSlotRepository.findByInchargeFacultyIdAndDayOfWeek(facultyId, dayOfWeek);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getSectionTimeSlotsByDay(UUID sectionId, DayOfWeek dayOfWeek) {
        log.debug("Getting section time slots by day: sectionId={}, dayOfWeek={}", sectionId, dayOfWeek);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySectionIdAndDayOfWeek(sectionId, dayOfWeek);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "timeSlotsBySection", key = "#sectionId")
    public List<TimeSlotDTO> getTimeSlotsBySection(UUID sectionId) {
        log.debug("Getting time slots by section: {}", sectionId);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySectionId(sectionId);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "timeSlotsByFaculty", key = "#facultyId")
    public List<TimeSlotDTO> getTimeSlotsByFaculty(UUID facultyId) {
        log.debug("Getting time slots by faculty: {}", facultyId);
        List<TimeSlot> timeSlots = timeSlotRepository.findByInchargeFacultyId(facultyId);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getActiveTimeSlotsBySection(UUID sectionId) {
        log.debug("Getting active time slots by section: {}", sectionId);
        List<TimeSlot> timeSlots = timeSlotRepository.findActiveBySectionId(sectionId);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getActiveTimeSlotsByFaculty(UUID facultyId) {
        log.debug("Getting active time slots by faculty: {}", facultyId);
        List<TimeSlot> timeSlots = timeSlotRepository.findActiveByFacultyId(facultyId);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TimeSlotValidationResponseDTO validateTimeSlot(TimeSlotDTO timeSlotDTO) {
        return validationService.validateTimeSlot(timeSlotDTO);
    }

    @Override
    public TimeSlotValidationResponseDTO validateTimeSlotUpdate(Integer timeSlotId, TimeSlotDTO timeSlotDTO) {
        return validationService.validateTimeSlotUpdate(timeSlotId, timeSlotDTO);
    }

    @Override
    public List<TimeSlotDTO> getConflictingTimeSlots(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime) {
        log.debug("Getting conflicting time slots: roomId={}, dayOfWeek={}, startTime={}, endTime={}", 
                  roomId, dayOfWeek, startTime, endTime);
        List<TimeSlot> conflicts = validationService.getConflictingTimeSlots(roomId, dayOfWeek, startTime, endTime, null);
        return conflicts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTimeSlotAvailable(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime) {
        return !timeSlotRepository.existsConflictingTimeSlot(roomId, dayOfWeek, startTime, endTime);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"timeSlotsBySection", "timeSlotsByFaculty"}, allEntries = true)
    public List<TimeSlotDTO> createTimeSlots(List<TimeSlotDTO> timeSlotDTOs) {
        log.debug("Creating {} time slots", timeSlotDTOs.size());
        
        return timeSlotDTOs.stream()
                .map(this::createTimeSlot)
                .collect(Collectors.toList());
    }

    // Legacy methods for backward compatibility
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "facultyTimeSlots", key = "#faculty.id")
    public List<TimeSlot> findByInchargeFaculty(User faculty) {
        return timeSlotRepository.findByInchargeFaculty(faculty);
    }

    @Override
    @Transactional(readOnly = true)
    public TimeSlotDTO getTimeSlotById(Integer id) {
        return getTimeSlot(id);
    }

    private TimeSlotDTO createTimeSlotInternal(TimeSlotDTO timeSlotDTO) {
        TimeSlot timeSlot = convertToEntity(timeSlotDTO);
        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        return convertToDTO(savedTimeSlot);
    }

    private void updateTimeSlotFields(TimeSlot existingTimeSlot, TimeSlotDTO timeSlotDTO) {
        existingTimeSlot.setStartTime(timeSlotDTO.getStartTime());
        existingTimeSlot.setEndTime(timeSlotDTO.getEndTime());
        existingTimeSlot.setSlotType(timeSlotDTO.getSlotType());
        existingTimeSlot.setTitle(timeSlotDTO.getTitle());
        existingTimeSlot.setDescription(timeSlotDTO.getDescription());
        existingTimeSlot.setDayOfWeek(timeSlotDTO.getDayOfWeek());

        // Update relationships if changed
        if (!existingTimeSlot.getInchargeFaculty().getId().equals(timeSlotDTO.getInchargeFacultyId())) {
            User faculty = userRepository.findById(timeSlotDTO.getInchargeFacultyId())
                    .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));
            existingTimeSlot.setInchargeFaculty(faculty);
        }

        if (!existingTimeSlot.getSection().getId().equals(timeSlotDTO.getSectionId())) {
            Section section = sectionRepository.findById(timeSlotDTO.getSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Section not found"));
            existingTimeSlot.setSection(section);
        }

        if (!existingTimeSlot.getRoom().getId().equals(timeSlotDTO.getRoomId())) {
            Room room = roomRepository.findById(timeSlotDTO.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found"));
            existingTimeSlot.setRoom(room);
        }
    }

    private TimeSlot convertToEntity(TimeSlotDTO dto) {
        User faculty = userRepository.findById(dto.getInchargeFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));
        
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
        
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        // Get the section schedule - use first available if multiple exist
        SectionSchedule schedule = sectionScheduleRepository.findFirstBySectionId(dto.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section schedule not found for section: " + dto.getSectionId()));

        return TimeSlot.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .slotType(dto.getSlotType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dayOfWeek(dto.getDayOfWeek())
                .inchargeFaculty(faculty)
                .section(section)
                .room(room)
                .schedule(schedule)
                .build();
    }

    private TimeSlotDTO convertToDTO(TimeSlot timeSlot) {
        return TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .slotType(timeSlot.getSlotType())
                .title(timeSlot.getTitle())
                .description(timeSlot.getDescription())
                .dayOfWeek(timeSlot.getDayOfWeek())
                .inchargeFacultyId(timeSlot.getInchargeFaculty().getId())
                .inchargeFacultyName(timeSlot.getInchargeFaculty().getName())
                .inchargeFacultyEmail(timeSlot.getInchargeFaculty().getEmail())
                .inchargeFacultyPhone(timeSlot.getInchargeFaculty().getPhone())
                .sectionId(timeSlot.getSection().getId())
                .sectionName(timeSlot.getSection().getName())
                .roomId(timeSlot.getRoom().getId())
                .roomName(timeSlot.getRoom().toString())
                .build();
    }
}
