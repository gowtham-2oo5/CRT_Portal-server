package com.crt.server.service;

import com.crt.server.dto.AttendanceDTO;
import com.crt.server.dto.AttendanceReportDTO;
import com.crt.server.dto.MarkAttendanceDTO;
import com.crt.server.dto.BulkAttendanceResponseDTO;
import com.crt.server.dto.SectionAttendanceRecordDTO;
import com.crt.server.dto.BulkAttendanceDTO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
     * @param file       The CSV file
     * @param timeSlotId The time slot ID
     * @param dateTime   The date and time of the attendance
     * @return Response containing success and failure information
     */
    BulkAttendanceResponseDTO processBulkAttendanceFile(MultipartFile file, Integer timeSlotId, String dateTime);
}