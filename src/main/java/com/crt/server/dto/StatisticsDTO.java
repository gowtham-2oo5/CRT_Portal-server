package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for holding various statistics about the CRT Portal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private long crtEligibleStudentCount;
    private long departmentCount;
    private double averageAttendance;
    private long totalStudents;
    private long totalSections;
    private long totalFaculty;
}
