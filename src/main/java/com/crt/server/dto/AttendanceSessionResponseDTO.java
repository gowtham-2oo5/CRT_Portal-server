package com.crt.server.dto;

import com.crt.server.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionResponseDTO {
    private String id;
    private String facultyId;
    private String sectionId;
    private String sectionName;
    private String topicTaught;
    private String date;
    private String timeSlot;
    private String room;
    private Integer totalStudents;
    private Integer presentCount;
    private Integer absentCount;
    private Double attendancePercentage;
    private LocalDateTime submittedAt;
    private SubmissionStatus submissionStatus;
    private String lateSubmissionReason;
}
