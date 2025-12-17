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

    PagedResponseDTO<StudentDTO> getStudentsPaginated(int page, int size, String sortBy, String direction);

    StudentDTO updateStudent(UUID id, StudentDTO studentDTO);

    void deleteStudent(UUID id);

    boolean existsByEmail(String email);

    boolean existsByRegNum(String regNum);

    List<StudentDTO> bulkCreateStudents(MultipartFile file) throws Exception;

    StudentDTO updateCrtEligibility(UUID studentId, Boolean crtEligibility, String reason);

    StudentDTO updateStudentSection(UUID studentId, UUID sectionId);

    StudentDTO updateStudentSectionByRegNumAndSectionName(String regNum, String sectionName);

    List<StudentAttendanceDTO> getStudentsWithAttendance(int days);

    PagedResponseDTO<StudentAttendanceDTO> getStudentsWithAttendancePaginated(int page, int size, String sortBy, String direction, int days);

    void updateStudentAttendancePercentage(UUID studentId, Double newAttdReport);
}