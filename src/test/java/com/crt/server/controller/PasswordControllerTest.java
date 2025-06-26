package com.crt.server.controller;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.PasswordUpdateDTO;
import com.crt.server.security.PasswordService;
import com.crt.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PasswordControllerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PasswordController passwordController;

    private MockMvc mockMvc;
    private UUID userId;
    private AuthResponseDTO mockAuthResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(passwordController).build();
        userId = UUID.randomUUID();

        mockAuthResponse = AuthResponseDTO.builder()
                .token("mock-jwt-token")
                .refreshToken("mock-refresh-token")
                .build();
    }

    @Test
    void updatePassword_Success() throws Exception {
        when(passwordService.updatePassword(any(UUID.class), any(PasswordUpdateDTO.class)))
                .thenReturn(mockAuthResponse);

        mockMvc.perform(put("/api/users/{id}/password", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldpass123\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
    }

    @Test
    void updatePasswordByEmail_Success() throws Exception {
        when(passwordService.updatePasswordByEmail(any(PasswordUpdateDTO.class)))
                .thenReturn(mockAuthResponse);

        mockMvc.perform(put("/api/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
    }
}