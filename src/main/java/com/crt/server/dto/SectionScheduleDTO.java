package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionScheduleDTO {
    private UUID id;
    private SectionDTO section;
    private RoomDTO room;
    private Set<TimeSlotDTO> timeSlots;
    private boolean isActive;
}