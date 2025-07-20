package com.crt.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateSectionDTO {
    @JsonProperty("TrainingId")
    private String trainingId;
    private String sectionName;
}