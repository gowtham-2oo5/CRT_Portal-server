package com.crt.server.controller;

import com.crt.server.dto.RoomDTO;
import com.crt.server.dto.SectionDTO;
import com.crt.server.dto.StudentDTO;
import com.crt.server.dto.TrainerDTO;
import com.crt.server.service.RoomService;
import com.crt.server.service.SectionService;
import com.crt.server.service.StudentService;
import com.crt.server.service.TrainerService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BulkOperationsControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private RoomService roomService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private BulkOperationsController bulkOperationsController;

    private MockMvc mockMvc;
    private UUID sectionId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bulkOperationsController).build();
        sectionId = UUID.randomUUID();
    }

    @Test
    void bulkUploadStudents_Success() throws Exception {
        List<StudentDTO> mockStudents = Arrays.asList(
                StudentDTO.builder().id(UUID.randomUUID()).name("Student 1").build(),
                StudentDTO.builder().id(UUID.randomUUID()).name("Student 2").build());
        when(studentService.bulkCreateStudents(any())).thenReturn(mockStudents);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "name,email,phone,regNum,department,batch\nJohn,john@test.com,123456,REG001,CS,2023".getBytes());

        mockMvc.perform(multipart("/api/bulk/students/upload")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("Student 1"))
                .andExpect(jsonPath("$[1].name").value("Student 2"));
    }

    @Test
    void bulkCreateRoomsSimple_Success() throws Exception {
        List<RoomDTO> mockRooms = Arrays.asList(
                RoomDTO.builder().id(UUID.randomUUID()).block("R").floor("5").roomNumber("04").build(),
                RoomDTO.builder().id(UUID.randomUUID()).block("R").floor("5").roomNumber("05").build());
        when(roomService.bulkCreateRoomsFromSimpleFormat(any())).thenReturn(mockRooms);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rooms.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "roomString,capacity,roomType\nR504,30,LAB\nR505,30,LAB".getBytes());

        mockMvc.perform(multipart("/api/bulk/simple-room/upload")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].block").value("R"))
                .andExpect(jsonPath("$[0].floor").value("5"))
                .andExpect(jsonPath("$[0].roomNumber").value("04"));
    }

    @Test
    void bulkUploadTrainers_Success() throws Exception {
        List<TrainerDTO> mockTrainers = Arrays.asList(
                TrainerDTO.builder().id(UUID.randomUUID()).name("Trainer 1").build(),
                TrainerDTO.builder().id(UUID.randomUUID()).name("Trainer 2").build());
        when(trainerService.bulkCreateTrainers(any())).thenReturn(mockTrainers);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trainers.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "name,email,sn\nJohn,john@test.com,T123".getBytes());

        mockMvc.perform(multipart("/api/bulk/trainers/upload")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("Trainer 1"))
                .andExpect(jsonPath("$[1].name").value("Trainer 2"));
    }

    @Test
    void registerStudentsToSection_Success() throws Exception {
        SectionDTO mockSection = SectionDTO.builder()
                .id(sectionId)
                .name("Test Section")
                .build();
        when(sectionService.registerStudents(any(), any())).thenReturn(mockSection);

        MockMultipartFile file = new MockMultipartFile(
                "studentsCSV",
                "students.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "REG001\nREG002\nREG003".getBytes());

        mockMvc.perform(multipart("/api/bulk/register-students")
                .file(file)
                .param("sectionId", sectionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sectionId.toString()))
                .andExpect(jsonPath("$.name").value("Test Section"));
    }

    @Test
    void registerStudentsToSection_InvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "studentsCSV",
                "sample.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]);

        mockMvc.perform(multipart("/api/bulk/register-students")
                .file(file)
                .param("sectionId", sectionId.toString()))
                .andExpect(status().isBadRequest());
    }
}