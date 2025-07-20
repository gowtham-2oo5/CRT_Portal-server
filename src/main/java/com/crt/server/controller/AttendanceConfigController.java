package com.crt.server.controller;

import com.crt.server.config.AttendanceConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance-config")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AttendanceConfigController {

    private final AttendanceConfig attendanceConfig;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAttendanceConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enforceEndTimeRestriction", attendanceConfig.isEnforceEndTimeRestriction());
        return ResponseEntity.ok(config);
    }

    @PutMapping("/enforce-end-time")
    public ResponseEntity<Map<String, Object>> setEnforceEndTimeRestriction(@RequestParam boolean enabled) {
        attendanceConfig.setEnforceEndTimeRestriction(enabled);
        
        Map<String, Object> response = new HashMap<>();
        response.put("enforceEndTimeRestriction", attendanceConfig.isEnforceEndTimeRestriction());
        response.put("message", "End time restriction " + (enabled ? "enabled" : "disabled") + " successfully");
        
        return ResponseEntity.ok(response);
    }
}
