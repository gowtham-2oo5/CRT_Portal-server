package com.crt.server.controller;

import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.CsvService;
import com.crt.server.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @Autowired
    private CsvService csvService;

    @PostMapping
    public ResponseEntity<SectionDTO> createSection(@RequestBody CreateSectionDTO createSectionDTO) {
        return ResponseEntity.ok(sectionService.createSection(createSectionDTO));
    }

    @GetMapping("/{sectionId}")
    public ResponseEntity<SectionDTO> getSection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(sectionService.getSection(sectionId));
    }

    @GetMapping
    public ResponseEntity<List<SectionDTO>> getAllSections() {
        return ResponseEntity.ok(sectionService.getAllSections());
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<SectionDTO> updateSection(
            @PathVariable UUID sectionId,
            @RequestBody CreateSectionDTO updateSectionDTO) {
        return ResponseEntity.ok(sectionService.updateSection(sectionId, updateSectionDTO));
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID sectionId) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{sectionId}/students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerStudents(
            @PathVariable UUID sectionId,
            @RequestParam("studentsCSV") MultipartFile studentsCSV) {
        try {
            if (studentsCSV.isEmpty()) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("CSV file is empty")
                        .path("/api/sections/" + sectionId + "/students")
                        .build();
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(error);
            }

            // Use CsvService to parse CSV and extract regNum field
            String[] headers = {"regNum"};
            List<String> regNums = csvService.parseCsv(studentsCSV, headers, record -> record.get("regNum"));
            
            if (regNums.isEmpty()) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("No valid registration numbers found in the CSV file")
                        .path("/api/sections/" + sectionId + "/students")
                        .build();
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(error);
            }
            
            System.out.println("regNums: " + regNums);
            return ResponseEntity.ok(sectionService.registerStudents(sectionId, regNums));
            
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/sections/" + sectionId + "/students")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @GetMapping("/Training/{trainingId}")
    public ResponseEntity<List<SectionDTO>> getSectionsByTraining(@PathVariable UUID trainingId) {
        return ResponseEntity.ok(sectionService.getSectionsByTraining(trainingId));
    }

    @PutMapping("/student/{studentId}/section/{sectionId}")
    public ResponseEntity<SectionDTO> updateStudentSection(
            @PathVariable UUID studentId,
            @PathVariable UUID sectionId) {
        return ResponseEntity.ok(sectionService.updateStudentSection(studentId, sectionId));
    }
}
