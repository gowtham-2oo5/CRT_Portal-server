package com.crt.server.controller;

import com.crt.server.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketService webSocketService;

    /**
     * Handle faculty joining their personal room
     */
    @MessageMapping("/faculty/join")
    public void joinFacultyRoom(@Payload Map<String, String> payload, Principal principal) {
        String facultyId = payload.get("facultyId");
        log.info("Faculty {} joined their personal room (Principal: {})", facultyId, 
                 principal != null ? principal.getName() : "null");
        
        try {
            // Send welcome message
            webSocketService.sendToUser(facultyId, "connection_established", 
                Map.of("message", "Connected to faculty room", 
                       "facultyId", facultyId,
                       "timestamp", System.currentTimeMillis()));
            
            log.info("Welcome message sent to faculty {}", facultyId);
        } catch (Exception e) {
            log.error("Error sending welcome message to faculty {}: {}", facultyId, e.getMessage());
        }
    }

    /**
     * Handle faculty subscribing to session updates
     */
    @SubscribeMapping("/topic/faculty_{facultyId}")
    public void subscribeFacultyUpdates(@DestinationVariable String facultyId, Principal principal) {
        log.info("Faculty {} subscribed to session updates (Principal: {})", facultyId,
                 principal != null ? principal.getName() : "null");
        
        try {
            // Send current session status if any
            webSocketService.sendToUser(facultyId, "subscription_confirmed", 
                Map.of("message", "Subscribed to session updates", 
                       "facultyId", facultyId,
                       "timestamp", System.currentTimeMillis()));
            
            log.info("Subscription confirmation sent to faculty {}", facultyId);
        } catch (Exception e) {
            log.error("Error sending subscription confirmation to faculty {}: {}", facultyId, e.getMessage());
        }
    }

    /**
     * Handle attendance session subscription
     */
    @MessageMapping("/attendance/join")
    public void joinAttendanceSession(@Payload Map<String, String> payload, Principal principal) {
        String sessionId = payload.get("sessionId");
        String sectionId = payload.get("sectionId");
        log.info("Joined attendance session {} for section {}", sessionId, sectionId);
        
        webSocketService.sendToTopic("section_" + sectionId, "user_joined_session", 
            Map.of("sessionId", sessionId, "user", principal.getName()));
    }

    /**
     * Handle admin dashboard subscription
     */
    @SubscribeMapping("/topic/admin_dashboard")
    public void subscribeAdminDashboard(Principal principal) {
        log.info("Admin {} subscribed to dashboard updates", principal.getName());
        
        webSocketService.sendToTopic("admin_dashboard", "admin_connected", 
            Map.of("admin", principal.getName(), "timestamp", System.currentTimeMillis()));
    }

    /**
     * Handle system alerts subscription
     */
    @SubscribeMapping("/topic/system_alerts")
    public void subscribeSystemAlerts(Principal principal) {
        log.info("User {} subscribed to system alerts", principal.getName());
    }

    /**
     * Handle heartbeat/ping messages
     */
    @MessageMapping("/ping")
    public void handlePing(@Payload Map<String, Object> payload, Principal principal) {
        String userId = principal.getName();
        log.debug("Received ping from user: {}", userId);
        
        webSocketService.sendToUser(userId, "pong", 
            Map.of("timestamp", System.currentTimeMillis(), "status", "alive"));
    }
}
