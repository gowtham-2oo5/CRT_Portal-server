package com.crt.server.service.impl;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.dto.TimeSlotValidationResponseDTO;
import com.crt.server.model.Room;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import com.crt.server.repository.RoomRepository;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.TimeSlotRepository;
import com.crt.server.service.TimeSlotValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotValidationServiceImpl implements TimeSlotValidationService {

    private final TimeSlotRepository timeSlotRepository;
    private final RoomRepository roomRepository;
    private final SectionRepository sectionRepository;

    @Override
    public TimeSlotValidationResponseDTO validateTimeSlot(TimeSlotDTO timeSlotDTO) {
        return validateTimeSlotInternal(null, timeSlotDTO);
    }

    @Override
    public TimeSlotValidationResponseDTO validateTimeSlotUpdate(Integer timeSlotId, TimeSlotDTO timeSlotDTO) {
        return validateTimeSlotInternal(timeSlotId, timeSlotDTO);
    }

    private TimeSlotValidationResponseDTO validateTimeSlotInternal(Integer excludeTimeSlotId, TimeSlotDTO timeSlotDTO) {
        log.debug("Validating time slot: {}, excluding ID: {}", timeSlotDTO, excludeTimeSlotId);

        // Basic field validation
        if (timeSlotDTO.getRoomId() == null) {
            return TimeSlotValidationResponseDTO.invalid("Room ID is required");
        }
        
        if (timeSlotDTO.getInchargeFacultyId() == null) {
            return TimeSlotValidationResponseDTO.invalid("Faculty ID is required");
        }
        
        if (timeSlotDTO.getSectionId() == null) {
            return TimeSlotValidationResponseDTO.invalid("Section ID is required");
        }
        
        if (timeSlotDTO.getDayOfWeek() == null) {
            return TimeSlotValidationResponseDTO.invalid("Day of week is required");
        }

        // Time format validation
        if (!isValidTimeRange(timeSlotDTO.getStartTime(), timeSlotDTO.getEndTime())) {
            return TimeSlotValidationResponseDTO.invalid("Invalid time range");
        }

        // Room capacity validation
        if (!isRoomCapacitySufficient(timeSlotDTO.getRoomId(), timeSlotDTO.getSectionId())) {
            return TimeSlotValidationResponseDTO.invalid("Room capacity is insufficient for section strength");
        }

        // Room conflict validation
        if (hasRoomConflict(timeSlotDTO.getRoomId(), timeSlotDTO.getDayOfWeek(), 
                           timeSlotDTO.getStartTime(), timeSlotDTO.getEndTime(), excludeTimeSlotId)) {
            return TimeSlotValidationResponseDTO.invalid("Room conflict detected for the specified time period");
        }

        // Faculty availability validation
        if (!isFacultyAvailable(timeSlotDTO.getInchargeFacultyId(), timeSlotDTO.getDayOfWeek(),
                               timeSlotDTO.getStartTime(), timeSlotDTO.getEndTime(), excludeTimeSlotId)) {
            return TimeSlotValidationResponseDTO.invalid("Faculty is not available for the specified time period");
        }

        return TimeSlotValidationResponseDTO.valid("Time slot validation successful");
    }

    @Override
    public boolean hasRoomConflict(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime, Integer excludeTimeSlotId) {
        List<TimeSlot> conflicts = getConflictingTimeSlots(roomId, dayOfWeek, startTime, endTime, excludeTimeSlotId);
        return !conflicts.isEmpty();
    }

    @Override
    public List<TimeSlot> getConflictingTimeSlots(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime, Integer excludeTimeSlotId) {
        return timeSlotRepository.findConflictingTimeSlots(roomId, dayOfWeek, startTime, endTime, excludeTimeSlotId);
    }

    @Override
    public boolean isFacultyAvailable(UUID facultyId, DayOfWeek dayOfWeek, String startTime, String endTime, Integer excludeTimeSlotId) {
        List<TimeSlot> conflicts = timeSlotRepository.findFacultyConflictingTimeSlots(
                facultyId, dayOfWeek, startTime, endTime, excludeTimeSlotId);
        return conflicts.isEmpty();
    }

    @Override
    public boolean isValidTimeRange(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);
            
            // End time must be after start time
            return end.isAfter(start);
        } catch (DateTimeParseException e) {
            log.warn("Invalid time format: startTime={}, endTime={}", startTime, endTime);
            return false;
        }
    }

    @Override
    public boolean isRoomCapacitySufficient(UUID roomId, UUID sectionId) {
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));
            
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new IllegalArgumentException("Section not found"));
            
            return room.getCapacity() >= section.getStrength();
        } catch (Exception e) {
            log.error("Error validating room capacity: {}", e.getMessage());
            return false;
        }
    }
}
