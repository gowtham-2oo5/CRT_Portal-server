package com.crt.server.controller;

import com.crt.server.dto.*;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.UserDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.DayOfWeek;import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bulk")
@Slf4j
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

    @Autowired
    private UserService userService;

    @Autowired
    private TimeSlotService timeSlotService;

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
                    .path("/api/bulk/section/upload")
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

    @PostMapping(value = "/faculties", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addBulkFaculties(
            @RequestParam("file") MultipartFile file) throws Exception {
        List<UserDTO> response = userService.bulkUploadFacs(file);
        return ResponseEntity.ok("Processed" + response.size() + " Faculties");
    }

    @PostMapping(value = "/timetable/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> bulkUploadTimetable(@RequestParam("file") MultipartFile file) {
        try {
            TimetableUploadResponseDTO response = timeSlotService.bulkCreateTimetable(file);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (Exception e) {
            log.error("Error in bulk timetable upload: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/bulk/timetable/upload")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @GetMapping("/section/{sectionName}/schedule")
    public ResponseEntity<?> getSectionSchedule(
            @PathVariable String sectionName,
            @RequestParam DayOfWeek dayOfWeek) {
        try {
            SectionDayScheduleDTO schedule = timeSlotService.getSectionScheduleByDay(sectionName, dayOfWeek);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error fetching section schedule: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/bulk/section/" + sectionName + "/schedule")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }
}
