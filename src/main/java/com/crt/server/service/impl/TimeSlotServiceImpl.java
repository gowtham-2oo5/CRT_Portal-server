package com.crt.server.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crt.server.dto.RoomDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.dto.UserDTO;
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

@Service
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
    public TimeSlotDTO createTimeSlot(TimeSlotDTO timeSlotDTO) {
        System.out.println("Creating time slot with DTO: " + timeSlotDTO);

        // Validate IDs exist
        Section section = sectionRepository.findById(timeSlotDTO.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
        System.out.println("Found section: " + section.getName());

        User inchargeFaculty = userRepository.findById(timeSlotDTO.getInchargeFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));
        System.out.println("Found faculty: " + inchargeFaculty.getName());

        Room room = roomRepository.findById(timeSlotDTO.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        System.out.println("Found room: " + room.toString());

        // Get or create schedule for the section
        SectionSchedule schedule = sectionScheduleRepository.findBySectionId(timeSlotDTO.getSectionId())
                .orElseGet(() -> {
                    SectionSchedule newSchedule = new SectionSchedule();
                    newSchedule.setSection(section);
                    newSchedule.setRoom(room);
                    return sectionScheduleRepository.save(newSchedule);
                });
        System.out.println("Found/created schedule: " + schedule.getId());

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        timeSlot.setBreak(timeSlotDTO.isBreak());
        timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());
        timeSlot.setInchargeFaculty(inchargeFaculty);
        timeSlot.setSection(section);
        timeSlot.setRoom(room);
        timeSlot.setSchedule(schedule);

        System.out.println("Created time slot with room: " + timeSlot.getRoom().getId());

        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        System.out.println("Saved time slot with ID: " + savedTimeSlot.getId());

        return convertToDTO(savedTimeSlot);
    }

    @Override
    public TimeSlotDTO getTimeSlot(Integer id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));
        return convertToDTO(timeSlot);
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsBySection(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
        return timeSlotRepository.findBySection(section).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsByFaculty(UUID facultyId) {
        User faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));
        return timeSlotRepository.findByInchargeFaculty(faculty).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TimeSlotDTO updateTimeSlot(Integer id, TimeSlotDTO timeSlotDTO) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));

        User inchargeFaculty = userRepository.findById(timeSlotDTO.getInchargeFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));
        Room room = roomRepository.findById(timeSlotDTO.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        timeSlot.setBreak(timeSlotDTO.isBreak());
        timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());
        timeSlot.setInchargeFaculty(inchargeFaculty);
        timeSlot.setRoom(room);

        TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);
        return convertToDTO(updatedTimeSlot);
    }

    @Override
    @Transactional
    public void deleteTimeSlot(Integer id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new EntityNotFoundException("Time slot not found");
        }
        timeSlotRepository.deleteById(id);
    }

    @Override
    public List<TimeSlotDTO> getActiveTimeSlotsBySection(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
        return timeSlotRepository.findActiveTimeSlotsBySection(section).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getActiveTimeSlotsByFaculty(UUID facultyId) {
        User faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));
        return timeSlotRepository.findActiveTimeSlotsByFaculty(faculty).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTimeSlotAvailable(UUID roomId, String startTime, String endTime) {
        Room room = roomRepository.findById(roomId)
                .orElse(null);
        if (room == null) {
            return false;
        }

        return !timeSlotRepository.existsByRoomAndTime(room, startTime, endTime);
    }

    private TimeSlotDTO convertToDTO(TimeSlot timeSlot) {
        return TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .isBreak(timeSlot.isBreak())
                .breakDescription(timeSlot.getBreakDescription())
                .inchargeFacultyId(timeSlot.getInchargeFaculty().getId())
                .sectionId(timeSlot.getSection().getId())
                .roomId(timeSlot.getRoom().getId())
                .build();
    }

    private SectionDTO mapSectionToDTO(Section section) {
        return SectionDTO.builder()
                .id(section.getId())
                .name(section.getName())
                .strength(section.getStrength())
                .build();
    }

    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }

    private RoomDTO mapRoomToDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .block(room.getBlock())
                .floor(room.getFloor())
                .roomNumber(room.getRoomNumber())
                .subRoom(room.getSubRoom())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .build();
    }
}