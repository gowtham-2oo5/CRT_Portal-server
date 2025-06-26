package com.crt.server.service.impl;

import com.crt.server.dto.TrainerDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.CRT_Trainer;
import com.crt.server.repository.TrainerRepository;
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
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private CsvService csvService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private CRT_Trainer testTrainer;
    private TrainerDTO testTrainerDTO;
    private UUID testTrainerId;

    @BeforeEach
    void setUp() {
        testTrainerId = UUID.randomUUID();
        testTrainer = CRT_Trainer.builder()
                .id(testTrainerId)
                .name("Test Trainer")
                .email("trainer@example.com")
                .sn("TT")
                .build();

        testTrainerDTO = TrainerDTO.builder()
                .id(testTrainerId)
                .name("Test Trainer")
                .email("trainer@example.com")
                .sn("TT")
                .build();
    }

    @Test
    void createTrainer_WithValidData_ShouldCreateTrainer() {
        // Arrange
        when(trainerRepository.existsByEmail(anyString())).thenReturn(false);
        when(trainerRepository.existsBySn(anyString())).thenReturn(false);
        when(trainerRepository.save(any(CRT_Trainer.class))).thenReturn(testTrainer);

        // Act
        TrainerDTO result = trainerService.createTrainer(testTrainerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testTrainerDTO.getName(), result.getName());
        assertEquals(testTrainerDTO.getEmail(), result.getEmail());
        assertEquals(testTrainerDTO.getSn(), result.getSn());
    }

    @Test
    void createTrainer_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(trainerRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> trainerService.createTrainer(testTrainerDTO));
    }

    @Test
    void createTrainer_WithExistingSn_ShouldThrowException() {
        // Arrange
        when(trainerRepository.existsByEmail(anyString())).thenReturn(false);
        when(trainerRepository.existsBySn(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> trainerService.createTrainer(testTrainerDTO));
    }

    @Test
    void getTrainerById_WithValidId_ShouldReturnTrainer() {
        // Arrange
        when(trainerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTrainer));

        // Act
        TrainerDTO result = trainerService.getTrainerById(testTrainerId);

        // Assert
        assertNotNull(result);
        assertEquals(testTrainerDTO.getId(), result.getId());
        assertEquals(testTrainerDTO.getName(), result.getName());
    }

    @Test
    void getTrainerById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(trainerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trainerService.getTrainerById(testTrainerId));
    }

    @Test
    void getTrainerByEmail_WithValidEmail_ShouldReturnTrainer() {
        // Arrange
        when(trainerRepository.findByEmail(anyString())).thenReturn(testTrainer);

        // Act
        TrainerDTO result = trainerService.getTrainerByEmail(testTrainer.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(testTrainerDTO.getEmail(), result.getEmail());
    }

    @Test
    void getTrainerBySn_WithValidSn_ShouldReturnTrainer() {
        // Arrange
        when(trainerRepository.findBySn(anyString())).thenReturn(testTrainer);

        // Act
        TrainerDTO result = trainerService.getTrainerBySn(testTrainer.getSn());

        // Assert
        assertNotNull(result);
        assertEquals(testTrainerDTO.getSn(), result.getSn());
    }

    @Test
    void getAllTrainers_ShouldReturnAllTrainers() {
        // Arrange
        List<CRT_Trainer> trainers = Arrays.asList(testTrainer);
        when(trainerRepository.findAll()).thenReturn(trainers);

        // Act
        List<TrainerDTO> result = trainerService.getAllTrainers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTrainerDTO.getName(), result.get(0).getName());
    }

    @Test
    void updateTrainer_WithValidData_ShouldUpdateTrainer() {
        // Arrange
        when(trainerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTrainer));
        when(trainerRepository.save(any(CRT_Trainer.class))).thenReturn(testTrainer);

        // Act
        TrainerDTO result = trainerService.updateTrainer(testTrainerId, testTrainerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testTrainerDTO.getName(), result.getName());
        assertEquals(testTrainerDTO.getEmail(), result.getEmail());
        assertEquals(testTrainerDTO.getSn(), result.getSn());
    }

    @Test
    void deleteTrainer_WithValidId_ShouldDeleteTrainer() {
        // Arrange
        when(trainerRepository.existsById(any(UUID.class))).thenReturn(true);

        // Act
        trainerService.deleteTrainer(testTrainerId);

        // Assert
        verify(trainerRepository).deleteById(testTrainerId);
    }

    @Test
    void bulkCreateTrainers_WithValidCsv_ShouldCreateTrainers() throws Exception {
        // Arrange
        String csvContent = "name,email,sn\n" +
                "Test Trainer,trainer@example.com,TT";
        MultipartFile file = new MockMultipartFile("trainers.csv", csvContent.getBytes());
        List<CRT_Trainer> trainers = Arrays.asList(testTrainer);
        when(csvService.parseCsv(any(), any(), any(CsvService.RecordMapper.class))).thenReturn(trainers);
        when(trainerRepository.saveAll(any())).thenReturn(trainers);

        // Act
        List<TrainerDTO> result = trainerService.bulkCreateTrainers(file);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTrainerDTO.getName(), result.get(0).getName());
    }

    @Test
    void bulkCreateTrainers_WithInvalidCsv_ShouldThrowException() throws Exception {
        // Arrange
        String csvContent = "name,email,sn\n" +
                "Test Trainer,invalid-email,TT";
        MultipartFile file = new MockMultipartFile("trainers.csv", csvContent.getBytes());
        when(csvService.parseCsv(any(), any(), any(CsvService.RecordMapper.class)))
                .thenThrow(new IllegalArgumentException("Invalid email format"));

        // Act & Assert
        assertThrows(Exception.class, () -> trainerService.bulkCreateTrainers(file));
    }
}