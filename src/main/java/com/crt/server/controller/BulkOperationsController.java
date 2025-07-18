package com.crt.server.controller;

import com.crt.server.dto.*;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.*;
import com.crt.server.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bulk")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class BulkOperationsController {


    @Autowired
    private StudentService studentService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private TrainingService TrainingService;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping(value = "/students/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> bulkUploadStudents(@RequestParam("file") MultipartFile file) {
        try {
            List<StudentDTO> uploadedStudents = studentService.bulkCreateStudents(file);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Processed " + uploadedStudents.size() + " students");
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
                    .body("Processed " + createdRooms.size() + " rooms");
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

    @PostMapping(value = "/Trainings/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> bulkUploadTrainings(@RequestParam("file") MultipartFile file) {
        try {
            List<TrainingDTO> uploadedTrainings = TrainingService.bulkCreateTrainings(file);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Processed " + uploadedTrainings.size() + " Trainings");
        } catch (Exception e) {
            log.error("Error in bulk Training upload: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/bulk/Trainings/upload")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @PostMapping(value = "/section/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> bulkUploadSections(@RequestParam("file") MultipartFile file) {
        try {
            List<SectionDTO> uploadedSections = sectionService.bulkCreateSections(file);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Processed " + uploadedSections.size() + " sections");
        } catch (Exception e) {
            log.error("Error in bulk Training upload: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/bulk/Trainings/upload")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @PostMapping(value = "/register-students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerStudentsToSections(@RequestParam("file") MultipartFile file) {

        try {
            List<SectionDTO> sections = sectionService.bulkRegisterStudentsToSections(file);
            StringBuilder res = new StringBuilder();
            for (SectionDTO section : sections) {
                res.append(section.getName()).append(" : ").append(section.getStudents().size());
                res.append("\n");
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Registration info: " + res);
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

//    @PostMapping("/attendance/mark")
//    @PreAuthorize("hasAuthority('FACULTY')")
//    public ResponseEntity<BulkAttendanceResponseDTO> markBulkAttendance(
//            @RequestBody BulkAttendanceDTO bulkAttendanceDTO) {
//
//        BulkAttendanceDTO safeBulkAttendanceDTO = createSafeBulkAttendanceDTO(bulkAttendanceDTO);
//        BulkAttendanceResponseDTO res = attendanceService.markBulkAttendance(safeBulkAttendanceDTO)
//        return ResponseEntity.ok(res);
//    }

//    @PostMapping("/attendance/upload")
//    @PreAuthorize("hasAuthority('FACULTY')")
//    public ResponseEntity<BulkAttendanceResponseDTO> uploadBulkAttendance(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("timeSlotId") Integer timeSlotId,
//            @RequestParam("dateTime") String dateTime) {
//        return ResponseEntity.ok(attendanceService.processBulkAttendanceFile(file, timeSlotId, dateTime));
//    }

    /**
     */
//    private BulkAttendanceDTO createSafeBulkAttendanceDTO(BulkAttendanceDTO original) {
//        if (original == null) {
//            return null;
//        }
//
//        BulkAttendanceDTO safe = new BulkAttendanceDTO();
//        safe.setTimeSlotId(original.getTimeSlotId());
//        safe.setDateTime(original.getDateTime());
//
//        // Create defensive copies of collections
//        if (original.getAbsentStudentIds() != null) {
//            safe.setAbsentStudentIds(CollectionUtils.defensiveCopy(original.getAbsentStudentIds()));
//        }
//
//        if (original.getLateStudents() != null) {
//            safe.setLateStudents(CollectionUtils.defensiveCopy(original.getLateStudents()));
//        }
//
//        return safe;
//    }
}
