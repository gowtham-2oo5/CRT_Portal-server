package com.crt.server.service.impl;

import com.crt.server.dto.SectionScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.Section;
import com.crt.server.model.SectionSchedule;
import com.crt.server.model.TimeSlot;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.SectionScheduleRepository;
import com.crt.server.repository.TimeSlotRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionScheduleServiceImplTest {

    @Mock
    private SectionScheduleRepository sectionScheduleRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private SectionScheduleServiceImpl sectionScheduleService;

    private Section testSection;
    private SectionSchedule testSchedule;
    private SectionScheduleDTO testScheduleDTO;
    private TimeSlot testTimeSlot;
    private TimeSlotDTO testTimeSlotDTO;
    private UUID testSectionId;
    private UUID testScheduleId;
    private Integer testTimeSlotId;

    @BeforeEach
    void setUp() {
        testSectionId = UUID.randomUUID();
        testScheduleId = UUID.randomUUID();
        testTimeSlotId = 1;

        testSection = Section.builder()
                .id(testSectionId)
                .name("Test Section")
                .build();

        testSchedule = SectionSchedule.builder()
                .id(testScheduleId)
                .section(testSection)
                .timeSlots(new HashSet<>())
                .build();

        testScheduleDTO = SectionScheduleDTO.builder()
                .id(testScheduleId)
                .sectionId(testSectionId)
                .timeSlots(new ArrayList<>())
                .build();

        testTimeSlot = TimeSlot.builder()
                .id(testTimeSlotId)
                .startTime("09:00")
                .endTime("10:00")
                .isBreak(false)
                .breakDescription(null)
                .section(testSection)
                .schedule(testSchedule)
                .build();

        testTimeSlotDTO = TimeSlotDTO.builder()
                .id(testTimeSlotId)
                .startTime("09:00")
                .endTime("10:00")
                .isBreak(false)
                .breakDescription(null)
                .sectionId(testSectionId)
                .build();
    }

    @Test
    void createSchedule_WithValidData_ShouldCreateSchedule() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(sectionScheduleRepository.save(any(SectionSchedule.class))).thenReturn(testSchedule);

        // Act
        SectionScheduleDTO result = sectionScheduleService.createSchedule(testScheduleDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testScheduleDTO.getSectionId(), result.getSectionId());
        assertEquals(testScheduleDTO.getTimeSlots().size(), result.getTimeSlots().size());
    }

    @Test
    void createSchedule_WithInvalidSection_ShouldThrowException() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> sectionScheduleService.createSchedule(testScheduleDTO));
    }

    @Test
    void getSchedule_WithValidId_ShouldReturnSchedule() {
        // Arrange
        when(sectionScheduleRepository.findById(testScheduleId)).thenReturn(Optional.of(testSchedule));

        // Act
        SectionScheduleDTO result = sectionScheduleService.getSchedule(testScheduleId);

        // Assert
        assertNotNull(result);
        assertEquals(testScheduleDTO.getId(), result.getId());
        assertEquals(testScheduleDTO.getSectionId(), result.getSectionId());
    }

    @Test
    void getSchedule_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(sectionScheduleRepository.findById(testScheduleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> sectionScheduleService.getSchedule(testScheduleId));
    }

    @Test
    void getScheduleBySection_WithValidSection_ShouldReturnSchedule() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(sectionScheduleRepository.findBySectionId(testSectionId)).thenReturn(Optional.of(testSchedule));

        // Act
        SectionScheduleDTO result = sectionScheduleService.getScheduleBySection(testSectionId);

        // Assert
        assertNotNull(result);
        assertEquals(testScheduleDTO.getSectionId(), result.getSectionId());
    }

    @Test
    void getScheduleBySection_WithInvalidSection_ShouldThrowException() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> sectionScheduleService.getScheduleBySection(testSectionId));
    }

    @Test
    void getAllSchedules_ShouldReturnAllSchedules() {
        // Arrange
        List<SectionSchedule> schedules = Collections.singletonList(testSchedule);
        when(sectionScheduleRepository.findAll()).thenReturn(schedules);

        // Act
        List<SectionScheduleDTO> result = sectionScheduleService.getAllSchedules();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testScheduleDTO.getSectionId(), result.get(0).getSectionId());
    }

    @Test
    void updateSchedule_WithValidData_ShouldUpdateSchedule() {
        // Arrange
        when(sectionScheduleRepository.findById(testScheduleId)).thenReturn(Optional.of(testSchedule));
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(sectionScheduleRepository.save(any(SectionSchedule.class))).thenReturn(testSchedule);

        // Act
        SectionScheduleDTO result = sectionScheduleService.updateSchedule(testScheduleId, testScheduleDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testScheduleDTO.getSectionId(), result.getSectionId());
    }

    @Test
    void deleteSchedule_WithValidId_ShouldDeleteSchedule() {
        // Arrange
        when(sectionScheduleRepository.existsById(testScheduleId)).thenReturn(true);

        // Act
        sectionScheduleService.deleteSchedule(testScheduleId);

        // Assert
        verify(sectionScheduleRepository).deleteById(testScheduleId);
    }

    @Test
    void addTimeSlot_WithValidData_ShouldAddTimeSlot() {
        // Arrange
        when(sectionScheduleRepository.findById(testScheduleId)).thenReturn(Optional.of(testSchedule));
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(sectionScheduleRepository.save(any(SectionSchedule.class))).thenReturn(testSchedule);

        // Act
        SectionScheduleDTO result = sectionScheduleService.addTimeSlot(testScheduleId, testTimeSlotDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testScheduleDTO.getSectionId(), result.getSectionId());
    }

    @Test
    void removeTimeSlot_WithValidData_ShouldRemoveTimeSlot() {
        // Arrange
        when(sectionScheduleRepository.findById(testScheduleId)).thenReturn(Optional.of(testSchedule));
        when(timeSlotRepository.findById(testTimeSlotId)).thenReturn(Optional.of(testTimeSlot));
        when(sectionScheduleRepository.save(any(SectionSchedule.class))).thenReturn(testSchedule);

        // Act
        SectionScheduleDTO result = sectionScheduleService.removeTimeSlot(testScheduleId, testTimeSlotId);

        // Assert
        assertNotNull(result);
        assertEquals(testScheduleDTO.getSectionId(), result.getSectionId());
    }

    @Test
    void updateTimeSlot_WithValidData_ShouldUpdateTimeSlot() {
        // Arrange
        when(sectionScheduleRepository.findById(testScheduleId)).thenReturn(Optional.of(testSchedule));
        when(timeSlotRepository.findById(testTimeSlotId)).thenReturn(Optional.of(testTimeSlot));
        when(sectionScheduleRepository.save(any(SectionSchedule.class))).thenReturn(testSchedule);

        // Act
        SectionScheduleDTO result = sectionScheduleService.updateTimeSlot(testScheduleId, testTimeSlotId,
                testTimeSlotDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testScheduleDTO.getSectionId(), result.getSectionId());
    }
}