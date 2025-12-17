package com.crt.server.dto;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingFacultyResponseDTO {

    private Integer timeSlotId;
    private String facultyName;
    private String sectionName;
    private String trainingName;
    private String roomName;
    private String facultyId;
    private String email;
    private String phone;

}
