package com.crt.server.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionEvent {
    private String sessionId;
    private String facultyId;
    private String sectionId;
    private String sectionName;
    private String timeSlotId;
    private Integer totalStudents;
    private Integer presentCount;
    private Integer absentCount;
    private Double attendancePercentage;
    private String status; // STARTED, IN_PROGRESS, COMPLETED
    private List<StudentAttendanceUpdate> recentUpdates;
    
    // Event types
    public static final String SESSION_STARTED = "attendance_session_started";
    public static final String STUDENT_MARKED = "student_marked_present";
    public static final String SESSION_COMPLETED = "attendance_session_completed";
    public static final String STATS_UPDATED = "attendance_stats_updated";
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendanceUpdate {
        private String studentId;
        private String studentName;
        private String rollNumber;
        private boolean present;
        private String feedback;
    }
}
