package com.crt.server.service.impl;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.UserDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Role;
import com.crt.server.model.User;
import com.crt.server.repository.UserRepository;
import com.crt.server.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .name("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .username("testuser")
                .password("encodedPassword")
                .role(Role.FACULTY)
                .build();

        testUserDTO = UserDTO.builder()
                .id(testUserId)
                .name("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .username("testuser")
                .role(Role.FACULTY)
                .build();
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.createUser(testUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(emailService).sendPasswordEmail(anyString(), anyString(), anyString());
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.createUser(testUserDTO));
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserById(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getUsername(), result.getUsername());
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(testUserId));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.updateUser(testUserId, testUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getName(), result.getName());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);

        // Act
        userService.deleteUser(testUserId);

        // Assert
        verify(userRepository).deleteById(testUserId);
    }

    @Test
    void resetPassword_WithValidEmail_ShouldResetPassword() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        // Act
        userService.resetPassword(testUser.getEmail());

        // Assert
        verify(userRepository).save(any(User.class));
        verify(emailService).sendPasswordEmail(anyString(), anyString(), anyString());
    }

    @Test
    void updatePassword_WithValidData_ShouldUpdatePassword() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        // Act
        AuthResponseDTO result = userService.updatePassword(testUserId, "currentPassword", "newPassword");

        // Assert
        assertNotNull(result);
        assertEquals("Password updated successfully", result.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updatePassword_WithInvalidCurrentPassword_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.updatePassword(testUserId, "wrongPassword", "newPassword"));
    }

    @Test
    void updatePasswordByEmail_WithValidEmail_ShouldUpdatePassword() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        // Act
        AuthResponseDTO result = userService.updatePasswordByEmail(testUser.getEmail(), "newPassword");

        // Assert
        assertNotNull(result);
        assertEquals("Password updated successfully", result.getMessage());
        verify(userRepository).save(any(User.class));
    }
}