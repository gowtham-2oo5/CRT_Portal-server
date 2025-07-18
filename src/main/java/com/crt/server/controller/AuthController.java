package com.crt.server.controller;

import com.crt.server.dto.AuthRequestDTO;
import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOTP(@RequestBody AuthRequestDTO otpVerification) {

        return ResponseEntity.ok(authService.verifyOTP(otpVerification));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message("Invalid refresh token format")
                    .path("/api/auth/refresh-token")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponseDTO> forgotPassword(@RequestParam String email) {
        System.out.println("Email: " + email);
        return ResponseEntity.ok(authService.forgotPassword(email));
    }
}
