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
public class WeeklyTimetableDTO {
    private List<WeeklyScheduleDTO> weeklyTimetable;
    private TodayScheduleDTO currentSlot;
    private TodayScheduleDTO nextSlot;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyScheduleDTO {
        private String id;
        private String day;
        private String startTime;
        private String endTime;
        private String sectionName;
        private String room;
        private boolean attendanceSubmitted;
        private String topicTaught;
    }
}
