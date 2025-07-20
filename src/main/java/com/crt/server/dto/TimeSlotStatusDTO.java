package com.crt.server.dto;

import com.crt.server.model.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotStatusDTO {
    private Integer timeSlotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private String day;
    private UUID sectionId;
    private String sectionName;
    private UUID facultyId;
    private String facultyName;
    private AttendanceStatus status;
    private boolean attendancePosted;
    private boolean pastEndTime;
}
