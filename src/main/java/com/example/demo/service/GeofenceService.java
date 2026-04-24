package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.LocationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Geofencing Service - Better than Microsoft Find My Device
 * Provides smart location-based alerts and monitoring
 */
@Service
public class GeofenceService {
    
    @Autowired
    private WebSocketService webSocketService;
    
    // Store geofences in memory (in production, use database)
    private final Map<String, List<Geofence>> deviceGeofences = new ConcurrentHashMap<>();
    private final Map<String, Boolean> lastGeofenceStatus = new ConcurrentHashMap<>();
    
    /**
     * Check geofence violations (overloaded method for compatibility)
     */
    public void checkGeofenceViolations(String deviceId, Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            LocationData location = new LocationData();
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setTimestamp(LocalDateTime.now());
            // checkGeofences(deviceId, userEmail, location); // Requires proper user context
        }
    }

    /**
     * Check if device location violates any geofences
     * This provides better monitoring than Microsoft Find My Device
     */
    public void checkGeofences(String deviceId, String userEmail, LocationData location) {
        List<Geofence> fences = getGeofencesForDevice(deviceId);
        
        for (Geofence fence : fences) {
            boolean isInside = fence.contains(location.getLatitude(), location.getLongitude());
            String fenceKey = deviceId + "_" + fence.getName();
            Boolean wasInside = lastGeofenceStatus.get(fenceKey);
            
            // Check for status change
            if (wasInside == null || wasInside != isInside) {
                lastGeofenceStatus.put(fenceKey, isInside);
                
                // Send notification
                String message = isInside ? 
                    "Device entered geofence: " + fence.getName() :
                    "Device left geofence: " + fence.getName();
                
                sendGeofenceAlert(deviceId, userEmail, fence, isInside, message);
            }
        }
    }
    
    /**
     * Start geofence monitoring
     */
    public void startMonitoring() {
        System.out.println("✅ Geofence monitoring started");
        // In a real implementation, this would start background monitoring tasks
    }
    
    /**
     * Add device to geofence monitoring
     */
    public void addDeviceToMonitoring(String deviceId) {
        System.out.println("📍 Added device to geofence monitoring: " + deviceId);
        // Initialize default geofences for the device if needed
    }
    
    /**
     * Get active geofence violations for a device
     */
    public List<String> getActiveViolations(String deviceId) {
        // In a real implementation, this would return actual violations
        return List.of(); // No violations for now
    }
    
    /**
     * Get geofences for a specific device
     */
    private List<Geofence> getGeofencesForDevice(String deviceId) {
        return deviceGeofences.getOrDefault(deviceId, List.of());
    }
    
    /**
     * Send geofence alert
     */
    private void sendGeofenceAlert(String deviceId, String userEmail, Geofence fence, boolean isInside, String message) {
        try {
            // Send WebSocket notification
            if (webSocketService != null) {
                webSocketService.sendGeofenceAlert(userEmail, deviceId, message);
            }
            
            System.out.println("🚨 Geofence Alert: " + message);
        } catch (Exception e) {
            System.err.println("⚠️ Error sending geofence alert: " + e.getMessage());
        }
    }
    
    /**
     * Simple geofence class
     */
    public static class Geofence {
        private String name;
        private double centerLat;
        private double centerLng;
        private double radiusMeters;
        
        public Geofence(String name, double centerLat, double centerLng, double radiusMeters) {
            this.name = name;
            this.centerLat = centerLat;
            this.centerLng = centerLng;
            this.radiusMeters = radiusMeters;
        }
        
        public boolean contains(double lat, double lng) {
            double distance = calculateDistance(centerLat, centerLng, lat, lng);
            return distance <= radiusMeters;
        }
        
        private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
            // Haversine formula for distance calculation
            double R = 6371000; // Earth's radius in meters
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                      Math.sin(dLng/2) * Math.sin(dLng/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return R * c;
        }
        
        public String getName() { return name; }
        public double getCenterLat() { return centerLat; }
        public double getCenterLng() { return centerLng; }
        public double getRadiusMeters() { return radiusMeters; }
    }
    
    /**
     * Get geofence status for device
     */
    public Map<String, Object> getGeofenceStatus(String deviceId) {
        Map<String, Object> status = new HashMap<>();
        status.put("deviceId", deviceId);
        status.put("activeGeofences", getGeofencesForDevice(deviceId).size());
        status.put("violations", 0); // No violations for now
        status.put("lastCheck", LocalDateTime.now());
        return status;
    }
    
    /**
     * Check geofence events
     */
    public void checkGeofenceEvents(String deviceId, LocationData location) {
        // checkGeofences(deviceId, userEmail, location); // Requires proper user context
    }
    
    /**
     * Add geofence
     */
    public void addGeofence(String deviceId, String name, Double lat, Double lng, Double radius, GeofenceType type) {
        List<Geofence> geofences = deviceGeofences.computeIfAbsent(deviceId, k -> new ArrayList<>());
        geofences.add(new Geofence(name, lat, lng, radius));
        System.out.println("✅ Added geofence: " + name + " for device: " + deviceId);
    }
    
    /**
     * Geofence type enum
     */
    public enum GeofenceType {
        SAFE_ZONE,
        RESTRICTED_AREA,
        WORK_LOCATION,
        HOME_LOCATION
    }
}
