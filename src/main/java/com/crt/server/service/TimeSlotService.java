package com.crt.server.service;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TimeSlotService {
    TimeSlotDTO createTimeSlot(TimeSlotDTO timeSlotDTO);

    TimeSlotDTO getTimeSlot(Integer id);

    @Transactional(readOnly = true)
    @Cacheable(value = "facultyTimeSlots", key = "#faculty.id")
    List<TimeSlot> findByInchargeFaculty(User faculty);

    @Transactional(readOnly = true)
    TimeSlotDTO getTimeSlotById(Integer id);

    List<TimeSlotDTO> getTimeSlotsBySection(UUID sectionId);

    List<TimeSlotDTO> getTimeSlotsByFaculty(UUID facultyId);

    TimeSlotDTO updateTimeSlot(Integer id, TimeSlotDTO timeSlotDTO);

    void deleteTimeSlot(Integer id);

    List<TimeSlotDTO> getActiveTimeSlotsBySection(UUID sectionId);

    List<TimeSlotDTO> getActiveTimeSlotsByFaculty(UUID facultyId);

    boolean isTimeSlotAvailable(UUID roomId, String startTime, String endTime);
}