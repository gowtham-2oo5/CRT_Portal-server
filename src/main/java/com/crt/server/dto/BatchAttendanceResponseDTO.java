package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAttendanceResponseDTO {
    private boolean success;
    private String message;
    private List<BatchSubmissionResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchSubmissionResult {
        private String timeSlotId;
        private String status; // "success" or "error"
        private String error;
    }
}
