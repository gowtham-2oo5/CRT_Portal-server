package com.crt.server.repository;

import com.crt.server.model.Attendance;
import com.crt.server.model.Student;
import com.crt.server.model.TimeSlot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

        List<Attendance> findByStudentAndDateBetween(Student student, LocalDateTime startDate, LocalDateTime endDate);

        List<Attendance> findByTimeSlotAndDate(TimeSlot timeSlot, LocalDateTime date);

        Optional<Attendance> findByStudentAndTimeSlotAndDate(Student student, TimeSlot timeSlot, LocalDateTime date);

        // FIX: Add method to find records by date range for archiving
        List<Attendance> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

        @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.date BETWEEN :startDate AND :endDate")
        long countAttendanceByStudentAndDateRange(
                        @Param("student") Student student,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.status = 'ABSENT' AND a.date BETWEEN :startDate AND :endDate")
        long countAbsencesByStudentAndDateRange(
                        @Param("student") Student student,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // FIX: Add batch processing methods to prevent memory issues
        @Query("SELECT a FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate ORDER BY a.date")
        List<Attendance> findByDateBetweenWithPagination(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        // FIX: Add method to count records for pagination
        @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate")
        long countByDateBetween(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}
