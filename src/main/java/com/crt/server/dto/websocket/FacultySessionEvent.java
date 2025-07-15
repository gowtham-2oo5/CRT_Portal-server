package com.crt.server.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultySessionEvent {
    private String facultyId;
    private String timeSlotId;
    private String sectionId;
    private String sectionName;
    private String room;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // STARTING, ACTIVE, ENDING, ENDED
    private Integer minutesRemaining;
    private String topicTaught;
    
    // Event types
    public static final String SESSION_STARTED = "faculty_session_started";
    public static final String SESSION_ENDED = "faculty_session_ended";
    public static final String SESSION_UPDATED = "faculty_session_updated";
    public static final String NEXT_SESSION_WARNING = "faculty_next_session";
}
