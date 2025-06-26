package com.crt.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private User faculty; // The faculty who recorded attendance

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String topicTaught; // What was taught in class today

    @Column(nullable = false)
    private Integer totalStudents;

    @Column(nullable = false)
    private Integer presentCount;

    @Column(nullable = false)
    private Integer absentCount;

    @Column(nullable = false)
    private Double attendancePercentage;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        if (totalStudents != null && presentCount != null) {
            attendancePercentage = (presentCount.doubleValue() / totalStudents.doubleValue()) * 100.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (totalStudents != null && presentCount != null) {
            attendancePercentage = (presentCount.doubleValue() / totalStudents.doubleValue()) * 100.0;
        }
    }
}
