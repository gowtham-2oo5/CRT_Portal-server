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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SectionServiceImpl implements SectionService {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TrainingRepository TrainingRepository;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CsvService csvService;

    @Override
    @Transactional
    public SectionDTO createSection(CreateSectionDTO createSectionDTO) {
        Training training = TrainingRepository.findById(createSectionDTO.getTrainingId())
                .orElseThrow(() -> new RuntimeException("Training not found"));

        Section section = new Section();
        section.setName(training.getSn() + " " + createSectionDTO.getSectionName());
        section.setTraining(training);
        section.setStrength(0);
        section.setCapacity(30);

        Section savedSection = sectionRepository.save(section);
        return mapToDTO(savedSection);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionDTO getSection(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        return mapToDTO(section);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionDTO> getAllSections() {
        return sectionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectionDTO updateSection(UUID sectionId, CreateSectionDTO updateSectionDTO) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Training training = TrainingRepository.findById(updateSectionDTO.getTrainingId())
                .orElseThrow(() -> new RuntimeException("Training not found"));

        section.setName(training.getSn() + " " + updateSectionDTO.getSectionName());
        section.setTraining(training);

        Section updatedSection = sectionRepository.save(section);
        return mapToDTO(updatedSection);
    }

    @Override
    @Transactional
    public void deleteSection(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        sectionRepository.delete(section);
    }

    @Override
    @Transactional
    public SectionDTO registerStudents(UUID sectionId, List<String> regNums) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        List<Student> students = studentRepository.findByRegNumIn(regNums);

        section.getStudents().addAll(students);
        section.setStrength(section.getStudents().size());
        Section updatedSection = sectionRepository.save(section);
        return mapToDTO(updatedSection);
    }

    @Override
    public List<SectionDTO> bulkRegisterStudentsToSections(MultipartFile file) throws Exception {
        String[] headers = {"SECTION", "STUDENTS"};
        try {

            List<CSVRecord> records = csvService.parseCsv(file, headers);
            List<SectionDTO> updatedSections = new ArrayList<>();

            for (CSVRecord record : records) {
                validateStudentRegistrationFields(record);

                String sectionName = record.get("SECTION");
                String studentsString = record.get("STUDENTS");

                Section section = sectionRepository.findByName(sectionName);
                if (section == null) {
                    throw new RuntimeException("Section not found: " + sectionName);
                }

                List<String> regNums = parseStudentsList(studentsString);
                if (!regNums.isEmpty()) {
                    SectionDTO sectionDTO = registerStudents(section.getId(), regNums);
                    updatedSections.add(sectionDTO);
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
        Training training = TrainingRepository.findById(trainingId)
                .orElse(null);
        if (training == null) {
            return null;
        }
        List<Section> sections = sectionRepository.findByTraining(training);
        return sections.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectionDTO updateStudentSection(UUID studentId, UUID sectionId) {
        // Get student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Get current section using a separate query
        Section currentSection = sectionRepository.findByStudentsContaining(student)
                .stream()
                .findFirst()
                .orElse(null);

        // Remove from current section if exists
        if (currentSection != null) {
            currentSection.getStudents().remove(student);
            currentSection.setStrength(currentSection.getStudents().size());
            sectionRepository.save(currentSection);
        }

        // Add to new section
        Section newSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        newSection.getStudents().add(student);
        newSection.setStrength(newSection.getStudents().size());
        Section updatedSection = sectionRepository.save(newSection);

        return mapToDTO(updatedSection);
    }

    @Override
    public List<SectionDTO> bulkCreateSections(MultipartFile file) throws Exception {
        String[] headers = {"TRAINING", "SECTIONS"};

        try {
            List<CSVRecord> records = csvService.parseCsv(file, headers);
            List<Section> allSections = new ArrayList<>();

            for (CSVRecord record : records) {
                validateRequiredFields(record);
                String trainingName = record.get("TRAINING");
                String sectionsString = record.get("SECTIONS");

                Training training = trainingService.getTrainingByName(trainingName);
                if (training == null) {
                    throw new RuntimeException("Training not found: " + trainingName);
                }
                System.out.println("Section STRING [DEBUG]" + sectionsString);
                List<String> sections = parseSectionsList(sectionsString);
                System.out.println("SECTIONS: " + sections);
                System.out.println("Creating " + sections.size() + " sections for training: " + trainingName);
                for (String sectionName : sections) {

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
            log.info("Saved {} sections", savedSections.size());

            return savedSections.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in bulk Training creation: {}", e.getMessage());
            throw new Exception("Error processing Training data: " + e.getMessage());
        }
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

    private SectionDTO mapToDTO(Section section) {
        SectionDTO dto = new SectionDTO();
        dto.setId(section.getId());
        dto.setName(section.getName());
        dto.setTraining(mapTrainingToDTO(section.getTraining()));
        dto.setStudents(section.getStudents().stream()
                .map(this::mapStudentToDTO)
                .collect(Collectors.toSet()));
        dto.setStrength(section.getStrength());
        return dto;
    }

    private TrainingDTO mapTrainingToDTO(Training training) {
        TrainingDTO dto = new TrainingDTO();
        dto.setId(training.getId());
        dto.setName(training.getName());
        return dto;
    }

    private StudentDTO mapStudentToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId().toString()); // Convert UUID to String
        dto.setRegNum(student.getRegNum());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setDepartment(student.getBranch().name());
        dto.setBatch(student.getBatch());
        return dto;
    }
}