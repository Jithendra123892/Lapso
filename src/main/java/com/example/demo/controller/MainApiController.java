package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🎯 MAIN API CONTROLLER
 * Central API endpoint that uses all essential services
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MainApiController {
    
    // All essential services
    @Autowired private DeviceService deviceService;
    @Autowired private UserService userService;
    @Autowired private AnalyticsService analyticsService;
    @Autowired private NotificationService notificationService;
    @Autowired private WebSocketService webSocketService;
    @Autowired private RealTimeMonitoringService realTimeService;
    @Autowired private EnhancedLocationService locationService;
    @Autowired private GeofenceService geofenceService;
    @Autowired private EncryptionService encryptionService;
    @Autowired private PerfectAuthService authService;
    @Autowired private QuickActionsService quickActionsService;
    @Autowired private DeviceActionService deviceActionService;
    @Autowired private ContinuousOperationService continuousOperationService;

    @Autowired private AgentAuthenticationService agentAuthService;
    @Autowired private CoreIntegrationService coreIntegrationService;
    
    /**
     * Simple status endpoint
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "LAPSO is running");
        status.put("message", "Better than Microsoft Find My Device");
        status.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get all devices (for demo/anonymous access)
     */
    @GetMapping("/devices")
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }
    
    /**
     * Delete fake devices (for demo cleanup)
     */
    @DeleteMapping("/devices/cleanup")
    public ResponseEntity<Map<String, Object>> deleteFakeDevices() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // List of fake device IDs to delete
            List<String> fakeDeviceIds = List.of("register", "DEMO-LAPTOP-001");
            
            // Delete the fake devices
            deviceService.deleteDevicesByDeviceIds(fakeDeviceIds);
            
            response.put("success", true);
            response.put("message", "Fake devices cleaned up successfully");
            response.put("deletedDevices", fakeDeviceIds);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get comprehensive dashboard data using all services
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            String userEmail = authService.getLoggedInUser();
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }
            
            // Get devices from DeviceService
            List<Device> devices = deviceService.getCurrentUserDevices();
            dashboard.put("devices", devices);
            dashboard.put("deviceCount", devices.size());
            
            // Get analytics from AnalyticsService
            Map<String, Object> analytics = analyticsService.getDashboardAnalytics(userEmail);
            dashboard.put("analytics", analytics);
            
            // Get system health from CoreIntegrationService
            CoreIntegrationService.SystemHealthStatus health = coreIntegrationService.getSystemHealth();
            dashboard.put("systemHealth", health);
            
            // Get real-time status from RealTimeMonitoringService
            dashboard.put("realTimeActive", realTimeService != null);
            
            // Get WebSocket status
            dashboard.put("webSocketConnections", webSocketService.getActiveSessionCount());
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Device update endpoint using multiple services
     */
    @PostMapping("/device/update")
    public ResponseEntity<Map<String, Object>> updateDevice(@RequestBody Map<String, Object> deviceData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String deviceId = (String) deviceData.get("deviceId");
            if (deviceId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Device ID required"));
            }
            
            // Update device using DeviceService
            Device device = deviceService.updateDevice(deviceId, deviceData);
            if (device == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Device not found"));
            }
            
            // Update location using EnhancedLocationService
            if (deviceData.containsKey("latitude") && deviceData.containsKey("longitude")) {
                Double lat = Double.parseDouble(deviceData.get("latitude").toString());
                Double lng = Double.parseDouble(deviceData.get("longitude").toString());
                String address = (String) deviceData.get("address");
                
                locationService.updateDeviceLocation(deviceId, lat, lng, address);
                
                // Check geofences using GeofenceService
                geofenceService.checkGeofenceViolations(deviceId, lat, lng);
            }
            
            // Connect to all services using CoreIntegrationService
            coreIntegrationService.connectDeviceToAllServices(device);
            
            // Record analytics using AnalyticsService
            analyticsService.recordDeviceUpdate(device);
            
            response.put("success", true);
            response.put("device", device);
            response.put("message", "Device updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Quick action endpoint using QuickActionsService
     */
    @PostMapping("/quick-actions/{action}/{deviceId}")
    public ResponseEntity<Map<String, Object>> executeQuickAction(
            @PathVariable String action, 
            @PathVariable String deviceId) {
        
        try {
            // Execute action using QuickActionsService
            Map<String, Object> result = quickActionsService.executeAction(deviceId, action);
            
            // Send notification using NotificationService
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device != null) {
                String message = "Action '" + action + "' executed on " + device.getDeviceName();
                notificationService.sendDeviceAction(device, action);
                
                // Broadcast via WebSocket
                webSocketService.broadcastDeviceUpdate(device);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get device location using multiple services
     */
    @GetMapping("/devices/{deviceId}/location")
    public ResponseEntity<Map<String, Object>> getDeviceLocation(@PathVariable String deviceId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get device from DeviceService
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Device not found"));
            }
            
            // Get enhanced location data using EnhancedLocationService
            Map<String, Object> locationData = locationService.getDeviceLocationData(deviceId);
            
            // Get location history using DeviceService
            List<?> locationHistory = deviceService.getLocationHistory(deviceId);
            
            response.put("device", device);
            response.put("currentLocation", locationData);
            response.put("locationHistory", locationHistory);
            response.put("realTimeTracking", realTimeService.isDeviceActive(deviceId));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get comprehensive system status using all services
     */
    @GetMapping("/system/comprehensive-status")
    public ResponseEntity<Map<String, Object>> getComprehensiveStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Core integration status
            CoreIntegrationService.SystemHealthStatus health = coreIntegrationService.getSystemHealth();
            status.put("coreIntegration", health);
            
            // Individual service status
            Map<String, Object> services = new HashMap<>();
            services.put("deviceService", deviceService != null ? "ACTIVE" : "INACTIVE");
            services.put("userService", userService != null ? "ACTIVE" : "INACTIVE");
            services.put("analyticsService", analyticsService != null ? "ACTIVE" : "INACTIVE");
            services.put("notificationService", notificationService != null ? "ACTIVE" : "INACTIVE");
            services.put("webSocketService", webSocketService != null ? "ACTIVE" : "INACTIVE");
            services.put("realTimeService", realTimeService != null ? "ACTIVE" : "INACTIVE");
            services.put("locationService", locationService != null ? "ACTIVE" : "INACTIVE");
            services.put("geofenceService", geofenceService != null ? "ACTIVE" : "INACTIVE");
            services.put("encryptionService", encryptionService != null ? "ACTIVE" : "INACTIVE");
            services.put("authService", authService != null ? "ACTIVE" : "INACTIVE");
            services.put("quickActionsService", quickActionsService != null ? "ACTIVE" : "INACTIVE");
            services.put("deviceActionService", deviceActionService != null ? "ACTIVE" : "INACTIVE");
            services.put("continuousOperationService", continuousOperationService != null ? "ACTIVE" : "INACTIVE");
            services.put("notificationService", "ACTIVE"); // Using NotificationService instead
            services.put("agentAuthService", agentAuthService != null ? "ACTIVE" : "INACTIVE");
            
            status.put("services", services);
            
            // System metrics
            status.put("timestamp", System.currentTimeMillis());
            status.put("uptime", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime());
            status.put("activeWebSocketSessions", webSocketService.getActiveSessionCount());
            
            // Overall status
            long activeServices = services.values().stream().mapToLong(v -> "ACTIVE".equals(v) ? 1 : 0).sum();
            status.put("overallStatus", activeServices >= 10 ? "HEALTHY" : "DEGRADED");
            status.put("activeServiceCount", activeServices);
            status.put("totalServiceCount", services.size());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
