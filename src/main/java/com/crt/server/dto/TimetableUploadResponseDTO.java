package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableUploadResponseDTO {
    private int processedSections;
    private int createdTimeSlots;
    private int skippedEntries;
    private DayOfWeek dayOfWeek;
    private LocalDate scheduleDate;
    private List<SectionTimetableDTO> sections;
    private List<String> errors;
    private List<String> warnings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionTimetableDTO {
        private String sectionName;
        private String roomCode;
        private String program;
        private List<TimeSlotSummaryDTO> timeSlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotSummaryDTO {
        private String startTime;
        private String endTime;
        private String facultyEmpId;
        private String title;
        private boolean isExam;
        private String slotType;
    }
}
