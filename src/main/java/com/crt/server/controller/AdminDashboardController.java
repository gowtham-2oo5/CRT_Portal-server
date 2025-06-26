package com.crt.server.controller;

import com.crt.server.dto.ActivityLogDTO;
import com.crt.server.dto.DashboardMetricsDTO;
import com.crt.server.service.ActivityLogService;
import com.crt.server.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final ActivityLogService activityLogService;

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDTO> getDashboardMetrics() {
        log.info("Admin dashboard metrics requested");
        DashboardMetricsDTO metrics = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/recent-actions")
    public ResponseEntity<List<ActivityLogDTO>> getRecentActions(
            @RequestParam(defaultValue = "15") int limit) {
        log.info("Recent actions requested with limit: {}", limit);
        
        // Ensure limit is within reasonable bounds
        int actualLimit = Math.min(Math.max(limit, 1), 50);
        
        List<ActivityLogDTO> recentActions = activityLogService.getRecentActivities(actualLimit);
        return ResponseEntity.ok(recentActions);
    }

    @GetMapping("/recent-actions/default")
    public ResponseEntity<List<ActivityLogDTO>> getRecentActionsDefault() {
        log.info("Recent actions requested with default limit");
        List<ActivityLogDTO> recentActions = activityLogService.getRecentActivities();
        return ResponseEntity.ok(recentActions);
    }
}
