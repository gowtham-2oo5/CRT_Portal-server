package com.crt.server.service;

import com.crt.server.dto.AttendanceSubmissionDTO;
import com.crt.server.dto.AttendanceSessionResponseDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.model.User;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;

import java.util.List;
import java.util.UUID;

public interface FacultyAttendanceService {
    
    /**
     * Submit attendance for a time slot
     */
    AttendanceSessionResponseDTO submitAttendance(User faculty, AttendanceSubmissionDTO submissionDTO);
    
    /**
     * Get students for a specific section
     */
    List<StudentDTO> getStudentsForSection(UUID sectionId);
    
    /**
     * Get students for a specific time slot session
     */
    List<StudentDTO> getStudentsForTimeSlot(Integer timeSlotId);
    
    /**
     * Validate if faculty can submit attendance for a time slot
     */
    boolean canSubmitAttendance(User faculty, TimeSlot timeSlot, String date);
    
    /**
     * Check if attendance already submitted for time slot and date
     */
    boolean isAttendanceAlreadySubmitted(User faculty, TimeSlot timeSlot, String date);
}
