package com.crt.server.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportDTO {
    private UUID studentId;
    private String studentName;
    private String regNum;
    private long totalClasses;
    private long absences;
    private double attendancePercentage;
    private List<AttendanceDTO> attendanceRecords;
}