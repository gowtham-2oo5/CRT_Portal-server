package com.crt.server.service;

import com.crt.server.dto.SectionScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface SectionScheduleService {
    SectionScheduleDTO createSchedule(SectionScheduleDTO scheduleDTO);

    SectionScheduleDTO getSchedule(UUID id);

    SectionScheduleDTO getScheduleBySection(UUID sectionId);

    List<SectionScheduleDTO> getAllSchedules();

    SectionScheduleDTO updateSchedule(UUID id, SectionScheduleDTO scheduleDTO);

    void deleteSchedule(UUID id);

    SectionScheduleDTO addTimeSlot(UUID scheduleId, TimeSlotDTO timeSlotDTO);

    SectionScheduleDTO removeTimeSlot(UUID scheduleId, Integer timeSlotId);

    SectionScheduleDTO updateTimeSlot(UUID scheduleId, Integer timeSlotId, TimeSlotDTO timeSlotDTO);

    String bulkUploadSchedule(MultipartFile file) throws Exception;
}