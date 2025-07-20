package com.crt.server.dto;

import com.crt.server.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionDTO {
    private UUID id;
    private UUID facultyId;
    private String facultyName;
    private UUID sectionId;
    private String sectionName;
    private Integer timeSlotId;
    private String startTime;
    private String endTime;
    private LocalDate date;
    private String topicTaught;
    private Integer totalStudents;
    private Integer presentCount;
    private Integer absentCount;
    private Double attendancePercentage;
    private LocalDateTime submittedAt;
    private SubmissionStatus submissionStatus;
    private String lateSubmissionReason;
}
