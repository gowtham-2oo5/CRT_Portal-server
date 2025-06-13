package com.crt.server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_slots")
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