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
public class TimeSlotFilterResponseDTO {
    private int totalTimeSlots;
    private int postedAttendanceCount;
    private int pendingAttendanceCount;
    private List<TimeSlotStatusDTO> timeSlots;
    private List<FacultyDTO> facultiesWithPendingAttendance;
}
