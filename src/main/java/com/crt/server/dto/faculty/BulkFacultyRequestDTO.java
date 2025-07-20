package com.crt.server.dto.faculty;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkFacultyRequestDTO {
    private List<FacultyDTO> faculties;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacultyDTO {
        private String name;
        private String email;
        private String phone;
        private String username;
        private String password;
        private String department;
        private List<String> sectionIds; // Optional: Sections to assign to the faculty
    }
}
