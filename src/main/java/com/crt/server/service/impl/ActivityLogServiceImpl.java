package com.crt.server.service.impl;

import com.crt.server.dto.ActivityLogDTO;
import com.crt.server.model.Section;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import com.crt.server.service.ActivityLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private static final int MAX_LOG_SIZE = 20;
    
    private final LinkedList<ActivityLogDTO> activityLogs = new LinkedList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void logAttendancePosted(User faculty, Section section, TimeSlot timeSlot, Double attendancePercentage) {
        lock.writeLock().lock();
        try {
            String timeSlotInfo = String.format("%s-%s", 
                timeSlot.getStartTime(),
                timeSlot.getEndTime()
            );
            
            String action = String.format("%s - %s posted attendance for %s for %s and %.1f%%",
                faculty.getUsername(),
                faculty.getName(),
                section.getName(),
                timeSlotInfo,
                attendancePercentage
            );

            ActivityLogDTO logEntry = ActivityLogDTO.builder()
                    .action(action)
                    .timestamp(LocalDateTime.now())
                    .facultyId(faculty.getUsername())
                    .facultyName(faculty.getName())
                    .sectionName(section.getName())
                    .timeSlotInfo(timeSlotInfo)
                    .attendancePercentage(attendancePercentage)
                    .build();

            // Add to the beginning of the list (most recent first)
            activityLogs.addFirst(logEntry);

            // Remove oldest entries if we exceed the maximum size
            while (activityLogs.size() > MAX_LOG_SIZE) {
                activityLogs.removeLast();
            }

            log.info("Activity logged: {}", action);
            
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<ActivityLogDTO> getRecentActivities(int limit) {
        lock.readLock().lock();
        try {
            int actualLimit = Math.min(limit, activityLogs.size());
            return new ArrayList<>(activityLogs.subList(0, actualLimit));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ActivityLogDTO> getRecentActivities() {
        return getRecentActivities(MAX_LOG_SIZE);
    }
}
