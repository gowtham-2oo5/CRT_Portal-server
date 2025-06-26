package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceDetailDTO {
    private StudentDTO student;
    private AttendanceSummaryDTO attendanceSummary;
    private List<SessionHistoryDTO> sessionHistory;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceSummaryDTO {
        private Integer totalSessions;
        private Integer attendedSessions;
        private Double attendancePercentage;
        private LocalDateTime lastAttended;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionHistoryDTO {
        private String id;
        private String date;
        private String topicTaught;
        private String timeSlot;
        private String room;
        private boolean isPresent;
        private LocalDateTime markedAt;
        private String feedback;
    }
}
