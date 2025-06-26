package com.crt.server.service;

import com.crt.server.dto.AttendanceAnalyticsDTO;
import com.crt.server.dto.WeeklyTimetableDTO;
import com.crt.server.model.User;

import java.util.UUID;

public interface FacultyAnalyticsService {
    
    /**
     * Get comprehensive attendance analytics for faculty
     */
    AttendanceAnalyticsDTO getAttendanceAnalytics(User faculty, UUID sectionId, String startDate, String endDate);
    
    /**
     * Get weekly timetable with attendance status
     */
    WeeklyTimetableDTO getWeeklyTimetable(User faculty, String week);
    
    /**
     * Export attendance report as CSV
     */
    byte[] exportAttendanceReportCSV(User faculty, UUID sectionId, String startDate, String endDate);
}
