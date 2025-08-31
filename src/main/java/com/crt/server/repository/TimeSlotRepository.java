package com.crt.server.repository;

import com.crt.server.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {
    
    // Find by slot type
    List<TimeSlot> findBySlotType(TimeSlotType slotType);
    List<TimeSlot> findBySectionIdAndSlotType(UUID sectionId, TimeSlotType slotType);
    
    // Find by day of week
    List<TimeSlot> findByDayOfWeek(DayOfWeek dayOfWeek);
    List<TimeSlot> findBySectionIdAndDayOfWeek(UUID sectionId, DayOfWeek dayOfWeek);
    List<TimeSlot> findByInchargeFacultyIdAndDayOfWeek(UUID facultyId, DayOfWeek dayOfWeek);
    
    // Room conflict detection
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.room.id = :roomId " +
           "AND ts.dayOfWeek = :dayOfWeek " +
           "AND ((ts.startTime < :endTime AND ts.endTime > :startTime)) " +
           "AND (:excludeId IS NULL OR ts.id != :excludeId)")
    List<TimeSlot> findConflictingTimeSlots(
            @Param("roomId") UUID roomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("excludeId") Integer excludeId);
    
    // Faculty conflict detection
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.inchargeFaculty.id = :facultyId " +
           "AND ts.dayOfWeek = :dayOfWeek " +
           "AND ((ts.startTime < :endTime AND ts.endTime > :startTime)) " +
           "AND (:excludeId IS NULL OR ts.id != :excludeId)")
    List<TimeSlot> findFacultyConflictingTimeSlots(
            @Param("facultyId") UUID facultyId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("excludeId") Integer excludeId);
    
    // Find by section
    List<TimeSlot> findBySectionId(UUID sectionId);
    List<TimeSlot> findBySection(Section section);
    
    // Find by faculty
    List<TimeSlot> findByInchargeFacultyId(UUID facultyId);
    List<TimeSlot> findByInchargeFaculty(User inchargeFaculty);
    
    // Find by room
    List<TimeSlot> findByRoomId(UUID roomId);
    
    // Find active time slots (non-break by default, or all if needed)
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.section.id = :sectionId AND ts.slotType != 'BREAK'")
    List<TimeSlot> findActiveBySectionId(@Param("sectionId") UUID sectionId);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.inchargeFaculty.id = :facultyId AND ts.slotType != 'BREAK'")
    List<TimeSlot> findActiveByFacultyId(@Param("facultyId") UUID facultyId);
    
    // Room availability check
    @Query("SELECT COUNT(ts) > 0 FROM TimeSlot ts WHERE ts.room.id = :roomId " +
           "AND ts.dayOfWeek = :dayOfWeek " +
           "AND ((ts.startTime < :endTime AND ts.endTime > :startTime))")
    boolean existsConflictingTimeSlot(
            @Param("roomId") UUID roomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    // Legacy methods for backward compatibility
    @Query("SELECT ts FROM TimeSlot ts " +
           "JOIN FETCH ts.section s " +
           "JOIN FETCH s.training " +
           "JOIN FETCH ts.room " +
           "WHERE ts.inchargeFaculty = :faculty")
    List<TimeSlot> findByInchargeFacultyWithDetails(@Param("faculty") User faculty);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.section = :section AND ts.slotType != 'BREAK'")
    List<TimeSlot> findActiveTimeSlotsBySection(@Param("section") Section section);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.inchargeFaculty = :faculty AND ts.slotType != 'BREAK'")
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
    
    // Methods for bulk timetable operations
    List<TimeSlot> findBySectionAndDayOfWeek(Section section, DayOfWeek dayOfWeek);
    
    List<TimeSlot> findByScheduleAndDayOfWeek(SectionSchedule schedule, DayOfWeek dayOfWeek);
}
