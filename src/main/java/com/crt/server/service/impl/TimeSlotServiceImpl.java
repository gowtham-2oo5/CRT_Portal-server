package com.crt.server.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.Room;
import com.crt.server.model.Section;
import com.crt.server.model.SectionSchedule;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import com.crt.server.repository.RoomRepository;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.SectionScheduleRepository;
import com.crt.server.repository.TimeSlotRepository;
import com.crt.server.repository.UserRepository;
import com.crt.server.service.TimeSlotService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TimeSlotServiceImpl implements TimeSlotService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SectionScheduleRepository sectionScheduleRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"facultyTimeSlots", "sectionDetails"}, allEntries = true)
    public TimeSlotDTO createTimeSlot(TimeSlotDTO timeSlotDTO) {
        log.debug("Creating time slot with DTO: {}", timeSlotDTO);

        // Get section
        Section section = sectionRepository.findById(timeSlotDTO.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        // Get faculty
        User faculty = userRepository.findById(timeSlotDTO.getInchargeFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));

        // Get room
        Room room = roomRepository.findById(timeSlotDTO.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        // Get schedule
        SectionSchedule schedule = sectionScheduleRepository.findBySectionId(section.getId())
                .orElseThrow(() -> new EntityNotFoundException("Section schedule not found"));

        // Create time slot
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        timeSlot.setBreak(timeSlotDTO.getIsBreak());
        timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());
        timeSlot.setInchargeFaculty(faculty);
        timeSlot.setSection(section);
        timeSlot.setRoom(room);
        timeSlot.setSchedule(schedule);

        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        return convertToDTO(savedTimeSlot);
    }

    @Override
    public TimeSlotDTO getTimeSlot(Integer id) {
        return mapToDTO(timeSlotRepository.getReferenceById(id));
    }

    private TimeSlotDTO mapToDTO(TimeSlot timeSlot) {
        return TimeSlotDTO.builder()
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .inchargeFacultyName(timeSlot.getInchargeFaculty().getName())
                .inchargeFacultyId(timeSlot.getInchargeFaculty().getId())
                .sectionName(timeSlot.getSection().getName())
                .sectionId(timeSlot.getSection().getId())
                .roomId(timeSlot.getRoom().getId())
                .roomName(timeSlot.getRoom().toString())
                .isBreak(timeSlot.isBreak())
                .breakDescription(timeSlot.getBreakDescription())
                .id(timeSlot.getId())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"facultyTimeSlots", "sectionDetails"}, allEntries = true)
    public TimeSlotDTO updateTimeSlot(Integer id, TimeSlotDTO timeSlotDTO) {
        log.debug("Updating time slot with ID: {} and DTO: {}", id, timeSlotDTO);

        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));

        // Get section
        Section section = sectionRepository.findById(timeSlotDTO.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        // Get faculty
        User faculty = userRepository.findById(timeSlotDTO.getInchargeFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));

        // Get room
        Room room = roomRepository.findById(timeSlotDTO.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        // Update time slot
        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        timeSlot.setBreak(timeSlotDTO.getIsBreak());
        timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());
        timeSlot.setInchargeFaculty(faculty);
        timeSlot.setSection(section);
        timeSlot.setRoom(room);

        TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);
        return convertToDTO(updatedTimeSlot);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "facultyTimeSlots", key = "#faculty.id")
    @Override
    public List<TimeSlot> findByInchargeFaculty(User faculty) {
        log.debug("Cache miss: Finding time slots for faculty: {}", faculty.getId());
        return timeSlotRepository.findByInchargeFaculty(faculty);
    }

    @Transactional(readOnly = true)
    @Override
    public TimeSlotDTO getTimeSlotById(Integer id) {
        log.debug("Getting time slot by ID: {}", id);
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));
        return convertToDTO(timeSlot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotDTO> getTimeSlotsBySection(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
        return timeSlotRepository.findBySection(section).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsByFaculty(UUID facultyId) {
        return List.of();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"facultyTimeSlots", "sectionDetails"}, allEntries = true)
    public void deleteTimeSlot(Integer id) {
        log.debug("Deleting time slot with ID: {}", id);
        if (!timeSlotRepository.existsById(id)) {
            throw new EntityNotFoundException("Time slot not found");
        }
        timeSlotRepository.deleteById(id);
    }

    @Override
    public List<TimeSlotDTO> getActiveTimeSlotsBySection(UUID sectionId) {
        return List.of();
    }

    @Override
    public List<TimeSlotDTO> getActiveTimeSlotsByFaculty(UUID facultyId) {
        return List.of();
    }

    @Override
    public boolean isTimeSlotAvailable(UUID roomId, String startTime, String endTime) {
        return false;
    }

    private TimeSlotDTO convertToDTO(TimeSlot timeSlot) {
        return TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .isBreak(timeSlot.isBreak())
                .breakDescription(timeSlot.getBreakDescription())
                .inchargeFacultyId(timeSlot.getInchargeFaculty().getId())
                .inchargeFacultyName(timeSlot.getInchargeFaculty().getName())
                .sectionId(timeSlot.getSection().getId())
                .sectionName(timeSlot.getSection().getName())
                .roomId(timeSlot.getRoom().getId())
                .roomName(timeSlot.getRoom().toString())
                .build();
    }
}
