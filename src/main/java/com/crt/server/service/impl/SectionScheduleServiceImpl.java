package com.crt.server.service.impl;

import com.crt.server.dto.SectionScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.SectionScheduleService;
import com.crt.server.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SectionScheduleServiceImpl implements SectionScheduleService {

    @Autowired
    private SectionScheduleRepository sectionScheduleRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public SectionScheduleDTO createSchedule(SectionScheduleDTO scheduleDTO) {
        Section section = sectionRepository.findById(scheduleDTO.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        Room room = roomRepository.findById(scheduleDTO.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        SectionSchedule schedule = new SectionSchedule();
        schedule.setSection(section);
        schedule.setRoom(room);

        SectionSchedule savedSchedule = sectionScheduleRepository.save(schedule);
        return mapToDTO(savedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionScheduleDTO getSchedule(UUID id) {
        SectionSchedule schedule = sectionScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));
        return mapToDTO(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionScheduleDTO> getAllSchedules() {
        return sectionScheduleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectionScheduleDTO updateSchedule(UUID id, SectionScheduleDTO scheduleDTO) {
        SectionSchedule schedule = sectionScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        Section section = sectionRepository.findById(scheduleDTO.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        Room room = roomRepository.findById(scheduleDTO.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        schedule.setSection(section);
        schedule.setRoom(room);

        SectionSchedule savedSchedule = sectionScheduleRepository.save(schedule);
        return mapToDTO(savedSchedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(UUID id) {
        if (!sectionScheduleRepository.existsById(id)) {
            throw new EntityNotFoundException("Schedule not found");
        }
        sectionScheduleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public SectionScheduleDTO addTimeSlot(UUID scheduleId, TimeSlotDTO timeSlotDTO) {

        SectionSchedule schedule = sectionScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        // Get required entities
        User inchargeFaculty = userRepository.findById(timeSlotDTO.getInchargeFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));

        // Determine section and room (use from DTO if provided, otherwise inherit from schedule)
        Section section;
        Room room;

        if (timeSlotDTO.getSectionId() != null) {
            section = sectionRepository.findById(timeSlotDTO.getSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Section not found"));
        } else {
            section = schedule.getSection(); // Inherit from schedule
        }

        if (timeSlotDTO.getRoomId() != null) {
            room = roomRepository.findById(timeSlotDTO.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        } else {
            room = schedule.getRoom(); // Inherit from schedule
        }

        // Create TimeSlot with all required fields
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        timeSlot.setBreak(timeSlotDTO.getIsBreak());
        timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());
        timeSlot.setInchargeFaculty(inchargeFaculty);
        timeSlot.setSection(section);
        timeSlot.setRoom(room);
        timeSlot.setSchedule(schedule);

        schedule.addTimeSlot(timeSlot);
        SectionSchedule savedSchedule = sectionScheduleRepository.save(schedule);
        return mapToDTO(savedSchedule);
    }

    @Override
    @Transactional
    public SectionScheduleDTO updateTimeSlot(UUID scheduleId, Integer timeSlotId, TimeSlotDTO timeSlotDTO) {
        SectionSchedule schedule = sectionScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found"));

        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        timeSlot.setBreak(timeSlotDTO.getIsBreak());
        timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());
        timeSlot.setInchargeFaculty(userService.getFacById(timeSlotDTO.getInchargeFacultyId()));

        timeSlotRepository.save(timeSlot);


        return mapToDTO(schedule);
    }

    @Override
    @Transactional
    public SectionScheduleDTO removeTimeSlot(UUID scheduleId, Integer timeSlotId) {
        SectionSchedule schedule = sectionScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found"));

        schedule.removeTimeSlot(timeSlot);
        timeSlotRepository.delete(timeSlot);

        SectionSchedule savedSchedule = sectionScheduleRepository.save(schedule);
        return mapToDTO(savedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionScheduleDTO getScheduleBySection(UUID sectionId) {
        return sectionScheduleRepository.findBySectionId(sectionId)
                .map(this::mapToDTO)
                .orElse(null); // Return null if no schedule found for section
    }

    private SectionScheduleDTO mapToDTO(SectionSchedule schedule) {
        List<TimeSlotDTO> timeSlotDTOs = schedule.getTimeSlots().stream()
                .map(this::mapTimeSlotToDTO)
                .collect(Collectors.toList());

        return SectionScheduleDTO.builder()
                .id(schedule.getId())
                .sectionId(schedule.getSection().getId())
                .roomId(schedule.getRoom().getId())
                .timeSlots(timeSlotDTOs)
                .build();
    }

    private TimeSlotDTO mapTimeSlotToDTO(TimeSlot timeSlot) {
        return TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .isBreak(timeSlot.isBreak())
                .breakDescription(timeSlot.getBreakDescription())
                .inchargeFacultyId(timeSlot.getInchargeFaculty() != null ? timeSlot.getInchargeFaculty().getId() : null)
                .inchargeFacultyName(timeSlot.getInchargeFaculty().getName())
                .sectionId(timeSlot.getSection() != null ? timeSlot.getSection().getId() : null)
                .roomId(timeSlot.getRoom() != null ? timeSlot.getRoom().getId() : null)
                .build();
    }
}
