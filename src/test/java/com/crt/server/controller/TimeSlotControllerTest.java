package com.crt.server.controller;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.service.TimeSlotService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TimeSlotControllerTest {

    @Mock
    private TimeSlotService timeSlotService;

    @InjectMocks
    private TimeSlotController timeSlotController;

    private MockMvc mockMvc;

    private TimeSlotDTO mockTimeSlotDTO;
    private final UUID sectionId = UUID.randomUUID();
    private final UUID facultyId = UUID.randomUUID();
    private final UUID roomId = UUID.fromString("02d4bb41-4c0b-47b6-9717-cbda961e79fe");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(timeSlotController).build();

        mockTimeSlotDTO = TimeSlotDTO.builder()
                .id(1)
                .title(1)
                .startTime("09:00")
                .endTime("10:00")
                .isBreak(false)
                .breakDescription(null)
                .inchargeFacultyId(facultyId)
                .sectionId(sectionId)
                .roomId(roomId)
                .build();
    }

    @Test
    void createTimeSlot_ValidData_ReturnsCreatedTimeSlot() throws Exception {
        when(timeSlotService.createTimeSlot(any(TimeSlotDTO.class))).thenReturn(mockTimeSlotDTO);

        mockMvc.perform(post("/api/time-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"title\":1,\"startTime\":\"09:00\",\"endTime\":\"10:00\",\"isBreak\":false,\"inchargeFacultyId\":\""
                                + facultyId + "\",\"sectionId\":\"" + sectionId + "\",\"roomId\":\"" + roomId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value(1))
                .andExpect(jsonPath("$.startTime").value("09:00"))
                .andExpect(jsonPath("$.endTime").value("10:00"))
                .andExpect(jsonPath("$.inchargeFacultyId").value(facultyId.toString()))
                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                .andExpect(jsonPath("$.roomId").value(roomId.toString()));

        verify(timeSlotService, times(1)).createTimeSlot(any(TimeSlotDTO.class));
    }

    @Test
    void getTimeSlot_ExistingId_ReturnsTimeSlot() throws Exception {
        when(timeSlotService.getTimeSlot(1)).thenReturn(mockTimeSlotDTO);

        mockMvc.perform(get("/api/time-slots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value(1))
                .andExpect(jsonPath("$.startTime").value("09:00"))
                .andExpect(jsonPath("$.endTime").value("10:00"));

        verify(timeSlotService, times(1)).getTimeSlot(1);
    }

    @Test
    void getTimeSlotsBySection_ValidSectionId_ReturnsTimeSlots() throws Exception {
        List<TimeSlotDTO> timeSlots = Arrays.asList(mockTimeSlotDTO);
        when(timeSlotService.getTimeSlotsBySection(sectionId)).thenReturn(timeSlots);

        mockMvc.perform(get("/api/time-slots/section/" + sectionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value(1))
                .andExpect(jsonPath("$[0].startTime").value("09:00"))
                .andExpect(jsonPath("$[0].endTime").value("10:00"));

        verify(timeSlotService, times(1)).getTimeSlotsBySection(sectionId);
    }

    @Test
    void getTimeSlotsByFaculty_ValidFacultyId_ReturnsTimeSlots() throws Exception {
        List<TimeSlotDTO> timeSlots = Arrays.asList(mockTimeSlotDTO);
        when(timeSlotService.getTimeSlotsByFaculty(facultyId)).thenReturn(timeSlots);

        mockMvc.perform(get("/api/time-slots/faculty/" + facultyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value(1))
                .andExpect(jsonPath("$[0].startTime").value("09:00"))
                .andExpect(jsonPath("$[0].endTime").value("10:00"));

        verify(timeSlotService, times(1)).getTimeSlotsByFaculty(facultyId);
    }

    @Test
    void updateTimeSlot_ValidData_ReturnsUpdatedTimeSlot() throws Exception {
        when(timeSlotService.updateTimeSlot(eq(1), any(TimeSlotDTO.class))).thenReturn(mockTimeSlotDTO);

        mockMvc.perform(put("/api/time-slots/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"title\":1,\"startTime\":\"09:00\",\"endTime\":\"10:00\",\"isBreak\":false,\"inchargeFacultyId\":\""
                                + facultyId + "\",\"sectionId\":\"" + sectionId + "\",\"roomId\":\"" + roomId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value(1))
                .andExpect(jsonPath("$.startTime").value("09:00"))
                .andExpect(jsonPath("$.endTime").value("10:00"));

        verify(timeSlotService, times(1)).updateTimeSlot(eq(1), any(TimeSlotDTO.class));
    }

    @Test
    void deleteTimeSlot_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(timeSlotService).deleteTimeSlot(1);

        mockMvc.perform(delete("/api/time-slots/1"))
                .andExpect(status().isNoContent());

        verify(timeSlotService, times(1)).deleteTimeSlot(1);
    }

    @Test
    void getActiveTimeSlotsBySection_ValidSectionId_ReturnsTimeSlots() throws Exception {
        List<TimeSlotDTO> timeSlots = Arrays.asList(mockTimeSlotDTO);
        when(timeSlotService.getActiveTimeSlotsBySection(sectionId)).thenReturn(timeSlots);

        mockMvc.perform(get("/api/time-slots/section/" + sectionId + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value(1))
                .andExpect(jsonPath("$[0].startTime").value("09:00"))
                .andExpect(jsonPath("$[0].endTime").value("10:00"));

        verify(timeSlotService, times(1)).getActiveTimeSlotsBySection(sectionId);
    }

    @Test
    void getActiveTimeSlotsByFaculty_ValidFacultyId_ReturnsTimeSlots() throws Exception {
        List<TimeSlotDTO> timeSlots = Arrays.asList(mockTimeSlotDTO);
        when(timeSlotService.getActiveTimeSlotsByFaculty(facultyId)).thenReturn(timeSlots);

        mockMvc.perform(get("/api/time-slots/faculty/" + facultyId + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value(1))
                .andExpect(jsonPath("$[0].startTime").value("09:00"))
                .andExpect(jsonPath("$[0].endTime").value("10:00"));

        verify(timeSlotService, times(1)).getActiveTimeSlotsByFaculty(facultyId);
    }

    @Test
    void isTimeSlotAvailable_WhenRoomIsAvailable_ReturnsTrue() throws Exception {
        when(timeSlotService.isTimeSlotAvailable(eq(roomId), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(get("/api/time-slots/check-availability")
                .param("roomId", roomId.toString())
                .param("startTime", "09:00")
                .param("endTime", "10:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(timeSlotService, times(1)).isTimeSlotAvailable(eq(roomId), anyString(), anyString());
    }

    @Test
    void isTimeSlotAvailable_WhenRoomIsNotAvailable_ReturnsFalse() throws Exception {
        when(timeSlotService.isTimeSlotAvailable(eq(roomId), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(get("/api/time-slots/check-availability")
                .param("roomId", roomId.toString())
                .param("startTime", "09:00")
                .param("endTime", "10:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(timeSlotService, times(1)).isTimeSlotAvailable(eq(roomId), anyString(), anyString());
    }

    @Test
    void isTimeSlotAvailable_WhenRoomNotFound_ReturnsFalse() throws Exception {
        when(timeSlotService.isTimeSlotAvailable(eq(roomId), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(get("/api/time-slots/check-availability")
                .param("roomId", roomId.toString())
                .param("startTime", "09:00")
                .param("endTime", "10:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(timeSlotService, times(1)).isTimeSlotAvailable(eq(roomId), anyString(), anyString());
    }
}