package com.crt.server.service;

import com.crt.server.dto.websocket.FacultySessionEvent;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import com.crt.server.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultySessionSchedulerService {

    private final TimeSlotRepository timeSlotRepository;
    private final WebSocketService webSocketService;
    
    // Track active sessions
    private final ConcurrentHashMap<String, TimeSlot> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> warningsSent = new ConcurrentHashMap<>();

    /**
     * Check for session updates every minute
     */
    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void checkSessionUpdates() {
        LocalTime now = LocalTime.now();
        log.debug("Checking session updates at {}", now);
        
        // Get all time slots (you might want to optimize this query)
        List<TimeSlot> allTimeSlots = timeSlotRepository.findAll();
        
        for (TimeSlot timeSlot : allTimeSlots) {
            if (timeSlot.isBreak() || timeSlot.getInchargeFaculty() == null) {
                continue;
            }
            
            String facultyId = timeSlot.getInchargeFaculty().getId().toString();
            String sessionKey = facultyId + "_" + timeSlot.getId();
            
            try {
                LocalTime startTime = LocalTime.parse(timeSlot.getStartTime());
                LocalTime endTime = LocalTime.parse(timeSlot.getEndTime());
                
                // Check if session is starting (within 1 minute of start time)
                if (isTimeWithinMinutes(now, startTime, 1) && !activeSessions.containsKey(sessionKey)) {
                    handleSessionStarting(timeSlot, facultyId, sessionKey);
                }
                
                // Check if session is active
                else if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    handleActiveSession(timeSlot, facultyId, sessionKey, now, endTime);
                }
                
                // Check if session is ending
                else if (isTimeWithinMinutes(now, endTime, 1) && activeSessions.containsKey(sessionKey)) {
                    handleSessionEnding(timeSlot, facultyId, sessionKey);
                }
                
                // Check for next session warning (15 minutes before)
                else if (isTimeWithinMinutes(now, startTime.minusMinutes(15), 1)) {
                    handleNextSessionWarning(timeSlot, facultyId, sessionKey);
                }
                
            } catch (Exception e) {
                log.error("Error processing time slot {}: {}", timeSlot.getId(), e.getMessage());
            }
        }
        
        // Clean up ended sessions
        cleanupEndedSessions();
    }

    private void handleSessionStarting(TimeSlot timeSlot, String facultyId, String sessionKey) {
        log.info("Session starting for faculty {} - TimeSlot {}", facultyId, timeSlot.getId());
        
        activeSessions.put(sessionKey, timeSlot);
        
        FacultySessionEvent event = FacultySessionEvent.builder()
                .facultyId(facultyId)
                .timeSlotId(timeSlot.getId().toString())
                .sectionId(timeSlot.getSection().getId().toString())
                .sectionName(timeSlot.getSection().getName())
                .room(timeSlot.getRoom().toString())
                .startTime(LocalTime.parse(timeSlot.getStartTime()))
                .endTime(LocalTime.parse(timeSlot.getEndTime()))
                .status("STARTING")
                .build();
        
        webSocketService.notifyFacultySessionStarted(facultyId, event);
    }

    private void handleActiveSession(TimeSlot timeSlot, String facultyId, String sessionKey, 
                                   LocalTime now, LocalTime endTime) {
        
        if (!activeSessions.containsKey(sessionKey)) {
            activeSessions.put(sessionKey, timeSlot);
        }
        
        int minutesRemaining = (int) now.until(endTime, ChronoUnit.MINUTES);
        
        FacultySessionEvent event = FacultySessionEvent.builder()
                .facultyId(facultyId)
                .timeSlotId(timeSlot.getId().toString())
                .sectionId(timeSlot.getSection().getId().toString())
                .sectionName(timeSlot.getSection().getName())
                .room(timeSlot.getRoom().toString())
                .startTime(LocalTime.parse(timeSlot.getStartTime()))
                .endTime(LocalTime.parse(timeSlot.getEndTime()))
                .status("ACTIVE")
                .minutesRemaining(minutesRemaining)
                .build();
        
        webSocketService.notifyFacultySessionUpdated(facultyId, event);
    }

    private void handleSessionEnding(TimeSlot timeSlot, String facultyId, String sessionKey) {
        log.info("Session ending for faculty {} - TimeSlot {}", facultyId, timeSlot.getId());
        
        FacultySessionEvent event = FacultySessionEvent.builder()
                .facultyId(facultyId)
                .timeSlotId(timeSlot.getId().toString())
                .sectionId(timeSlot.getSection().getId().toString())
                .sectionName(timeSlot.getSection().getName())
                .room(timeSlot.getRoom().toString())
                .startTime(LocalTime.parse(timeSlot.getStartTime()))
                .endTime(LocalTime.parse(timeSlot.getEndTime()))
                .status("ENDED")
                .build();
        
        webSocketService.notifyFacultySessionEnded(facultyId, event);
        activeSessions.remove(sessionKey);
        warningsSent.remove(sessionKey);
    }

    private void handleNextSessionWarning(TimeSlot timeSlot, String facultyId, String sessionKey) {
        String warningKey = sessionKey + "_warning";
        
        if (!warningsSent.containsKey(warningKey)) {
            log.info("Sending 15-minute warning for faculty {} - TimeSlot {}", facultyId, timeSlot.getId());
            
            FacultySessionEvent event = FacultySessionEvent.builder()
                    .facultyId(facultyId)
                    .timeSlotId(timeSlot.getId().toString())
                    .sectionId(timeSlot.getSection().getId().toString())
                    .sectionName(timeSlot.getSection().getName())
                    .room(timeSlot.getRoom().toString())
                    .startTime(LocalTime.parse(timeSlot.getStartTime()))
                    .endTime(LocalTime.parse(timeSlot.getEndTime()))
                    .status("UPCOMING")
                    .minutesRemaining(15)
                    .build();
            
            webSocketService.notifyNextSessionWarning(facultyId, event);
            warningsSent.put(warningKey, true);
        }
    }

    private void cleanupEndedSessions() {
        LocalTime now = LocalTime.now();
        activeSessions.entrySet().removeIf(entry -> {
            try {
                TimeSlot timeSlot = entry.getValue();
                LocalTime endTime = LocalTime.parse(timeSlot.getEndTime());
                return now.isAfter(endTime.plusMinutes(5)); // Clean up 5 minutes after end
            } catch (Exception e) {
                log.error("Error cleaning up session {}: {}", entry.getKey(), e.getMessage());
                return true; // Remove problematic entries
            }
        });
    }

    private boolean isTimeWithinMinutes(LocalTime current, LocalTime target, int minutes) {
        return Math.abs(current.until(target, ChronoUnit.MINUTES)) <= minutes;
    }

    // Public method to get active sessions (for debugging/monitoring)
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
