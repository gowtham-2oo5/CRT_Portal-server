package com.crt.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crt.server.model.Training;
import com.crt.server.model.Section;
import com.crt.server.model.Student;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    @Query("SELECT COUNT(s) FROM Section s WHERE s.training = :training")
    long countByTraining(@Param("Training") Training training);


    @Query("SELECT t FROM Section  t WHERE t.name = :name")
    Section findByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Section t WHERE t.name = :name")
    boolean existsByName(@Param("name") String name);

    @EntityGraph(attributePaths = { "students", "Training", "room" })
    List<Section> findByTraining(@Param("Training") Training training);

    List<Section> findByStudentsContaining(Student student);
}