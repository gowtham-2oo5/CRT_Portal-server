package com.crt.server.controller;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.PasswordUpdateDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.security.PasswordService;
import com.crt.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserService userService;

    @PutMapping("/{id}/password")
    public ResponseEntity<AuthResponseDTO> updatePassword(
            @PathVariable UUID id,
            @RequestBody PasswordUpdateDTO passwordUpdate) {
        return ResponseEntity.ok(passwordService.updatePassword(id, passwordUpdate));
    }

    @PutMapping("/password")
    public ResponseEntity<AuthResponseDTO> updatePasswordByEmail(
            @RequestBody PasswordUpdateDTO passwordUpdate) {
        return ResponseEntity.ok(passwordService.updatePasswordByEmail(passwordUpdate));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email) {
        try {
            userService.resetPassword(email);
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .build();
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message("User not found with email: " + email)
                    .path("/api/users/reset-password")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }
}