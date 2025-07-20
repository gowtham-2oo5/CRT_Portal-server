package com.crt.server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crt.server.dto.faculty.BulkFacultyRequestDTO;
import com.crt.server.dto.faculty.BulkFacultyResponseDTO;
import com.crt.server.service.BulkOperationsService;

@RestController
@RequestMapping("/api/bulk")
public class BulkOperationsController {

    @Autowired
    private BulkOperationsService bulkOperationsService;
    
    /**
     * Add multiple faculty members in a single request
     * 
     * @param request The bulk faculty request containing multiple faculty details
     * @return Response with results of the bulk operation
     */
    @PostMapping("/faculties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkFacultyResponseDTO> addBulkFaculties(
            @RequestBody BulkFacultyRequestDTO request) {
        
        BulkFacultyResponseDTO response = bulkOperationsService.addBulkFaculties(request);
        return ResponseEntity.ok(response);
    }
}
