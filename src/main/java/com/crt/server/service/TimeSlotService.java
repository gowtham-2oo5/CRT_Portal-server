package com.crt.server.service;

import com.crt.server.dto.SectionDayScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.dto.TimeSlotValidationResponseDTO;
import com.crt.server.dto.TimetableUploadResponseDTO;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.TimeSlotType;
import com.crt.server.model.User;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface TimeSlotService {
    
    // Basic CRUD operations
    TimeSlotDTO createTimeSlot(TimeSlotDTO timeSlotDTO);
    TimeSlotDTO getTimeSlot(Integer id);
    TimeSlotDTO updateTimeSlot(Integer id, TimeSlotDTO timeSlotDTO);
    void deleteTimeSlot(Integer id);
    
    // Slot type filtering
    List<TimeSlotDTO> getTimeSlotsByType(TimeSlotType slotType);
    List<TimeSlotDTO> getSectionTimeSlotsByType(UUID sectionId, TimeSlotType slotType);
    
    // Day-based scheduling
    List<TimeSlotDTO> getTimeSlotsByDay(DayOfWeek dayOfWeek);
    List<TimeSlotDTO> getFacultyTimeSlotsByDay(UUID facultyId, DayOfWeek dayOfWeek);
    List<TimeSlotDTO> getSectionTimeSlotsByDay(UUID sectionId, DayOfWeek dayOfWeek);
    
    // Section and faculty queries
    List<TimeSlotDTO> getTimeSlotsBySection(UUID sectionId);
    List<TimeSlotDTO> getTimeSlotsByFaculty(UUID facultyId);
    List<TimeSlotDTO> getActiveTimeSlotsBySection(UUID sectionId);
    List<TimeSlotDTO> getActiveTimeSlotsByFaculty(UUID facultyId);
    
    // Validation and conflict detection
    TimeSlotValidationResponseDTO validateTimeSlot(TimeSlotDTO timeSlotDTO);
    TimeSlotValidationResponseDTO validateTimeSlotUpdate(Integer timeSlotId, TimeSlotDTO timeSlotDTO);
    List<TimeSlotDTO> getConflictingTimeSlots(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime);
    
    // Room availability
    boolean isTimeSlotAvailable(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime);
    
    // Bulk operations
    List<TimeSlotDTO> createTimeSlots(List<TimeSlotDTO> timeSlotDTOs);
    
    // Legacy methods for backward compatibility
    @Transactional(readOnly = true)
    @Cacheable(value = "facultyTimeSlots", key = "#faculty.id")
    List<TimeSlot> findByInchargeFaculty(User faculty);
    
    @Transactional(readOnly = true)
    TimeSlotDTO getTimeSlotById(Integer id);
    
    // Bulk timetable operations
    TimetableUploadResponseDTO bulkCreateTimetable(MultipartFile file) throws Exception;

    @Transactional
    TimetableUploadResponseDTO.SectionTimetableDTO processSectionTimetableTransactional(
            String sectionName, String program, String roomCode, CSVRecord record,
            int startCol, int endCol, DayOfWeek dayOfWeek, List<String> errors, List<String> warnings);

    // Section schedule queries
    SectionDayScheduleDTO getSectionScheduleByDay(String sectionName, DayOfWeek dayOfWeek);
    SectionDayScheduleDTO getSectionScheduleByDay(UUID sectionId, DayOfWeek dayOfWeek);
}
