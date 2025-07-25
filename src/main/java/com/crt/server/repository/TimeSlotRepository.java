package com.crt.server.repository;

import com.crt.server.model.Room;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {
    List<TimeSlot> findBySection(Section section);

    List<TimeSlot> findByInchargeFaculty(User inchargeFaculty);

    // Optimized query with eager loading to prevent N+1 queries
    @Query("SELECT ts FROM TimeSlot ts " +
           "JOIN FETCH ts.section s " +
           "JOIN FETCH s.training " +
           "JOIN FETCH ts.room " +
           "WHERE ts.inchargeFaculty = :faculty")
    List<TimeSlot> findByInchargeFacultyWithDetails(@Param("faculty") User faculty);

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
            
    List<TimeSlot> findByInchargeFacultyAndIsBreakFalse(User faculty);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.id = :timeSlot1Id OR ts.id = :timeSlot2Id")
    List<TimeSlot> findByIds(@Param("timeSlot1Id") Integer timeSlot1Id, @Param("timeSlot2Id") Integer timeSlot2Id);

    @Query("SELECT ts1, ts2 FROM TimeSlot ts1, TimeSlot ts2 " +
            "WHERE ts1.inchargeFaculty = :faculty AND ts2.inchargeFaculty = :faculty " +
            "AND ts1.endTime = ts2.startTime AND ts1.section = ts2.section AND ts1.room = ts2.room")
    List<Object[]> findAdjacentTimeSlotsByFaculty(@Param("faculty") User faculty);

    @Query("SELECT CASE WHEN COUNT(ts1) > 0 THEN true ELSE false END FROM TimeSlot ts1, TimeSlot ts2 " +
            "WHERE ts1.id = :timeSlot1Id AND ts2.id = :timeSlot2Id " +
            "AND ts1.endTime = ts2.startTime AND ts1.section = ts2.section AND ts1.room = ts2.room")
    boolean areTimeSlotsAdjacent(@Param("timeSlot1Id") Integer timeSlot1Id, @Param("timeSlot2Id") Integer timeSlot2Id);
}