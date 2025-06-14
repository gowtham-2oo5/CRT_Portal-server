package com.crt.server.service.impl;

import com.crt.server.dto.AuthRequestDTO;
import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.UserDTO;
import com.crt.server.exception.AuthenticationException;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.User;
import com.crt.server.repository.UserRepository;
import com.crt.server.security.JwtService;
import com.crt.server.security.OTPService;
import com.crt.server.security.PasswordService;
import com.crt.server.service.EmailService;
import com.crt.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private OTPService otpService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserDTO testUserDTO;
    private AuthRequestDTO loginRequest;
    private AuthRequestDTO otpVerificationRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        testUserDTO = UserDTO.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();

        loginRequest = AuthRequestDTO.builder()
                .usernameOrEmail("test@example.com")
                .password("password123")
                .build();

        otpVerificationRequest = AuthRequestDTO.builder()
                .usernameOrEmail("test@example.com")
                .otp("123456")
                .build();
    }

    @Test
    void login_WithValidCredentials_ShouldSendOTP() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        when(otpService.generateOTP()).thenReturn("123456");

        // Act
        AuthResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("OTP sent to t***test@example.com", response.getMessage());
        assertEquals(testUserDTO, response.getUser());
        verify(emailService).sendLoginOtp(anyString(), anyString());
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
    }

    @Test
    void verifyOTP_WithValidOTP_ShouldReturnTokens() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpService.verifyOTP(anyString(), anyString())).thenReturn(true);
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        // Act
        AuthResponseDTO response = authService.verifyOTP(otpVerificationRequest);

        // Assert
        assertNotNull(response);
        assertEquals("OTP verified successfully", response.getMessage());
        assertEquals("jwtToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals(testUserDTO, response.getUser());
    }

    @Test
    void verifyOTP_WithInvalidOTP_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpService.verifyOTP(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.verifyOTP(otpVerificationRequest));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        // Arrange
        String refreshToken = "validRefreshToken";
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(true);
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        when(jwtService.generateToken(any(User.class))).thenReturn("newJwtToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("newRefreshToken");

        // Act
        AuthResponseDTO response = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("Token refreshed successfully", response.getMessage());
        assertEquals("newJwtToken", response.getToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
        assertEquals(testUserDTO, response.getUser());
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String refreshToken = "invalidRefreshToken";
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(false);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.refreshToken(refreshToken));
    }

    @Test
    void forgotPassword_WithValidEmail_ShouldSendTemporaryPassword() {
        // Arrange
        String email = "test@example.com";
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        when(passwordService.generateTemporaryPassword()).thenReturn("tempPass123");

        // Act
        AuthResponseDTO response = authService.forgotPassword(email);

        // Assert
        assertNotNull(response);
        assertEquals("Temporary password has been sent to t***test@example.com", response.getMessage());
        verify(passwordService).updatePasswordByEmail(any());
        verify(emailService).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void forgotPassword_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(email));
    }
}