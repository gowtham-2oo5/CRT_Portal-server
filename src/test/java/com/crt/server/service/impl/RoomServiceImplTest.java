package com.crt.server.service.impl;

import com.crt.server.dto.RoomDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Room;
import com.crt.server.model.RoomType;
import com.crt.server.repository.RoomRepository;
import com.crt.server.service.CsvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private CsvService csvService;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room testRoom;
    private RoomDTO testRoomDTO;
    private UUID testRoomId;

    @BeforeEach
    void setUp() {
        testRoomId = UUID.randomUUID();
        testRoom = Room.builder()
                .id(testRoomId)
                .block("R")
                .floor("6")
                .roomNumber("07")
                .subRoom(null)
                .roomType(RoomType.LAB)
                .capacity(30)
                .build();

        testRoomDTO = RoomDTO.builder()
                .id(testRoomId)
                .block("R")
                .floor("6")
                .roomNumber("07")
                .subRoom(null)
                .roomType(RoomType.LAB)
                .capacity(30)
                .build();
    }

    @Test
    void createRoom_WithValidData_ShouldCreateRoom() {
        // Arrange
        when(roomRepository.existsByBlockAndFloorAndRoomNumber(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // Act
        RoomDTO result = roomService.createRoom(testRoomDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testRoomDTO.getBlock(), result.getBlock());
        assertEquals(testRoomDTO.getFloor(), result.getFloor());
        assertEquals(testRoomDTO.getRoomNumber(), result.getRoomNumber());
        assertEquals(testRoomDTO.getRoomType(), result.getRoomType());
        assertEquals(testRoomDTO.getCapacity(), result.getCapacity());
    }

    @Test
    void createRoom_WithExistingRoom_ShouldThrowException() {
        // Arrange
        when(roomRepository.existsByBlockAndFloorAndRoomNumber(anyString(), anyString(), anyString()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> roomService.createRoom(testRoomDTO));
    }

    @Test
    void getRoomById_WithValidId_ShouldReturnRoom() {
        // Arrange
        when(roomRepository.findById(any(UUID.class))).thenReturn(Optional.of(testRoom));

        // Act
        RoomDTO result = roomService.getRoomById(testRoomId);

        // Assert
        assertNotNull(result);
        assertEquals(testRoomDTO.getId(), result.getId());
        assertEquals(testRoomDTO.getBlock(), result.getBlock());
    }

    @Test
    void getRoomById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(roomRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoomById(testRoomId));
    }

    @Test
    void getAllRooms_ShouldReturnAllRooms() {
        // Arrange
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        // Act
        List<RoomDTO> result = roomService.getAllRooms();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRoomDTO.getBlock(), result.get(0).getBlock());
    }

    @Test
    void updateRoom_WithValidData_ShouldUpdateRoom() {
        // Arrange
        when(roomRepository.findById(any(UUID.class))).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // Act
        RoomDTO result = roomService.updateRoom(testRoomId, testRoomDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testRoomDTO.getBlock(), result.getBlock());
        assertEquals(testRoomDTO.getFloor(), result.getFloor());
        assertEquals(testRoomDTO.getRoomNumber(), result.getRoomNumber());
    }

    @Test
    void deleteRoom_WithValidId_ShouldDeleteRoom() {
        // Arrange
        when(roomRepository.existsById(any(UUID.class))).thenReturn(true);

        // Act
        roomService.deleteRoom(testRoomId);

        // Assert
        verify(roomRepository).deleteById(testRoomId);
    }

    @Test
    void parseRoomString_WithValidFormat_ShouldParseCorrectly() {
        // Arrange
        String roomString = "R607 - Lab";

        // Act
        RoomDTO result = roomService.parseRoomString(roomString);

        // Assert
        assertNotNull(result);
        assertEquals("R", result.getBlock());
        assertEquals("6", result.getFloor());
        assertEquals("07", result.getRoomNumber());
        assertEquals(RoomType.LAB, result.getRoomType());
    }

    @Test
    void parseRoomString_WithInvalidFormat_ShouldThrowException() {
        // Arrange
        String roomString = "Invalid Room Format";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> roomService.parseRoomString(roomString));
    }

    @Test
    void bulkCreateRoomsFromSimpleFormat_WithValidCsv_ShouldCreateRooms() throws Exception {
        // Arrange
        String csvContent = "roomString,capacity,roomType\n" +
                "R607,30,LAB";
        MultipartFile file = new MockMultipartFile("rooms.csv", csvContent.getBytes());
        List<Room> rooms = Arrays.asList(testRoom);
        when(csvService.parseCsv(any(), any(), any(CsvService.RecordMapper.class))).thenReturn(rooms);
        when(roomRepository.saveAll(any())).thenReturn(rooms);

        // Act
        List<RoomDTO> result = roomService.bulkCreateRoomsFromSimpleFormat(file);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRoomDTO.getBlock(), result.get(0).getBlock());
    }

    @Test
    void bulkCreateRoomsFromSimpleFormat_WithInvalidCsv_ShouldThrowException() throws Exception {
        // Arrange
        String csvContent = "roomString,capacity,roomType\n" +
                "InvalidRoom,invalid,InvalidType";
        MultipartFile file = new MockMultipartFile("rooms.csv", csvContent.getBytes());
        when(csvService.parseCsv(any(), any(), any(CsvService.RecordMapper.class)))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        // Act & Assert
        assertThrows(Exception.class, () -> roomService.bulkCreateRoomsFromSimpleFormat(file));
    }
}