package com.crt.server.controller;

import com.crt.server.dto.*;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.EmailService;
import com.crt.server.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    @Autowired
    private StudentService studentService;
    
    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody StudentDTO studentDTO) {
        try {
            StudentDTO response = studentService.createStudent(studentDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.CONFLICT.value())
                    .error(HttpStatus.CONFLICT.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/students")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable UUID id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<StudentDTO> getStudentByEmail(@PathVariable String email) {
        return ResponseEntity.ok(studentService.getStudentByEmail(email));
    }

    @GetMapping("/regnum/{regNum}")
    public ResponseEntity<StudentDTO> getStudentByRegNum(@PathVariable String regNum) {
        return ResponseEntity.ok(studentService.getStudentByRegNum(regNum));
    }

    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<PagedResponseDTO<StudentDTO>> getStudentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "regNum") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        return ResponseEntity.ok(studentService.getStudentsPaginated(page, size, sortBy, direction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable UUID id, @RequestBody StudentDTO studentDTO) {
        try {
            StudentDTO response = studentService.updateStudent(id, studentDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/students/" + id)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/remove-from-crt")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> removeStudentFromCrt(
            @PathVariable UUID id,
            @RequestParam String reason) {
        try {
            StudentDTO response = studentService.updateCrtEligibility(id, false, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/students/" + id + "/remove-from-crt")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }

    @PostMapping("/{id}/add-to-crt")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addStudentToCrt(
            @PathVariable UUID id,
            @RequestParam String reason) {
        try {
            StudentDTO response = studentService.updateCrtEligibility(id, true, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/students/" + id + "/add-to-crt")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }
    
    @GetMapping("/with-attendance")
    public ResponseEntity<List<StudentAttendanceDTO>> getStudentsWithAttendance(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(studentService.getStudentsWithAttendance(days));
    }
    
    @GetMapping("/with-attendance/paginated")
    public ResponseEntity<PagedResponseDTO<StudentAttendanceDTO>> getStudentsWithAttendancePaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(studentService.getStudentsWithAttendancePaginated(page, size, sortBy, direction, days));
    }
    
    @PostMapping("/send-mail-in-bulk")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BulkEmailResponseDTO> sendMailInBulk(@RequestBody BulkEmailRequestDTO request) {
        int sentCount = emailService.sendBulkEmail(request.getSubject(), request.getBody(), request.getEmailIds());
        
        BulkEmailResponseDTO response = BulkEmailResponseDTO.builder()
                .message("Mail has been sent to " + sentCount + " students.")
                .content(request.getBody())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
