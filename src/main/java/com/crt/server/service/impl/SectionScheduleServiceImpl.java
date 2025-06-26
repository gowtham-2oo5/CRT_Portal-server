package com.crt.server.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crt.server.dto.SectionScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.Room;
import com.crt.server.model.Section;
import com.crt.server.model.SectionSchedule;
import com.crt.server.model.TimeSlot;
import com.crt.server.repository.RoomRepository;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.SectionScheduleRepository;
import com.crt.server.repository.TimeSlotRepository;
import com.crt.server.service.SectionScheduleService;

import jakarta.persistence.EntityNotFoundException;

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

                TimeSlot timeSlot = new TimeSlot();
                timeSlot.setStartTime(timeSlotDTO.getStartTime());
                timeSlot.setEndTime(timeSlotDTO.getEndTime());
                timeSlot.setBreak(timeSlotDTO.isBreak());
                timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());
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
                timeSlot.setBreak(timeSlotDTO.isBreak());
                timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());

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
                SectionSchedule schedule = sectionScheduleRepository.findBySectionId(sectionId)
                                .orElseThrow(() -> new EntityNotFoundException("Schedule not found for section"));
                return mapToDTO(schedule);
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
                                .sectionId(timeSlot.getSection() != null ? timeSlot.getSection().getId() : null)
                                .roomId(timeSlot.getRoom() != null ? timeSlot.getRoom().getId() : null)
                                .build();
        }
}
