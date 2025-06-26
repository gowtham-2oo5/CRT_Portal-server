package com.crt.server.service.impl;

import com.crt.server.dto.StudentDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Batch;
import com.crt.server.model.Student;
import com.crt.server.repository.StudentRepository;
import com.crt.server.service.CsvService;
import com.crt.server.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final CsvService csvService;

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {
        // Check if email or registration number already exists
        if (existsByEmail(studentDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (existsByRegNum(studentDTO.getRegNum())) {
            throw new RuntimeException("Registration number already exists");
        }

        Student student = Student.builder()
                .name(studentDTO.getName())
                .email(studentDTO.getEmail())
                .phone(studentDTO.getPhone())
                .regNum(studentDTO.getRegNum())
                .department(studentDTO.getDepartment())
                .batch(studentDTO.getBatch())
                .crtEligibility(studentDTO.getCrtEligibility() != null ? studentDTO.getCrtEligibility() : true)
                .feedback(studentDTO.getFeedback())
                .build();

        Student savedStudent = studentRepository.save(student);
        return convertToDTO(savedStudent);
    }

    @Override
    public StudentDTO getStudentById(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return convertToDTO(student);
    }

    @Override
    public StudentDTO getStudentByEmail(String email) {
        try {
            Student student = studentRepository.findByEmail(email);
            return convertToDTO(student);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Student not found with email: " + email);
        }
    }

    @Override
    public StudentDTO getStudentByRegNum(String regNum) {
        try {
            Student student = studentRepository.findByRegNum(regNum);
            return convertToDTO(student);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Student not found with registration number: " + regNum);
        }
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StudentDTO updateStudent(UUID id, StudentDTO studentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Update student fields
        student.setName(studentDTO.getName());
        student.setEmail(studentDTO.getEmail());
        student.setPhone(studentDTO.getPhone());
        student.setRegNum(studentDTO.getRegNum());
        student.setDepartment(studentDTO.getDepartment());
        student.setBatch(studentDTO.getBatch());
        
        // Update CRT eligibility and feedback if provided
        if (studentDTO.getCrtEligibility() != null) {
            student.setCrtEligibility(studentDTO.getCrtEligibility());
        }
        if (studentDTO.getFeedback() != null) {
            student.setFeedback(studentDTO.getFeedback());
        }

        Student updatedStudent = studentRepository.save(student);
        return convertToDTO(updatedStudent);
    }

    @Override
    public void deleteStudent(UUID id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found");
        }
        studentRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByRegNum(String regNum) {
        return studentRepository.existsByRegNum(regNum);
    }

    @Override
    public List<StudentDTO> bulkCreateStudents(MultipartFile file) throws Exception {
        String[] headers = { "name", "email", "phone", "regNum", "department", "batch" };

        try {
            List<Student> students = csvService.parseCsv(file, headers, record -> {
                // Validate required fields
                try {
                    validateRequiredFields(record);
                } catch (Exception e) {
                    System.err.println(e.getLocalizedMessage());
                }

                // Create and return Student entity
                return Student.builder()
                        .name(record.get("name"))
                        .email(record.get("email"))
                        .phone(record.get("phone"))
                        .regNum(record.get("regNum"))
                        .department(record.get("department"))
                        .batch(Batch.valueOf(record.get("batch").trim()))
                        .crtEligibility(true) // Default to true for bulk imports
                        .build();
            });

            // Save all students in a single transaction
            List<Student> savedStudents = studentRepository.saveAll(students);
            return savedStudents.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in bulk student creation: {}", e.getMessage());
            throw new Exception("Error processing student data: " + e.getMessage());
        }
    }

    @Override
    public StudentDTO updateCrtEligibility(UUID studentId, Boolean crtEligibility, String reason) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        student.setCrtEligibility(crtEligibility);
        student.setFeedback(reason);

        Student updatedStudent = studentRepository.save(student);
        log.info("Updated CRT eligibility for student {} (ID: {}) to {} with reason: {}", 
                student.getRegNum(), studentId, crtEligibility, reason);

        return convertToDTO(updatedStudent);
    }

    private void validateRequiredFields(org.apache.commons.csv.CSVRecord record) throws Exception {
        String[] requiredFields = { "name", "email", "phone", "regNum", "department", "batch" };
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new Exception("Required field '" + field + "' is missing or empty");
            }
        }

        // Validate email format
        String email = record.get("email");
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Invalid email format for: " + email);
        }

        // Validate batch value
        try {
            String batchValue = record.get("batch").trim();
            Batch.valueOf(batchValue);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid batch value: " + record.get("batch") + ". Valid values are: " +
                    String.join(", ", Batch.values().toString()));
        }
    }

    private StudentDTO convertToDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId().toString()) // Convert UUID to String
                .name(student.getName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .regNum(student.getRegNum())
                .department(student.getDepartment())
                .batch(student.getBatch())
                .crtEligibility(student.getCrtEligibility())
                .feedback(student.getFeedback())
                .build();
    }
}
