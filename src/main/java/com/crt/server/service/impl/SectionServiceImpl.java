package com.crt.server.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crt.server.dto.CRT_TrainerDTO;
import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.model.CRT_Trainer;
import com.crt.server.model.Section;
import com.crt.server.model.Student;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.StudentRepository;
import com.crt.server.repository.TrainerRepository;
import com.crt.server.service.SectionService;

@Service
public class SectionServiceImpl implements SectionService {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    @Transactional
    public SectionDTO createSection(CreateSectionDTO createSectionDTO) {
        CRT_Trainer trainer = trainerRepository.findById(createSectionDTO.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        Section section = new Section();
        section.setName(trainer.getSn() + " " + createSectionDTO.getSectionName());
        section.setTrainer(trainer);
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

        CRT_Trainer trainer = trainerRepository.findById(updateSectionDTO.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        section.setName(trainer.getSn() + " " + updateSectionDTO.getSectionName());
        section.setTrainer(trainer);

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
    @Transactional(readOnly = true)
    public List<SectionDTO> getSectionsByTrainer(UUID trainerId) {
        CRT_Trainer trainer = trainerRepository.findById(trainerId)
                .orElse(null);
        if (trainer == null) {
            return null;
        }
        List<Section> sections = sectionRepository.findByTrainer(trainer);
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

    private SectionDTO mapToDTO(Section section) {
        SectionDTO dto = new SectionDTO();
        dto.setId(section.getId());
        dto.setName(section.getName());
        dto.setTrainer(mapTrainerToDTO(section.getTrainer()));
        dto.setStudents(section.getStudents().stream()
                .map(this::mapStudentToDTO)
                .collect(Collectors.toSet()));
        dto.setStrength(section.getStrength());
        return dto;
    }

    private CRT_TrainerDTO mapTrainerToDTO(CRT_Trainer trainer) {
        CRT_TrainerDTO dto = new CRT_TrainerDTO();
        dto.setId(trainer.getId());
        dto.setName(trainer.getName());
        return dto;
    }

    private StudentDTO mapStudentToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setRegNum(student.getRegNum());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setDepartment(student.getDepartment());
        dto.setBatch(student.getBatch());
        return dto;
    }
}