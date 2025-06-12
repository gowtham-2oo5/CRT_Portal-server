package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionDTO {
    private UUID id;
    private String name;
    private CRT_TrainerDTO trainer;
    private Set<StudentDTO> students;
    private Integer strength;
}