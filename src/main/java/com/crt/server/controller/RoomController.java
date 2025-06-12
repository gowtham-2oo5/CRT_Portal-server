package com.crt.server.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crt.server.dto.RoomDTO;
import com.crt.server.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "APIs for managing rooms")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "Create a new room")
    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO) {
        log.info("Creating new room: {}", roomDTO);
        return new ResponseEntity<>(roomService.createRoom(roomDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get room by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable UUID id) {
        log.info("Fetching room with id: {}", id);
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @Operation(summary = "Get all rooms")
    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        log.info("Fetching all rooms");
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @Operation(summary = "Update room by ID")
    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable UUID id, @RequestBody RoomDTO roomDTO) {
        log.info("Updating room with id: {}", id);
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    @Operation(summary = "Delete room by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        log.info("Deleting room with id: {}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Parse room string (e.g., 'R504' or 'R504A')")
    @PostMapping("/parse")
    public ResponseEntity<RoomDTO> parseRoomString(@RequestParam String roomString) {
        log.info("Parsing room string: {}", roomString);
        return ResponseEntity.ok(roomService.parseRoomString(roomString));
    }
}