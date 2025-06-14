package com.crt.server.service.impl;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.UserDTO;
import com.crt.server.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private static final String TEST_SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TEST_EXPIRATION = 86400000; // 24 hours

    private UserDTO testUserDTO;
    private AuthResponseDTO testAuthResponse;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);

        testUserDTO = UserDTO.builder()
                .username("testuser")
                .role(Role.FACULTY)
                .build();

        testAuthResponse = AuthResponseDTO.builder()
                .user(testUserDTO)
                .build();

        testUserDetails = new User(
                "testuser",
                "password",
                new ArrayList<>());
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        // Act
        String token = jwtService.generateToken(testAuthResponse);

        // Assert
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(testAuthResponse);

        // Act
        boolean isValid = jwtService.validateToken(token, testUserDetails);

        // Assert
        assertTrue(isValid);
    }

    //TODO: FIX THIS TEST
    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.validateToken(invalidToken, testUserDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractUserName_ShouldReturnCorrectUsername() {
        // Arrange
        String token = jwtService.generateToken(testAuthResponse);

        // Act
        String extractedUsername = jwtService.extractUserName(token);

        // Assert
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        // Arrange
        String token = jwtService.generateToken(testAuthResponse);

        // Act
        String extractedRole = jwtService.extractRole(token);

        // Assert
        assertEquals(Role.FACULTY.toString(), extractedRole);
    }

    @Test
    void extractUserName_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.extractUserName(invalidToken));
    }

    @Test
    void extractRole_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.extractRole(invalidToken));
    }
}