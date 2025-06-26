package com.crt.server.controller;

import com.crt.server.dto.AuthRequestDTO;
import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private AuthResponseDTO mockAuthResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        mockAuthResponse = AuthResponseDTO.builder()
                .token("mock-jwt-token")
                .refreshToken("mock-refresh-token")
                .build();
    }

    @Test
    void login_Success() throws Exception {
        when(authService.login(any(AuthRequestDTO.class))).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
    }

    @Test
    void verifyOTP_Success() throws Exception {
        when(authService.verifyOTP(any(AuthRequestDTO.class))).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
    }

    @Test
    void refreshToken_Success() throws Exception {
        when(authService.refreshToken("mock-refresh-token")).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", "Bearer mock-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
    }

    @Test
    void refreshToken_InvalidFormat() throws Exception {
        mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", "InvalidFormat"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid refresh token format"))
                .andExpect(jsonPath("$.path").value("/api/auth/refresh-token"));
    }

    @Test
    void forgotPassword_Success() throws Exception {
        when(authService.forgotPassword("user@example.com")).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/auth/forgot-password")
                .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
    }
}