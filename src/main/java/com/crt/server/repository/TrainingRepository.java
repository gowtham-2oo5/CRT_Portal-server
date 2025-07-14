package com.crt.server.repository;

import com.crt.server.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TrainingRepository extends JpaRepository<Training, UUID> {

    @Query("SELECT t FROM Training t WHERE t.sn = :sn")
    Training findBySn(@Param("sn") String sn);

    @Query("SELECT t FROM Training  t WHERE t.name = :name")
    Training findByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Training t WHERE t.name = :name")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Training t WHERE t.sn = :sn")
    boolean existsBySn(@Param("sn") String sn);
}