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
@Table(name = "attendance_marking_logs")
public class AttendanceMarkingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private User faculty;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private LocalDateTime markedAt;

    @Column(nullable = false)
    private Integer totalStudents;

    @Column(nullable = false)
    private Integer presentCount;

    @Column(nullable = false)
    private Integer absentCount;

    @Column(nullable = false, precision = 5, scale = 2)
    private Integer attendancePercentage;

    @Column(length = 500)
    private String topicTaught;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @PrePersist
    protected void onCreate() {
        markedAt = LocalDateTime.now();
        calculateAttendanceMetrics();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateAttendanceMetrics();
    }

    private void calculateAttendanceMetrics() {
        if (totalStudents != null && presentCount != null) {
            absentCount = totalStudents - presentCount;
            if (totalStudents > 0) {
                attendancePercentage = (presentCount / totalStudents) * 100;
            } else {
                attendancePercentage = 0;
            }
        }
    }
}
