package com.crt.server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_slots", indexes = {
    @Index(name = "idx_timeslots_faculty", columnList = "incharge_faculty_id"),
    @Index(name = "idx_timeslots_section", columnList = "section_id"),
    @Index(name = "idx_timeslots_faculty_section", columnList = "incharge_faculty_id, section_id"),
    @Index(name = "idx_timeslots_schedule", columnList = "schedule_id"),
    @Index(name = "idx_timeslots_room", columnList = "room_id"),
    @Index(name = "idx_timeslots_slot_type", columnList = "slot_type"),
    @Index(name = "idx_timeslots_day_of_week", columnList = "day_of_week")
})
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    @Builder.Default
    private TimeSlotType slotType = TimeSlotType.REGULAR;

    @Column
    private String title; // For exams, special events, or break descriptions

    @Column
    private String description; // Additional details

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    // Legacy fields for backward compatibility - will be removed in future versions
    @Column(nullable = false)
    @Builder.Default
    private boolean isBreak = false;

    @Column
    private String breakDescription;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "incharge_faculty_id", nullable = false)
    private User inchargeFaculty;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private SectionSchedule schedule;

    @PrePersist
    @PreUpdate
    public void validateAndSync() {
        // Validate room capacity for non-break slots
        if (slotType != TimeSlotType.BREAK && section.getStrength() > room.getCapacity()) {
            throw new IllegalStateException("Section strength cannot exceed room capacity");
        }
        
        // Sync legacy fields for backward compatibility
        this.isBreak = (slotType == TimeSlotType.BREAK);
        if (slotType == TimeSlotType.BREAK && title != null) {
            this.breakDescription = title;
        }
    }
}
