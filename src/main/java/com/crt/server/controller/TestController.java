package com.crt.server.controller;

import com.crt.server.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequiredArgsConstructor
public class TestController {

    private final WebSocketService webSocketService;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        log.info("Test ping endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Server is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("server", "CRT Portal Core Server");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cors")
    public ResponseEntity<Map<String, Object>> testCors() {
        log.info("CORS test endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("cors", "enabled");
        response.put("message", "CORS is working");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> payload) {
        log.info("Echo endpoint called with payload: {}", payload);
        
        Map<String, Object> response = new HashMap<>();
        response.put("echo", payload);
        response.put("received_at", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/websocket/{facultyId}")
    public ResponseEntity<Map<String, Object>> testWebSocket(@PathVariable String facultyId) {
        log.info("Testing WebSocket for faculty: {}", facultyId);
        
        try {
            // Test sending a message via WebSocket
            webSocketService.sendToUser(facultyId, "test_message", 
                Map.of("message", "Test message from REST endpoint", 
                       "facultyId", facultyId,
                       "timestamp", System.currentTimeMillis()));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "WebSocket test message sent");
            response.put("facultyId", facultyId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("WebSocket test failed for faculty {}: {}", facultyId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "WebSocket test failed: " + e.getMessage());
            response.put("facultyId", facultyId);
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("components", Map.of(
            "database", "UP",
            "websocket", "UP",
            "cors", "ENABLED"
        ));
        
        return ResponseEntity.ok(response);
    }
}
