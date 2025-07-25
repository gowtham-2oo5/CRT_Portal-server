package com.crt.server.repository;

import com.crt.server.dto.AbsenteeDTO;
import com.crt.server.model.Attendance;
import com.crt.server.model.AttendanceSession;
import com.crt.server.model.Student;
import com.crt.server.model.AttendanceStatus;
import com.crt.server.model.TimeSlot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.timeSlot IN :timeSlots")
    Long countByStudentAndTimeSlotIn(@Param("student") Student student, @Param("timeSlots") List<TimeSlot> timeSlots);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.status = :status AND a.timeSlot IN :timeSlots")
    Long countByStudentAndStatusAndTimeSlotIn(@Param("student") Student student, @Param("status") AttendanceStatus status, @Param("timeSlots") List<TimeSlot> timeSlots);

    Optional<Attendance> findTopByStudentAndStatusOrderByDateDesc(Student student, AttendanceStatus status);

    List<Attendance> findByStudentAndTimeSlotInOrderByDateDesc(Student student, List<TimeSlot> timeSlots);

    // Methods required by AttendanceControllerIntegrationTest
    List<Attendance> findByStudentIdAndDateBetween(UUID studentId, LocalDateTime startDate, LocalDateTime endDate);

    List<Attendance> findByTimeSlotIdAndDate(Integer timeSlotId, LocalDateTime date);

    Long countByStudentIdAndStatusAndDateBetween(UUID studentId, AttendanceStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Attendance a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month")
    List<Attendance> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT s.id, s.name, s.regNum, a.status, ts.startTime, a.date " +
           "FROM Attendance a " +
           "JOIN a.student s " +
           "JOIN a.timeSlot ts " +
           "WHERE ts.section.id = :sectionId AND a.date BETWEEN :startDate AND :endDate " +
           "ORDER BY a.date DESC")
    List<Object[]> findSectionAttendanceRecords(@Param("sectionId") UUID sectionId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
                                               
    // Methods for fetching absentees
    @Query("SELECT a FROM Attendance a WHERE a.status = 'ABSENT' AND CAST(a.date AS LocalDate) = :date")
    List<Attendance> findAbsenteesByDate(@Param("date") LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.status = 'ABSENT' AND CAST(a.date AS LocalDate) = :date AND a.timeSlot.section.id = :sectionId")
    List<Attendance> findAbsenteesByDateAndSection(@Param("date") LocalDate date, @Param("sectionId") UUID sectionId);
    
    List<Attendance> findByStatusAndTimeSlotAndDate(AttendanceStatus status, TimeSlot timeSlot, LocalDateTime date);
    
    /**
     * Check if attendance has been posted for a time slot on a specific date
     * 
     * @param timeSlot The time slot
     * @param date The date
     * @return True if attendance has been posted, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a WHERE a.timeSlot = :timeSlot AND CAST(a.date AS LocalDate) = :date")
    boolean existsByTimeSlotAndDate(@Param("timeSlot") TimeSlot timeSlot, @Param("date") LocalDate date);
    
    /**
     * Find faculties who haven't posted attendance for their time slots on a specific date
     * 
     * @param date The date
     * @return List of faculty IDs who haven't posted attendance
     */
    @Query("SELECT DISTINCT ts.inchargeFaculty.id FROM TimeSlot ts WHERE ts.id NOT IN " +
           "(SELECT a.timeSlot.id FROM Attendance a WHERE CAST(a.date AS LocalDate) = :date)")
    List<UUID> findFacultiesWithPendingAttendance(@Param("date") LocalDate date);
    
    /**
     * Find attendance records by attendance session
     * 
     * @param attendanceSession The attendance session
     * @return List of attendance records
     */
    List<Attendance> findByAttendanceSession(AttendanceSession attendanceSession);

    @Query("SELECT a FROM Attendance a WHERE a.status = 'ABSENT' AND a.timeSlot = :timeSlot")
    List<Attendance> getAbsenteesByTimeSlot(@Param("timeSlot") TimeSlot timeSlot);

    @Query("SELECT a FROM Attendance a WHERE a.status = 'ABSENT' AND a.timeSlot = :timeSlot AND DATE(a.date) = DATE(:date)")
    List<Attendance> getAbsenteesByTimeSlotAndDate(@Param("timeSlot") TimeSlot timeSlot, @Param("date") LocalDateTime date);
    
    // FIX: Add batch processing for archiving to prevent OutOfMemoryError
    @Query("SELECT a FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate ORDER BY a.id")
    List<Attendance> findByDateBetweenOrderById(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);
                        
    // DEBUG: Method to help troubleshoot absentee queries
    @Query("SELECT a FROM Attendance a WHERE a.timeSlot.id = :timeSlotId AND DATE(a.date) = DATE(:date)")
    List<Attendance> findByTimeSlotIdAndDateDebug(@Param("timeSlotId") Integer timeSlotId, @Param("date") LocalDateTime date);
}
