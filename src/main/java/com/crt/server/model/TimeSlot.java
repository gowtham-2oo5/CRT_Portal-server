package com.crt.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_slots", indexes = {
    @Index(name = "idx_timeslots_faculty", columnList = "incharge_faculty_id"),
    @Index(name = "idx_timeslots_faculty_break", columnList = "incharge_faculty_id, isBreak"),
    @Index(name = "idx_timeslots_section", columnList = "section_id"),
    @Index(name = "idx_timeslots_faculty_section", columnList = "incharge_faculty_id, section_id"),
    @Index(name = "idx_timeslots_schedule", columnList = "schedule_id"),
    @Index(name = "idx_timeslots_room", columnList = "room_id")
})
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @Column(nullable = false)
    private boolean isBreak;

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
    public void validateRoomCapacity() {
        if (!isBreak && section.getStrength() > room.getCapacity()) {
            throw new IllegalStateException("Section strength cannot exceed room capacity");
        }
    }
}
