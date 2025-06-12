package com.crt.server.controller;

import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @PostMapping
    public ResponseEntity<SectionDTO> createSection(@RequestBody CreateSectionDTO createSectionDTO) {
        return ResponseEntity.ok(sectionService.createSection(createSectionDTO));
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
                // Assuming each line contains just the registration number
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
}