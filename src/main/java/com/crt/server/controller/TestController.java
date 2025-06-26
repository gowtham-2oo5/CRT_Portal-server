package com.crt.server.controller;

import com.crt.server.dto.ActivityLogDTO;
import com.crt.server.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class TestController {

    private final ActivityLogService activityLogService;

    @PostMapping("/add-sample-activity")
    public ResponseEntity<String> addSampleActivity() {
        // Create a sample activity log entry
        ActivityLogDTO sampleLog = ActivityLogDTO.builder()
                .action("TEST_USER - Test Faculty posted attendance for Java Basics for 09:00-10:30 and 85.5%")
                .timestamp(LocalDateTime.now())
                .facultyId("TEST_USER")
                .facultyName("Test Faculty")
                .sectionName("Java Basics")
                .timeSlotInfo("09:00-10:30")
                .attendancePercentage(85.5)
                .build();

        // Manually add to the activity log (for testing purposes)
        // Note: This is a hack for testing - normally this would be done through attendance posting
        try {
            // We can't directly add to the service, so let's return a message
            return ResponseEntity.ok("Sample activity would be added. Use attendance posting to generate real logs.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<List<ActivityLogDTO>> getTestActivities() {
        List<ActivityLogDTO> activities = activityLogService.getRecentActivities(10);
        return ResponseEntity.ok(activities);
    }
}
