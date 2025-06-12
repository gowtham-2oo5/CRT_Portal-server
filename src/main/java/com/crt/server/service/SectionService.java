package com.crt.server.service;

import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;

import java.util.List;
import java.util.UUID;

public interface SectionService {
    SectionDTO createSection(CreateSectionDTO createSectionDTO);

    SectionDTO registerStudents(UUID sectionId, List<String> regNums);

    List<SectionDTO> getSectionsByTrainer(UUID trainerId);
}