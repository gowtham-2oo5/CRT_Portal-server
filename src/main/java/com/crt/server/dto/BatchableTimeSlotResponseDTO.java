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
public class BatchableTimeSlotResponseDTO {
    private List<BatchGroup> batchGroups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchGroup {
        private String sectionId;
        private String sectionName;
        private List<BatchableTimeSlot> batchableSlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchableTimeSlot {
        private String id;
        private String startTime;
        private String endTime;
        private String attendanceStatus; // PENDING, POSTED, MISSED
    }

    public void addBatch(com.crt.server.model.Section section, List<com.crt.server.model.TimeSlot> batch) {
        if (batchGroups == null) {
            batchGroups = new java.util.ArrayList<>();
        }

        BatchGroup batchGroup = new BatchGroup();
        batchGroup.setSectionId(section.getId().toString());
        batchGroup.setSectionName(section.getName());

        List<BatchableTimeSlot> batchableSlots = new java.util.ArrayList<>();
        for (com.crt.server.model.TimeSlot timeSlot : batch) {
            BatchableTimeSlot batchableTimeSlot = new BatchableTimeSlot();
            batchableTimeSlot.setId(timeSlot.getId().toString());
            batchableTimeSlot.setStartTime(timeSlot.getStartTime().toString());
            batchableTimeSlot.setEndTime(timeSlot.getEndTime().toString());
            batchableTimeSlot.setAttendanceStatus("PENDING");
            batchableSlots.add(batchableTimeSlot);
        }

        batchGroup.setBatchableSlots(batchableSlots);
        batchGroups.add(batchGroup);
    }
}
