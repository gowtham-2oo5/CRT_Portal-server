package com.crt.server.dto.faculty;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkFacultyResponseDTO {
    private boolean success;
    private String message;
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private List<FacultyResultDTO> results;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacultyResultDTO {
        private String email;
        private String username;
        private String status;
        private String facultyId;
        private String error;
    }
}
