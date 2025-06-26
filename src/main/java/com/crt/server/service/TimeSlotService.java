package com.crt.server.service;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.Section;
import com.crt.server.model.User;

import java.util.List;
import java.util.UUID;

public interface TimeSlotService {
    TimeSlotDTO createTimeSlot(TimeSlotDTO timeSlotDTO);

    TimeSlotDTO getTimeSlot(Integer id);

    List<TimeSlotDTO> getTimeSlotsBySection(UUID sectionId);

    List<TimeSlotDTO> getTimeSlotsByFaculty(UUID facultyId);

    TimeSlotDTO updateTimeSlot(Integer id, TimeSlotDTO timeSlotDTO);

    void deleteTimeSlot(Integer id);

    List<TimeSlotDTO> getActiveTimeSlotsBySection(UUID sectionId);

    List<TimeSlotDTO> getActiveTimeSlotsByFaculty(UUID facultyId);

    boolean isTimeSlotAvailable(UUID roomId, String startTime, String endTime);
}