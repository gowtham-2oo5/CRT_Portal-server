package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAnalyticsDTO {
    private OverallStatsDTO overallStats;
    private List<SectionStatsDTO> sectionStats;
    private List<DailyAttendanceDTO> dailyTrends;
    private List<StudentPerformanceDTO> topPerformers;
    private List<StudentPerformanceDTO> lowPerformers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStatsDTO {
        private Integer totalSessions;
        private Integer totalStudents;
        private Double averageAttendance;
        private Integer presentToday;
        private Integer absentToday;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionStatsDTO {
        private String sectionId;
        private String sectionName;
        private Integer totalStudents;
        private Integer totalSessions;
        private Double averageAttendance;
        private String trainerName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAttendanceDTO {
        private String date;
        private Integer totalSessions;
        private Integer totalPresent;
        private Integer totalAbsent;
        private Double attendancePercentage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentPerformanceDTO {
        private String studentId;
        private String rollNumber;
        private String name;
        private String section;
        private Double attendancePercentage;
        private Integer totalSessions;
        private Integer attendedSessions;
    }
}
