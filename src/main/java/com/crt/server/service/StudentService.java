package com.crt.server.service;

import com.crt.server.dto.PagedResponseDTO;
import com.crt.server.dto.StudentAttendanceDTO;
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

    /**
     * Get paginated list of students
     *
     * @param page      Page number (0-based)
     * @param size      Page size
     * @param sortBy    Field to sort by
     * @param direction Sort direction (ASC or DESC)
     * @return Paginated response with student DTOs
     */
    PagedResponseDTO<StudentDTO> getStudentsPaginated(int page, int size, String sortBy, String direction);

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

    /**
     * Update CRT eligibility status for a student
     *
     * @param studentId      The ID of the student
     * @param crtEligibility The new CRT eligibility status
     * @param reason         The reason for the status change
     * @return Updated student DTO
     */
    StudentDTO updateCrtEligibility(UUID studentId, Boolean crtEligibility, String reason);

    /**
     * Update a student's section
     *
     * @param studentId The ID of the student
     * @param sectionId The ID of the new section
     * @return Updated student DTO
     */
    StudentDTO updateStudentSection(UUID studentId, UUID sectionId);

    StudentDTO updateStudentSectionByRegNumAndSectionName(String regNum, String sectionName);

    /**
     * Get students with their attendance data
     *
     * @param days Number of days to consider for attendance calculation
     * @return List of students with attendance data
     */
    List<StudentAttendanceDTO> getStudentsWithAttendance(int days);

    /**
     * Get paginated list of students with their attendance data
     *
     * @param page      Page number (0-based)
     * @param size      Page size
     * @param sortBy    Field to sort by
     * @param direction Sort direction (ASC or DESC)
     * @param days      Number of days to consider for attendance calculation
     * @return Paginated response with students and their attendance data
     */
    PagedResponseDTO<StudentAttendanceDTO> getStudentsWithAttendancePaginated(int page, int size, String sortBy, String direction, int days);
    
    /**
     * Update a student's attendance percentage based on their attendance records
     *
     * @param studentId The ID of the student
     * @return The updated attendance percentage
     */
    Double updateStudentAttendancePercentage(UUID studentId);
}