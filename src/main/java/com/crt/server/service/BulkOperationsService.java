package com.crt.server.service;

import com.crt.server.dto.faculty.BulkFacultyRequestDTO;
import com.crt.server.dto.faculty.BulkFacultyResponseDTO;

public interface BulkOperationsService {
    
    /**
     * Add multiple faculty members in a single operation
     * 
     * @param request The bulk faculty request containing multiple faculty details
     * @return Response with results of the bulk operation
     */
    BulkFacultyResponseDTO addBulkFaculties(BulkFacultyRequestDTO request);
}
