package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDTO {
    private String currentPassword; // Optional for password reset flow
    private String newPassword;
    private String email; // Optional for email-based password reset
}