package com.crt.server.service.impl;

import com.crt.server.dto.AssignedSectionDTO;
import com.crt.server.dto.TodayScheduleDTO;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import com.crt.server.repository.TimeSlotRepository;
import com.crt.server.service.FacultyTimetableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyTimetableServiceImpl implements FacultyTimetableService {

    private final TimeSlotRepository timeSlotRepository;

    @Override
    public List<TodayScheduleDTO> getTodaySchedule(User faculty) {
        log.info("Getting today's schedule for faculty: {}", faculty.getUsername());
        
        // Get all time slots for faculty (since TimeSlot doesn't have day field)
        List<TimeSlot> facultySlots = timeSlotRepository.findByInchargeFaculty(faculty);
        
        return facultySlots.stream()
                .filter(slot -> !slot.isBreak()) // Exclude break slots
                .map(slot -> TodayScheduleDTO.builder()
                        .id(slot.getId().toString())
                        .day("Today") // Since TimeSlot doesn't store day info
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .sectionName(slot.getSection().getName())
                        .room(slot.getRoom().toString()) // Using toString() method
                        .isActive(isTimeSlotActive(slot))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignedSectionDTO> getAssignedSections(User faculty) {
        log.info("Getting assigned sections for faculty: {}", faculty.getUsername());
        
        List<TimeSlot> facultyTimeSlots = timeSlotRepository.findByInchargeFaculty(faculty);
        
        return facultyTimeSlots.stream()
                .filter(slot -> !slot.isBreak())
                .map(TimeSlot::getSection)
                .distinct()
                .map(section -> AssignedSectionDTO.builder()
                        .id(section.getId().toString())
                        .name(section.getName())
                        .trainerName(section.getTrainer().getName())
                        .totalStudents(section.getStrength())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TimeSlot> getCurrentActiveTimeSlot(User faculty) {
        List<TimeSlot> facultySlots = timeSlotRepository.findByInchargeFaculty(faculty);
        
        return facultySlots.stream()
                .filter(this::isTimeSlotActive)
                .findFirst();
    }

    @Override
    public boolean isTimeSlotActive(TimeSlot timeSlot) {
        if (timeSlot.isBreak()) {
            return false;
        }
        
        try {
            LocalTime now = LocalTime.now();
            // Assuming time format is "HH:mm" (e.g., "09:00", "14:30")
            LocalTime startTime = LocalTime.parse(timeSlot.getStartTime());
            LocalTime endTime = LocalTime.parse(timeSlot.getEndTime());
            
            // Check if current time is within the slot time range
            return (now.equals(startTime) || now.isAfter(startTime)) && 
                   (now.equals(endTime) || now.isBefore(endTime));
        } catch (Exception e) {
            log.error("Error parsing time for slot {}: {}", timeSlot.getId(), e.getMessage());
            return false;
        }
    }
}
