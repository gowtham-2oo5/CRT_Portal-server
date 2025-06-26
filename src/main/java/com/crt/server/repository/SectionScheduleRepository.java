package com.crt.server.repository;

import com.crt.server.model.SectionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionScheduleRepository extends JpaRepository<SectionSchedule, UUID> {
    Optional<SectionSchedule> findBySectionId(UUID sectionId);
}