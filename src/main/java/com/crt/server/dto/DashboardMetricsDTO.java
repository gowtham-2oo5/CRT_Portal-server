package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDTO {
    
    private Long totalStudents;
    private Long totalFaculties;
    private Long totalSections;
    private Long totalRooms;
    private Long totalTimeSlots;
    
    // Additional useful metrics
    private Long activeStudents; // students with crtEligibility = true
    private Long totalAttendanceRecords;
}
