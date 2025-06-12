package com.crt.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sections")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private CRT_Trainer trainer;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "section_students", joinColumns = @JoinColumn(name = "section_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    @Column(nullable = false)
    private Integer strength;

    @Column(nullable = false)
    private Integer capacity;

}