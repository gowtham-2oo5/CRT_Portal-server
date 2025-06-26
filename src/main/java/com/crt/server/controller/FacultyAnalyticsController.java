package com.crt.server.controller;

import com.crt.server.dto.AttendanceAnalyticsDTO;
import com.crt.server.dto.WeeklyTimetableDTO;
import com.crt.server.model.User;
import com.crt.server.service.CurrentUserService;
import com.crt.server.service.FacultyAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/faculty/analytics")
@RequiredArgsConstructor
@Tag(name = "Faculty Analytics", description = "Advanced analytics and reporting for faculty")
public class FacultyAnalyticsController {

    private final FacultyAnalyticsService facultyAnalyticsService;
    private final CurrentUserService currentUserService;

    @GetMapping("/attendance")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get attendance analytics", description = "Returns comprehensive attendance analytics with trends and insights")
    public ResponseEntity<AttendanceAnalyticsDTO> getAttendanceAnalytics(
            @RequestParam(required = false) UUID sectionId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Getting attendance analytics - sectionId: {}, startDate: {}, endDate: {}", 
                sectionId, startDate, endDate);
        
        User currentUser = currentUserService.getCurrentUser();
        AttendanceAnalyticsDTO analytics = facultyAnalyticsService.getAttendanceAnalytics(
                currentUser, sectionId, startDate, endDate);
        
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/timetable/weekly")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get weekly timetable", description = "Returns weekly timetable with attendance status and current/next slots")
    public ResponseEntity<WeeklyTimetableDTO> getWeeklyTimetable(
            @RequestParam(required = false) String week) {
        
        log.info("Getting weekly timetable for week: {}", week);
        
        User currentUser = currentUserService.getCurrentUser();
        WeeklyTimetableDTO timetable = facultyAnalyticsService.getWeeklyTimetable(currentUser, week);
        
        return ResponseEntity.ok(timetable);
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Export attendance report", description = "Exports attendance report as CSV file")
    public ResponseEntity<byte[]> exportAttendanceReportCSV(
            @RequestParam(required = false) UUID sectionId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Exporting attendance report CSV - sectionId: {}, startDate: {}, endDate: {}", 
                sectionId, startDate, endDate);
        
        User currentUser = currentUserService.getCurrentUser();
        byte[] csvData = facultyAnalyticsService.exportAttendanceReportCSV(
                currentUser, sectionId, startDate, endDate);
        
        String filename = String.format("attendance_report_%s_%s.csv", 
                currentUser.getUsername(), LocalDate.now().toString());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
