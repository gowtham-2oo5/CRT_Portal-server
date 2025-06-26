package com.crt.server.service;

import com.crt.server.dto.TodayScheduleDTO;
import com.crt.server.dto.AssignedSectionDTO;
import com.crt.server.model.User;
import com.crt.server.model.TimeSlot;

import java.util.List;
import java.util.Optional;

public interface FacultyTimetableService {
    
    /**
     * Get today's schedule for faculty with active session detection
     */
    List<TodayScheduleDTO> getTodaySchedule(User faculty);
    
    /**
     * Get all sections assigned to faculty
     */
    List<AssignedSectionDTO> getAssignedSections(User faculty);
    
    /**
     * Get current active time slot for faculty (if any)
     */
    Optional<TimeSlot> getCurrentActiveTimeSlot(User faculty);
    
    /**
     * Check if a time slot is currently active
     */
    boolean isTimeSlotActive(TimeSlot timeSlot);
}
