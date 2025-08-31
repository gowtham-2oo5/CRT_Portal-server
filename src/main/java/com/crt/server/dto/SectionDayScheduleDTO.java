package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionDayScheduleDTO {
    private String sectionName;
    private String sectionId;
    private DayOfWeek dayOfWeek;
    private String roomCode;
    private String roomName;
    private List<TimeSlotDetailDTO> timeSlots;
    private int totalSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotDetailDTO {
        private Integer timeSlotId;
        private String startTime;
        private String endTime;
        private String title;
        private String description;
        private String slotType;
        private boolean isExam;
        private FacultyInfoDTO faculty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacultyInfoDTO {
        private String empId;
        private String name;
        private String email;
    }
}
