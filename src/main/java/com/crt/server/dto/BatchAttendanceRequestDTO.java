package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAttendanceRequestDTO {
    @NotBlank(message = "Date cannot be empty")
    private String date;
    @NotBlank(message = "Section ID cannot be empty")
    private String sectionId;
    @NotBlank(message = "Topic taught cannot be empty")
    private String topicTaught;
    private String sessionNotes;
    @NotEmpty(message = "Time slot IDs cannot be empty")
    private List<String> timeSlotIds;
    @Valid
    @NotEmpty(message = "Attendance records cannot be empty")
    private List<AttendanceRecord> attendanceRecords;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceRecord {
        @NotBlank(message = "Student ID cannot be empty")
        private String studentId;
        @NotNull(message = "Presence status cannot be null")
        private boolean present;
    }
}
