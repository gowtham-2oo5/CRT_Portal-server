package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student data with attendance information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceDTO {
    private String id;
    private String name;
    private String email;
    private String regNum;
    private String department;
    private String section;
    private String batch;
    private boolean crtEligibility;
    private long totalAttendance;
    private long presentCount;
    private String feedback;
    private double attendancePercentage;
}
