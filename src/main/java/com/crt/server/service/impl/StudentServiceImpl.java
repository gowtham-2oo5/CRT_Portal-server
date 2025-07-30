package com.crt.server.service.impl;

import com.crt.server.dto.PagedResponseDTO;
import com.crt.server.dto.StudentAttendanceDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Branch;
import com.crt.server.model.Section;
import com.crt.server.model.Student;
import com.crt.server.repository.AttendanceRepository;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.StudentRepository;
import com.crt.server.service.CsvService;
import com.crt.server.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final SectionRepository sectionRepository;
    private final AttendanceRepository attendanceRepository;
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

        // Get default section if needed
        Section defaultSection = getOrCreateDefaultSection();

        Student student = Student.builder()
                .name(studentDTO.getName())
                .email(studentDTO.getEmail())
                .phone(studentDTO.getPhone())
                .regNum(studentDTO.getRegNum())
                .branch(Branch.valueOf(studentDTO.getDepartment().toUpperCase()))
                .batch(studentDTO.getBatch())
                .crtEligibility(studentDTO.getCrtEligibility() != null ? studentDTO.getCrtEligibility() : true)
                .feedback(studentDTO.getFeedback())
                .section(defaultSection)
                .isActive(true)
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
            throw new ResourceNotFoundException("Student not found with registration number: %s".formatted(regNum));
        }
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .sorted((s1, s2) -> s1.getRegNum().compareToIgnoreCase(s2.getRegNum()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponseDTO<StudentDTO> getStudentsPaginated(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Student> studentPage = studentRepository.findAll(pageable);
        List<StudentDTO> studentDTOs = studentPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PagedResponseDTO.<StudentDTO>builder()
                .content(studentDTOs)
                .page(studentPage.getNumber())
                .size(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .first(studentPage.isFirst())
                .last(studentPage.isLast())
                .hasNext(studentPage.hasNext())
                .hasPrevious(studentPage.hasPrevious())
                .build();
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
        String[] headers = {"ID", "NAME", "EMAIL", "BRANCH"};

        try {
            List<Student> students = csvService.parseCsv(file, headers, record -> {
                try {
                    validateRequiredFields(record);
                } catch (Exception e) {
                    System.err.println(e.getLocalizedMessage());
                }
                if (studentRepository.existsByRegNum(record.get("ID"))) return null;

                // Get default section
                Section defaultSection = getOrCreateDefaultSection();

                return Student.builder()
                        .name(record.get("NAME"))
                        .email(record.get("EMAIL"))
                        .phone("000")
                        .regNum(record.get("ID"))
                        .branch(Branch.valueOf(record.get("BRANCH").toUpperCase()))
                        .batch(getStudentBatch(record.get("ID")))
                        .crtEligibility(true)
                        .section(defaultSection)
                        .isActive(true)
                        .build();
            }).stream().filter(Objects::nonNull).collect(Collectors.toList());

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

    @Override
    public StudentDTO updateStudentSection(UUID studentId, UUID sectionId) {
        log.info("Updating section for student ID: {} to section ID: {}", studentId, sectionId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + sectionId));

        student.setSection(section);
        Student updatedStudent = studentRepository.save(student);

        log.info("Updated section for student {} (ID: {}) to section {} (ID: {})",
                student.getRegNum(), studentId, section.getName(), sectionId);

        return convertToDTO(updatedStudent);
    }

    @Override
    public StudentDTO updateStudentSectionByRegNumAndSectionName(String regNum, String sectionName) {
        log.info("Updating section for student with reg number: {} to section: {}", regNum, sectionName);

        Student student = studentRepository.findByRegNum(regNum);
        if (student == null) {
            throw new ResourceNotFoundException("Student not found with registration number: " + regNum);
        }

        Section section = sectionRepository.findByName(sectionName);
        if (section == null) {
            throw new ResourceNotFoundException("Section not found with name: " + sectionName);
        }

        student.setSection(section);
        Student updatedStudent = studentRepository.save(student);

        log.info("Updated section for student {} to section {} (ID: {})",
                regNum, sectionName, section.getId());

        return convertToDTO(updatedStudent);
    }

    private void validateRequiredFields(org.apache.commons.csv.CSVRecord record) throws Exception {
        String[] requiredFields = {"ID", "NAME", "EMAIL", "BRANCH"};
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
                .section(student.getSection() != null ? student.getSection().getName() : null)
                .attendancePercentage(student.getAttendancePercentage())
                .build();
    }

    private Section getOrCreateDefaultSection() {
        List<Section> sections = sectionRepository.findAll();
        if (sections.isEmpty()) {
            log.info("No sections found. Creating default section...");
            return null; // We'll handle this case in the controller
        } else {
            return sections.getFirst();
        }
    }

    @Override
    public List<StudentAttendanceDTO> getStudentsWithAttendance(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Student> students = studentRepository.findAll();
        List<StudentAttendanceDTO> result = new ArrayList<>();

        for (Student student : students) {
            long totalAttendance = attendanceRepository.countAttendanceByStudentAndDateRange(student, startDate, endDate);
            long presentCount = totalAttendance - attendanceRepository.countAbsencesByStudentAndDateRange(student, startDate, endDate);
            double attendancePercentage = totalAttendance > 0 ? ((double) presentCount / totalAttendance) * 100.0 : 0.0;

            StudentAttendanceDTO dto = StudentAttendanceDTO.builder()
                    .id(student.getId().toString())
                    .name(student.getName())
                    .email(student.getEmail())
                    .regNum(student.getRegNum())
                    .department(student.getBranch().toString())
                    .section(student.getSection() != null ? student.getSection().getName() : null)
                    .batch(student.getBatch())
                    .crtEligibility(student.getCrtEligibility())
                    .totalAttendance(totalAttendance)
                    .presentCount(presentCount)
                    .attendancePercentage(attendancePercentage)
                    .build();

            result.add(dto);
        }

        return result;
    }

    @Override
    public PagedResponseDTO<StudentAttendanceDTO> getStudentsWithAttendancePaginated(int page, int size, String sortBy, String direction, int days) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Student> studentPage = studentRepository.findAll(pageable);
        List<StudentAttendanceDTO> studentDTOs = new ArrayList<>();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        for (Student student : studentPage.getContent()) {
            long totalAttendance = attendanceRepository.countAttendanceByStudentAndDateRange(student, startDate, endDate);
            long presentCount = totalAttendance - attendanceRepository.countAbsencesByStudentAndDateRange(student, startDate, endDate);
            double attendancePercentage = totalAttendance > 0 ? ((double) presentCount / totalAttendance) * 100.0 : 0.0;

            StudentAttendanceDTO dto = StudentAttendanceDTO.builder()
                    .id(student.getId().toString())
                    .name(student.getName())
                    .email(student.getEmail())
                    .regNum(student.getRegNum())
                    .department(student.getBranch().toString())
                    .section(student.getSection() != null ? student.getSection().getName() : null)
                    .batch(student.getBatch())
                    .crtEligibility(student.getCrtEligibility())
                    .totalAttendance(totalAttendance)
                    .presentCount(presentCount)
                    .attendancePercentage(attendancePercentage)
                    .build();

            studentDTOs.add(dto);
        }

        return PagedResponseDTO.<StudentAttendanceDTO>builder()
                .content(studentDTOs)
                .page(studentPage.getNumber())
                .size(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .first(studentPage.isFirst())
                .last(studentPage.isLast())
                .hasNext(studentPage.hasNext())
                .hasPrevious(studentPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public void updateStudentAttendancePercentage(UUID studentId, Double newAttdReport) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        student.setAttendancePercentage(newAttdReport);
        studentRepository.save(student);

    }
}
