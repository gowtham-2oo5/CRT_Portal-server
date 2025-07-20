package com.crt.server.service;

import com.crt.server.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    List<AttendanceDTO> markAttendance(MarkAttendanceDTO markAttendanceDTO);

    BulkAttendanceResponseDTO markBulkAttendance(BulkAttendanceDTO bulkAttendanceDTO);

    List<AttendanceDTO> getStudentAttendance(UUID studentId, LocalDateTime startDate, LocalDateTime endDate);

    List<AttendanceDTO> getTimeSlotAttendance(Integer timeSlotId, LocalDateTime date);

    AttendanceReportDTO getStudentAttendanceReport(UUID studentId, LocalDateTime startDate, LocalDateTime endDate);

    void archiveAttendanceRecords(int year, int month);

    List<AttendanceDTO> getArchivedStudentAttendance(UUID studentId, LocalDateTime startDate, LocalDateTime endDate);

    AttendanceReportDTO getArchivedStudentAttendanceReport(UUID studentId, LocalDateTime startDate,
            LocalDateTime endDate);

    List<SectionAttendanceRecordDTO> getSectionAttendanceRecords(UUID sectionId, LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Process a CSV file containing bulk attendance data
     * 
     * @param file           The CSV file
     * @param timeSlotId     The time slot ID
     * @param dateTime       The date and time of the attendance
     * @param isAdminRequest Flag indicating if the request is from an admin (allows updating existing attendance)
     * @return Response containing success and failure information
     */
    BulkAttendanceResponseDTO processBulkAttendanceFile(MultipartFile file, Integer timeSlotId, String dateTime, Boolean isAdminRequest);
    
    /**
     * Get all absentees for a specific date across all sections
     * 
     * @param date The date to check for absentees
     * @return List of absentee DTOs
     */
    List<AbsenteeDTO> getAbsenteesByDate(LocalDate date);
    
    /**
     * Get all absentees for a specific date in a specific section
     * 
     * @param date      The date to check for absentees
     * @param sectionId The section ID
     * @return List of absentee DTOs
     */
    List<AbsenteeDTO> getAbsenteesByDateAndSection(LocalDate date, UUID sectionId);

    List<AbsenteeDTO> getAbsenteesByTimeSlotId(Integer timeSlotId);
    
    /**
     * Get time slots filtered by day, start time, and end time
     * 
     * @param date The date to filter by
     * @param startTime Optional start time to filter by (can be null)
     * @param endTime Optional end time to filter by (can be null)
     * @return Response containing time slot status information
     */
    TimeSlotFilterResponseDTO getTimeSlotsByDayAndTime(LocalDate date, LocalTime startTime, LocalTime endTime);
}
