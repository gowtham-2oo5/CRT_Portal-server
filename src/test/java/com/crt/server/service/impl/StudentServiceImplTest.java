package com.crt.server.service.impl;

import com.crt.server.dto.StudentDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Batch;
import com.crt.server.model.Student;
import com.crt.server.repository.StudentRepository;
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
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CsvService csvService;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student testStudent;
    private StudentDTO testStudentDTO;
    private UUID testStudentId;

    @BeforeEach
    void setUp() {
        testStudentId = UUID.randomUUID();
        testStudent = Student.builder()
                .id(testStudentId)
                .name("Test Student")
                .email("test@example.com")
                .phone("1234567890")
                .regNum("REG123")
                .department("Computer Science")
                .batch(Batch.Y23)
                .build();

        testStudentDTO = StudentDTO.builder()
                .id(testStudentId)
                .name("Test Student")
                .email("test@example.com")
                .phone("1234567890")
                .regNum("REG123")
                .department("Computer Science")
                .batch(Batch.Y23)
                .build();
    }

    @Test
    void createStudent_WithValidData_ShouldCreateStudent() {
        // Arrange
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentRepository.existsByRegNum(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        StudentDTO result = studentService.createStudent(testStudentDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testStudentDTO.getName(), result.getName());
        assertEquals(testStudentDTO.getEmail(), result.getEmail());
        assertEquals(testStudentDTO.getRegNum(), result.getRegNum());
    }

    @Test
    void createStudent_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(studentRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> studentService.createStudent(testStudentDTO));
    }

    @Test
    void createStudent_WithExistingRegNum_ShouldThrowException() {
        // Arrange
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentRepository.existsByRegNum(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> studentService.createStudent(testStudentDTO));
    }

    @Test
    void getStudentById_WithValidId_ShouldReturnStudent() {
        // Arrange
        when(studentRepository.findById(any(UUID.class))).thenReturn(Optional.of(testStudent));

        // Act
        StudentDTO result = studentService.getStudentById(testStudentId);

        // Assert
        assertNotNull(result);
        assertEquals(testStudentDTO.getId(), result.getId());
        assertEquals(testStudentDTO.getName(), result.getName());
    }

    @Test
    void getStudentById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(studentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(testStudentId));
    }

    @Test
    void getStudentByEmail_WithValidEmail_ShouldReturnStudent() {
        // Arrange
        when(studentRepository.findByEmail(anyString())).thenReturn(testStudent);

        // Act
        StudentDTO result = studentService.getStudentByEmail(testStudent.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(testStudentDTO.getEmail(), result.getEmail());
    }

    @Test
    void getStudentByRegNum_WithValidRegNum_ShouldReturnStudent() {
        // Arrange
        when(studentRepository.findByRegNum(anyString())).thenReturn(testStudent);

        // Act
        StudentDTO result = studentService.getStudentByRegNum(testStudent.getRegNum());

        // Assert
        assertNotNull(result);
        assertEquals(testStudentDTO.getRegNum(), result.getRegNum());
    }

    @Test
    void getAllStudents_ShouldReturnAllStudents() {
        // Arrange
        List<Student> students = Arrays.asList(testStudent);
        when(studentRepository.findAll()).thenReturn(students);

        // Act
        List<StudentDTO> result = studentService.getAllStudents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStudentDTO.getName(), result.get(0).getName());
    }

    @Test
    void updateStudent_WithValidData_ShouldUpdateStudent() {
        // Arrange
        when(studentRepository.findById(any(UUID.class))).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        StudentDTO result = studentService.updateStudent(testStudentId, testStudentDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testStudentDTO.getName(), result.getName());
        assertEquals(testStudentDTO.getEmail(), result.getEmail());
    }

    @Test
    void deleteStudent_WithValidId_ShouldDeleteStudent() {
        // Arrange
        when(studentRepository.existsById(any(UUID.class))).thenReturn(true);

        // Act
        studentService.deleteStudent(testStudentId);

        // Assert
        verify(studentRepository).deleteById(testStudentId);
    }

    @Test
    void bulkCreateStudents_WithValidCsv_ShouldCreateStudents() throws Exception {
        // Arrange
        String csvContent = "name,email,phone,regNum,department,batch\n" +
                "Test Student,test@example.com,1234567890,REG123,Computer Science,Y23";
        MultipartFile file = new MockMultipartFile("students.csv", csvContent.getBytes());
        List<Student> students = Arrays.asList(testStudent);
        when(csvService.parseCsv(any(), any(), any(CsvService.RecordMapper.class))).thenReturn(students);
        when(studentRepository.saveAll(any())).thenReturn(students);

        // Act
        List<StudentDTO> result = studentService.bulkCreateStudents(file);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStudentDTO.getName(), result.get(0).getName());
    }

    @Test
    void bulkCreateStudents_WithInvalidCsv_ShouldThrowException() throws Exception {
        // Arrange
        String csvContent = "name,email,phone,regNum,department,batch\n" +
                "Test Student,invalid-email,1234567890,REG123,Computer Science,InvalidBatch";
        MultipartFile file = new MockMultipartFile("students.csv", csvContent.getBytes());
        when(csvService.parseCsv(any(), any(), any(CsvService.RecordMapper.class)))
                .thenThrow(new Exception("Invalid data"));

        // Act & Assert
        assertThrows(Exception.class, () -> studentService.bulkCreateStudents(file));
    }
}