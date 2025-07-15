package com.crt.server.service.impl;

import com.crt.server.dto.*;
import com.crt.server.model.User;
import com.crt.server.repository.AttendanceSessionRepository;
import com.crt.server.service.FacultyDashboardService;
import com.crt.server.service.FacultyTimetableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyDashboardServiceImpl implements FacultyDashboardService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final FacultyTimetableService facultyTimetableService;

    @Override
    public FacultyDashboardDTO getFacultyDashboard(User faculty) {
        log.info("Getting dashboard data for faculty: {}", faculty.getUsername());
        
        // Get schedule data once and reuse
        List<TodayScheduleDTO> todaySchedule = facultyTimetableService.getTodaySchedule(faculty);
        List<AssignedSectionDTO> assignedSections = facultyTimetableService.getAssignedSections(faculty);
        
        // Get attendance counts in parallel (these are lightweight queries)
        Long todayCount = getTodayAttendanceCount(faculty);
        Long weeklyCount = getWeeklyAttendanceCount(faculty);
        
        return FacultyDashboardDTO.builder()
                .profile(getFacultyProfile(faculty))
                .todaySchedule(todaySchedule)
                .assignedSections(assignedSections)
                .todayAttendanceCount(todayCount)
                .weeklyAttendanceCount(weeklyCount)
                .build();
    }

    @Override
    public FacultyProfileDTO getFacultyProfile(User faculty) {
        return FacultyProfileDTO.builder()
                .id(faculty.getId().toString())
                .name(faculty.getName())
                .email(faculty.getEmail())
                .department(faculty.getBranch() != null ? faculty.getBranch().name() : null)
                .employeeId(faculty.getEmployeeId())
                .phone(faculty.getPhone())
                .build();
    }

    @Override
    public Long getTodayAttendanceCount(User faculty) {
        LocalDate today = LocalDate.now();
        return attendanceSessionRepository.countTodayAttendanceByFaculty(faculty, today);
    }

    @Override
    public Long getWeeklyAttendanceCount(User faculty) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        
        return attendanceSessionRepository.countWeeklyAttendanceByFaculty(faculty, startOfWeek, endOfWeek);
    }
}
