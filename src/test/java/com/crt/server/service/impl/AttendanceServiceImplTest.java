package com.crt.server.service.impl;

import com.crt.server.dto.*;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.SectionService;
import com.crt.server.service.StudentService;
import com.crt.server.service.TimeSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AttendanceServiceImplTest {

        @MockitoBean
        private AttendanceRepository attendanceRepository;
        @MockitoBean
        private TimeSlotRepository timeSlotRepository;
        @MockitoBean
        private SectionRepository sectionRepository;
        @MockitoBean
        private SectionScheduleRepository sectionScheduleRepository;
        @MockitoBean
        private StudentRepository studentRepository;

        @Autowired
        private AttendanceServiceImpl attendanceService;
        @Autowired
        private StudentService studentService;
        @Autowired
        private TimeSlotService timeSlotService;
        @Autowired
        private SectionService sectionService;

        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private LocalDateTime testDateTime;
        private Student student;
        private TimeSlot timeSlot;
        private Section section;
        private SectionSchedule schedule;

        @BeforeEach
        void setUp() {
                testDateTime = LocalDateTime.now();

                // Create test data
                student = new Student();
                student.setId(UUID.randomUUID());
                student.setName("Test Student");

                section = new Section();
                section.setId(UUID.randomUUID());
                section.setStudents(new HashSet<>(Collections.singletonList(student)));

                timeSlot = new TimeSlot();
                timeSlot.setId(1);
                timeSlot.setSection(section);

                schedule = new SectionSchedule();
                schedule.setId(UUID.randomUUID());
                schedule.setSection(section);
        }

        @Test
        void markAttendance_WithValidData_ShouldCreateAttendanceRecords() {
                // Arrange
                when(timeSlotRepository.findById(any())).thenReturn(Optional.of(timeSlot));
                when(sectionRepository.findById(any())).thenReturn(Optional.of(section));
                when(sectionScheduleRepository.findBySectionId(any())).thenReturn(Optional.of(schedule));
                when(timeSlotRepository.existsByIdAndScheduleId(any(), any())).thenReturn(true);
                when(attendanceRepository.findByTimeSlotAndDate(any(), any())).thenReturn(Collections.emptyList());

                MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
                markAttendanceDTO.setTimeSlotId(1);
                markAttendanceDTO.setDateTime(testDateTime.format(DATE_TIME_FORMATTER));

                List<Attendance> mockAttendances = Arrays.asList(
                                createAttendance(student, AttendanceStatus.PRESENT));
                when(attendanceRepository.saveAll(any())).thenReturn(mockAttendances);

                // Act
                List<AttendanceDTO> result = attendanceService.markAttendance(markAttendanceDTO);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                verify(attendanceRepository).saveAll(any());
        }

        @Test
        void markAttendance_WithAbsentStudents_ShouldMarkCorrectStatus() {
                // Arrange
                when(timeSlotRepository.findById(any())).thenReturn(Optional.of(timeSlot));
                when(sectionRepository.findById(any())).thenReturn(Optional.of(section));
                when(sectionScheduleRepository.findBySectionId(any())).thenReturn(Optional.of(schedule));
                when(timeSlotRepository.existsByIdAndScheduleId(any(), any())).thenReturn(true);
                when(attendanceRepository.findByTimeSlotAndDate(any(), any())).thenReturn(Collections.emptyList());

                MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
                markAttendanceDTO.setTimeSlotId(1);
                markAttendanceDTO.setDateTime(testDateTime.format(DATE_TIME_FORMATTER));
                markAttendanceDTO.setAbsentStudentIds(Collections.singletonList(student.getId()));

                List<Attendance> mockAttendances = Arrays.asList(
                                createAttendance(student, AttendanceStatus.ABSENT));
                when(attendanceRepository.saveAll(any())).thenReturn(mockAttendances);

                // Act
                List<AttendanceDTO> result = attendanceService.markAttendance(markAttendanceDTO);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(AttendanceStatus.ABSENT, result.get(0).getStatus());
                verify(attendanceRepository).saveAll(any());
        }

        @Test
        void markAttendance_WithLateStudents_ShouldMarkCorrectStatus() {
                // Arrange
                when(timeSlotRepository.findById(any())).thenReturn(Optional.of(timeSlot));
                when(sectionRepository.findById(any())).thenReturn(Optional.of(section));
                when(sectionScheduleRepository.findBySectionId(any())).thenReturn(Optional.of(schedule));
                when(timeSlotRepository.existsByIdAndScheduleId(any(), any())).thenReturn(true);
                when(attendanceRepository.findByTimeSlotAndDate(any(), any())).thenReturn(Collections.emptyList());

                MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
                markAttendanceDTO.setTimeSlotId(1);
                markAttendanceDTO.setDateTime(testDateTime.format(DATE_TIME_FORMATTER));

                StudentAttendanceDTO lateStudent = new StudentAttendanceDTO();
                lateStudent.setStudentId(student.getId());
                lateStudent.setFeedback("Traffic delay");
                markAttendanceDTO.setLateStudents(Collections.singletonList(lateStudent));

                List<Attendance> mockAttendances = Arrays.asList(
                                createAttendance(student, AttendanceStatus.LATE));
                when(attendanceRepository.saveAll(any())).thenReturn(mockAttendances);

                // Act
                List<AttendanceDTO> result = attendanceService.markAttendance(markAttendanceDTO);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(AttendanceStatus.LATE, result.get(0).getStatus());
                verify(attendanceRepository).saveAll(any());
        }

        @Test
        void getStudentAttendance_ShouldReturnAttendanceRecords() {
                // Arrange
                UUID studentId = student.getId();
                LocalDateTime startDate = testDateTime.minusDays(7);
                LocalDateTime endDate = testDateTime;

                Attendance attendance = createAttendance(student, AttendanceStatus.PRESENT);
                List<Attendance> mockAttendances = Collections.singletonList(attendance);

                when(attendanceRepository.findByStudentAndDateBetween(any(), any(), any()))
                                .thenReturn(mockAttendances);

                // Act
                List<AttendanceDTO> result = attendanceService.getStudentAttendance(studentId, startDate, endDate);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                verify(attendanceRepository).findByStudentAndDateBetween(any(), any(), any());
        }

        @Test
        void getTimeSlotAttendance_ShouldReturnAttendanceRecords() {
                // Arrange
                Integer timeSlotId = 1;
                LocalDateTime date = testDateTime;

                Attendance attendance = createAttendance(student, AttendanceStatus.PRESENT);
                List<Attendance> mockAttendances = Collections.singletonList(attendance);

                when(attendanceRepository.findByTimeSlotAndDate(any(), any()))
                                .thenReturn(mockAttendances);

                // Act
                List<AttendanceDTO> result = attendanceService.getTimeSlotAttendance(timeSlotId, date);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                verify(attendanceRepository).findByTimeSlotAndDate(any(), any());
        }

        @Test
        void testIntegrationWithRealData() {
                // Get a real student
                List<StudentDTO> students = studentService.getAllStudents();
                assertFalse(students.isEmpty(), "No students found in the system");
                StudentDTO realStudent = students.get(0);

                // Get all sections
                List<SectionDTO> sections = sectionService.getAllSections();
                assertFalse(sections.isEmpty(), "No sections found in the system");

                // Find a section that has the student
                SectionDTO studentSection = sections.stream()
                                .filter(section -> section.getStudents().stream()
                                                .anyMatch(s -> s.getId().equals(realStudent.getId())))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Student is not enrolled in any section"));

                // Get time slots for the student's section
                List<TimeSlotDTO> timeSlots = timeSlotService.getTimeSlotsBySection(studentSection.getId());
                if (timeSlots.isEmpty()) {
                        // Skip test if no time slots found
                        return;
                }
                TimeSlotDTO realTimeSlot = timeSlots.get(0);

                // 1. Mark attendance
                MarkAttendanceDTO markDTO = new MarkAttendanceDTO();
                markDTO.setTimeSlotId(realTimeSlot.getId());
                markDTO.setDateTime(testDateTime.format(DATE_TIME_FORMATTER));

                List<AttendanceDTO> markedAttendance = attendanceService.markAttendance(markDTO);
                assertNotNull(markedAttendance);
                assertFalse(markedAttendance.isEmpty());

                // 2. Get attendance for the student
                List<AttendanceDTO> studentAttendance = attendanceService.getStudentAttendance(
                        UUID.fromString(realStudent.getId()),
                                testDateTime.minusDays(1),
                                testDateTime.plusDays(1));
                assertNotNull(studentAttendance);
                assertFalse(studentAttendance.isEmpty());
                assertEquals(realStudent.getId(), studentAttendance.get(0).getStudentId());

                // 3. Get attendance for the time slot
                List<AttendanceDTO> timeSlotAttendance = attendanceService.getTimeSlotAttendance(
                                realTimeSlot.getId(),
                                testDateTime);
                assertNotNull(timeSlotAttendance);
                assertFalse(timeSlotAttendance.isEmpty());
                assertEquals(realTimeSlot.getId(), timeSlotAttendance.get(0).getTimeSlotId());
        }

        private Attendance createAttendance(Student student, AttendanceStatus status) {
                Attendance attendance = new Attendance();
                attendance.setStudent(student);
                attendance.setTimeSlot(timeSlot);
                attendance.setStatus(status);
                attendance.setDate(testDateTime);
                attendance.setPostedAt(testDateTime);
                return attendance;
        }
}