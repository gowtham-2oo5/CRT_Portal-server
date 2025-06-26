package com.crt.server.controller;

import com.crt.server.dto.FacultyAttendanceReportDTO;
import com.crt.server.dto.PagedResponseDTO;
import com.crt.server.dto.StudentAttendanceDetailDTO;
import com.crt.server.model.User;
import com.crt.server.service.CurrentUserService;
import com.crt.server.service.FacultyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/faculty/reports")
@RequiredArgsConstructor
@Tag(name = "Faculty Reports", description = "Faculty attendance reports and analytics")
public class FacultyReportController {

    private final FacultyReportService facultyReportService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get attendance reports", description = "Returns paginated attendance reports for faculty's sections")
    public ResponseEntity<PagedResponseDTO<FacultyAttendanceReportDTO.StudentAttendanceReportDTO>> getAttendanceReports(
            @RequestParam(required = false) UUID sectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("Getting attendance reports for faculty - page: {}, size: {}", page, size);
        
        User currentUser = currentUserService.getCurrentUser();
        PagedResponseDTO<FacultyAttendanceReportDTO.StudentAttendanceReportDTO> report = 
                facultyReportService.getAttendanceReportsPaged(currentUser, sectionId, page, size, startDate, endDate);
        
        return ResponseEntity.ok(report);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get student detail report", description = "Returns detailed attendance report for a specific student")
    public ResponseEntity<StudentAttendanceDetailDTO> getStudentDetailReport(@PathVariable UUID studentId) {
        log.info("Getting detailed report for student: {}", studentId);
        
        User currentUser = currentUserService.getCurrentUser();
        StudentAttendanceDetailDTO report = facultyReportService.getStudentDetailReport(currentUser, studentId);
        
        return ResponseEntity.ok(report);
    }
}
