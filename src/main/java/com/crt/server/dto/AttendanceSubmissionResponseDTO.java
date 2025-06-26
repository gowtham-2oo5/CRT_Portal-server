package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSubmissionResponseDTO {
    private boolean success;
    private String message;
    private AttendanceSessionResponseDTO attendanceSession;
}
