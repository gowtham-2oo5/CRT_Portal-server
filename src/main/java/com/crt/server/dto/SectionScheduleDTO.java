package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionScheduleDTO {
    private UUID id;
    private UUID sectionId;
    private UUID roomId;
    private List<TimeSlotDTO> timeSlots;
}