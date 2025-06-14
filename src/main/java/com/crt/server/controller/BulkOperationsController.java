package com.crt.server.controller;

import com.crt.server.dto.BulkAttendanceDTO;
import com.crt.server.dto.BulkAttendanceResponseDTO;
import com.crt.server.dto.RoomDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.dto.TrainerDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.AttendanceService;
import com.crt.server.service.RoomService;
import com.crt.server.service.SectionService;
import com.crt.server.service.StudentService;
import com.crt.server.service.TrainerService;
import com.crt.server.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/bulk")
@RequiredArgsConstructor
public class BulkOperationsController {

    private final StudentService studentService;
    private final RoomService roomService;
    private final TrainerService trainerService;
    private final SectionService sectionService;
    private final AttendanceService attendanceService;

    @PostMapping(value = "/students/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> bulkUploadStudents(@RequestParam("file") MultipartFile file) {
        try {
            List<StudentDTO> uploadedStudents = studentService.bulkCreateStudents(file);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(uploadedStudents);
        } catch (Exception e) {
            log.error("Error in bulk student upload: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/bulk/students/upload")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @PostMapping(value = "/simple-room/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> bulkCreateRoomsSimple(@RequestParam("file") MultipartFile file) {
        log.info("Bulk creating rooms from simple format file: {}", file.getOriginalFilename());
        try {
            List<RoomDTO> createdRooms = roomService.bulkCreateRoomsFromSimpleFormat(file);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdRooms);
        } catch (Exception e) {
            log.error("Error in bulk room creation from simple format: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/bulk/simple-room/upload")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @PostMapping(value = "/trainers/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> bulkUploadTrainers(@RequestParam("file") MultipartFile file) {
        try {
            List<TrainerDTO> uploadedTrainers = trainerService.bulkCreateTrainers(file);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(uploadedTrainers);
        } catch (Exception e) {
            log.error("Error in bulk trainer upload: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/bulk/trainers/upload")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @PostMapping(value = "/register-students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerStudentsToSection(
            @RequestParam("sectionId") UUID sectionId,
            @RequestParam("studentsCSV") MultipartFile studentsCSV) {

        // FIX: Use try-with-resources and thread-safe collections
        try {
            if (studentsCSV.isEmpty()) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("File is empty")
                        .path("/api/bulk/register-students")
                        .build();
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(error);
            }

            // FIX: Use thread-safe collection and proper resource management
            List<String> regNums = Collections.synchronizedList(new ArrayList<>());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(studentsCSV.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        regNums.add(trimmedLine);
                    }
                }
            }

            // FIX: Create defensive copy before passing to service
            List<String> safeRegNums = CollectionUtils.defensiveCopy(regNums);
            SectionDTO updatedSection = sectionService.registerStudents(sectionId, safeRegNums);
            return ResponseEntity.ok(updatedSection);

        } catch (Exception e) {
            log.error("Error in bulk student registration: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/bulk/register-students")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @PostMapping("/attendance/mark")
    @PreAuthorize("hasAuthority('FACULTY')")
    public ResponseEntity<BulkAttendanceResponseDTO> markBulkAttendance(
            @RequestBody BulkAttendanceDTO bulkAttendanceDTO) {

        // FIX: Create defensive copies of input collections to prevent ConcurrentModificationException
        BulkAttendanceDTO safeBulkAttendanceDTO = createSafeBulkAttendanceDTO(bulkAttendanceDTO);

        return ResponseEntity.ok(attendanceService.markBulkAttendance(safeBulkAttendanceDTO));
    }

    @PostMapping("/attendance/upload")
    @PreAuthorize("hasAuthority('FACULTY')")
    public ResponseEntity<BulkAttendanceResponseDTO> uploadBulkAttendance(
            @RequestParam("file") MultipartFile file,
            @RequestParam("timeSlotId") Integer timeSlotId,
            @RequestParam("dateTime") String dateTime) {
        return ResponseEntity.ok(attendanceService.processBulkAttendanceFile(file, timeSlotId, dateTime));
    }

    /**
     * Creates a safe copy of BulkAttendanceDTO with defensive copies of all collections
     */
    private BulkAttendanceDTO createSafeBulkAttendanceDTO(BulkAttendanceDTO original) {
        if (original == null) {
            return null;
        }

        BulkAttendanceDTO safe = new BulkAttendanceDTO();
        safe.setTimeSlotId(original.getTimeSlotId());
        safe.setDateTime(original.getDateTime());

        // Create defensive copies of collections
        if (original.getAbsentStudentIds() != null) {
            safe.setAbsentStudentIds(CollectionUtils.defensiveCopy(original.getAbsentStudentIds()));
        }

        if (original.getLateStudents() != null) {
            safe.setLateStudents(CollectionUtils.defensiveCopy(original.getLateStudents()));
        }

        return safe;
    }
}
