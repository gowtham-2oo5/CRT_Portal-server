package com.crt.server.service;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.dto.TimeSlotValidationResponseDTO;
import com.crt.server.model.TimeSlot;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface TimeSlotValidationService {
    
    /**
     * Validates if a time slot can be created without conflicts
     */
    TimeSlotValidationResponseDTO validateTimeSlot(TimeSlotDTO timeSlotDTO);
    
    /**
     * Validates if a time slot can be updated without conflicts
     */
    TimeSlotValidationResponseDTO validateTimeSlotUpdate(Integer timeSlotId, TimeSlotDTO timeSlotDTO);
    
    /**
     * Checks for room conflicts on a specific day of week
     */
    boolean hasRoomConflict(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime, Integer excludeTimeSlotId);
    
    /**
     * Gets conflicting time slots for a room and time period
     */
    List<TimeSlot> getConflictingTimeSlots(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime, Integer excludeTimeSlotId);
    
    /**
     * Validates faculty availability
     */
    boolean isFacultyAvailable(UUID facultyId, DayOfWeek dayOfWeek, String startTime, String endTime, Integer excludeTimeSlotId);
    
    /**
     * Validates time format and logical consistency
     */
    boolean isValidTimeRange(String startTime, String endTime);
    
    /**
     * Validates room capacity against section strength
     */
    boolean isRoomCapacitySufficient(UUID roomId, UUID sectionId);
}
