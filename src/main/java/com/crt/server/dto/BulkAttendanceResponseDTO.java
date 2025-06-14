package com.crt.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceResponseDTO {
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private List<AttendanceDTO> successfulRecords;
    private List<String> errors;
}