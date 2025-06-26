package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedSectionDTO {
    private String id;
    private String name;
    private String trainerName; // Since trainer teaches, faculty records attendance
    private Integer totalStudents;
}
