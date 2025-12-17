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

    BulkAttendanceResponseDTO processBulkAttendanceFile(MultipartFile file, Integer timeSlotId, String dateTime, Boolean isAdminRequest);

    List<AbsenteeDTO> getAbsenteesByDate(LocalDate date);

    List<AbsenteeDTO> getAbsenteesByDateAndSection(LocalDate date, UUID sectionId);

    List<AbsenteeDTO> getAbsenteesByTimeSlotId(Integer timeSlotId);

    TimeSlotFilterResponseDTO getTimeSlotsByDayAndTime(LocalDate date, LocalTime startTime, LocalTime endTime);

    BulkAttendanceResponseDTO adminOverrideAttendance(AdminAttendanceRequestDTO requestDTO);

    List<AbsenteeDTO> getAbsenteesByTimeSlotIdAndDate(Integer timeSlotId, LocalDateTime date);

    List<AttendanceDTO> debugGetAllAttendanceForTimeSlotAndDate(Integer timeSlotId, LocalDateTime date);

    List<TimeSlotDTO> getPendingAttendanceTimeSlots(LocalDate date, LocalTime startTime, LocalTime endTime);
}
