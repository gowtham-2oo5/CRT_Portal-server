package com.crt.server.service;

import com.crt.server.dto.*;
import com.crt.server.model.User;

public interface FacultyDashboardService {
    
    /**
     * Get complete faculty dashboard data
     */
    FacultyDashboardDTO getFacultyDashboard(User faculty);
    
    /**
     * Get faculty profile information
     */
    FacultyProfileDTO getFacultyProfile(User faculty);
    
    /**
     * Get today's attendance count for faculty
     */
    Long getTodayAttendanceCount(User faculty);
    
    /**
     * Get weekly attendance count for faculty
     */
    Long getWeeklyAttendanceCount(User faculty);
}
