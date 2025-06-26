package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayScheduleDTO {
    private String id;
    private String day;
    private String startTime;
    private String endTime;
    private String sectionName;
    private String room;
    private boolean isActive;
}
