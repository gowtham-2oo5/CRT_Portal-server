package com.crt.server.controller;

import com.crt.server.security.JwtService;
import com.crt.server.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestEmailControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TestController testEmailController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(testEmailController).build();
    }

    @Test
    void testStudentEmail_Success() throws Exception {
        mockMvc.perform(post("/api/test/student")
                .param("email", "student@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Student confirmation email sent successfully"));
    }

    @Test
    void testStudentEmail_Failure() throws Exception {
        doThrow(new RuntimeException("Failed to send email"))
                .when(emailService).sendStudentAccountConfirmationMail(anyString(), any());

        mockMvc.perform(post("/api/test/student")
                .param("email", "student@example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to send student confirmation email: Failed to send email"));
    }

    @Test
    void getTokenInfo_Success() throws Exception {
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("username", "johndoe");
        tokenInfo.put("role", "ADMIN");

        when(jwtService.extractUsername("valid-token")).thenReturn("johndoe");
        when(jwtService.extractRole("valid-token")).thenReturn("ADMIN");

        mockMvc.perform(get("/api/test/token-info")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void getTokenInfo_InvalidFormat() throws Exception {
        mockMvc.perform(get("/api/test/token-info")
                .header("Authorization", "InvalidFormat"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No valid token provided"));
    }
}