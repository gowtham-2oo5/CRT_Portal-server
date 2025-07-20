package com.crt.server.repository;

import com.crt.server.model.Section;
import com.crt.server.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    @Query("SELECT s FROM Student s WHERE s.email = :email")
    Student findByEmail(@Param("email") String email);

    @Query("SELECT s FROM Student s WHERE s.regNum = :regNum")
    Student findByRegNum(@Param("regNum") String regNum);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.regNum = :regNum")
    boolean existsByRegNum(@Param("regNum") String regNum);

    List<Student> findByIdIn(List<UUID> ids);

    List<Student> findByRegNumIn(List<String> regNums);

    Long countByCrtEligibility(Boolean crtEligibility);

    List<Student> findBySection(Section section);

    long countBySection(Section section);

    @Query("SELECT s, " +
            "(SELECT COUNT(a) FROM Attendance a WHERE a.student = s AND a.date BETWEEN :startDate AND :endDate) as totalAttendance, " +
            "(SELECT COUNT(a) FROM Attendance a WHERE a.student = s AND a.status = 'PRESENT' AND a.date BETWEEN :startDate AND :endDate) as presentCount " +
            "FROM Student s")
    List<Object[]> findStudentsWithAttendanceData(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Modifying
    @Query("UPDATE Student s SET s.attendancePercentage = :percentage WHERE s.id = :studentId")
    void updateAttendancePercentage(@Param("studentId") UUID studentId, @Param("percentage") Double percentage);
}