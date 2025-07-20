package com.crt.server.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crt.server.dto.faculty.BulkFacultyRequestDTO;
import com.crt.server.dto.faculty.BulkFacultyResponseDTO;
import com.crt.server.dto.faculty.BulkFacultyRequestDTO.FacultyDTO;
import com.crt.server.dto.faculty.BulkFacultyResponseDTO.FacultyResultDTO;
import com.crt.server.model.Branch;
import com.crt.server.model.Role;
import com.crt.server.model.Section;
import com.crt.server.model.User;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.UserRepository;
import com.crt.server.service.BulkOperationsService;

@Service
public class BulkOperationsServiceImpl implements BulkOperationsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public BulkFacultyResponseDTO addBulkFaculties(BulkFacultyRequestDTO request) {
        if (request.getFaculties() == null || request.getFaculties().isEmpty()) {
            return BulkFacultyResponseDTO.builder()
                    .success(false)
                    .message("No faculty data provided")
                    .totalProcessed(0)
                    .successCount(0)
                    .failureCount(0)
                    .results(new ArrayList<>())
                    .build();
        }
        
        List<FacultyResultDTO> results = new ArrayList<>();
        int successCount = 0;
        
        for (FacultyDTO facultyDTO : request.getFaculties()) {
            try {
                // Check if username or email already exists
                if (userRepository.existsByUsername(facultyDTO.getUsername())) {
                    results.add(FacultyResultDTO.builder()
                            .email(facultyDTO.getEmail())
                            .username(facultyDTO.getUsername())
                            .status("error")
                            .error("Username already exists")
                            .build());
                    continue;
                }
                
                if (userRepository.existsByEmail(facultyDTO.getEmail())) {
                    results.add(FacultyResultDTO.builder()
                            .email(facultyDTO.getEmail())
                            .username(facultyDTO.getUsername())
                            .status("error")
                            .error("Email already exists")
                            .build());
                    continue;
                }
                
                // Determine branch from department
                Branch branch;
                try {
                    branch = Branch.valueOf(facultyDTO.getDepartment().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Default to CSE if branch not found
                    branch = Branch.CSE;
                }
                
                // Create faculty user
                User faculty = User.builder()
                        .name(facultyDTO.getName())
                        .email(facultyDTO.getEmail())
                        .phone(facultyDTO.getPhone())
                        .username(facultyDTO.getUsername())
                        .password(passwordEncoder.encode(facultyDTO.getPassword()))
                        .role(Role.FACULTY)
                        .department(branch.name())
                        .isFirstLogin(true)
                        .isActive(true)
                        .build();
                
                User savedFaculty = userRepository.save(faculty);
                
                // Assign sections if provided
                if (facultyDTO.getSectionIds() != null && !facultyDTO.getSectionIds().isEmpty()) {
                    for (String sectionIdStr : facultyDTO.getSectionIds()) {
                        try {
                            UUID sectionId = UUID.fromString(sectionIdStr);
                            Optional<Section> sectionOpt = sectionRepository.findById(sectionId);
                            
                            if (sectionOpt.isPresent()) {
                                Section section = sectionOpt.get();
                                // Update section with faculty reference
                                // Note: This depends on how sections are linked to faculty in your model
                                // This is a placeholder - adjust according to your actual model
                                sectionRepository.save(section);
                            }
                        } catch (IllegalArgumentException e) {
                            // Invalid UUID format, skip this section
                        }
                    }
                }
                
                // Add success result
                results.add(FacultyResultDTO.builder()
                        .email(facultyDTO.getEmail())
                        .username(facultyDTO.getUsername())
                        .status("success")
                        .facultyId(savedFaculty.getId().toString())
                        .build());
                
                successCount++;
                
            } catch (Exception e) {
                results.add(FacultyResultDTO.builder()
                        .email(facultyDTO.getEmail())
                        .username(facultyDTO.getUsername())
                        .status("error")
                        .error(e.getMessage())
                        .build());
            }
        }
        
        int totalProcessed = request.getFaculties().size();
        int failureCount = totalProcessed - successCount;
        
        return BulkFacultyResponseDTO.builder()
                .success(failureCount == 0)
                .message(successCount > 0 ? 
                        "Successfully processed " + successCount + " faculty members" : 
                        "Failed to process any faculty members")
                .totalProcessed(totalProcessed)
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .build();
    }
}
