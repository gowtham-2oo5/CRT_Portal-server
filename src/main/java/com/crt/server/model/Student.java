package com.crt.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "students",
        indexes = {
                @Index(name = "idx_students_branch", columnList = "branch"),
                @Index(name = "idx_students_batch", columnList = "batch"),
                @Index(name = "idx_students_regNum", columnList = "regNum"),
                @Index(name = "idx_students_section", columnList = "section_id"),
                @Index(name = "idx_students_section_active", columnList = "section_id, isActive")
        })
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
    @Builder.Default
    private String phone = "0";

    @Column(nullable = false, unique = true)
    private String regNum;

    @Column(nullable = false)
    private Branch branch;

    @Column(nullable = false)
    private String batch;

    @Column(nullable = false)
    @Builder.Default
    private Boolean crtEligibility = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(length = 500)
    private String feedback;
    
    @Column(nullable = false)
    @Builder.Default
    private Double attendancePercentage = 0.0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "section_id", nullable = true)
    private Section section;

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
