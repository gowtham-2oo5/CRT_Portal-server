package com.crt.server.repository;

import com.crt.server.model.Room;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {
    List<TimeSlot> findBySection(Section section);

    List<TimeSlot> findByInchargeFaculty(User inchargeFaculty);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.section = :section AND ts.isBreak = false")
    List<TimeSlot> findActiveTimeSlotsBySection(@Param("section") Section section);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.inchargeFaculty = :faculty AND ts.isBreak = false")
    List<TimeSlot> findActiveTimeSlotsByFaculty(@Param("faculty") User faculty);

    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END FROM TimeSlot ts " +
            "WHERE ts.section = :section AND ts.startTime = :startTime AND ts.endTime = :endTime")
    boolean existsBySectionAndTime(
            @Param("section") Section section,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END FROM TimeSlot ts " +
            "WHERE ts.room = :room AND ts.startTime = :startTime AND ts.endTime = :endTime")
    boolean existsByRoomAndTime(
            @Param("room") Room room,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END FROM TimeSlot ts " +
            "WHERE ts.id = :timeSlotId AND ts.schedule.id = :scheduleId")
    boolean existsByIdAndScheduleId(
            @Param("timeSlotId") Integer timeSlotId,
            @Param("scheduleId") UUID scheduleId);


}