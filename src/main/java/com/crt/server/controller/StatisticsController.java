package com.crt.server.controller;

import com.crt.server.dto.StatisticsDTO;
import com.crt.server.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for fetching various statistics about the CRT Portal
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Get the count of CRT eligible students
     * @return Count of CRT eligible students
     */
    @GetMapping("/crt-eligible-count")
    public ResponseEntity<Long> getCrtEligibleStudentCount() {
        log.info("Fetching CRT eligible student count");
        return ResponseEntity.ok(statisticsService.getCrtEligibleStudentCount());
    }

    /**
     * Get the number of departments
     * @return Number of departments
     */
    @GetMapping("/department-count")
    public ResponseEntity<Long> getDepartmentCount() {
        log.info("Fetching department count");
        return ResponseEntity.ok(statisticsService.getDepartmentCount());
    }

    /**
     * Get the average student attendance
     * @return Average student attendance as a percentage
     */
    @GetMapping("/avg-attendance")
    public ResponseEntity<Double> getAverageStudentAttendance() {
        log.info("Fetching average student attendance");
        return ResponseEntity.ok(statisticsService.getAverageStudentAttendance());
    }
    
    /**
     * Get all statistics in a single call
     * @return Combined statistics
     */
    @GetMapping
    public ResponseEntity<StatisticsDTO> getAllStatistics() {
        log.info("Fetching all statistics");
        return ResponseEntity.ok(statisticsService.getAllStatistics());
    }
}
