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
public class AttendanceSubmissionDTO {
    private String timeSlotId;
    private String sectionId;
    private String topicTaught;
    private String date;
    private List<StudentAttendanceRecordDTO> attendanceRecords;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendanceRecordDTO {
        private String studentId;
        private String rollNumber;
        private String name;
        private boolean isPresent;
        private String feedback;
    }
}
