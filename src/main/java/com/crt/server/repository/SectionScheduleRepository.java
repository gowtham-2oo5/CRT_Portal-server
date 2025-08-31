package com.crt.server.repository;

import com.crt.server.model.Room;
import com.crt.server.model.Section;
import com.crt.server.model.SectionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionScheduleRepository extends JpaRepository<SectionSchedule, UUID> {
    
    // Get all schedules for a section
    List<SectionSchedule> findAllBySectionId(UUID sectionId);
    
    // Get the first schedule for a section (to handle multiple schedules)
    @Query("SELECT ss FROM SectionSchedule ss WHERE ss.section.id = :sectionId ORDER BY ss.id LIMIT 1")
    Optional<SectionSchedule> findFirstBySectionId(@Param("sectionId") UUID sectionId);
    
    // Original method - kept for backward compatibility but may throw exception if multiple exist
    Optional<SectionSchedule> findBySectionId(UUID sectionId);

    List<SectionSchedule> findBySection(Section section);
}
