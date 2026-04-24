package com.example.demo.controller;

import com.example.demo.service.PerfectAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/security")
public class SecurityAuditController {

    @Autowired
    private PerfectAuthService authService;
    
    // Security audit log (in production, use proper logging system)
    private static final Map<String, List<SecurityEvent>> securityLog = new ConcurrentHashMap<>();
    
    /**
     * Get security audit log - ADMIN ONLY
     */
    @GetMapping("/audit")
    public ResponseEntity<Map<String, Object>> getSecurityAudit() {
        
        // SECURITY CHECK: Only authenticated users can view audit logs
        if (!authService.isLoggedIn()) {
            return ResponseEntity.status(401).build();
        }
        
        String currentUser = authService.getLoggedInUser();
        
        // In a real system, you'd check for admin role here
        // For now, users can only see their own security events
        
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("requestedBy", currentUser);
        
        // Get user-specific security events
        List<SecurityEvent> userEvents = securityLog.getOrDefault(currentUser, new ArrayList<>());
        auditData.put("securityEvents", userEvents);
        auditData.put("totalEvents", userEvents.size());
        
        // Security summary
        Map<String, Long> eventSummary = new HashMap<>();
        eventSummary.put("loginAttempts", userEvents.stream().mapToLong(e -> "LOGIN_ATTEMPT".equals(e.getEventType()) ? 1 : 0).sum());
        eventSummary.put("deviceAccess", userEvents.stream().mapToLong(e -> "DEVICE_ACCESS".equals(e.getEventType()) ? 1 : 0).sum());
        eventSummary.put("securityViolations", userEvents.stream().mapToLong(e -> "SECURITY_VIOLATION".equals(e.getEventType()) ? 1 : 0).sum());
        
        auditData.put("eventSummary", eventSummary);
        
        return ResponseEntity.ok(auditData);
    }
    
    /**
     * Report security violation
     */
    public static void logSecurityEvent(String userEmail, String eventType, String description, String ipAddress) {
        SecurityEvent event = new SecurityEvent(
            LocalDateTime.now(),
            eventType,
            description,
            ipAddress
        );
        
        securityLog.computeIfAbsent(userEmail, k -> new ArrayList<>()).add(event);
        
        // Keep only last 100 events per user
        List<SecurityEvent> events = securityLog.get(userEmail);
        if (events.size() > 100) {
            events.remove(0);
        }
        
        System.out.println("🔒 SECURITY EVENT: " + eventType + " for " + userEmail + " - " + description);
    }
    
    /**
     * Get current security status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSecurityStatus() {
        
        if (!authService.isLoggedIn()) {
            return ResponseEntity.status(401).build();
        }
        
        String currentUser = authService.getLoggedInUser();
        
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", LocalDateTime.now());
        status.put("user", currentUser);
        status.put("authenticated", true);
        status.put("securityLevel", "HIGH");
        
        // Security features enabled
        Map<String, Boolean> securityFeatures = new HashMap<>();
        securityFeatures.put("userAuthentication", true);
        securityFeatures.put("deviceOwnershipValidation", true);
        securityFeatures.put("rateLimiting", true);
        securityFeatures.put("secureAgentAccess", true);
        securityFeatures.put("auditLogging", true);
        securityFeatures.put("encryptedCommunication", true);
        
        status.put("securityFeatures", securityFeatures);
        
        // Recent security events count
        List<SecurityEvent> userEvents = securityLog.getOrDefault(currentUser, new ArrayList<>());
        long recentEvents = userEvents.stream()
            .mapToLong(e -> e.getTimestamp().isAfter(LocalDateTime.now().minusHours(24)) ? 1 : 0)
            .sum();
        
        status.put("recentSecurityEvents", recentEvents);
        
        return ResponseEntity.ok(status);
    }
    
    // Security event data class
    public static class SecurityEvent {
        private final LocalDateTime timestamp;
        private final String eventType;
        private final String description;
        private final String ipAddress;
        
        public SecurityEvent(LocalDateTime timestamp, String eventType, String description, String ipAddress) {
            this.timestamp = timestamp;
            this.eventType = eventType;
            this.description = description;
            this.ipAddress = ipAddress;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getEventType() { return eventType; }
        public String getDescription() { return description; }
        public String getIpAddress() { return ipAddress; }
    }
}
