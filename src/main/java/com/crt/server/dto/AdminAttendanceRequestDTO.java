package com.crt.server.dto;

import com.crt.server.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for admin attendance override functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAttendanceRequestDTO {
    private Integer timeSlotId;
    private String dateTime;
    private List<UUID> absentStudentIds;
    private List<StudentAttendanceDTO> lateStudents;
    private String overrideReason; // Reason for the override
    private UUID overriddenBy; // Admin user ID who performed the override

    public LocalDateTime getParsedDateTime() {
        return DateTimeUtil.parseDateTime(dateTime);
    }
}
