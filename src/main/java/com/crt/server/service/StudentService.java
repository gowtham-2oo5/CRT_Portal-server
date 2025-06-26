package com.crt.server.service;

import com.crt.server.dto.StudentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StudentService {
    StudentDTO createStudent(StudentDTO studentDTO);

    StudentDTO getStudentById(UUID id);

    StudentDTO getStudentByEmail(String email);

    StudentDTO getStudentByRegNum(String regNum);

    List<StudentDTO> getAllStudents();

    StudentDTO updateStudent(UUID id, StudentDTO studentDTO);

    void deleteStudent(UUID id);

    boolean existsByEmail(String email);

    boolean existsByRegNum(String regNum);

    /**
     * Bulk create students from a CSV file
     * 
     * @param file The CSV file containing student data
     * @return List of created student DTOs
     * @throws Exception if there is an error processing the file or creating
     *                   students
     */
    List<StudentDTO> bulkCreateStudents(MultipartFile file) throws Exception;
}
