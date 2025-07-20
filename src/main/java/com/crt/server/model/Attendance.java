package com.crt.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendances", indexes = {
    @Index(name = "idx_attendance_student_date", columnList = "student_id, date"),
    @Index(name = "idx_attendance_status_date", columnList = "status, date"),
    @Index(name = "idx_attendance_student_timeslot_date", columnList = "student_id, time_slot_id, date"),
    @Index(name = "idx_attendance_session", columnList = "attendance_session_id"),
    @Index(name = "idx_attendance_timeslot", columnList = "time_slot_id"),
    @Index(name = "idx_attendance_date", columnList = "date")
})
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Column
    private String feedback;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @Column(nullable = false)
    private LocalDateTime date;
    
    @ManyToOne
    @JoinColumn(name = "attendance_session_id")
    private AttendanceSession attendanceSession; // Link to the session this attendance belongs to
    
    @PrePersist
    protected void onCreate() {
        postedAt = LocalDateTime.now();
    }
}
