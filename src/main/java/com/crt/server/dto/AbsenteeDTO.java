package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenteeDTO {
    private UUID studentId;
    private String studentName;
    private String email;
    private String phone;
    private String regNum;
    private UUID sectionId;
    private String sectionName;
    private Integer timeSlotId;
    private String startTime;
    private String endTime;
    private LocalDateTime date;
}
