package com.crt.server.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.crt.server.dto.RoomDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Room;
import com.crt.server.model.RoomType;
import com.crt.server.repository.RoomRepository;
import com.crt.server.service.CsvService;
import com.crt.server.service.RoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final CsvService csvService;
    private static final Pattern ROOM_PATTERN = Pattern.compile("^([A-Z])(\\d)(\\d{2})\\s*-\\s*([A-Za-z]+)$");

    @Override
    public RoomDTO createRoom(RoomDTO roomDTO) {
        if (existsByDetails(roomDTO.getBlock(), roomDTO.getFloor(), roomDTO.getRoomNumber(), roomDTO.getSubRoom())) {
            throw new RuntimeException("Room already exists");
        }

        Room room = Room.builder()
                .block(roomDTO.getBlock())
                .floor(roomDTO.getFloor())
                .roomNumber(roomDTO.getRoomNumber())
                .subRoom(roomDTO.getSubRoom())
                .roomType(roomDTO.getRoomType())
                .capacity(roomDTO.getCapacity())
                .build();

        Room savedRoom = roomRepository.save(room);
        return convertToDTO(savedRoom);
    }

    @Override
    public RoomDTO getRoomById(UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return convertToDTO(room);
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomDTO updateRoom(UUID id, RoomDTO roomDTO) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.setBlock(roomDTO.getBlock());
        room.setFloor(roomDTO.getFloor());
        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setSubRoom(roomDTO.getSubRoom());
        room.setRoomType(roomDTO.getRoomType());
        room.setCapacity(roomDTO.getCapacity());

        Room updatedRoom = roomRepository.save(room);
        return convertToDTO(updatedRoom);
    }

    @Override
    public void deleteRoom(UUID id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found");
        }
        roomRepository.deleteById(id);
    }

    public boolean existsByDetails(String block, String floor, String roomNumber, String subRoom) {
        return roomRepository.existsByBlockAndFloorAndRoomNumber(block, floor, roomNumber, subRoom);
    }

    @Override
    public RoomDTO parseRoomString(String roomString) {
        Matcher matcher = ROOM_PATTERN.matcher(roomString.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid room string format. Expected format: 'R607 - Lab'");
        }

        String block = matcher.group(1);
        String floor = matcher.group(2);
        String roomNumber = matcher.group(3);
        String roomTypeStr = matcher.group(4);

        try {
            RoomType roomType = RoomType.valueOf(roomTypeStr.toUpperCase());
            return RoomDTO.builder()
                    .block(block)
                    .floor(floor)
                    .roomNumber(roomNumber)
                    .roomType(roomType)
                    .build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid room type: " + roomTypeStr +
                    ". Valid types are: " + String.join(", ", RoomType.values().toString()));
        }
    }

    @Override
    public RoomDTO getRoomByCode(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode);
        if (room == null) {
            throw new ResourceNotFoundException("Room not found with code: " + roomCode);
        }
        return convertToDTO(room);
    }    @Override
    public List<RoomDTO> bulkCreateRoomsFromSimpleFormat(MultipartFile file) throws Exception {
        String[] headers = { "roomString", "capacity", "roomType" };

        try {
            List<Room> rooms = csvService.parseCsv(file, headers, record -> {
                // Validate required fields
                validateSimpleFormatFields(record);

                String roomString = record.get("roomString").trim();
                String capacity = record.get("capacity").trim();
                String roomType = record.get("roomType").trim();

                // Parse room string
                RoomDTO roomDTO = parseSimpleRoomString(roomString);
                roomDTO.setCapacity(Integer.parseInt(capacity));
                roomDTO.setRoomType(RoomType.valueOf(roomType.toUpperCase()));

                // Create and return Room entity
                return Room.builder()
                        .block(roomDTO.getBlock())
                        .floor(roomDTO.getFloor())
                        .roomNumber(roomDTO.getRoomNumber())
                        .subRoom(roomDTO.getSubRoom())
                        .roomType(roomDTO.getRoomType())
                        .capacity(roomDTO.getCapacity())
                        .build();
            });

            // Save all rooms in a single transaction
            List<Room> savedRooms = roomRepository.saveAll(rooms);
            return savedRooms.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in bulk room creation from simple format: {}", e.getMessage());
            throw new Exception("Error processing room data: " + e.getMessage());
        }
    }

    private void validateSimpleFormatFields(org.apache.commons.csv.CSVRecord record) {
        String[] requiredFields = { "roomString", "capacity", "roomType" };
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new IllegalArgumentException("Required field '" + field + "' is missing or empty");
            }
        }

        // Validate room type
        try {
            RoomType.valueOf(record.get("roomType").trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid room type: " + record.get("roomType") +
                    ". Valid types are: " + String.join(", ", RoomType.values().toString()));
        }

        // Validate capacity
        try {
            int capacity = Integer.parseInt(record.get("capacity").trim());
            if (capacity <= 0) {
                throw new IllegalArgumentException("Capacity must be greater than 0");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid capacity value: " + record.get("capacity"));
        }
    }

    private RoomDTO parseSimpleRoomString(String roomString) {
        // Remove any whitespace
        roomString = roomString.trim();

        // Pattern to match: R504 or R504A
        Pattern pattern = Pattern.compile("^([A-Z])(\\d)(\\d{2})([A-Z])?$");
        Matcher matcher = pattern.matcher(roomString);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid room string format. Expected format: 'R504' or 'R504A'");
        }

        String block = matcher.group(1);
        String floor = matcher.group(2);
        String roomNumber = matcher.group(3);
        String subRoom = matcher.group(4); // This will be null if no subRoom is present

        return RoomDTO.builder()
                .block(block)
                .floor(floor)
                .roomNumber(roomNumber)
                .subRoom(subRoom)
                .build();
    }

    private RoomDTO convertToDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .block(room.getBlock())
                .floor(room.getFloor())
                .roomNumber(room.getRoomNumber())
                .subRoom(room.getSubRoom())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .roomString(room.toString())
                .build();
    }
}
