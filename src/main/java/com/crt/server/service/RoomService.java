package com.crt.server.service;

import com.crt.server.dto.RoomDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface RoomService {
    // Basic CRUD operations
    RoomDTO createRoom(RoomDTO roomDTO);

    RoomDTO getRoomById(UUID id);

    List<RoomDTO> getAllRooms();

    RoomDTO updateRoom(UUID id, RoomDTO roomDTO);

    void deleteRoom(UUID id);

    // Room string parsing
    RoomDTO parseRoomString(String roomString);
    
    // Room lookup by code (e.g., "C625", "R505B")
    RoomDTO getRoomByCode(String roomCode);

    // Bulk operations
    List<RoomDTO> bulkCreateRoomsFromSimpleFormat(MultipartFile file) throws Exception;
}