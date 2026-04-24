package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.service.DeviceService;
import com.example.demo.service.PerfectAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/device-location")
@CrossOrigin(origins = "*", allowedHeaders = "*")  // Allow cross-origin requests
public class DeviceLocationController {

    private final DeviceService deviceService;
    private final PerfectAuthService authService;
    
    // Rate limiting: Track last update time per device (10 updates per hour max)
    private final Map<String, LocalDateTime> lastUpdateMap = new ConcurrentHashMap<>();
    private static final int MAX_UPDATES_PER_HOUR = 10;
    private static final int MINUTES_BETWEEN_UPDATES = 60 / MAX_UPDATES_PER_HOUR;  // 6 minutes

    public DeviceLocationController(DeviceService deviceService, PerfectAuthService authService) {
        this.deviceService = deviceService;
        this.authService = authService;
    }

    /**
     * Update device location manually
     * POST /api/device-location/update
     * Note: This is a public endpoint for device location updates from the browser
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateDeviceLocation(
            @RequestParam String deviceId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Completely remove authentication check for Fix Location feature
            // String userEmail = null;
            // try {
            //     userEmail = authService.getLoggedInUser();
            // } catch (Exception e) {
            //     // User not authenticated - this is OK for the Fix Location button
            //     System.out.println("⚠️ Anonymous location update for device: " + deviceId);
            // }
            
            // Find device
            Device device = deviceService.getDeviceByDeviceId(deviceId);
            if (device == null) {
                response.put("success", false);
                response.put("message", "Device not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Remove ownership check to allow anonymous updates
            // if (userEmail != null && !device.getUser().getEmail().equals(userEmail)) {
            //     response.put("success", false);
            //     response.put("message", "You don't own this device");
            //     return ResponseEntity.status(403).body(response);
            // }
            
            // Validate coordinates
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                response.put("success", false);
                response.put("message", "Invalid coordinates");
                return ResponseEntity.status(400).body(response);
            }
            
            // Rate limiting: Check if update is too soon
            LocalDateTime lastUpdate = lastUpdateMap.get(deviceId);
            if (lastUpdate != null) {
                long minutesSinceLastUpdate = java.time.Duration.between(lastUpdate, LocalDateTime.now()).toMinutes();
                if (minutesSinceLastUpdate < MINUTES_BETWEEN_UPDATES) {
                    long remainingMinutes = MINUTES_BETWEEN_UPDATES - minutesSinceLastUpdate;
                    response.put("success", false);
                    response.put("message", "Rate limit exceeded. Please wait " + remainingMinutes + " more minute(s)");
                    response.put("rateLimited", true);
                    response.put("retryAfterMinutes", remainingMinutes);
                    return ResponseEntity.status(429).body(response);  // 429 Too Many Requests
                }
            }
            
            // Update location
            device.setLatitude(latitude);
            device.setLongitude(longitude);
            device.setLocationSource("BROWSER");  // Set source as BROWSER GPS for Fix Location button
            device.setLastSeen(LocalDateTime.now());
            deviceService.saveDevice(device);
            
            // Update rate limit timestamp
            lastUpdateMap.put(deviceId, LocalDateTime.now());
            
            response.put("success", true);
            response.put("message", "Location updated successfully");
            response.put("device", device.getDeviceName());
            response.put("latitude", latitude);
            response.put("longitude", longitude);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating location: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get current location from browser geolocation
     * This endpoint can be called from the frontend
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentLocation() {
        Map<String, Object> response = new HashMap<>();
        
        // Remove authentication check for this endpoint as well
        // String userEmail = authService.getLoggedInUser();
        // if (userEmail == null) {
        //     response.put("success", false);
        //     response.put("message", "Not authenticated");
        //     return ResponseEntity.status(401).body(response);
        // }
        
        response.put("success", true);
        response.put("message", "Use browser geolocation API to get coordinates");
        
        return ResponseEntity.ok(response);
    }
}