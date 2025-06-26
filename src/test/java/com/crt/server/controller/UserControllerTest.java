package com.crt.server.controller;

import com.crt.server.dto.UserDTO;
import com.crt.server.model.Role;
import com.crt.server.service.UserService;
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

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private UUID userId;
    private UserDTO mockUserDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        userId = UUID.randomUUID();

        mockUserDTO = UserDTO.builder()
                .id(userId)
                .username("johndoe")
                .email("john@example.com")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void createUser_Success() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(mockUserDTO);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"email\":\"john@example.com\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void createUser_DuplicateUsername() throws Exception {
        when(userService.createUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("Username or email already exists"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"email\":\"john@example.com\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(userId)).thenReturn(mockUserDTO);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        List<UserDTO> users = Arrays.asList(mockUserDTO);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("johndoe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void updateUser_Success() throws Exception {
        when(userService.updateUser(any(UUID.class), any(UserDTO.class))).thenReturn(mockUserDTO);

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"email\":\"john@example.com\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    void updateUser_NotFound() throws Exception {
        when(userService.updateUser(any(UUID.class), any(UserDTO.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"email\":\"john@example.com\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isNotFound());
    }
}