package com.crt.server.service;

import com.crt.server.dto.TimeSlotTemplateDTO;

import java.util.List;

public interface TimeSlotTemplateService {

    TimeSlotTemplateDTO getTimeSlotTemplate(String templateName);

    List<TimeSlotTemplateDTO> getAllTimeSlotTemplates();

    TimeSlotTemplateDTO createTimeSlotTemplate(TimeSlotTemplateDTO timeSlotTemplateDTO);

    TimeSlotTemplateDTO updateTimeSlotTemplate(String templateName, TimeSlotTemplateDTO timeSlotTemplateDTO);

    void deleteTimeSlotTemplate(String templateName);

}
