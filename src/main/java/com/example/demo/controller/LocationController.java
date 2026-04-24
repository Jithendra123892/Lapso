package com.example.demo.controller;

import com.example.demo.model.LocationData;
import com.example.demo.service.EnhancedLocationService;
import com.example.demo.service.GeofenceService;
import com.example.demo.service.WebSocketService;
import com.example.demo.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🚀 LAPSO Location API Controller - Superior to Microsoft Find My Device
 * ✨ Features Microsoft doesn't have:
 * - Automatic updates every 30 seconds (vs manual refresh)
 * - Cross-platform support (Windows, macOS, Linux, Android)
 * - Real-time geofencing with instant alerts
 * - Multiple location sources (GPS + WiFi + IP)
 * - Advanced device commands (lock, wipe, sound, screenshot)
 * - Complete privacy (self-hosted, no Microsoft servers)
 * - Always free (no subscription fees)
 */
@RestController
@RequestMapping("/api/location")
public class LocationController {
    
    @Autowired
    private EnhancedLocationService enhancedLocationService;
    
    @Autowired
    private GeofenceService geofenceService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    /**
     * Get current location for device (real-time)
     */
    @GetMapping("/current/{deviceId}")
    public ResponseEntity<LocationData> getCurrentLocation(@PathVariable String deviceId) {
        try {
            LocationData location = enhancedLocationService.getBestLocation(deviceId);
            if (location != null) {
                return ResponseEntity.ok(location);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get location history for device
     */
    @GetMapping("/history/{deviceId}")
    public ResponseEntity<List<LocationData>> getLocationHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<LocationData> history = enhancedLocationService.getLocationHistory(deviceId, days);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update device location (from agent)
     */
    @PostMapping("/update")
    public ResponseEntity<String> updateLocation(@RequestBody LocationUpdateRequest request) {
        try {
            LocationData location = LocationData.builder()
                .deviceId(request.getDeviceId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracy(request.getAccuracy())
                .source(request.getSource())
                .address(request.getAddress())
                .timestamp(java.time.LocalDateTime.now())
                .build();
            
            // Check geofences
            geofenceService.checkGeofences(request.getDeviceId(), request.getUserEmail(), location);
            
            // Send real-time update
            Map<String, Object> locationMap = new HashMap<>();
            locationMap.put("latitude", location.getLatitude());
            locationMap.put("longitude", location.getLongitude());
            locationMap.put("address", location.getAddress());
            locationMap.put("timestamp", location.getTimestamp());
            webSocketService.sendLocationUpdate(request.getUserEmail(), locationMap);
            
            return ResponseEntity.ok("Location updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update location");
        }
    }
    
    /**
     * Get geofence status for device
     */
    @GetMapping("/geofences/{deviceId}")
    public ResponseEntity<Map<String, Object>> getGeofenceStatus(@PathVariable String deviceId) {
        try {
            Map<String, Object> status = geofenceService.getGeofenceStatus(deviceId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Add custom geofence
     */
    @PostMapping("/geofences")
    public ResponseEntity<String> addGeofence(@RequestBody GeofenceRequest request) {
        try {
            geofenceService.addGeofence(
                request.getDeviceId(),
                request.getName(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius(),
                GeofenceService.GeofenceType.valueOf(request.getType())
            );
            return ResponseEntity.ok("Geofence added successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to add geofence");
        }
    }
    
    /**
     * Location update request DTO
     */
    public static class LocationUpdateRequest {
        private String deviceId;
        private String userEmail;
        private Double latitude;
        private Double longitude;
        private Double accuracy;
        private String source;
        private String address;
        
        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public Double getAccuracy() { return accuracy; }
        public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
    
    /**
     * Get monitoring statistics - Shows LAPSO's superiority over Microsoft
     */
    @GetMapping("/monitoring-stats")
    public ResponseEntity<Map<String, Object>> getMonitoringStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get real-time statistics
            long totalDevices = deviceRepository.count();
            long onlineDevices = deviceRepository.countByIsOnlineTrue();
            long offlineDevices = deviceRepository.countByIsOnlineFalse();
            
            stats.put("totalDevices", totalDevices);
            stats.put("onlineDevices", onlineDevices);
            stats.put("offlineDevices", offlineDevices);
            stats.put("updateFrequency", "30 seconds");
            stats.put("lastUpdate", LocalDateTime.now());
            
            // LAPSO advantages over Microsoft Find My Device
            Map<String, Object> advantages = new HashMap<>();
            advantages.put("automaticUpdates", "Every 30 seconds vs Microsoft's manual refresh");
            advantages.put("crossPlatform", "Windows, macOS, Linux, Android vs Windows only");
            advantages.put("deviceCommands", "Lock, wipe, sound, screenshot vs basic tracking");
            advantages.put("privacy", "Self-hosted, your data vs Microsoft servers");
            advantages.put("cost", "Always free vs potential Microsoft subscription");
            advantages.put("geofencing", "Real-time alerts vs basic location");
            advantages.put("customization", "Open source, modifiable vs locked ecosystem");
            
            stats.put("lapsoAdvantages", advantages);
            stats.put("microsoftLimitations", List.of(
                "Manual location refresh only",
                "Windows devices only", 
                "Basic tracking features",
                "Data stored on Microsoft servers",
                "Limited customization",
                "Vendor lock-in"
            ));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to get monitoring stats: " + e.getMessage())
            );
        }
    }
    
    /**
     * Geofence request DTO
     */
    public static class GeofenceRequest {
        private String deviceId;
        private String name;
        private Double latitude;
        private Double longitude;
        private Double radius;
        private String type;
        
        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public Double getRadius() { return radius; }
        public void setRadius(Double radius) { this.radius = radius; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
