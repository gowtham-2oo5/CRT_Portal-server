package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LateSubmissionDTO {
    @NotNull(message = "Session ID is required")
    private UUID sessionId;
    
    @NotBlank(message = "Reason is required")
    private String reason;
}
