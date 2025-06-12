package com.crt.server.controller;

import com.crt.server.dto.CreateSectionDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.service.SectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SectionControllerTest {

        @Mock
        private SectionService sectionService;

        @InjectMocks
        private SectionController sectionController;

        private MockMvc mockMvc;

        private UUID trainerId;
        private UUID sectionId;
        private SectionDTO mockSectionDTO;
        private CreateSectionDTO createSectionDTO;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(sectionController).build();

                trainerId = UUID.randomUUID();
                sectionId = UUID.randomUUID();

                // Setup mock DTOs
                createSectionDTO = new CreateSectionDTO();
                createSectionDTO.setTrainerId(trainerId);
                createSectionDTO.setSectionName("Test Section");

                mockSectionDTO = SectionDTO.builder()
                                .id(sectionId)
                                .name("T123 Test Section")
                                .trainer(null) // Will be set in specific tests
                                .students(new HashSet<>())
                                .strength(0)
                                .build();
        }

        @Test
        void createSection_Success() throws Exception {
                trainerId = UUID.fromString("4e98d9d7-a0cc-445a-9596-107ae725a0b6");
                when(sectionService.createSection(any(CreateSectionDTO.class))).thenReturn(mockSectionDTO);

                mockMvc.perform(post("/api/sections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"trainerId\":\"" + trainerId + "\",\"sectionName\":\"Test Section\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(sectionId.toString()))
                                .andExpect(jsonPath("$.name").value("T123 Test Section"))
                                .andExpect(jsonPath("$.strength").value(0));
        }

        @Test
        void registerStudents_Success() throws Exception {
                when(sectionService.registerStudents(any(UUID.class), any())).thenReturn(mockSectionDTO);

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "students.txt",
                                MediaType.TEXT_PLAIN_VALUE,
                                "REG001\nREG002\nREG003".getBytes());

                mockMvc.perform(multipart("/api/sections/{sectionId}/students", sectionId)
                                .file(file))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(sectionId.toString()));
        }

        @Test
        void registerStudents_InvalidFile() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "students.txt",
                                MediaType.TEXT_PLAIN_VALUE,
                                new byte[0]);

                mockMvc.perform(multipart("/api/sections/{sectionId}/students", sectionId)
                                .file(file))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getSectionsByTrainer_Success() throws Exception {
                List<SectionDTO> sections = Arrays.asList(mockSectionDTO);
                when(sectionService.getSectionsByTrainer(trainerId)).thenReturn(sections);

                mockMvc.perform(get("/api/sections/trainer/{trainerId}", trainerId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(sectionId.toString()))
                                .andExpect(jsonPath("$[0].name").value("T123 Test Section"));
        }

        @Test
        void getSectionsByTrainer_NotFound() throws Exception {
                when(sectionService.getSectionsByTrainer(any(UUID.class)))
                                .thenReturn(null);

                mockMvc.perform(get("/api/sections/trainer/{trainerId}", UUID.randomUUID()))
                                .andExpect(status().isOk())
                                .andExpect(content().string(""));
        }
}