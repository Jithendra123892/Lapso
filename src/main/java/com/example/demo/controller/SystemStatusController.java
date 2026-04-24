package com.example.demo.controller;

import com.example.demo.service.ContinuousOperationService;
import com.example.demo.service.DeviceActionService;
import com.example.demo.service.WebSocketService;
import com.example.demo.service.LapsoIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * SYSTEM STATUS CONTROLLER
 * Provides real-time status for 24/7 operation monitoring
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemStatusController {

    @Autowired
    private ContinuousOperationService continuousOperationService;
    
    @Autowired
    private DeviceActionService deviceActionService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private LapsoIntegrationService integrationService;

    /**
     * Get comprehensive system status for 24/7 monitoring
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Basic system info
            status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            status.put("status", "OPERATIONAL");
            status.put("mode", "24/7_CONTINUOUS");
            
            // Continuous operation metrics
            Map<String, Object> operationMetrics = continuousOperationService.getSystemMetrics();
            status.put("operationMetrics", operationMetrics);
            
            // Device statistics
            status.put("totalDevices", deviceActionService.getDeviceCount());
            status.put("activeWebSocketSessions", webSocketService.getActiveSessionCount());
            
            // System health
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> systemHealth = new HashMap<>();
            systemHealth.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
            systemHealth.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
            systemHealth.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
            systemHealth.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
            systemHealth.put("availableProcessors", runtime.availableProcessors());
            systemHealth.put("activeThreads", Thread.activeCount());
            status.put("systemHealth", systemHealth);
            
            // Network status
            Map<String, Object> networkStatus = new HashMap<>();
            try {
                boolean isConnected = java.net.InetAddress.getByName("google.com").isReachable(5000);
                networkStatus.put("internetConnectivity", isConnected ? "CONNECTED" : "DISCONNECTED");
            } catch (Exception e) {
                networkStatus.put("internetConnectivity", "ERROR");
            }
            status.put("networkStatus", networkStatus);
            
            // Service health
            Map<String, Object> serviceHealth = new HashMap<>();
            serviceHealth.put("continuousOperation", continuousOperationService.isOperational() ? "HEALTHY" : "DEGRADED");
            serviceHealth.put("webSocketService", webSocketService.isHealthy() ? "HEALTHY" : "DEGRADED");
            serviceHealth.put("deviceService", "HEALTHY"); // Assume healthy if we got device count
            status.put("serviceHealth", serviceHealth);
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.status(500).body(status);
        }
    }

    /**
     * Get simple health check for monitoring tools
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            boolean isOperational = continuousOperationService.isOperational();
            boolean webSocketHealthy = webSocketService.isHealthy();
            
            health.put("status", (isOperational && webSocketHealthy) ? "UP" : "DOWN");
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            health.put("uptime", getUptimeInfo());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Get detailed performance metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get all continuous operation metrics
            metrics.putAll(continuousOperationService.getSystemMetrics());
            
            // Add additional performance data
            Runtime runtime = Runtime.getRuntime();
            metrics.put("jvmMetrics", Map.of(
                "maxMemory", runtime.maxMemory(),
                "totalMemory", runtime.totalMemory(),
                "freeMemory", runtime.freeMemory(),
                "processors", runtime.availableProcessors()
            ));
            
            // Device metrics
            metrics.put("deviceMetrics", Map.of(
                "totalDevices", deviceActionService.getDeviceCount(),
                "activeConnections", webSocketService.getActiveSessionCount()
            ));
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));
        }
    }

    /**
     * Get comprehensive LAPSO system status
     */
    @GetMapping("/lapso-status")
    public ResponseEntity<Map<String, Object>> getLapsoSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Get comprehensive system health from integration service
            LapsoIntegrationService.SystemHealth health = integrationService.getSystemHealth();
            
            status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            status.put("status", health.isHealthy() ? "OPERATIONAL" : "DEGRADED");
            status.put("version", "2.0.0-Free-Honest");
            status.put("name", "LAPSO - Free & Open Source");
            
            // Integration service health
            Map<String, Boolean> services = new HashMap<>();
            services.put("database", health.isDatabaseConnected());
            services.put("authentication", health.isAuthServiceActive());
            services.put("deviceService", health.isDeviceServiceActive());
            services.put("locationService", health.isLocationServiceActive());
            services.put("webSocket", health.isWebSocketActive());
            services.put("realTime", health.isRealTimeActive());
            status.put("services", services);
            
            // Statistics
            status.put("totalUsers", health.getUserCount());
            status.put("totalDevices", health.getDeviceCount());
            
            // Legacy metrics for compatibility
            status.put("activeWebSocketSessions", webSocketService.getActiveSessionCount());
            
            if (!health.isHealthy() && health.getErrorMessage() != null) {
                status.put("error", health.getErrorMessage());
            }
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.status(500).body(status);
        }
    }

    /**
     * Get device status through integration service
     */
    @GetMapping("/device/{deviceId}/status")
    public ResponseEntity<Map<String, Object>> getDeviceStatus(@PathVariable String deviceId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LapsoIntegrationService.DeviceStatus deviceStatus = integrationService.getComprehensiveDeviceStatus(deviceId);
            
            if (!deviceStatus.isFound()) {
                response.put("found", false);
                response.put("error", "Device not found");
                return ResponseEntity.status(404).body(response);
            }
            
            response.put("found", true);
            response.put("device", deviceStatus.getDevice());
            response.put("locationAvailable", deviceStatus.isLocationAvailable());
            response.put("realTimeActive", deviceStatus.isRealTimeActive());
            response.put("geofenceViolations", deviceStatus.getGeofenceViolations());
            response.put("analytics", deviceStatus.getAnalytics());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            if (deviceStatus.getError() != null) {
                response.put("error", deviceStatus.getError());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Trigger manual system health check
     */
    @PostMapping("/health-check")
    public ResponseEntity<Map<String, Object>> triggerHealthCheck() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Perform comprehensive health check
            boolean systemHealthy = performSystemHealthCheck();
            
            result.put("healthCheckTriggered", true);
            result.put("systemHealthy", systemHealthy);
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("recommendations", getHealthRecommendations(systemHealthy));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("healthCheckTriggered", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Get system uptime information
     */
    private Map<String, Object> getUptimeInfo() {
        Map<String, Object> uptime = new HashMap<>();
        
        try {
            long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
            long uptimeSeconds = uptimeMs / 1000;
            long uptimeMinutes = uptimeSeconds / 60;
            long uptimeHours = uptimeMinutes / 60;
            long uptimeDays = uptimeHours / 24;
            
            uptime.put("uptimeMs", uptimeMs);
            uptime.put("uptimeSeconds", uptimeSeconds);
            uptime.put("uptimeMinutes", uptimeMinutes);
            uptime.put("uptimeHours", uptimeHours);
            uptime.put("uptimeDays", uptimeDays);
            uptime.put("uptimeFormatted", String.format("%d days, %d hours, %d minutes", 
                uptimeDays, uptimeHours % 24, uptimeMinutes % 60));
            
        } catch (Exception e) {
            uptime.put("error", "Unable to calculate uptime: " + e.getMessage());
        }
        
        return uptime;
    }

    /**
     * Perform comprehensive system health check
     */
    private boolean performSystemHealthCheck() {
        try {
            // Check continuous operation service
            if (!continuousOperationService.isOperational()) {
                return false;
            }
            
            // Check WebSocket service
            if (!webSocketService.isHealthy()) {
                return false;
            }
            
            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsage = (double) usedMemory / maxMemory;
            
            if (memoryUsage > 0.9) {
                return false; // Memory usage too high
            }
            
            // Check device service
            try {
                deviceActionService.getDeviceCount();
            } catch (Exception e) {
                return false; // Device service not responding
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get health recommendations based on system status
     */
    private Map<String, Object> getHealthRecommendations(boolean systemHealthy) {
        Map<String, Object> recommendations = new HashMap<>();
        
        if (systemHealthy) {
            recommendations.put("status", "HEALTHY");
            recommendations.put("message", "System is operating normally");
            recommendations.put("actions", new String[]{
                "Continue monitoring",
                "Regular maintenance scheduled"
            });
        } else {
            recommendations.put("status", "NEEDS_ATTENTION");
            recommendations.put("message", "System requires attention");
            recommendations.put("actions", new String[]{
                "Check system logs",
                "Restart services if needed",
                "Monitor resource usage",
                "Contact support if issues persist"
            });
        }
        
        return recommendations;
    }
}
