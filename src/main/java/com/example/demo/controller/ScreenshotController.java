package com.example.demo.controller;

import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;

/**
 * 📸 SCREENSHOT CONTROLLER
 * Handles screenshot uploads from agents and retrieval for UI
 */
@RestController
@RequestMapping("/api/screenshots")
@CrossOrigin(origins = "*")
public class ScreenshotController {

    @Autowired
    private PerfectAuthService authService;
    
    @Autowired
    private DeviceService deviceService;
    
    // In-memory storage for screenshots (in production, use database + file storage)
    private static final Map<String, List<Screenshot>> deviceScreenshots = new ConcurrentHashMap<>();
    private static final String SCREENSHOT_DIR = System.getProperty("user.home") + "/.lapso/screenshots/";
    
    static {
        // Create screenshot directory
        new File(SCREENSHOT_DIR).mkdirs();
    }

    /**
     * Agent uploads screenshot
     */
    @PostMapping("/upload/{deviceId}")
    public ResponseEntity<Map<String, Object>> uploadScreenshot(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String imageData = request.get("imageData");
            String userEmail = request.get("userEmail");
            String timestamp = request.get("timestamp");
            
            if (imageData == null || imageData.isEmpty()) {
                response.put("success", false);
                response.put("error", "No image data provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verify device exists
            var deviceOpt = deviceService.findByDeviceId(deviceId);
            if (deviceOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "Device not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Decode base64 image
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            
            // Generate filename
            String filename = String.format("%s_%s.png", 
                deviceId, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            );
            
            String filepath = SCREENSHOT_DIR + filename;
            
            // Save to disk
            try (FileOutputStream fos = new FileOutputStream(filepath)) {
                fos.write(imageBytes);
            }
            
            // Store metadata
            Screenshot screenshot = new Screenshot(
                filename,
                filepath,
                deviceId,
                userEmail,
                LocalDateTime.now(),
                imageBytes.length
            );
            
            deviceScreenshots.computeIfAbsent(deviceId, k -> new ArrayList<>()).add(screenshot);
            
            // Keep only last 10 screenshots per device
            List<Screenshot> screenshots = deviceScreenshots.get(deviceId);
            if (screenshots.size() > 10) {
                Screenshot oldest = screenshots.remove(0);
                // Delete old file
                new File(oldest.filepath).delete();
            }
            
            response.put("success", true);
            response.put("message", "Screenshot uploaded successfully");
            response.put("filename", filename);
            response.put("url", "/api/screenshots/view/" + deviceId + "/" + filename);
            response.put("timestamp", screenshot.timestamp);
            
            System.out.println("📸 Screenshot uploaded for device: " + deviceId + " (" + imageBytes.length + " bytes)");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Upload failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get list of screenshots for a device
     */
    @GetMapping("/list/{deviceId}")
    public ResponseEntity<Map<String, Object>> listScreenshots(@PathVariable String deviceId) {
        
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
            
            List<Screenshot> screenshots = deviceScreenshots.getOrDefault(deviceId, new ArrayList<>());
            
            List<Map<String, Object>> screenshotList = new ArrayList<>();
            for (Screenshot screenshot : screenshots) {
                screenshotList.add(Map.of(
                    "filename", screenshot.filename,
                    "timestamp", screenshot.timestamp,
                    "size", screenshot.size,
                    "url", "/api/screenshots/view/" + deviceId + "/" + screenshot.filename
                ));
            }
            
            response.put("success", true);
            response.put("deviceId", deviceId);
            response.put("screenshots", screenshotList);
            response.put("count", screenshotList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to list screenshots: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * View/download a specific screenshot
     */
    @GetMapping("/view/{deviceId}/{filename}")
    public ResponseEntity<byte[]> viewScreenshot(
            @PathVariable String deviceId,
            @PathVariable String filename) {
        
        if (!authService.isLoggedIn()) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            // Verify device ownership
            var deviceOpt = deviceService.findByDeviceId(deviceId);
            if (deviceOpt.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            
            var device = deviceOpt.get();
            String currentUser = authService.getLoggedInUser();
            
            if (!device.getUserEmail().equals(currentUser)) {
                return ResponseEntity.status(403).build();
            }
            
            // Find screenshot
            List<Screenshot> screenshots = deviceScreenshots.getOrDefault(deviceId, new ArrayList<>());
            Screenshot screenshot = screenshots.stream()
                .filter(s -> s.filename.equals(filename))
                .findFirst()
                .orElse(null);
            
            if (screenshot == null) {
                return ResponseEntity.status(404).build();
            }
            
            // Read file
            byte[] imageBytes = Files.readAllBytes(Paths.get(screenshot.filepath));
            
            return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .body(imageBytes);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Delete a screenshot
     */
    @DeleteMapping("/delete/{deviceId}/{filename}")
    public ResponseEntity<Map<String, Object>> deleteScreenshot(
            @PathVariable String deviceId,
            @PathVariable String filename) {
        
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
            
            // Find and delete screenshot
            List<Screenshot> screenshots = deviceScreenshots.getOrDefault(deviceId, new ArrayList<>());
            Screenshot screenshot = screenshots.stream()
                .filter(s -> s.filename.equals(filename))
                .findFirst()
                .orElse(null);
            
            if (screenshot == null) {
                response.put("success", false);
                response.put("error", "Screenshot not found");
                return ResponseEntity.status(404).body(response);
            }
            
            screenshots.remove(screenshot);
            new File(screenshot.filepath).delete();
            
            response.put("success", true);
            response.put("message", "Screenshot deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete screenshot: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Screenshot metadata class
     */
    private static class Screenshot {
        final String filename;
        final String filepath;
        final String deviceId;
        final String userEmail;
        final LocalDateTime timestamp;
        final long size;
        
        Screenshot(String filename, String filepath, String deviceId, String userEmail, LocalDateTime timestamp, long size) {
            this.filename = filename;
            this.filepath = filepath;
            this.deviceId = deviceId;
            this.userEmail = userEmail;
            this.timestamp = timestamp;
            this.size = size;
        }
    }
}
