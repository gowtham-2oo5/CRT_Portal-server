package com.crt.server.repository;

import com.crt.server.model.AttendanceArchive;
import com.crt.server.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceArchiveRepository extends JpaRepository<AttendanceArchive, UUID> {

    List<AttendanceArchive> findByStudentAndDateBetween(Student student, LocalDateTime startDate,
            LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM AttendanceArchive a WHERE a.student = :student AND a.date BETWEEN :startDate AND :endDate")
    long countAttendanceByStudentAndDateRange(
            @Param("student") Student student,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM AttendanceArchive a WHERE a.student = :student AND a.status = 'ABSENT' AND a.date BETWEEN :startDate AND :endDate")
    long countAbsencesByStudentAndDateRange(
            @Param("student") Student student,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Method required by AttendanceControllerIntegrationTest
    List<AttendanceArchive> findByStudentIdAndDateBetween(UUID studentId, LocalDateTime startDate, LocalDateTime endDate);
}
