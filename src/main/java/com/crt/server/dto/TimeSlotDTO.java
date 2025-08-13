package com.crt.server.dto;

import com.crt.server.model.TimeSlotType;
import lombok.*;

import java.time.DayOfWeek;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TimeSlotDTO {

    private Integer id;

    private UUID inchargeFacultyId;
    private UUID sectionId;
    private UUID roomId;


    private String startTime;
    private String endTime;
    
    @Builder.Default
    private TimeSlotType slotType = TimeSlotType.REGULAR;
    
    private String title; // For exams, special events, or break descriptions
    private String description; // Additional details
    private String roomName;

    private DayOfWeek dayOfWeek;

    // Display fields
    private String inchargeFacultyName;
    private String inchargeFacultyEmail;
    private String inchargeFacultyPhone;
    private String sectionName;

    // Legacy fields for backward compatibility
    private Boolean isBreak;
    private String breakDescription;

    // Helper methods for backward compatibility
    public Boolean getIsBreak() {
        return slotType == TimeSlotType.BREAK;
    }

    public void setIsBreak(Boolean isBreak) {
        if (Boolean.TRUE.equals(isBreak)) {
            this.slotType = TimeSlotType.BREAK;
        }
    }

    public String getBreakDescription() {
        return slotType == TimeSlotType.BREAK ? title : breakDescription;
    }

    public void setBreakDescription(String breakDescription) {
        if (slotType == TimeSlotType.BREAK) {
            this.title = breakDescription;
        }
        this.breakDescription = breakDescription;
    }
}
