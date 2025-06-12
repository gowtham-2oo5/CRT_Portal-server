package com.crt.server.dto;

import com.crt.server.model.Batch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String regNum;
    private String department;
    private Batch batch;

}
