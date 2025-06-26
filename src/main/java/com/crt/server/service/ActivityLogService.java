package com.crt.server.service;

import com.crt.server.dto.ActivityLogDTO;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;

import java.util.List;

public interface ActivityLogService {
    
    /**
     * Log attendance posting activity
     * 
     * @param faculty The faculty who posted attendance
     * @param section The section for which attendance was posted
     * @param timeSlot The time slot for which attendance was posted
     * @param attendancePercentage The calculated attendance percentage for the section
     */
    void logAttendancePosted(User faculty, Section section, TimeSlot timeSlot, Double attendancePercentage);
    
    /**
     * Get recent activity logs
     * 
     * @param limit Maximum number of logs to return (default 20)
     * @return List of recent activity logs
     */
    List<ActivityLogDTO> getRecentActivities(int limit);
    
    /**
     * Get recent activity logs with default limit
     * 
     * @return List of recent activity logs (last 20)
     */
    List<ActivityLogDTO> getRecentActivities();
}
