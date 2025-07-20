package com.crt.server.repository;

import com.crt.server.model.TimeSlotTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotTemplateRepository extends JpaRepository<TimeSlotTemplate, UUID> {
    @Query("SELECT t FROM TimeSlotTemplate t WHERE t.name = :templateName")
    Optional<TimeSlotTemplate> findByName(@Param("templateName") String templateName);
}
