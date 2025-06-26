package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSessionDTO {
    private boolean hasActiveSession;
    private TodayScheduleDTO currentSlot;
    private TodayScheduleDTO nextSlot; // Optional: next upcoming slot
}
