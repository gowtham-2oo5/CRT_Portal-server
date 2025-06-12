package com.crt.server.repository;

import com.crt.server.model.CRT_Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TrainerRepository extends JpaRepository<CRT_Trainer, UUID> {
    @Query("SELECT t FROM CRT_Trainer t WHERE t.email = :email")
    CRT_Trainer findByEmail(@Param("email") String email);

    @Query("SELECT t FROM CRT_Trainer t WHERE t.sn = :sn")
    CRT_Trainer findBySn(@Param("sn") String sn);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM CRT_Trainer t WHERE t.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM CRT_Trainer t WHERE t.sn = :sn")
    boolean existsBySn(@Param("sn") String sn);
}