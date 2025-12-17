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
 * DTO for marking attendance for adjacent time slots at once
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjacentTimeSlotAttendanceDTO {
    private List<Integer> timeSlotIds; // List of adjacent time slot IDs (should be 2)
    private String dateTime; // Date and time for the attendance
    private List<UUID> absentStudentIds; // Students absent in both time slots
    private List<StudentAttendanceDTO> lateStudents; // Students late in either time slot
    private String topicTaught; // Topic taught during these sessions
    private Boolean isAdminRequest; // Flag to indicate if this is an admin request

    public LocalDateTime getParsedDateTime() {
        return DateTimeUtil.parseDateTime(dateTime);
    }
}
