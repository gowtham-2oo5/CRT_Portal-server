package com.crt.server.service.impl;

import com.crt.server.dto.*;
import com.crt.server.model.CRT_Trainer;
import com.crt.server.model.Section;
import com.crt.server.model.Student;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.StudentRepository;
import com.crt.server.repository.TrainerRepository;
import com.crt.server.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        section.setName(trainer.getSn() + " " + createSectionDTO.getSectionName()); // Format: "TRAINER_SN
                                                                                    // InputSectionName"
        section.setTrainer(trainer);
        section.setStrength(0); // Initial strength is 0
        section.setCapacity(30); // Default capacity

        Section savedSection = sectionRepository.save(section);
        return mapToDTO(savedSection);
    }

    @Override
    @Transactional
    public SectionDTO registerStudents(UUID sectionId, List<String> regNums) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        List<Student> students = studentRepository.findByRegNumIn(regNums);

        section.getStudents().addAll(students);
        Section updatedSection = sectionRepository.save(section);
        return mapToDTO(updatedSection);
    }

    @Transactional(readOnly = true)
    public List<SectionDTO> getSectionsByTrainer(UUID trainerId) {
        CRT_Trainer trainer = trainerRepository.findById(trainerId)
                .orElse(null);
        if(trainer == null) {
            return null;
        }
        List<Section> sections = sectionRepository.findByTrainer(trainer);
        return sections.stream()
                .map(section -> {
                    SectionDTO dto = new SectionDTO();
                    dto.setId(section.getId());
                    dto.setName(section.getName());
                    dto.setTrainer(mapTrainerToDTO(section.getTrainer()));

                    Set<StudentDTO> studentDTOs = section.getStudents()
                            .stream()
                            .map(this::mapStudentToDTO)
                            .collect(Collectors.toSet());
                    dto.setStudents(studentDTOs);

                    dto.setStrength(section.getStrength());
                    return dto;
                })
                .collect(Collectors.toList());
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