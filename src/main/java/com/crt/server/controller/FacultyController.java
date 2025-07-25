package com.crt.server.controller;

import com.crt.server.dto.*;
import com.crt.server.model.User;
import com.crt.server.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
@Tag(name = "Faculty Dashboard", description = "Faculty dashboard and attendance management APIs")
public class FacultyController {


    private final FacultyDashboardService facultyDashboardService;
    private final FacultyTimetableService facultyTimetableService;
    private final FacultyAttendanceService facultyAttendanceService;
    private final CurrentUserService currentUserService;
    private final UserService userService;
    private final SectionService sectionService;
    private final TimeSlotService timeSlotService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get faculty dashboard data", description = "Returns complete faculty dashboard with profile, schedule, sections, and attendance counts")
    public ResponseEntity<FacultyDashboardDTO> getFacultyDashboard(@RequestParam("id") UUID id) {
        log.info("Getting faculty dashboard data");
        User currentUser = userService.getFacById(id);
        FacultyDashboardDTO dashboard = facultyDashboardService.getFacultyDashboard(currentUser);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/timetable")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get faculty timetable", description = "Returns today's schedule with active session detection")
    public ResponseEntity<List<TodayScheduleDTO>> getFacultyTimetable(@RequestParam("id") UUID id) {
        log.info("Getting faculty timetable");
        User currentUser = userService.getFacById(id);
        List<TodayScheduleDTO> schedule = facultyTimetableService.getTodaySchedule(currentUser);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/current-session")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get current active session", description = "Returns current active time slot if any")
    public ResponseEntity<CurrentSessionDTO> getCurrentSession(@RequestParam("id") UUID id) {
        log.info("Getting current active session");
        User currentUser = userService.getFacById(id);

        return facultyTimetableService.getCurrentActiveTimeSlot(currentUser)
                .map(timeSlot -> {
                    CurrentSessionDTO currentSession = CurrentSessionDTO.builder()
                            .hasActiveSession(true)
                            .currentSlot(TodayScheduleDTO.builder()
                                    .id(timeSlot.getId().toString())
                                    .day("Today")
                                    .startTime(timeSlot.getStartTime())
                                    .endTime(timeSlot.getEndTime())
                                    .sectionName(timeSlot.getSection().getName())
                                    .room(timeSlot.getRoom().toString())
                                    .isActive(true)
                                    .build())
                            .build();
                    return ResponseEntity.ok(currentSession);
                })
                .orElse(ResponseEntity.ok(CurrentSessionDTO.builder()
                        .hasActiveSession(false)
                        .build()));
    }

    @GetMapping("/students/{sectionId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get students for section", description = "Returns list of students in a specific section for attendance")
    public ResponseEntity<StudentListDTO> getStudentsForSection(@PathVariable UUID sectionId) {
        log.info("Getting students for section: {}", sectionId);
        List<StudentDTO> students = facultyAttendanceService.getStudentsForSection(sectionId);

        StudentListDTO response = StudentListDTO.builder()
                .students(students.stream().sorted(Comparator.comparing(StudentDTO::getRegNum)).collect(Collectors.toList()))
                .totalCount(students.size())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/session/{timeSlotId}/students")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get students for time slot", description = "Returns list of students for a specific time slot session")
    public ResponseEntity<StudentListDTO> getStudentsForTimeSlot(@PathVariable Integer timeSlotId) {
        log.info("Getting students for time slot: {}", timeSlotId);
        List<StudentDTO> students = facultyAttendanceService.getStudentsForTimeSlot(timeSlotId);

        StudentListDTO response = StudentListDTO.builder()
                .students(students)
                .totalCount(students.size())
                .sectionId(sectionService.getSectionByName(students.getFirst().getSection()).getId().toString())
                .sectionName(students.getFirst().getSection())
                .timeSlot(timeSlotService.getTimeSlot(timeSlotId))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Submit attendance", description = "Submit attendance for a time slot with topic taught")
    public ResponseEntity<AttendanceSubmissionResponseDTO> submitAttendance(
            @RequestBody AttendanceSubmissionDTO submissionDTO) {
        log.info("Submitting attendance for time slot: {}", submissionDTO.getTimeSlotId());
        log.info("REQUEST: {}", submissionDTO);

        User currentUser = currentUserService.getCurrentUser();
        AttendanceSessionResponseDTO sessionResponse = facultyAttendanceService.submitAttendance(currentUser, submissionDTO);

        AttendanceSubmissionResponseDTO response = AttendanceSubmissionResponseDTO.builder()
                .success(true)
                .message("Attendance submitted successfully")
                .attendanceSession(sessionResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sections")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get assigned sections", description = "Returns all sections assigned to the faculty")
    public ResponseEntity<List<AssignedSectionDTO>> getAssignedSections(@RequestParam(required = true) UUID id) {
        log.info("Getting assigned sections for faculty");
        User currentUser = userService.getFacById(id);
        List<AssignedSectionDTO> sections = facultyTimetableService.getAssignedSections(currentUser);
        return ResponseEntity.ok(sections);
    }

    @GetMapping("/missed-sessions")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get missed attendance sessions", description = "Returns all missed attendance sessions for the faculty")
    public ResponseEntity<List<AttendanceSessionDTO>> getMissedAttendanceSessions() {
        log.info("Getting missed attendance sessions for faculty");
        User currentUser = currentUserService.getCurrentUser();
        List<AttendanceSessionDTO> missedSessions = facultyAttendanceService.getMissedAttendanceSessions(currentUser);
        return ResponseEntity.ok(missedSessions);
    }

    @GetMapping("/missed-sessions/date")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Get missed attendance sessions by date", description = "Returns all missed attendance sessions for the faculty on a specific date")
    public ResponseEntity<List<AttendanceSessionDTO>> getMissedAttendanceSessionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Getting missed attendance sessions for faculty on date: {}", date);
        User currentUser = currentUserService.getCurrentUser();
        List<AttendanceSessionDTO> missedSessions = facultyAttendanceService.getMissedAttendanceSessionsByDate(currentUser, date);
        return ResponseEntity.ok(missedSessions);
    }

    @PostMapping("/late-submission")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('FACULTY')")
    @Operation(summary = "Submit late attendance reason", description = "Submit reason for late attendance submission")
    public ResponseEntity<AttendanceSessionDTO> submitLateAttendanceReason(
            @RequestBody LateSubmissionDTO lateSubmissionDTO) {
        log.info("Submitting late attendance reason for session: {}", lateSubmissionDTO.getSessionId());
        User currentUser = currentUserService.getCurrentUser();
        AttendanceSessionDTO updatedSession = facultyAttendanceService.submitLateAttendanceReason(lateSubmissionDTO, currentUser);
        return ResponseEntity.ok(updatedSession);
    }
}
