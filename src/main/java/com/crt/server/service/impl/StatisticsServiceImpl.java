package com.crt.server.service.impl;

import com.crt.server.dto.StatisticsDTO;
import com.crt.server.model.AttendanceStatus;
import com.crt.server.model.Role;
import com.crt.server.model.Student;
import com.crt.server.repository.AttendanceRepository;
import com.crt.server.repository.SectionRepository;
import com.crt.server.repository.StudentRepository;
import com.crt.server.repository.UserRepository;
import com.crt.server.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the StatisticsService
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SectionRepository sectionRepository;

    @Override
    @Transactional(readOnly = true)
    public long getCrtEligibleStudentCount() {
        return studentRepository.countByCrtEligibility(true);
    }

    @Override
    @Transactional(readOnly = true)
    public long getDepartmentCount() {
        // Get distinct departments from students
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(Student::getBranch)
                .distinct()
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageStudentAttendance() {
        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            return 0.0;
        }
        
        // Calculate attendance for the last 30 days
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        double totalAttendancePercentage = 0.0;
        int studentCount = 0;
        
        for (Student student : students) {
            long totalAttendance = attendanceRepository.countAttendanceByStudentAndDateRange(student, startDate, endDate);
            if (totalAttendance > 0) {
                long absences = attendanceRepository.countAbsencesByStudentAndDateRange(student, startDate, endDate);
                double attendancePercentage = ((double)(totalAttendance - absences) / totalAttendance) * 100.0;
                totalAttendancePercentage += attendancePercentage;
                studentCount++;
            }
        }
        
        return studentCount > 0 ? totalAttendancePercentage / studentCount : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsDTO getAllStatistics() {
        long crtEligibleCount = getCrtEligibleStudentCount();
        long departmentCount = getDepartmentCount();
        double avgAttendance = getAverageStudentAttendance();
        long totalStudents = studentRepository.count();
        long totalSections = sectionRepository.count();
        long totalFaculty = userRepository.countByRole(Role.FACULTY);
        
        return StatisticsDTO.builder()
                .crtEligibleStudentCount(crtEligibleCount)
                .departmentCount(departmentCount)
                .averageAttendance(avgAttendance)
                .totalStudents(totalStudents)
                .totalSections(totalSections)
                .totalFaculty(totalFaculty)
                .build();
    }
}
