package com.crt.server.dto;

import com.crt.server.model.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentListDTO {
    private List<StudentDTO> students;
    private Integer totalCount;
    private String sectionName;
    private String sectionId;
    private TimeSlotDTO timeSlot;
}
