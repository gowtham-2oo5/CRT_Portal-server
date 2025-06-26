package com.crt.server.controller;

import com.crt.server.dto.SectionScheduleDTO;
import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.service.SectionScheduleService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SectionScheduleControllerTest {

        @Mock
        private SectionScheduleService sectionScheduleService;

        @InjectMocks
        private SectionScheduleController sectionScheduleController;

        private MockMvc mockMvc;

        private SectionScheduleDTO mockScheduleDTO;
        private TimeSlotDTO mockTimeSlotDTO;
        private final UUID scheduleId = UUID.randomUUID();
        private final UUID sectionId = UUID.randomUUID();
        private final UUID facultyId = UUID.randomUUID();
        private final UUID roomId = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(sectionScheduleController).build();

                mockScheduleDTO = SectionScheduleDTO.builder()
                                .id(scheduleId)
                                .sectionId(sectionId)
                                .roomId(roomId)
                                .build();

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
        void createSchedule_ValidData_ReturnsCreatedSchedule() throws Exception {
                when(sectionScheduleService.createSchedule(any(SectionScheduleDTO.class))).thenReturn(mockScheduleDTO);

                mockMvc.perform(post("/api/section-schedules")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"sectionId\":\"" + sectionId + "\",\"roomId\":\"" + roomId + "\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                                .andExpect(jsonPath("$.roomId").value(roomId.toString()));

                verify(sectionScheduleService, times(1)).createSchedule(any(SectionScheduleDTO.class));
        }

        @Test
        void getSchedule_ExistingId_ReturnsSchedule() throws Exception {
                when(sectionScheduleService.getSchedule(scheduleId)).thenReturn(mockScheduleDTO);

                mockMvc.perform(get("/api/section-schedules/" + scheduleId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                                .andExpect(jsonPath("$.roomId").value(roomId.toString()));

                verify(sectionScheduleService, times(1)).getSchedule(scheduleId);
        }

        @Test
        void getScheduleBySection_ValidSectionId_ReturnsSchedule() throws Exception {
                when(sectionScheduleService.getScheduleBySection(sectionId)).thenReturn(mockScheduleDTO);

                mockMvc.perform(get("/api/section-schedules/section/" + sectionId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                                .andExpect(jsonPath("$.roomId").value(roomId.toString()));

                verify(sectionScheduleService, times(1)).getScheduleBySection(sectionId);
        }

        @Test
        void updateSchedule_ValidData_ReturnsUpdatedSchedule() throws Exception {
                when(sectionScheduleService.updateSchedule(eq(scheduleId), any(SectionScheduleDTO.class)))
                                .thenReturn(mockScheduleDTO);

                mockMvc.perform(put("/api/section-schedules/" + scheduleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"sectionId\":\"" + sectionId + "\",\"roomId\":\"" + roomId + "\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                                .andExpect(jsonPath("$.roomId").value(roomId.toString()));

                verify(sectionScheduleService, times(1)).updateSchedule(eq(scheduleId), any(SectionScheduleDTO.class));
        }

        @Test
        void deleteSchedule_ExistingId_ReturnsNoContent() throws Exception {
                doNothing().when(sectionScheduleService).deleteSchedule(scheduleId);

                mockMvc.perform(delete("/api/section-schedules/" + scheduleId))
                                .andExpect(status().isNoContent());

                verify(sectionScheduleService, times(1)).deleteSchedule(scheduleId);
        }

        @Test
        void addTimeSlot_ValidData_ReturnsUpdatedSchedule() throws Exception {
                when(sectionScheduleService.addTimeSlot(eq(scheduleId), any(TimeSlotDTO.class)))
                                .thenReturn(mockScheduleDTO);

                mockMvc.perform(post("/api/section-schedules/" + scheduleId + "/time-slots")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":1,\"startTime\":\"09:00\",\"endTime\":\"10:00\",\"isBreak\":false,\"inchargeFacultyId\":\""
                                                + facultyId + "\",\"sectionId\":\"" + sectionId + "\",\"roomId\":\""
                                                + roomId + "\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                                .andExpect(jsonPath("$.roomId").value(roomId.toString()));

                verify(sectionScheduleService, times(1)).addTimeSlot(eq(scheduleId), any(TimeSlotDTO.class));
        }

        @Test
        void removeTimeSlot_ValidData_ReturnsUpdatedSchedule() throws Exception {
                when(sectionScheduleService.removeTimeSlot(eq(scheduleId), eq(1))).thenReturn(mockScheduleDTO);

                mockMvc.perform(delete("/api/section-schedules/" + scheduleId + "/time-slots/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                                .andExpect(jsonPath("$.roomId").value(roomId.toString()));

                verify(sectionScheduleService, times(1)).removeTimeSlot(eq(scheduleId), eq(1));
        }

        @Test
        void updateTimeSlot_ValidData_ReturnsUpdatedSchedule() throws Exception {
                when(sectionScheduleService.updateTimeSlot(eq(scheduleId), eq(1), any(TimeSlotDTO.class)))
                                .thenReturn(mockScheduleDTO);

                mockMvc.perform(put("/api/section-schedules/" + scheduleId + "/time-slots/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":1,\"startTime\":\"09:00\",\"endTime\":\"10:00\",\"isBreak\":false,\"inchargeFacultyId\":\""
                                                + facultyId + "\",\"sectionId\":\"" + sectionId + "\",\"roomId\":\""
                                                + roomId + "\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                                .andExpect(jsonPath("$.roomId").value(roomId.toString()));

                verify(sectionScheduleService, times(1)).updateTimeSlot(eq(scheduleId), eq(1), any(TimeSlotDTO.class));
        }
}