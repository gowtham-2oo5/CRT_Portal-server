package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyProfileDTO {
    private String id;
    private String name;
    private String email;
    private String department;
    private String employeeId;
    private String phone;
}
