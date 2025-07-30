package com.crt.server.service;

import com.crt.server.dto.*;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FacultyAttendanceService {

    AttendanceSessionResponseDTO submitAttendance(User faculty, AttendanceSubmissionDTO submissionDTO);


    List<StudentDTO> getStudentsForSection(UUID sectionId);


    List<StudentDTO> getStudentsForTimeSlot(Integer timeSlotId);


    boolean canSubmitAttendance(User faculty, TimeSlot timeSlot, String date);

    boolean isAttendanceAlreadySubmitted(User faculty, TimeSlot timeSlot, String date);


    AttendanceSessionDTO submitLateAttendanceReason(LateSubmissionDTO lateSubmissionDTO, User faculty);


    List<AttendanceSessionDTO> getMissedAttendanceSessions(User faculty);


    List<AttendanceSessionDTO> getMissedAttendanceSessionsByDate(User faculty, LocalDate date);
}
