package com.crt.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "students")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true)
    private String regNum;

    @Column(nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Batch batch;

    @Column(nullable = false)
    @Builder.Default
    private Boolean crtEligibility = true;

    @Column(length = 500)
    private String feedback;

    @ManyToMany(mappedBy = "students")
    private Set<Section> sections;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student that = (Student) o;
        return regNum != null && regNum.equals(that.regNum);
    }

    @Override
    public int hashCode() {
        return regNum != null ? regNum.hashCode() : 0;
    }
}
