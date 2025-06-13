package com.crt.server.controller;

import com.crt.server.dto.RoomDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.dto.TrainerDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.RoomService;
import com.crt.server.service.SectionService;
import com.crt.server.service.StudentService;
import com.crt.server.service.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bulk")
@RequiredArgsConstructor
public class BulkOperationsController {

    private final StudentService studentService;
    private final RoomService roomService;
    private final TrainerService trainerService;
    private final SectionService sectionService;

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

            List<String> regNums = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(studentsCSV.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                regNums.add(line.trim());
            }
            SectionDTO updatedSection = sectionService.registerStudents(sectionId, regNums);
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
}