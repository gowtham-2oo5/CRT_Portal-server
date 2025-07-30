package com.crt.server.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TimeSlotDTO {

    private Integer id;

    private UUID inchargeFacultyId;
    private UUID sectionId;
    private UUID roomId;

    private String startTime;
    private String endTime;
    private String breakDescription;

    private String inchargeFacultyName;
    private String inchargeFacultyEmail;
    private String inchargeFacultyPhone;

    private String sectionName;
    private String roomName;
    private Boolean isBreak;
}
