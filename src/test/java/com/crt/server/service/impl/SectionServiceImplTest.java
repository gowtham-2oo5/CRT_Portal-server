package com.crt.server.service.impl;

import com.crt.server.dto.CRT_TrainerDTO;
import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.model.Batch;
import com.crt.server.model.CRT_Trainer;
import com.crt.server.model.Section;
import com.crt.server.model.Student;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.StudentRepository;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CsvService csvService;

    @InjectMocks
    private SectionServiceImpl sectionService;

    private CRT_Trainer testTrainer;
    private Section testSection;
    private CreateSectionDTO testCreateSectionDTO;
    private Student testStudent;
    private UUID testSectionId;
    private UUID testTrainerId;
    private UUID testStudentId;

    @BeforeEach
    void setUp() {
        testSectionId = UUID.randomUUID();
        testTrainerId = UUID.randomUUID();
        testStudentId = UUID.randomUUID();

        testTrainer = CRT_Trainer.builder()
                .id(testTrainerId)
                .name("John Doe")
                .sn("JD")
                .build();

        testSection = Section.builder()
                .id(testSectionId)
                .name("JD Section A")
                .trainer(testTrainer)
                .students(new HashSet<>())
                .strength(0)
                .capacity(30)
                .build();

        testCreateSectionDTO = CreateSectionDTO.builder()
                .trainerId(testTrainerId)
                .sectionName("Section A")
                .build();

        testStudent = Student.builder()
                .id(testStudentId)
                .regNum("2023001")
                .name("Jane Smith")
                .email("jane@example.com")
                .phone("1234567890")
                .department("Computer Science")
                .batch(Batch.Y23)
                .build();
    }

    @Test
    void createSection_WithValidData_ShouldCreateSection() {
        // Arrange
        when(trainerRepository.findById(testTrainerId)).thenReturn(Optional.of(testTrainer));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);

        // Act
        SectionDTO result = sectionService.createSection(testCreateSectionDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testSection.getName(), result.getName());
        assertEquals(testSection.getStrength(), result.getStrength());
        assertNotNull(result.getTrainer());
        assertEquals(testTrainer.getName(), result.getTrainer().getName());
    }

    @Test
    void createSection_WithInvalidTrainer_ShouldThrowException() {
        // Arrange
        when(trainerRepository.findById(testTrainerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> sectionService.createSection(testCreateSectionDTO));
    }

    @Test
    void getSection_WithValidId_ShouldReturnSection() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));

        // Act
        SectionDTO result = sectionService.getSection(testSectionId);

        // Assert
        assertNotNull(result);
        assertEquals(testSection.getId(), result.getId());
        assertEquals(testSection.getName(), result.getName());
    }

    @Test
    void getSection_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> sectionService.getSection(testSectionId));
    }

    @Test
    void getAllSections_ShouldReturnAllSections() {
        // Arrange
        List<Section> sections = Collections.singletonList(testSection);
        when(sectionRepository.findAll()).thenReturn(sections);

        // Act
        List<SectionDTO> result = sectionService.getAllSections();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSection.getName(), result.get(0).getName());
    }

    @Test
    void updateSection_WithValidData_ShouldUpdateSection() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(trainerRepository.findById(testTrainerId)).thenReturn(Optional.of(testTrainer));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);

        // Act
        SectionDTO result = sectionService.updateSection(testSectionId, testCreateSectionDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testSection.getName(), result.getName());
        assertEquals(testSection.getTrainer().getId(), result.getTrainer().getId());
    }

    @Test
    void deleteSection_WithValidId_ShouldDeleteSection() {
        // Arrange
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));

        // Act
        sectionService.deleteSection(testSectionId);

        // Assert
        verify(sectionRepository).delete(testSection);
    }

    @Test
    void registerStudents_WithValidData_ShouldRegisterStudents() {
        // Arrange
        List<String> regNums = Collections.singletonList(testStudent.getRegNum());
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(studentRepository.findByRegNumIn(regNums)).thenReturn(Collections.singletonList(testStudent));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);

        // Act
        SectionDTO result = sectionService.registerStudents(testSectionId, regNums);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getStrength());
        assertTrue(result.getStudents().stream()
                .anyMatch(student -> student.getRegNum().equals(testStudent.getRegNum())));
    }

    @Test
    void getSectionsByTrainer_WithValidTrainer_ShouldReturnSections() {
        // Arrange
        when(trainerRepository.findById(testTrainerId)).thenReturn(Optional.of(testTrainer));
        when(sectionRepository.findByTrainer(testTrainer)).thenReturn(Collections.singletonList(testSection));

        // Act
        List<SectionDTO> result = sectionService.getSectionsByTrainer(testTrainerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSection.getName(), result.get(0).getName());
    }

    @Test
    void getSectionsByTrainer_WithInvalidTrainer_ShouldReturnNull() {
        // Arrange
        when(trainerRepository.findById(testTrainerId)).thenReturn(Optional.empty());

        // Act
        List<SectionDTO> result = sectionService.getSectionsByTrainer(testTrainerId);

        // Assert
        assertNull(result);
    }

    @Test
    void updateStudentSection_WithValidData_ShouldUpdateSection() {
        // Arrange
        Section currentSection = Section.builder()
                .id(UUID.randomUUID())
                .name("Current Section")
                .students(new HashSet<>(Collections.singletonList(testStudent)))
                .strength(1)
                .build();

        when(studentRepository.findById(testStudentId)).thenReturn(Optional.of(testStudent));
        when(sectionRepository.findByStudentsContaining(testStudent))
                .thenReturn(Collections.singletonList(currentSection));
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);

        // Act
        SectionDTO result = sectionService.updateStudentSection(testStudentId, testSectionId);

        // Assert
        assertNotNull(result);
        assertEquals(testSection.getId(), result.getId());
        assertTrue(result.getStudents().stream()
                .anyMatch(student -> student.getId().equals(testStudentId)));
    }

}