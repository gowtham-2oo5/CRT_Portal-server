package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionAttendanceRecordDTO {
    private String regNum;
    private String name;
    private double attendancePercentage;
    private String monthTitle;
    private long totalClasses;
    private long absences;
}