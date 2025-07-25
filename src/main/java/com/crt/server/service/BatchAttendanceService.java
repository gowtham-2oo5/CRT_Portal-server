package com.crt.server.service;

import com.crt.server.dto.BatchAttendanceRequestDTO;
import com.crt.server.dto.BatchAttendanceResponseDTO;
import com.crt.server.dto.BatchableTimeSlotResponseDTO;
import com.crt.server.dto.TimeSlotValidationResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling batch attendance operations
 */
public interface BatchAttendanceService {

    /**
     * Get time slots that are eligible for batch attendance submission
     * 
     * @param facultyId The faculty ID
     * @param date The date for which to retrieve batchable time slots
     * @return BatchableTimeSlotResponseDTO containing groups of batchable time slots
     */
    BatchableTimeSlotResponseDTO getBatchableTimeSlots(UUID facultyId, LocalDate date);

    /**
     * Validate if a set of time slots can be processed together in a batch
     * 
     * @param timeSlotIds List of time slot IDs to validate
     * @return TimeSlotValidationResponseDTO with validation result
     */
    TimeSlotValidationResponseDTO validateBatchTimeSlots(List<String> timeSlotIds);

    /**
     * Submit attendance for multiple time slots in a single batch operation
     * 
     * @param request BatchAttendanceRequestDTO containing attendance data
     * @return BatchAttendanceResponseDTO with results of the batch operation
     */
    BatchAttendanceResponseDTO submitBatchAttendance(BatchAttendanceRequestDTO request);
}
