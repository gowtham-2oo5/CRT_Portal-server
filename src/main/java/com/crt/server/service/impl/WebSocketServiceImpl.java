package com.crt.server.service.impl;

import com.crt.server.dto.websocket.AttendanceSessionEvent;
import com.crt.server.dto.websocket.FacultySessionEvent;
import com.crt.server.dto.websocket.WebSocketMessage;
import com.crt.server.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyFacultySessionStarted(String facultyId, FacultySessionEvent event) {
        log.info("Notifying faculty {} of session started", facultyId);
        WebSocketMessage message = WebSocketMessage.createForUser(
            FacultySessionEvent.SESSION_STARTED, event, facultyId);
        sendToUser(facultyId, FacultySessionEvent.SESSION_STARTED, event);
    }

    @Override
    public void notifyFacultySessionEnded(String facultyId, FacultySessionEvent event) {
        log.info("Notifying faculty {} of session ended", facultyId);
        sendToUser(facultyId, FacultySessionEvent.SESSION_ENDED, event);
    }

    @Override
    public void notifyFacultySessionUpdated(String facultyId, FacultySessionEvent event) {
        log.debug("Updating faculty {} session status", facultyId);
        sendToUser(facultyId, FacultySessionEvent.SESSION_UPDATED, event);
    }

    @Override
    public void notifyNextSessionWarning(String facultyId, FacultySessionEvent event) {
        log.info("Warning faculty {} of next session in 15 minutes", facultyId);
        sendToUser(facultyId, FacultySessionEvent.NEXT_SESSION_WARNING, event);
    }

    @Override
    public void notifyAttendanceSessionStarted(String sectionId, AttendanceSessionEvent event) {
        log.info("Notifying section {} of attendance session started", sectionId);
        sendToTopic("section_" + sectionId, AttendanceSessionEvent.SESSION_STARTED, event);
    }

    @Override
    public void notifyStudentMarked(String sectionId, AttendanceSessionEvent event) {
        log.debug("Notifying section {} of student marked", sectionId);
        sendToTopic("section_" + sectionId, AttendanceSessionEvent.STUDENT_MARKED, event);
    }

    @Override
    public void notifyAttendanceSessionCompleted(String sectionId, AttendanceSessionEvent event) {
        log.info("Notifying section {} of attendance session completed", sectionId);
        sendToTopic("section_" + sectionId, AttendanceSessionEvent.SESSION_COMPLETED, event);
    }

    @Override
    public void notifyAttendanceStatsUpdated(String sectionId, AttendanceSessionEvent event) {
        log.debug("Updating attendance stats for section {}", sectionId);
        sendToTopic("section_" + sectionId, AttendanceSessionEvent.STATS_UPDATED, event);
    }

    @Override
    public void notifyAdminDashboard(Object dashboardData) {
        log.debug("Updating admin dashboard");
        sendToTopic("admin_dashboard", "dashboard_updated", dashboardData);
    }

    @Override
    public void notifySystemAlert(String message, String severity) {
        log.warn("System alert: {} ({})", message, severity);
        WebSocketMessage alert = WebSocketMessage.builder()
                .type("ALERT")
                .event("system_alert")
                .data(new SystemAlert(message, severity))
                .build();
        sendToTopic("system_alerts", "system_alert", alert);
    }

    @Override
    public void sendToUser(String userId, String event, Object data) {
        try {
            log.info("Attempting to send message to user {}: {}", userId, event);
            WebSocketMessage message = WebSocketMessage.createForUser(event, data, userId);
            log.info("Created WebSocket message: {}", message);
            
            messagingTemplate.convertAndSendToUser(userId, "/queue/messages", message);
            log.info("Successfully sent message to user {}: {}", userId, event);
        } catch (Exception e) {
            log.error("Failed to send message to user {}: {}", userId, e.getMessage(), e);
        }
    }

    @Override
    public void sendToTopic(String topic, String event, Object data) {
        try {
            log.info("Attempting to send message to topic {}: {}", topic, event);
            WebSocketMessage message = WebSocketMessage.create(event, data);
            log.info("Created WebSocket message for topic: {}", message);
            
            messagingTemplate.convertAndSend("/topic/" + topic, message);
            log.info("Successfully sent message to topic {}: {}", topic, event);
        } catch (Exception e) {
            log.error("Failed to send message to topic {}: {}", topic, e.getMessage(), e);
        }
    }

    @Override
    public void broadcastToAllFaculty(String event, Object data) {
        try {
            WebSocketMessage message = WebSocketMessage.create(event, data);
            messagingTemplate.convertAndSend("/topic/all_faculty", message);
            log.debug("Broadcasted message to all faculty: {}", event);
        } catch (Exception e) {
            log.error("Failed to broadcast to all faculty: {}", e.getMessage());
        }
    }

    // Inner class for system alerts
    private static class SystemAlert {
        public final String message;
        public final String severity;
        public final long timestamp;

        public SystemAlert(String message, String severity) {
            this.message = message;
            this.severity = severity;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
