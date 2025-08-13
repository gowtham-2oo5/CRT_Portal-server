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
public class TimeSlotValidationResponseDTO {
    private boolean valid;
    private String message;
    private String reason;
    private List<TimeSlotDetail> timeSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotDetail {
        private String id;
        private String startTime;
        private String endTime;
        private String sectionId;
        private String sectionName;
    }

    // Static factory methods for convenience
    public static TimeSlotValidationResponseDTO valid(String message) {
        return TimeSlotValidationResponseDTO.builder()
                .valid(true)
                .message(message)
                .build();
    }

    public static TimeSlotValidationResponseDTO invalid(String message) {
        return TimeSlotValidationResponseDTO.builder()
                .valid(false)
                .message(message)
                .build();
    }

    public static TimeSlotValidationResponseDTO invalid(String message, String reason) {
        return TimeSlotValidationResponseDTO.builder()
                .valid(false)
                .message(message)
                .reason(reason)
                .build();
    }
}
