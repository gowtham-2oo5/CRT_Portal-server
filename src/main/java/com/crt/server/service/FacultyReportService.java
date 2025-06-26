package com.crt.server.service;

import com.crt.server.dto.FacultyAttendanceReportDTO;
import com.crt.server.dto.PagedResponseDTO;
import com.crt.server.dto.StudentAttendanceDetailDTO;
import com.crt.server.model.User;

import java.util.UUID;

public interface FacultyReportService {
    
    /**
     * Get attendance reports for faculty's sections
     */
    FacultyAttendanceReportDTO getAttendanceReports(User faculty, UUID sectionId, int limit);
    
    /**
     * Get paginated attendance reports for faculty's sections
     */
    PagedResponseDTO<FacultyAttendanceReportDTO.StudentAttendanceReportDTO> getAttendanceReportsPaged(
            User faculty, UUID sectionId, int page, int size, String startDate, String endDate);
    
    /**
     * Get detailed attendance report for a specific student
     */
    StudentAttendanceDetailDTO getStudentDetailReport(User faculty, UUID studentId);
}
