package com.crt.server.service.impl;

import com.crt.server.dto.SectionScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.Section;
import com.crt.server.model.SectionSchedule;
import com.crt.server.model.TimeSlot;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.SectionScheduleRepository;
import com.crt.server.repository.TimeSlotRepository;
import com.crt.server.service.SectionScheduleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
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

        @Override
        @Transactional
        public SectionScheduleDTO createSchedule(SectionScheduleDTO scheduleDTO) {
                Section section = sectionRepository.findById(scheduleDTO.getSectionId())
                                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

                SectionSchedule schedule = new SectionSchedule();
                schedule.setSection(section);

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
        public SectionScheduleDTO getScheduleBySection(UUID sectionId) {
                Section section = sectionRepository.findById(sectionId)
                                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
                System.out.println("Section found: " + section.getName());
                SectionSchedule schedule = sectionScheduleRepository.findBySectionId(sectionId)
                                .orElseThrow(() -> new EntityNotFoundException("Schedule not found for section"));
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
                SectionSchedule existingSchedule = sectionScheduleRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

                Section section = sectionRepository.findById(scheduleDTO.getSectionId())
                                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

                existingSchedule.setSection(section);
                SectionSchedule updatedSchedule = sectionScheduleRepository.save(existingSchedule);
                return mapToDTO(updatedSchedule);
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

                Section section = sectionRepository.findById(timeSlotDTO.getSectionId())
                                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

                TimeSlot timeSlot = new TimeSlot();
                timeSlot.setStartTime(timeSlotDTO.getStartTime());
                timeSlot.setEndTime(timeSlotDTO.getEndTime());
                timeSlot.setBreak(timeSlotDTO.isBreak());
                timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());
                timeSlot.setSection(section);
                timeSlot.setSchedule(schedule);

                schedule.getTimeSlots().add(timeSlot);
                SectionSchedule updatedSchedule = sectionScheduleRepository.save(schedule);
                return mapToDTO(updatedSchedule);
        }

        @Override
        @Transactional
        public SectionScheduleDTO removeTimeSlot(UUID scheduleId, Integer timeSlotId) {
                SectionSchedule schedule = sectionScheduleRepository.findById(scheduleId)
                                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

                TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));

                schedule.getTimeSlots().remove(timeSlot);
                SectionSchedule updatedSchedule = sectionScheduleRepository.save(schedule);
                return mapToDTO(updatedSchedule);
        }

        @Override
        @Transactional
        public SectionScheduleDTO updateTimeSlot(UUID scheduleId, Integer timeSlotId, TimeSlotDTO timeSlotDTO) {
                SectionSchedule schedule = sectionScheduleRepository.findById(scheduleId)
                                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

                TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                                .orElseThrow(() -> new EntityNotFoundException("Time slot not found"));

                timeSlot.setStartTime(timeSlotDTO.getStartTime());
                timeSlot.setEndTime(timeSlotDTO.getEndTime());
                timeSlot.setBreak(timeSlotDTO.isBreak());
                timeSlot.setBreakDescription(timeSlotDTO.getBreakDescription());

                SectionSchedule updatedSchedule = sectionScheduleRepository.save(schedule);
                return mapToDTO(updatedSchedule);
        }

        private SectionScheduleDTO mapToDTO(SectionSchedule schedule) {
                return SectionScheduleDTO.builder()
                                .id(schedule.getId())
                                .sectionId(schedule.getSection().getId())
                                .timeSlots(timeSlotRepository.findBySection(schedule.getSection()).stream()
                                                .map(this::mapTimeSlotToDTO)
                                                .collect(Collectors.toList()))
                                .build();
        }

        private TimeSlotDTO mapTimeSlotToDTO(TimeSlot timeSlot) {
                return TimeSlotDTO.builder()
                                .id(timeSlot.getId())
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .isBreak(timeSlot.isBreak())
                                .breakDescription(timeSlot.getBreakDescription())
                                .sectionId(timeSlot.getSection().getId())
                                .build();
        }
}