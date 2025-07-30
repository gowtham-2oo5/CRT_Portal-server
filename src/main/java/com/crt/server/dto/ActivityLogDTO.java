package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    
    private String action;
    private LocalDateTime timestamp;
    
    private String facultyId;
    private String facultyName;
    private String sectionName;
    private String timeSlotInfo;
    private Integer absentCount;
}
