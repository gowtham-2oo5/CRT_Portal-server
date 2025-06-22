package com.crt.server.controller;

import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    @Autowired
    private SectionService sectionService;

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

    @PostMapping("/{sectionId}/students")
    public ResponseEntity<?> registerStudents(
            @PathVariable UUID sectionId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("File is empty")
                        .path("/api/sections/" + sectionId + "/students")
                        .build();
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(error);
            }

            List<String> regNums = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                regNums.add(line.trim());
            }
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

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<SectionDTO>> getSectionsByTrainer(@PathVariable UUID trainerId) {
        return ResponseEntity.ok(sectionService.getSectionsByTrainer(trainerId));
    }

    @PutMapping("/student/{studentId}/section/{sectionId}")
    public ResponseEntity<SectionDTO> updateStudentSection(
            @PathVariable UUID studentId,
            @PathVariable UUID sectionId) {
        return ResponseEntity.ok(sectionService.updateStudentSection(studentId, sectionId));
    }
}