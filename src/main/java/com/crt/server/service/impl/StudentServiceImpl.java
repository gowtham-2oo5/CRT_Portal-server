package com.crt.server.service.impl;

import com.crt.server.dto.StudentDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Branch;
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
import java.util.Objects;
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
                .branch(Branch.valueOf(studentDTO.getDepartment().toUpperCase()))
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
        student.setBranch(Branch.valueOf(studentDTO.getDepartment()));
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
        String[] headers = {"ID","NAME","EMAIL","BRANCH"};

        try {
            List<Student> students = csvService.parseCsv(file, headers, record -> {
                try {
                    validateRequiredFields(record);
                } catch (Exception e) {
                    System.err.println(e.getLocalizedMessage());
                }
                if(studentRepository.existsByRegNum(record.get("ID"))) return null;
                return Student.builder()
                        .name(record.get("NAME"))
                        .email(record.get("EMAIL"))
                        .phone("000")
                        .regNum(record.get("ID"))
                        .branch(Branch.valueOf(record.get("BRANCH").toUpperCase()))
                        .batch(getStudentBatch(record.get("ID")))
                        .crtEligibility(true)
                        .build();
            }).stream().filter(Objects::nonNull).collect(Collectors.toList());;

            List<Student> savedStudents = studentRepository.saveAll(students);
            return savedStudents.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in bulk student creation: {}", e.getMessage());
            throw new Exception("Error processing student data: " + e.getMessage());
        }
    }

    private String getStudentBatch(String regNum) {
        return "Y" + regNum.charAt(0) + regNum.charAt(1);
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
        String[] requiredFields = {"ID","NAME","EMAIL","BRANCH"};
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new Exception("Required field '" + field + "' is missing or empty");
            }
        }

        // Validate email format
        String email = record.get("EMAIL");
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Invalid email format for: " + email);
        }
    }

    private StudentDTO convertToDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId().toString()) // Convert UUID to String
                .name(student.getName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .regNum(student.getRegNum())
                .department(String.valueOf(student.getBranch()))
                .batch(student.getBatch())
                .crtEligibility(student.getCrtEligibility())
                .feedback(student.getFeedback())
                .build();
    }
}
