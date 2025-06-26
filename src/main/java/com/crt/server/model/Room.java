package com.crt.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String block;

    @Column(nullable = false)
    private String floor;

    @Column(nullable = false)
    private String roomNumber;

    @Column
    private String subRoom;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @Column(nullable = false)
    private Integer capacity = 0;

    @OneToMany(mappedBy = "room")
    private Set<SectionSchedule> sectionSchedules;

    @Override
    public String toString() {
        return block + floor + roomNumber + " (" + roomType + ")";
    }
}
