package com.crt.server.controller;

import com.crt.server.dto.TrainerDTO;
import com.crt.server.service.TrainerService;
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

class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainerController trainerController;

    private MockMvc mockMvc;
    private UUID trainerId;
    private TrainerDTO mockTrainerDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController).build();
        trainerId = UUID.randomUUID();

        mockTrainerDTO = TrainerDTO.builder()
                .id(trainerId)
                .name("John Trainer")
                .email("trainer@example.com")
                .sn("T123")
                .build();
    }

    @Test
    void createTrainer_Success() throws Exception {
        when(trainerService.createTrainer(any(TrainerDTO.class))).thenReturn(mockTrainerDTO);

        mockMvc.perform(post("/api/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Trainer\",\"email\":\"trainer@example.com\",\"sn\":\"T123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(trainerId.toString()))
                .andExpect(jsonPath("$.name").value("John Trainer"))
                .andExpect(jsonPath("$.email").value("trainer@example.com"))
                .andExpect(jsonPath("$.sn").value("T123"));
    }

    @Test
    void createTrainer_DuplicateEmail() throws Exception {
        when(trainerService.createTrainer(any(TrainerDTO.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Trainer\",\"email\":\"trainer@example.com\",\"sn\":\"T123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getTrainerById_Success() throws Exception {
        when(trainerService.getTrainerById(trainerId)).thenReturn(mockTrainerDTO);

        mockMvc.perform(get("/api/trainers/{id}", trainerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(trainerId.toString()))
                .andExpect(jsonPath("$.name").value("John Trainer"))
                .andExpect(jsonPath("$.email").value("trainer@example.com"));
    }

    @Test
    void getTrainerByEmail_Success() throws Exception {
        when(trainerService.getTrainerByEmail("trainer@example.com")).thenReturn(mockTrainerDTO);

        mockMvc.perform(get("/api/trainers/email/{email}", "trainer@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(trainerId.toString()))
                .andExpect(jsonPath("$.email").value("trainer@example.com"));
    }

    @Test
    void getTrainerBySn_Success() throws Exception {
        when(trainerService.getTrainerBySn("T123")).thenReturn(mockTrainerDTO);

        mockMvc.perform(get("/api/trainers/sn/{sn}", "T123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(trainerId.toString()))
                .andExpect(jsonPath("$.sn").value("T123"));
    }

    @Test
    void getAllTrainers_Success() throws Exception {
        List<TrainerDTO> trainers = Arrays.asList(mockTrainerDTO);
        when(trainerService.getAllTrainers()).thenReturn(trainers);

        mockMvc.perform(get("/api/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(trainerId.toString()))
                .andExpect(jsonPath("$[0].name").value("John Trainer"))
                .andExpect(jsonPath("$[0].email").value("trainer@example.com"));
    }

    @Test
    void updateTrainer_Success() throws Exception {
        when(trainerService.updateTrainer(any(UUID.class), any(TrainerDTO.class))).thenReturn(mockTrainerDTO);

        mockMvc.perform(put("/api/trainers/{id}", trainerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Trainer\",\"email\":\"trainer@example.com\",\"sn\":\"T123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(trainerId.toString()))
                .andExpect(jsonPath("$.name").value("John Trainer"));
    }

    @Test
    void updateTrainer_NotFound() throws Exception {
        when(trainerService.updateTrainer(any(UUID.class), any(TrainerDTO.class)))
                .thenThrow(new RuntimeException("Trainer not found"));

        mockMvc.perform(put("/api/trainers/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Trainer\",\"email\":\"trainer@example.com\",\"sn\":\"T123\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTrainer_Success() throws Exception {
        mockMvc.perform(delete("/api/trainers/{id}", trainerId))
                .andExpect(status().isNoContent());
    }
}