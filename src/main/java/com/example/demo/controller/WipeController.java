package com.example.demo.controller;

import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Base64;

/**
 * 🗑️ WIPE CONTROLLER
 * Handles wipe reports from agents and management of wiped devices
 */
@RestController
@RequestMapping("/api/wipe")
@CrossOrigin(origins = "*")
public class WipeController {

    @Autowired
    private PerfectAuthService authService;
    
    @Autowired
    private DeviceService deviceService;
    
    private static final String WIPE_LOGS_DIR = System.getProperty("user.home") + "/.lapso/wipe-logs/";
    
    static {
        // Create wipe logs directory
        new File(WIPE_LOGS_DIR).mkdirs();
    }

    /**
     * Agent reports wipe completion
     */
    @PostMapping("/report/{deviceId}")
    public ResponseEntity<Map<String, Object>> reportWipeCompletion(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String logData = request.get("logData");
            String userEmail = request.get("userEmail");
            String timestamp = request.get("timestamp");
            String status = request.get("status");
            
            if (logData == null || logData.isEmpty()) {
                response.put("success", false);
                response.put("error", "No log data provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verify device exists
            var deviceOpt = deviceService.findByDeviceId(deviceId);
            if (deviceOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "Device not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Decode base64 log data
            byte[] logBytes = Base64.getDecoder().decode(logData);
            
            // Generate filename
            String filename = String.format("%s_%s.log", 
                deviceId, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            );
            
            String filepath = WIPE_LOGS_DIR + filename;
            
            // Save to disk
            try (FileOutputStream fos = new FileOutputStream(filepath)) {
                fos.write(logBytes);
            }
            
            // Update device status
            var device = deviceOpt.get();
            device.setIsWiped(true);
            device.setWipedAt(LocalDateTime.now());
            deviceService.saveDevice(device);
            
            response.put("success", true);
            response.put("message", "Wipe completion reported successfully");
            response.put("filename", filename);
            response.put("filepath", filepath);
            
            System.out.println("🗑️ Wipe completion reported for device: " + deviceId + " (" + logBytes.length + " bytes)");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Report failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get wipe status for a device
     */
    @GetMapping("/status/{deviceId}")
    public ResponseEntity<Map<String, Object>> getWipeStatus(@PathVariable String deviceId) {
        
        if (!authService.isLoggedIn()) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verify device ownership
            var deviceOpt = deviceService.findByDeviceId(deviceId);
            if (deviceOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "Device not found");
                return ResponseEntity.status(404).body(response);
            }
            
            var device = deviceOpt.get();
            String currentUser = authService.getLoggedInUser();
            
            if (!device.getUserEmail().equals(currentUser)) {
                response.put("success", false);
                response.put("error", "Access denied");
                return ResponseEntity.status(403).body(response);
            }
            
            response.put("success", true);
            response.put("deviceId", deviceId);
            response.put("isWiped", device.getIsWiped());
            response.put("wipedAt", device.getWipedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get wipe status: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}