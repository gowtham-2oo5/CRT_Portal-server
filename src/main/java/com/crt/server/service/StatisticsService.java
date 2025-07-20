package com.crt.server.service;

import com.crt.server.dto.StatisticsDTO;

/**
 * Service for fetching various statistics about the CRT Portal
 */
public interface StatisticsService {
    
    /**
     * Get the count of CRT eligible students
     * @return Count of CRT eligible students
     */
    long getCrtEligibleStudentCount();
    
    /**
     * Get the number of departments
     * @return Number of departments
     */
    long getDepartmentCount();
    
    /**
     * Get the average student attendance
     * @return Average student attendance as a percentage
     */
    double getAverageStudentAttendance();
    
    /**
     * Get all statistics in a single call
     * @return Combined statistics
     */
    StatisticsDTO getAllStatistics();
}
