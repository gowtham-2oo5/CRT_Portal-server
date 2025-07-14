package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private String id; // Changed to String for consistency
    private String name;
    private String email;
    private String phone;
    private String rollNumber; // Added rollNumber field
    private String regNum;
    private String department;
    private String section; // Added section field
    private String batch;
    private Boolean crtEligibility;
    private String feedback;
    private double attendancePercentage;

}
