package com.crt.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "section_schedules")
public class SectionSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<TimeSlot> timeSlots = new HashSet<>();

    public void addTimeSlot(TimeSlot timeSlot) {
        if (timeSlots == null) {
            timeSlots = new HashSet<>();
        }
        timeSlots.add(timeSlot);
        timeSlot.setSchedule(this);
    }

    public void removeTimeSlot(TimeSlot timeSlot) {
        if (timeSlots != null) {
            timeSlots.remove(timeSlot);
            timeSlot.setSchedule(null);
        }
    }
}