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

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyDashboardServiceImpl implements FacultyDashboardService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final FacultyTimetableService facultyTimetableService;

    @Override
    public FacultyDashboardDTO getFacultyDashboard(User faculty) {
        log.info("Getting dashboard data for faculty: {}", faculty.getUsername());
        
        return FacultyDashboardDTO.builder()
                .profile(getFacultyProfile(faculty))
                .todaySchedule(facultyTimetableService.getTodaySchedule(faculty))
                .assignedSections(facultyTimetableService.getAssignedSections(faculty))
                .todayAttendanceCount(getTodayAttendanceCount(faculty))
                .weeklyAttendanceCount(getWeeklyAttendanceCount(faculty))
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
