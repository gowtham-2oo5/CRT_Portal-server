package com.crt.server.controller;

import com.crt.server.dto.RoomDTO;
import com.crt.server.model.RoomType;
import com.crt.server.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private MockMvc mockMvc;
    private UUID roomId;
    private RoomDTO mockRoomDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(roomController).build();
        roomId = UUID.randomUUID();

        mockRoomDTO = RoomDTO.builder()
                .id(roomId)
                .block("R")
                .floor("5")
                .roomNumber("04")
                .roomType(RoomType.LAB)
                .capacity(30)
                .roomString("R504 (LAB)")
                .build();
    }

    @Test
    void createRoom_Success() throws Exception {
        when(roomService.createRoom(any(RoomDTO.class))).thenReturn(mockRoomDTO);

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"block\":\"R\",\"floor\":\"5\",\"roomNumber\":\"04\",\"roomType\":\"LAB\",\"capacity\":30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(roomId.toString()))
                .andExpect(jsonPath("$.block").value("R"))
                .andExpect(jsonPath("$.floor").value("5"))
                .andExpect(jsonPath("$.roomNumber").value("04"))
                .andExpect(jsonPath("$.roomType").value("LAB"))
                .andExpect(jsonPath("$.capacity").value(30));
    }

    @Test
    void getRoomById_Success() throws Exception {
        when(roomService.getRoomById(roomId)).thenReturn(mockRoomDTO);

        mockMvc.perform(get("/api/rooms/{id}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roomId.toString()))
                .andExpect(jsonPath("$.block").value("R"))
                .andExpect(jsonPath("$.roomNumber").value("04"));
    }

    @Test
    void getAllRooms_Success() throws Exception {
        List<RoomDTO> rooms = Arrays.asList(mockRoomDTO);
        when(roomService.getAllRooms()).thenReturn(rooms);

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(roomId.toString()))
                .andExpect(jsonPath("$[0].block").value("R"))
                .andExpect(jsonPath("$[0].roomNumber").value("04"));
    }

    @Test
    void updateRoom_Success() throws Exception {
        when(roomService.updateRoom(any(UUID.class), any(RoomDTO.class))).thenReturn(mockRoomDTO);

        mockMvc.perform(put("/api/rooms/{id}", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"block\":\"R\",\"floor\":\"5\",\"roomNumber\":\"04\",\"roomType\":\"LAB\",\"capacity\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roomId.toString()))
                .andExpect(jsonPath("$.block").value("R"))
                .andExpect(jsonPath("$.roomNumber").value("04"));
    }

    @Test
    void deleteRoom_Success() throws Exception {
        mockMvc.perform(delete("/api/rooms/{id}", roomId))
                .andExpect(status().isNoContent());
    }

    @Test
    void parseRoomString_Success() throws Exception {
        when(roomService.parseRoomString("R504")).thenReturn(mockRoomDTO);

        mockMvc.perform(post("/api/rooms/parse")
                .param("roomString", "R504"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.block").value("R"))
                .andExpect(jsonPath("$.floor").value("5"))
                .andExpect(jsonPath("$.roomNumber").value("04"));
    }
}