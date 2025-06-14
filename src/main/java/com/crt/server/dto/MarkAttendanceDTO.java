package com.crt.server.dto;

import com.crt.server.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceDTO {
    private Integer timeSlotId;
    private String dateTime;
    private List<UUID> absentStudentIds;
    private List<StudentAttendanceDTO> lateStudents;

    public LocalDateTime getParsedDateTime() {
        return DateTimeUtil.parseDateTime(dateTime);
    }
}