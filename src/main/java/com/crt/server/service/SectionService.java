package com.crt.server.service;

import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.model.Section;
import com.crt.server.model.Training;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface SectionService {
    SectionDTO createSection(CreateSectionDTO createSectionDTO);

    SectionDTO getSection(UUID sectionId);

    List<SectionDTO> getAllSections();


    SectionDTO updateSection(UUID sectionId, CreateSectionDTO updateSectionDTO);

    void deleteSection(UUID sectionId);

    SectionDTO registerStudents(UUID sectionId, List<String> regNums);

    SectionDTO registerStudent(UUID sectionId, String regNum);

    List<SectionDTO> bulkRegisterStudentsToSections(MultipartFile file) throws Exception;

    List<SectionDTO> getSectionsByTraining(UUID TrainingId);

    SectionDTO updateStudentSection(UUID studentId, UUID sectionId);

    //    List<TrainingDTO> bulkCreateTrainings(MultipartFile file) throws Exception;
    List<SectionDTO> bulkCreateSections(MultipartFile file) throws Exception;

    Section getSectionByName(String name);



}