package com.crt.server.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;
    private String event;
    private Object data;
    private String userId;
    private String sessionId;
    private LocalDateTime timestamp;
    
    public static WebSocketMessage create(String event, Object data) {
        return WebSocketMessage.builder()
                .type("EVENT")
                .event(event)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static WebSocketMessage createForUser(String event, Object data, String userId) {
        return WebSocketMessage.builder()
                .type("USER_EVENT")
                .event(event)
                .data(data)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
