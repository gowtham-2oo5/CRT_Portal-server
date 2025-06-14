package com.crt.server.controller;

import com.crt.server.dto.AttendanceDTO;
import com.crt.server.dto.AttendanceReportDTO;
import com.crt.server.dto.MarkAttendanceDTO;
import com.crt.server.dto.StudentAttendanceDTO;
import com.crt.server.dto.BulkAttendanceResponseDTO;
import com.crt.server.dto.BulkAttendanceDTO;
import com.crt.server.model.AttendanceStatus;
import com.crt.server.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AttendanceControllerTest {

        @Mock
        private AttendanceService attendanceService;

        @InjectMocks
        private AttendanceController attendanceController;

        private MockMvc mockMvc;
        private UUID studentId;
        private Integer timeSlotId;
        private LocalDateTime testDate;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(attendanceController).build();
                studentId = UUID.randomUUID();
                timeSlotId = 1;
                testDate = LocalDateTime.now();
        }

        @Test
        void markAttendance_Success() throws Exception {
                // Arrange
                MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
                markAttendanceDTO.setTimeSlotId(timeSlotId);
                markAttendanceDTO.setDateTime("14/6/2025, 2:15:16 pm");
                markAttendanceDTO.setAbsentStudentIds(Arrays.asList(UUID.randomUUID()));
                markAttendanceDTO.setLateStudents(Arrays.asList(
                                new StudentAttendanceDTO(UUID.randomUUID(), "Late due to traffic")));

                List<AttendanceDTO> expectedResponse = Arrays.asList(
                                AttendanceDTO.builder()
                                                .studentId(studentId)
                                                .timeSlotId(timeSlotId)
                                                .status(AttendanceStatus.PRESENT)
                                                .build());

                when(attendanceService.markAttendance(any(MarkAttendanceDTO.class))).thenReturn(expectedResponse);

                // Act & Assert
                mockMvc.perform(post("/api/attendance/mark")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"timeSlotId\":1,\"dateTime\":\"14/6/2025, 2:15:16 pm\",\"absentStudentIds\":[\""
                                                + UUID.randomUUID() + "\"],\"lateStudents\":[{\"studentId\":\""
                                                + UUID.randomUUID() + "\",\"feedback\":\"Late due to traffic\"}]}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].studentId").exists())
                                .andExpect(jsonPath("$[0].timeSlotId").value(timeSlotId))
                                .andExpect(jsonPath("$[0].status").value("PRESENT"));
        }

        @Test
        void getStudentAttendance_Success() throws Exception {
                // Arrange
                List<AttendanceDTO> expectedResponse = Arrays.asList(
                                AttendanceDTO.builder()
                                                .studentId(studentId)
                                                .timeSlotId(timeSlotId)
                                                .status(AttendanceStatus.PRESENT)
                                                .build());

                when(attendanceService.getStudentAttendance(any(), any(), any())).thenReturn(expectedResponse);

                // Act & Assert
                mockMvc.perform(get("/api/attendance/student/{studentId}", studentId)
                                .param("startDate", testDate.toString())
                                .param("endDate", testDate.plusDays(7).toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].studentId").exists())
                                .andExpect(jsonPath("$[0].timeSlotId").value(timeSlotId))
                                .andExpect(jsonPath("$[0].status").value("PRESENT"));
        }

        @Test
        void getTimeSlotAttendance_Success() throws Exception {
                // Arrange
                List<AttendanceDTO> expectedResponse = Arrays.asList(
                                AttendanceDTO.builder()
                                                .studentId(studentId)
                                                .timeSlotId(timeSlotId)
                                                .status(AttendanceStatus.PRESENT)
                                                .build());

                when(attendanceService.getTimeSlotAttendance(any(), any())).thenReturn(expectedResponse);

                // Act & Assert
                mockMvc.perform(get("/api/attendance/time-slot/{timeSlotId}", timeSlotId)
                                .param("date", testDate.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].studentId").exists())
                                .andExpect(jsonPath("$[0].timeSlotId").value(timeSlotId))
                                .andExpect(jsonPath("$[0].status").value("PRESENT"));
        }

        @Test
        void getStudentAttendanceReport_Success() throws Exception {
                // Arrange
                AttendanceReportDTO expectedResponse = AttendanceReportDTO.builder()
                                .studentId(studentId)
                                .studentName("Test Student")
                                .regNum("REG001")
                                .totalClasses(10)
                                .absences(2)
                                .attendancePercentage(80.0)
                                .build();

                when(attendanceService.getStudentAttendanceReport(any(), any(), any())).thenReturn(expectedResponse);

                // Act & Assert
                mockMvc.perform(get("/api/attendance/report/{studentId}", studentId)
                                .param("startDate", testDate.toString())
                                .param("endDate", testDate.plusDays(7).toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.studentId").exists())
                                .andExpect(jsonPath("$.studentName").value("Test Student"))
                                .andExpect(jsonPath("$.regNum").value("REG001"))
                                .andExpect(jsonPath("$.totalClasses").value(10))
                                .andExpect(jsonPath("$.absences").value(2))
                                .andExpect(jsonPath("$.attendancePercentage").value(80.0));
        }

        @Test
        void archiveAttendanceRecords_Success() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/attendance/archive")
                                .param("year", "2024")
                                .param("month", "6"))
                                .andExpect(status().isOk());
                verify(attendanceService).archiveAttendanceRecords(2024, 6);
        }

        @Test
        void getArchivedStudentAttendance_Success() throws Exception {
                // Arrange
                List<AttendanceDTO> expectedResponse = Arrays.asList(
                                AttendanceDTO.builder()
                                                .studentId(studentId)
                                                .timeSlotId(timeSlotId)
                                                .status(AttendanceStatus.PRESENT)
                                                .build());

                when(attendanceService.getArchivedStudentAttendance(any(), any(), any())).thenReturn(expectedResponse);

                // Act & Assert
                mockMvc.perform(get("/api/attendance/archived/student/{studentId}", studentId)
                                .param("startDate", testDate.toString())
                                .param("endDate", testDate.plusDays(7).toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].studentId").exists())
                                .andExpect(jsonPath("$[0].timeSlotId").value(timeSlotId))
                                .andExpect(jsonPath("$[0].status").value("PRESENT"));
        }

        @Test
        void getArchivedStudentAttendanceReport_Success() throws Exception {
                // Arrange
                AttendanceReportDTO expectedResponse = AttendanceReportDTO.builder()
                                .studentId(studentId)
                                .studentName("Test Student")
                                .regNum("REG001")
                                .totalClasses(10)
                                .absences(2)
                                .attendancePercentage(80.0)
                                .build();

                when(attendanceService.getArchivedStudentAttendanceReport(any(), any(), any()))
                                .thenReturn(expectedResponse);

                // Act & Assert
                mockMvc.perform(get("/api/attendance/archived/report/{studentId}", studentId)
                                .param("startDate", testDate.toString())
                                .param("endDate", testDate.plusDays(7).toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.studentId").exists())
                                .andExpect(jsonPath("$.studentName").value("Test Student"))
                                .andExpect(jsonPath("$.regNum").value("REG001"))
                                .andExpect(jsonPath("$.totalClasses").value(10))
                                .andExpect(jsonPath("$.absences").value(2))
                                .andExpect(jsonPath("$.attendancePercentage").value(80.0));
        }

        @Test
        void markBulkAttendance_Success() throws Exception {
                // Prepare test data
                UUID studentId1 = UUID.randomUUID();
                UUID studentId2 = UUID.randomUUID();
                BulkAttendanceDTO bulkAttendanceDTO = BulkAttendanceDTO.builder()
                                .timeSlotId(1)
                                .dateTime("2024-03-20T10:00:00")
                                .absentStudentIds(Arrays.asList(studentId1))
                                .lateStudents(Arrays.asList(
                                                StudentAttendanceDTO.builder()
                                                                .studentId(studentId2)
                                                                .feedback("Late due to traffic")
                                                                .build()))
                                .build();

                // Mock service response
                BulkAttendanceResponseDTO responseDTO = BulkAttendanceResponseDTO.builder()
                                .totalProcessed(2)
                                .successCount(2)
                                .failureCount(0)
                                .successfulRecords(Arrays.asList(
                                                AttendanceDTO.builder()
                                                                .studentId(studentId1)
                                                                .timeSlotId(1)
                                                                .status(AttendanceStatus.ABSENT)
                                                                .build(),
                                                AttendanceDTO.builder()
                                                                .studentId(studentId2)
                                                                .timeSlotId(1)
                                                                .status(AttendanceStatus.LATE)
                                                                .feedback("Late due to traffic")
                                                                .build()))
                                .errors(List.of())
                                .build();

                when(attendanceService.markBulkAttendance(any(BulkAttendanceDTO.class))).thenReturn(responseDTO);

                // Perform test
                mockMvc.perform(post("/api/attendance/mark/bulk")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"timeSlotId\":1,\"dateTime\":\"2024-03-20T10:00:00\",\"absentStudentIds\":[\""
                                                + studentId1 + "\"],\"lateStudents\":[{\"studentId\":\"" + studentId2
                                                + "\",\"feedback\":\"Late due to traffic\"}]}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalProcessed").value(2))
                                .andExpect(jsonPath("$.successCount").value(2))
                                .andExpect(jsonPath("$.failureCount").value(0))
                                .andExpect(jsonPath("$.successfulRecords").isArray())
                                .andExpect(jsonPath("$.successfulRecords.length()").value(2))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors.length()").value(0));

                verify(attendanceService).markBulkAttendance(any(BulkAttendanceDTO.class));
        }

        @Test
        void markBulkAttendance_WithErrors() throws Exception {
                // Prepare test data
                UUID studentId1 = UUID.randomUUID();
                UUID studentId2 = UUID.randomUUID();
                BulkAttendanceDTO bulkAttendanceDTO = BulkAttendanceDTO.builder()
                                .timeSlotId(1)
                                .dateTime("2024-03-20T10:00:00")
                                .absentStudentIds(Arrays.asList(studentId1))
                                .lateStudents(Arrays.asList(
                                                StudentAttendanceDTO.builder()
                                                                .studentId(studentId2)
                                                                .feedback("Late due to traffic")
                                                                .build()))
                                .build();

                // Mock service response with errors
                BulkAttendanceResponseDTO responseDTO = BulkAttendanceResponseDTO.builder()
                                .totalProcessed(2)
                                .successCount(1)
                                .failureCount(1)
                                .successfulRecords(Arrays.asList(
                                                AttendanceDTO.builder()
                                                                .studentId(studentId1)
                                                                .timeSlotId(1)
                                                                .status(AttendanceStatus.ABSENT)
                                                                .build()))
                                .errors(Arrays.asList("Student " + studentId2 + " is not enrolled in this section"))
                                .build();

                when(attendanceService.markBulkAttendance(any(BulkAttendanceDTO.class))).thenReturn(responseDTO);

                // Perform test
                mockMvc.perform(post("/api/attendance/mark/bulk")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"timeSlotId\":1,\"dateTime\":\"2024-03-20T10:00:00\",\"absentStudentIds\":[\""
                                                + studentId1 + "\"],\"lateStudents\":[{\"studentId\":\"" + studentId2
                                                + "\",\"feedback\":\"Late due to traffic\"}]}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalProcessed").value(2))
                                .andExpect(jsonPath("$.successCount").value(1))
                                .andExpect(jsonPath("$.failureCount").value(1))
                                .andExpect(jsonPath("$.successfulRecords").isArray())
                                .andExpect(jsonPath("$.successfulRecords.length()").value(1))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors.length()").value(1))
                                .andExpect(jsonPath("$.errors[0]")
                                                .value("Student " + studentId2 + " is not enrolled in this section"));

                verify(attendanceService).markBulkAttendance(any(BulkAttendanceDTO.class));
        }

        @Test
        void markBulkAttendance_InvalidInput() throws Exception {
                // Test with invalid input (missing required fields)
                mockMvc.perform(post("/api/attendance/mark/bulk")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());

                verify(attendanceService, never()).markBulkAttendance(any(BulkAttendanceDTO.class));
        }
}