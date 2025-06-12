package com.crt.server.controller;

import com.crt.server.dto.SectionDTO;
import com.crt.server.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bulk")
public class BulkController {

    @Autowired
    private SectionService sectionService;

    @PostMapping("/sections/{sectionId}/students")
    public ResponseEntity<SectionDTO> registerStudentsToSection(
            @PathVariable UUID sectionId,
            @RequestParam("file") MultipartFile file) {
        try {
            List<String> regNums = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming each line contains just the registration number
                regNums.add(line.trim());
            }
            return ResponseEntity.ok(sectionService.registerStudents(sectionId, regNums));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}