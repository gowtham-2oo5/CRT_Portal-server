package com.crt.server.controller;

import com.crt.server.dto.StudentDTO;
import com.crt.server.model.Batch;
import com.crt.server.service.StudentService;
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

class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private MockMvc mockMvc;
    private UUID studentId;
    private StudentDTO mockStudentDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();
        studentId = UUID.randomUUID();

        mockStudentDTO = StudentDTO.builder()
                .id(studentId)
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .regNum("REG001")
                .department("CS")
                .batch(Batch.Y23)
                .build();
    }

    @Test
    void createStudent_Success() throws Exception {
        when(studentService.createStudent(any(StudentDTO.class))).thenReturn(mockStudentDTO);

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"phone\":\"1234567890\",\"regNum\":\"REG001\",\"department\":\"CS\",\"batch\":\"Y23\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(studentId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.regNum").value("REG001"));
    }

    @Test
    void createStudent_DuplicateEmail() throws Exception {
        when(studentService.createStudent(any(StudentDTO.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"phone\":\"1234567890\",\"regNum\":\"REG001\",\"department\":\"CS\",\"batch\":\"Y23\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getStudentById_Success() throws Exception {
        when(studentService.getStudentById(studentId)).thenReturn(mockStudentDTO);

        mockMvc.perform(get("/api/students/{id}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getStudentByEmail_Success() throws Exception {
        when(studentService.getStudentByEmail("john@example.com")).thenReturn(mockStudentDTO);

        mockMvc.perform(get("/api/students/email/{email}", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getStudentByRegNum_Success() throws Exception {
        when(studentService.getStudentByRegNum("REG001")).thenReturn(mockStudentDTO);

        mockMvc.perform(get("/api/students/regnum/{regNum}", "REG001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId.toString()))
                .andExpect(jsonPath("$.regNum").value("REG001"));
    }

    @Test
    void getAllStudents_Success() throws Exception {
        List<StudentDTO> students = Arrays.asList(mockStudentDTO);
        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(studentId.toString()))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void updateStudent_Success() throws Exception {
        when(studentService.updateStudent(any(UUID.class), any(StudentDTO.class))).thenReturn(mockStudentDTO);

        mockMvc.perform(put("/api/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"phone\":\"1234567890\",\"regNum\":\"REG001\",\"department\":\"CS\",\"batch\":\"Y23\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void updateStudent_NotFound() throws Exception {
        when(studentService.updateStudent(any(UUID.class), any(StudentDTO.class)))
                .thenThrow(new RuntimeException("Student not found"));

        mockMvc.perform(put("/api/students/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"phone\":\"1234567890\",\"regNum\":\"REG001\",\"department\":\"CS\",\"batch\":\"Y23\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudent_Success() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", studentId))
                .andExpect(status().isNoContent());
    }
}