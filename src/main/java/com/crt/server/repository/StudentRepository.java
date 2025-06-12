package com.crt.server.repository;

import com.crt.server.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}