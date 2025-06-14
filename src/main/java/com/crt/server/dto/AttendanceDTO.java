package com.crt.server.dto;

import com.crt.server.model.AttendanceStatus;
import com.crt.server.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private UUID id;
    private UUID studentId;
    private Integer timeSlotId;
    private AttendanceStatus status;
    private String feedback;
    private String postedAt;
    private String date;

    public static AttendanceDTO fromLocalDateTime(AttendanceDTO dto, LocalDateTime postedAt, LocalDateTime date) {
        dto.setPostedAt(DateTimeUtil.formatDateTime(postedAt));
        dto.setDate(DateTimeUtil.formatDateTime(date));
        return dto;
    }
}