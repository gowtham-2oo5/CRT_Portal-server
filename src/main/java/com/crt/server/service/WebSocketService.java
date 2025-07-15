package com.crt.server.service;

import com.crt.server.dto.websocket.AttendanceSessionEvent;
import com.crt.server.dto.websocket.FacultySessionEvent;
import com.crt.server.dto.websocket.WebSocketMessage;

public interface WebSocketService {
    
    // Faculty Session Management
    void notifyFacultySessionStarted(String facultyId, FacultySessionEvent event);
    void notifyFacultySessionEnded(String facultyId, FacultySessionEvent event);
    void notifyFacultySessionUpdated(String facultyId, FacultySessionEvent event);
    void notifyNextSessionWarning(String facultyId, FacultySessionEvent event);
    
    // Attendance Session Management
    void notifyAttendanceSessionStarted(String sectionId, AttendanceSessionEvent event);
    void notifyStudentMarked(String sectionId, AttendanceSessionEvent event);
    void notifyAttendanceSessionCompleted(String sectionId, AttendanceSessionEvent event);
    void notifyAttendanceStatsUpdated(String sectionId, AttendanceSessionEvent event);
    
    // Admin Monitoring
    void notifyAdminDashboard(Object dashboardData);
    void notifySystemAlert(String message, String severity);
    
    // Generic messaging
    void sendToUser(String userId, String event, Object data);
    void sendToTopic(String topic, String event, Object data);
    void broadcastToAllFaculty(String event, Object data);
}
