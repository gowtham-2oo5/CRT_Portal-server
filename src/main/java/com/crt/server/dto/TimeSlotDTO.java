package com.crt.server.dto;

import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TimeSlotDTO {
    private Integer id;
    private String startTime;
    private String endTime;
    private Boolean isBreak;
    private String breakDescription;
    private UUID inchargeFacultyId;
    private String inchargeFacultyName;
    private UUID sectionId;
    private String sectionName;
    private UUID roomId;
    private String roomName;
}
