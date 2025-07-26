package com.crt.server.dto;

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
    private UUID sectionId;
    private UUID facultyId;
    private String facEmpId;
    private String day;
    private String sectionName;
    private String room;
    private String facultyName;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean attendancePosted;
    private boolean pastEndTime;
}
