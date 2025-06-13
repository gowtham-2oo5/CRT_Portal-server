package com.crt.server.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {
    private Integer id;
    private Integer title;
    private String startTime;
    private String endTime;
    private boolean isBreak;
    private String breakDescription;
    private UUID inchargeFacultyId;
    private UUID sectionId;
    private UUID roomId;
}