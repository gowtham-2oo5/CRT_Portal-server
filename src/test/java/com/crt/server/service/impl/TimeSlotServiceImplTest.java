package com.crt.server.service.impl;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.Room;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import com.crt.server.repository.RoomRepository;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.TimeSlotRepository;
import com.crt.server.repository.UserRepository;
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
class TimeSlotServiceImplTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private TimeSlotServiceImpl timeSlotService;

    private TimeSlot testTimeSlot;
    private TimeSlotDTO testTimeSlotDTO;
    private Section testSection;
    private User testFaculty;
    private Room testRoom;
    private UUID testSectionId;
    private UUID testFacultyId;
    private UUID testRoomId;
    private Integer testTimeSlotId;

    @BeforeEach
    void setUp() {
        testSectionId = UUID.randomUUID();
        testFacultyId = UUID.randomUUID();
        testRoomId = UUID.randomUUID();
        testTimeSlotId = 1;

        testSection = Section.builder()
                .id(testSectionId)
                .name("Test Section")
                .build();

        testFaculty = User.builder()
                .id(testFacultyId)
                .name("Test Faculty")
                .email("faculty@test.com")
                .build();

        testRoom = Room.builder()
                .id(testRoomId)
                .block("A")
                .floor("1")
                .roomNumber("101")
                .capacity(30)
                .build();

        testTimeSlot = TimeSlot.builder()
                .id(testTimeSlotId)
                .startTime("09:00")
                .endTime("10:00")
                .isBreak(false)
                .breakDescription(null)
                .section(testSection)
                .inchargeFaculty(testFaculty)
                .room(testRoom)
                .build();

        testTimeSlotDTO = TimeSlotDTO.builder()
                .id(testTimeSlotId)
                .startTime("09:00")
                .endTime("10:00")
                .isBreak(false)
                .breakDescription(null)
                .sectionId(testSectionId)
                .inchargeFacultyId(testFacultyId)
                .roomId(testRoomId)
                .build();
    }

    @Test
    void createTimeSlot_WithValidData_ShouldCreateTimeSlot() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(userRepository.findById(testFacultyId)).thenReturn(Optional.of(testFaculty));
        when(roomRepository.findById(testRoomId)).thenReturn(Optional.of(testRoom));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(testTimeSlot);

        // Act
        TimeSlotDTO result = timeSlotService.createTimeSlot(testTimeSlotDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testTimeSlotDTO.getStartTime(), result.getStartTime());
        assertEquals(testTimeSlotDTO.getEndTime(), result.getEndTime());
        assertEquals(testTimeSlotDTO.getSectionId(), result.getSectionId());
        assertEquals(testTimeSlotDTO.getInchargeFacultyId(), result.getInchargeFacultyId());
        assertEquals(testTimeSlotDTO.getRoomId(), result.getRoomId());
    }

    @Test
    void createTimeSlot_WithInvalidSection_ShouldThrowException() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> timeSlotService.createTimeSlot(testTimeSlotDTO));
    }

    @Test
    void getTimeSlot_WithValidId_ShouldReturnTimeSlot() {
        // Arrange
        when(timeSlotRepository.findById(testTimeSlotId)).thenReturn(Optional.of(testTimeSlot));

        // Act
        TimeSlotDTO result = timeSlotService.getTimeSlot(testTimeSlotId);

        // Assert
        assertNotNull(result);
        assertEquals(testTimeSlotDTO.getId(), result.getId());
        assertEquals(testTimeSlotDTO.getStartTime(), result.getStartTime());
        assertEquals(testTimeSlotDTO.getEndTime(), result.getEndTime());
    }

    @Test
    void getTimeSlot_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(timeSlotRepository.findById(testTimeSlotId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> timeSlotService.getTimeSlot(testTimeSlotId));
    }

    @Test
    void getTimeSlotsBySection_WithValidSection_ShouldReturnTimeSlots() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(timeSlotRepository.findBySection(testSection)).thenReturn(Collections.singletonList(testTimeSlot));

        // Act
        List<TimeSlotDTO> result = timeSlotService.getTimeSlotsBySection(testSectionId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTimeSlotDTO.getSectionId(), result.get(0).getSectionId());
    }

    @Test
    void getTimeSlotsByFaculty_WithValidFaculty_ShouldReturnTimeSlots() {
        // Arrange
        when(userRepository.findById(testFacultyId)).thenReturn(Optional.of(testFaculty));
        when(timeSlotRepository.findByInchargeFaculty(testFaculty)).thenReturn(Collections.singletonList(testTimeSlot));

        // Act
        List<TimeSlotDTO> result = timeSlotService.getTimeSlotsByFaculty(testFacultyId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTimeSlotDTO.getInchargeFacultyId(), result.get(0).getInchargeFacultyId());
    }

    @Test
    void updateTimeSlot_WithValidData_ShouldUpdateTimeSlot() {
        // Arrange
        when(timeSlotRepository.findById(testTimeSlotId)).thenReturn(Optional.of(testTimeSlot));
        when(userRepository.findById(testFacultyId)).thenReturn(Optional.of(testFaculty));
        when(roomRepository.findById(testRoomId)).thenReturn(Optional.of(testRoom));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(testTimeSlot);

        // Act
        TimeSlotDTO result = timeSlotService.updateTimeSlot(testTimeSlotId, testTimeSlotDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testTimeSlotDTO.getStartTime(), result.getStartTime());
        assertEquals(testTimeSlotDTO.getEndTime(), result.getEndTime());
        assertEquals(testTimeSlotDTO.getSectionId(), result.getSectionId());
        assertEquals(testTimeSlotDTO.getInchargeFacultyId(), result.getInchargeFacultyId());
        assertEquals(testTimeSlotDTO.getRoomId(), result.getRoomId());
    }

    @Test
    void deleteTimeSlot_WithValidId_ShouldDeleteTimeSlot() {
        // Arrange
        when(timeSlotRepository.existsById(testTimeSlotId)).thenReturn(true);

        // Act
        timeSlotService.deleteTimeSlot(testTimeSlotId);

        // Assert
        verify(timeSlotRepository).deleteById(testTimeSlotId);
    }

    @Test
    void getActiveTimeSlotsBySection_WithValidSection_ShouldReturnActiveTimeSlots() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(timeSlotRepository.findActiveTimeSlotsBySection(testSection))
                .thenReturn(Collections.singletonList(testTimeSlot));

        // Act
        List<TimeSlotDTO> result = timeSlotService.getActiveTimeSlotsBySection(testSectionId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTimeSlotDTO.getSectionId(), result.get(0).getSectionId());
        assertFalse(result.get(0).isBreak());
    }

    @Test
    void getActiveTimeSlotsByFaculty_WithValidFaculty_ShouldReturnActiveTimeSlots() {
        // Arrange
        when(userRepository.findById(testFacultyId)).thenReturn(Optional.of(testFaculty));
        when(timeSlotRepository.findActiveTimeSlotsByFaculty(testFaculty))
                .thenReturn(Collections.singletonList(testTimeSlot));

        // Act
        List<TimeSlotDTO> result = timeSlotService.getActiveTimeSlotsByFaculty(testFacultyId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTimeSlotDTO.getInchargeFacultyId(), result.get(0).getInchargeFacultyId());
        assertFalse(result.get(0).isBreak());
    }

    @Test
    void isTimeSlotAvailable_WithAvailableTime_ShouldReturnTrue() {
        // Arrange
        when(roomRepository.findById(testRoomId)).thenReturn(Optional.of(testRoom));
        when(timeSlotRepository.existsByRoomAndTime(testRoom, "09:00", "10:00")).thenReturn(false);

        // Act
        boolean result = timeSlotService.isTimeSlotAvailable(testRoomId, "09:00", "10:00");

        // Assert
        assertTrue(result);
    }

    @Test
    void isTimeSlotAvailable_WithUnavailableTime_ShouldReturnFalse() {
        // Arrange
        when(roomRepository.findById(testRoomId)).thenReturn(Optional.of(testRoom));
        when(timeSlotRepository.existsByRoomAndTime(testRoom, "09:00", "10:00")).thenReturn(true);

        // Act
        boolean result = timeSlotService.isTimeSlotAvailable(testRoomId, "09:00", "10:00");

        // Assert
        assertFalse(result);
    }
}