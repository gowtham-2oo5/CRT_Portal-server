package com.crt.server.controller;

import com.crt.server.dto.UserDTO;
import com.crt.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserSearchControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserSearchController userSearchController;

    private MockMvc mockMvc;
    private UUID userId;
    private UserDTO mockUserDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userSearchController).build();
        userId = UUID.randomUUID();

        mockUserDTO = UserDTO.builder()
                .id(userId)
                .username("johndoe")
                .email("john@example.com")
                .build();
    }

    @Test
    void getUserByUsername_Success() throws Exception {
        when(userService.getUserByUsername("johndoe")).thenReturn(mockUserDTO);

        mockMvc.perform(get("/api/users/username/{username}", "johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getUserByUsername_NotFound() throws Exception {
        when(userService.getUserByUsername(anyString()))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/users/username/{username}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByEmail_Success() throws Exception {
        when(userService.getUserByEmail("john@example.com")).thenReturn(mockUserDTO);

        mockMvc.perform(get("/api/users/email/{email}", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}