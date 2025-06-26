package com.crt.server.service;

import com.crt.server.dto.DashboardMetricsDTO;

public interface DashboardService {
    
    /**
     * Get dashboard metrics including counts of all major entities
     * 
     * @return Dashboard metrics with counts
     */
    DashboardMetricsDTO getDashboardMetrics();
}
