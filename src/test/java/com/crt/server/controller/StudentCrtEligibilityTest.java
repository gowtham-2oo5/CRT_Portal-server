package com.crt.server.controller;

import com.crt.server.dto.StudentDTO;
import com.crt.server.model.Batch;
import com.crt.server.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
public class StudentCrtEligibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testRemoveStudentFromCrt() throws Exception {
        UUID studentId = UUID.randomUUID();
        String reason = "Academic performance below threshold";
        
        StudentDTO mockResponse = StudentDTO.builder()
                .id(studentId)
                .name("Test Student")
                .email("test@example.com")
                .phone("1234567890")
                .regNum("ST001")
                .department("Computer Science")
                .batch(Batch.Y23)
                .crtEligibility(false)
                .feedback(reason)
                .build();

        when(studentService.updateCrtEligibility(eq(studentId), eq(false), eq(reason)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/students/{id}/remove-from-crt", studentId)
                .param("reason", reason)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId.toString()))
                .andExpect(jsonPath("$.crtEligibility").value(false))
                .andExpect(jsonPath("$.feedback").value(reason));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testAddStudentToCrt() throws Exception {
        UUID studentId = UUID.randomUUID();
        String reason = "Improved performance, eligible for CRT again";
        
        StudentDTO mockResponse = StudentDTO.builder()
                .id(studentId)
                .name("Test Student")
                .email("test@example.com")
                .phone("1234567890")
                .regNum("ST001")
                .department("Computer Science")
                .batch(Batch.Y23)
                .crtEligibility(true)
                .feedback(reason)
                .build();

        when(studentService.updateCrtEligibility(eq(studentId), eq(true), eq(reason)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/students/{id}/add-to-crt", studentId)
                .param("reason", reason)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId.toString()))
                .andExpect(jsonPath("$.crtEligibility").value(true))
                .andExpect(jsonPath("$.feedback").value(reason));
    }

    @Test
    @WithMockUser(authorities = "FACULTY")
    public void testRemoveStudentFromCrt_ForbiddenForFaculty() throws Exception {
        UUID studentId = UUID.randomUUID();
        String reason = "Test reason";

        mockMvc.perform(post("/api/students/{id}/remove-from-crt", studentId)
                .param("reason", reason)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
