package com.crt.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crt.server.model.CRT_Trainer;
import com.crt.server.model.Section;
import com.crt.server.model.Student;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    @Query("SELECT COUNT(s) FROM Section s WHERE s.trainer = :trainer")
    long countByTrainer(@Param("trainer") CRT_Trainer trainer);

    @EntityGraph(attributePaths = { "students", "trainer", "room" })
    List<Section> findByTrainer(@Param("trainer") CRT_Trainer trainer);

    List<Section> findByStudentsContaining(Student student);
}