package com.crt.server.repository;

import com.crt.server.model.AttendanceSession;
import com.crt.server.model.User;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {
    
    // Find sessions by faculty
    List<AttendanceSession> findByFacultyOrderByDateDesc(User faculty);
    
    // Find sessions by faculty and date range
    List<AttendanceSession> findByFacultyAndDateBetweenOrderByDateDesc(User faculty, LocalDate startDate, LocalDate endDate);
    
    // Find today's sessions by faculty
    List<AttendanceSession> findByFacultyAndDate(User faculty, LocalDate date);
    
    // Find sessions by section
    List<AttendanceSession> findBySectionOrderByDateDesc(Section section);
    
    // Count today's attendance sessions by faculty
    @Query("SELECT COUNT(a) FROM AttendanceSession a WHERE a.faculty = :faculty AND a.date = :date")
    Long countTodayAttendanceByFaculty(@Param("faculty") User faculty, @Param("date") LocalDate date);
    
    // Count weekly attendance sessions by faculty
    @Query("SELECT COUNT(a) FROM AttendanceSession a WHERE a.faculty = :faculty AND a.date BETWEEN :startDate AND :endDate")
    Long countWeeklyAttendanceByFaculty(@Param("faculty") User faculty, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Check if attendance already submitted for a specific time slot and date
    boolean existsByFacultyAndTimeSlotAndDate(User faculty, TimeSlot timeSlot, LocalDate date);
    
    // Find specific attendance session
    Optional<AttendanceSession> findByFacultyAndTimeSlotAndDate(User faculty, TimeSlot timeSlot, LocalDate date);
    
    /**
     * Find attendance session by time slot and date
     * 
     * @param timeSlot The time slot
     * @param date The date
     * @return Optional attendance session
     */
    Optional<AttendanceSession> findByTimeSlotAndDate(TimeSlot timeSlot, LocalDate date);
}
