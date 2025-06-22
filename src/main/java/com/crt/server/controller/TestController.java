package com.crt.server.controller;

import com.crt.server.security.JwtService;
import com.crt.server.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtService jwtService;

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