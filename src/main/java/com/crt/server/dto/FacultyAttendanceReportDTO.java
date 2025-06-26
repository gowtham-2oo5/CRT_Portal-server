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
public class FacultyAttendanceReportDTO {
    private List<AssignedSectionDTO> sections;
    private List<StudentAttendanceReportDTO> attendanceReports;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendanceReportDTO {
        private String studentId;
        private String rollNumber;
        private String name;
        private String section;
        private Integer totalSessions;
        private Integer attendedSessions;
        private Double attendancePercentage;
        private LocalDateTime lastAttended;
    }
}
