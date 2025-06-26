package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDashboardDTO {
    private FacultyProfileDTO profile;
    private List<TodayScheduleDTO> todaySchedule;
    private List<AssignedSectionDTO> assignedSections;
    private Long todayAttendanceCount;
    private Long weeklyAttendanceCount;
}
