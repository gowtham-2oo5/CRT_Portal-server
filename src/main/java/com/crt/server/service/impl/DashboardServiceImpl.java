package com.crt.server.service.impl;

import com.crt.server.dto.DashboardMetricsDTO;
import com.crt.server.model.Role;
import com.crt.server.repository.*;
import com.crt.server.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public DashboardMetricsDTO getDashboardMetrics() {
        log.info("Calculating dashboard metrics");
        
        try {
            Long totalStudents = studentRepository.count();
            Long totalFaculties = userRepository.countByRole(Role.FACULTY);
            Long totalSections = sectionRepository.count();
            Long totalRooms = roomRepository.count();
            Long totalTimeSlots = timeSlotRepository.count();
            Long activeStudents = studentRepository.countByCrtEligibility(true);
            Long totalAttendanceRecords = attendanceRepository.count();

            DashboardMetricsDTO metrics = DashboardMetricsDTO.builder()
                    .totalStudents(totalStudents)
                    .totalFaculties(totalFaculties)
                    .totalSections(totalSections)
                    .totalRooms(totalRooms)
                    .totalTimeSlots(totalTimeSlots)
                    .activeStudents(activeStudents)
                    .totalAttendanceRecords(totalAttendanceRecords)
                    .build();

            log.info("Dashboard metrics calculated: Students={}, Faculties={}, Sections={}, Rooms={}", 
                    totalStudents, totalFaculties, totalSections, totalRooms);

            return metrics;
            
        } catch (Exception e) {
            log.error("Error calculating dashboard metrics: {}", e.getMessage());
            // Return empty metrics in case of error
            return DashboardMetricsDTO.builder()
                    .totalStudents(0L)
                    .totalFaculties(0L)
                    .totalSections(0L)
                    .totalRooms(0L)
                    .totalTimeSlots(0L)
                    .activeStudents(0L)
                    .totalAttendanceRecords(0L)
                    .build();
        }
    }
}
