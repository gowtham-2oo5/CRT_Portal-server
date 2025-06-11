package com.crt.server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crt.server.dto.AccountConfirmationMailDTO;
import com.crt.server.service.EmailService;
import com.crt.server.security.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestEmailController {

    private final EmailService emailService;
    private final JwtService jwtService;

    @PostMapping("/otp")
    public ResponseEntity<String> testOtpEmail(@RequestParam String email) {
        try {
            emailService.sendLoginOtp("123456", email);
            return ResponseEntity.ok("OTP email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send OTP email: " + e.getMessage());
        }
    }

    @PostMapping("/password")
    public ResponseEntity<String> testPasswordEmail(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password) {
        try {
            emailService.sendPasswordEmail(email, username, password);
            return ResponseEntity.ok("Password email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send password email: " + e.getMessage());
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> testResetEmail(@RequestParam String email) {
        try {
            emailService.sendPasswordResetEmail(email, "RESET123");
            return ResponseEntity.ok("Reset email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send reset email: " + e.getMessage());
        }
    }

    @PostMapping("/student")
    public ResponseEntity<String> testStudentEmail(@RequestParam String email) {
        try {
            AccountConfirmationMailDTO student = AccountConfirmationMailDTO.builder()
                    .name("Test Student")
                    .username("teststudent")
                    .password("testpass123")
                    .build();
            emailService.sendStudentAccountConfirmationMail(email, student);
            return ResponseEntity.ok("Student confirmation email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send student confirmation email: " + e.getMessage());
        }
    }

    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("username", jwtService.extractUsername(token));
            tokenInfo.put("role", jwtService.extractRole(token));

            return ResponseEntity.ok(tokenInfo);
        }
        return ResponseEntity.badRequest().body(Map.of("error", "No valid token provided"));
    }
}