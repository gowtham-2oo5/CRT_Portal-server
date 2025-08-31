package com.crt.server.service.impl;

import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.dto.TrainingDTO;
import com.crt.server.model.Section;
import com.crt.server.model.Student;
import com.crt.server.model.Training;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.StudentRepository;
import com.crt.server.repository.TrainingRepository;
import com.crt.server.service.CsvService;
import com.crt.server.service.SectionService;
import com.crt.server.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SectionServiceImpl implements SectionService {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CsvService csvService;

    @Override
    @Transactional
    @CacheEvict(value = {"sectionDetails", "studentsBySection"}, allEntries = true)
    public SectionDTO createSection(CreateSectionDTO createSectionDTO) {
        log.debug("Creating section with DTO: {}", createSectionDTO);

        Training training = trainingRepository.findById(UUID.fromString(createSectionDTO.getTrainingId()))
                .orElseThrow(() -> new RuntimeException("Training not found"));

        Section section = Section.builder()
                .name(createSectionDTO.getSectionName())
                .training(training)
                .strength(0) // Default value
                .capacity(30) // Default value
                .build();

        Section savedSection = sectionRepository.save(section);
        return mapToDTO(savedSection);
    }

    @Transactional(readOnly = true)
    public SectionDTO getSection(UUID sectionId) {
        return getSectionById(sectionId);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionDTO getSectionById(UUID id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        return mapToDTO(section);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionDTO> getAllSections() {
        log.debug("Getting all sections");
        List<SectionDTO> sections = new ArrayList<>( sectionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));

        sections.sort((t1, t2) -> {
            String name1 = t1.getName();
            String name2 = t2.getName();

            // Extract numbers from the names
            Integer num1 = extractNumber(name1);
            Integer num2 = extractNumber(name2);

            // If both have numbers, compare numerically
            if (num1 != null && num2 != null) {
                return Integer.compare(num1, num2);
            }

            // Fallback to alphabetical comparison
            return name1.compareToIgnoreCase(name2);
        });

        return sections;
    }
    private Integer extractNumber(String str) {
        if (str == null) return null;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("-(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"sectionDetails", "studentsBySection"}, allEntries = true)
    public SectionDTO updateSection(UUID id, CreateSectionDTO updateSectionDTO) {
        log.debug("Updating section with ID: {} and DTO: {}", id, updateSectionDTO);

        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Training training = trainingRepository.findById(UUID.fromString(updateSectionDTO.getTrainingId()))
                .orElseThrow(() -> new RuntimeException("Training not found"));

        section.setName(updateSectionDTO.getSectionName());
        section.setTraining(training);
        // Keep existing strength and capacity values

        Section updatedSection = sectionRepository.save(section);
        return mapToDTO(updatedSection);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"sectionDetails", "studentsBySection"}, allEntries = true)
    public void deleteSection(UUID id) {
        log.debug("Deleting section with ID: {}", id);

        if (!sectionRepository.existsById(id)) {
            throw new RuntimeException("Section not found");
        }
        sectionRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"sectionDetails", "studentsBySection"}, allEntries = true)
    public SectionDTO registerStudents(UUID sectionId, List<String> regNums) {
        log.debug("Registering students with reg numbers {} to section {}", regNums, sectionId);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        List<Student> students = studentRepository.findByRegNumIn(regNums);

        if (students.size() != regNums.size()) {
            List<String> foundRegNums = students.stream()
                    .map(Student::getRegNum)
                    .collect(Collectors.toList());

            List<String> notFoundRegNums = regNums.stream()
                    .filter(regNum -> !foundRegNums.contains(regNum))
                    .collect(Collectors.toList());

            throw new RuntimeException("Some students not found: " + String.join(", ", notFoundRegNums));
        }

        // Update each student's section
        for (Student student : students) {
            student.setSection(section);
            studentRepository.save(student);
        }

        // Update section strength
        section.setStrength(section.getStudents().size());
        sectionRepository.save(section);

        return mapToDTO(section);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"sectionDetails", "studentsBySection"}, allEntries = true)
    public SectionDTO registerStudent(UUID sectionId, String regNum) {
        log.debug("Registering student with reg number {} to section {}", regNum, sectionId);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Student student = studentRepository.findByRegNum(regNum);
        if (student == null) {
            throw new RuntimeException("Student not found with registration number: " + regNum);
        }

        student.setSection(section);
        studentRepository.save(student);

        // Update section strength
        section.setStrength(section.getStudents().size());
        sectionRepository.save(section);

        return mapToDTO(section);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "studentsBySection", key = "#sectionId")
    @Override
    public List<StudentDTO> getStudentsBySection(UUID sectionId) {
        log.debug("Cache miss: Getting students for section: {}", sectionId);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        return section.getStudents().stream()
                .map(this::mapStudentToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"sectionDetails", "studentsBySection"}, allEntries = true)
    public List<SectionDTO> bulkCreateSections(MultipartFile file) throws Exception {
        String[] headers = {"TRAINING", "SECTIONS"};

        try {
            List<CSVRecord> records = csvService.parseCsv(file, headers);
            List<Section> allSections = new ArrayList<>();
            int skippedCount = 0;

            for (CSVRecord record : records) {
                validateRequiredFields(record);
                String trainingName = record.get("TRAINING");
                String sectionsString = record.get("SECTIONS");

                System.out.println("Training STRING [DEBUG]" + trainingName);

                Training training = getTraining(trainingName);

                if (training == null) {
                    log.warn("Training not found, skipping: {}", trainingName);
                    continue;
                }
                System.out.println("Section STRING [DEBUG]" + sectionsString);
                List<String> sections = parseSectionsList(sectionsString);
                System.out.println("SECTIONS: " + sections);
                System.out.println("Creating " + sections.size() + " sections for training: " + trainingName);
                for (String sectionName : sections) {
                    // Check if section already exists
                    if (sectionRepository.existsByName(sectionName)) {
                        log.info("Section already exists, skipping: {}", sectionName);
                        skippedCount++;
                        continue;
                    }
                    Section section = Section.builder()
                            .name(sectionName)
                            .training(training)
                            .strength(0)
                            .capacity(30)
                            .build();
                    allSections.add(section);
                }
            }

            List<Section> savedSections = sectionRepository.saveAll(allSections);
            log.info("Saved {} sections, skipped {} duplicates", savedSections.size(), skippedCount);

            return savedSections.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in bulk Training creation: {}", e.getMessage());
            throw new Exception("Error processing Training data: " + e.getMessage());
        }
    }

    private Training getTraining(String trainingName) {
        Training t = trainingService.getTrainingByName(trainingName);
        if(t == null) {
            t = trainingService.getTrainingBySn(trainingName);
        }
        return t;
    }

    @Override
    public Section getSectionByName(String name) {
        return sectionRepository.findByName(name);
    }

    private List<String> parseSectionsList(String sectionString) {
        System.out.println("Section STRING at parse List[DEBUG]" + sectionString);
        String cleanString = sectionString.replaceAll("[\\[\\]]", "").trim();
        if (cleanString.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(cleanString.split(";"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private void validateRequiredFields(org.apache.commons.csv.CSVRecord record) {
        String[] requiredFields = {"TRAINING", "SECTIONS"};
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new IllegalArgumentException("Required field '" + field + "' is missing or empty");
            }
        }
    }


    @Override
    @Transactional
    @CacheEvict(value = {"sectionDetails", "studentsBySection"}, allEntries = true)
    public List<SectionDTO> bulkRegisterStudentsToSections(MultipartFile file) throws Exception {
        String[] headers = {"SECTION", "STUDENTS"};
        try {
            List<CSVRecord> records = csvService.parseCsv(file, headers);
            List<SectionDTO> updatedSections = new ArrayList<>();

            // Process records sequentially to avoid LazyInitializationException
            for (CSVRecord record : records) {
                try {
                    validateStudentRegistrationFields(record);

                    String sectionName = record.get("SECTION");
                    String studentsString = record.get("STUDENTS");

                    // Use findByName inside the transaction context
                    Section section = sectionRepository.findByName(sectionName);
                    if (section == null) {
                        throw new RuntimeException("Section not found: " + sectionName);
                    }

                    List<String> regNums = parseStudentsList(studentsString);
                    if (!regNums.isEmpty()) {
                        // This method already has @Transactional
                        SectionDTO sectionDTO = registerStudents(section.getId(), regNums);
                        updatedSections.add(sectionDTO);
                    }
                } catch (Exception e) {
                    log.error("Error processing record for section {}: {}", 
                            record.get("SECTION"), e.getMessage());
                    // We don't rethrow here to allow other records to be processed
                }
            }

            log.info("Updated {} sections", updatedSections.size());
            return updatedSections;

        } catch (Exception e) {
            log.error("Error in bulk Student registration: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private void validateStudentRegistrationFields(CSVRecord record) {
        String[] requiredFields = {"SECTION", "STUDENTS"};
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new IllegalArgumentException("Required field '" + field + "' is missing or empty");
            }
        }
    }

    private List<String> parseStudentsList(String studentsString) {
        String cleanString = studentsString.replaceAll("[\\[\\]]", "").trim();
        if (cleanString.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(cleanString.split(";"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionDTO> getSectionsByTraining(UUID trainingId) {
        log.debug("Getting sections by training ID: {}", trainingId);

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        return sectionRepository.findByTraining(training).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"sectionDetails", "studentsBySection"}, allEntries = true)
    public SectionDTO updateStudentSection(UUID studentId, UUID sectionId) {
        log.debug("Updating section for student {} to section {}", studentId, sectionId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        // If student already has a section, update that section's strength
        if (student.getSection() != null) {
            Section oldSection = student.getSection();
            student.setSection(null);
            studentRepository.save(student);
            
            // Update old section's strength
            oldSection.setStrength(oldSection.getStudents().size());
            sectionRepository.save(oldSection);
        }

        student.setSection(section);
        studentRepository.save(student);

        // Update new section's strength
        section.setStrength(section.getStudents().size());
        sectionRepository.save(section);

        return mapToDTO(section);
    }

    private void validateSectionRecord(CSVRecord record) throws Exception {
        String[] requiredFields = {"NAME", "TRAINING_ID"};
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new Exception("Required field '" + field + "' is missing or empty");
            }
        }

        try {
            UUID.fromString(record.get("TRAINING_ID"));
        } catch (IllegalArgumentException e) {
            throw new Exception("TRAINING_ID must be a valid UUID");
        }
    }

    private void validateRegistrationRecord(CSVRecord record) throws Exception {
        String[] requiredFields = {"STUDENT_ID", "SECTION_ID"};
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new Exception("Required field '" + field + "' is missing or empty");
            }
        }

        try {
            UUID.fromString(record.get("STUDENT_ID"));
        } catch (IllegalArgumentException e) {
            throw new Exception("STUDENT_ID must be a valid UUID");
        }

        try {
            UUID.fromString(record.get("SECTION_ID"));
        } catch (IllegalArgumentException e) {
            throw new Exception("SECTION_ID must be a valid UUID");
        }
    }

    private SectionDTO mapToDTO(Section section) {
        TrainingDTO trainingDTO = trainingService.getTrainingById(section.getTraining().getId());

        return SectionDTO.builder()
                .id(section.getId())
                .name(section.getName())
                .training(trainingDTO)
                .students(section.getStudents() != null ? section.getStudents().stream()
                        .map(this::mapStudentToDTO)
                        .collect(Collectors.toSet()) : null)
                .strength(section.getStrength())
                .capacity(section.getCapacity())
                .build();
    }

    private StudentDTO mapStudentToDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId().toString())
                .name(student.getName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .regNum(student.getRegNum())
                .rollNumber(student.getRegNum()) // Ensure rollNumber is also set
                .department(student.getBranch().toString())
                .batch(student.getBatch())
                .section(student.getSection() != null ? student.getSection().getName() : null)
                .crtEligibility(student.getCrtEligibility())
                .build();
    }
}
