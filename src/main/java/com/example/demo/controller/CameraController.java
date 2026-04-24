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
 * 📷 CAMERA CONTROLLER
 * Handles camera photo uploads from agents and retrieval for UI
 */
@RestController
@RequestMapping("/api/camera")
@CrossOrigin(origins = "*")
public class CameraController {

    @Autowired
    private PerfectAuthService authService;
    
    @Autowired
    private DeviceService deviceService;
    
    // In-memory storage for camera photos (in production, use database + file storage)
    private static final Map<String, List<CameraPhoto>> devicePhotos = new ConcurrentHashMap<>();
    private static final String CAMERA_DIR = System.getProperty("user.home") + "/.lapso/camera/";
    
    static {
        // Create camera directory
        new File(CAMERA_DIR).mkdirs();
    }

    /**
     * Agent uploads camera photo
     */
    @PostMapping("/upload/{deviceId}")
    public ResponseEntity<Map<String, Object>> uploadCameraPhoto(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String imageData = request.get("imageData");
            String userEmail = request.get("userEmail");
            String timestamp = request.get("timestamp");
            String source = request.get("source");
            String note = request.get("note");
            
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
            String extension = "webcam".equals(source) ? ".jpg" : ".png";
            String filename = String.format("%s_%s%s", 
                deviceId, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                extension
            );
            
            String filepath = CAMERA_DIR + filename;
            
            // Save to disk
            try (FileOutputStream fos = new FileOutputStream(filepath)) {
                fos.write(imageBytes);
            }
            
            // Store metadata
            CameraPhoto photo = new CameraPhoto(
                filename,
                filepath,
                deviceId,
                userEmail,
                LocalDateTime.now(),
                imageBytes.length,
                source,
                note
            );
            
            devicePhotos.computeIfAbsent(deviceId, k -> new ArrayList<>()).add(photo);
            
            // Keep only last 10 photos per device
            List<CameraPhoto> photos = devicePhotos.get(deviceId);
            if (photos.size() > 10) {
                CameraPhoto oldest = photos.remove(0);
                // Delete old file
                new File(oldest.filepath).delete();
            }
            
            response.put("success", true);
            response.put("message", "Camera photo uploaded successfully");
            response.put("filename", filename);
            response.put("url", "/api/camera/view/" + deviceId + "/" + filename);
            response.put("timestamp", photo.timestamp);
            
            System.out.println("📷 Camera photo uploaded for device: " + deviceId + " (" + imageBytes.length + " bytes) from " + source);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Upload failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get list of camera photos for a device
     */
    @GetMapping("/list/{deviceId}")
    public ResponseEntity<Map<String, Object>> listCameraPhotos(@PathVariable String deviceId) {
        
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
            
            List<CameraPhoto> photos = devicePhotos.getOrDefault(deviceId, new ArrayList<>());
            
            List<Map<String, Object>> photoList = new ArrayList<>();
            for (CameraPhoto photo : photos) {
                photoList.add(Map.of(
                    "filename", photo.filename,
                    "timestamp", photo.timestamp,
                    "size", photo.size,
                    "source", photo.source,
                    "note", photo.note != null ? photo.note : "",
                    "url", "/api/camera/view/" + deviceId + "/" + photo.filename
                ));
            }
            
            response.put("success", true);
            response.put("deviceId", deviceId);
            response.put("photos", photoList);
            response.put("count", photoList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to list camera photos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * View/download a specific camera photo
     */
    @GetMapping("/view/{deviceId}/{filename}")
    public ResponseEntity<byte[]> viewCameraPhoto(
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
            
            // Find photo
            List<CameraPhoto> photos = devicePhotos.getOrDefault(deviceId, new ArrayList<>());
            CameraPhoto photo = photos.stream()
                .filter(s -> s.filename.equals(filename))
                .findFirst()
                .orElse(null);
            
            if (photo == null) {
                return ResponseEntity.status(404).build();
            }
            
            // Read file
            byte[] imageBytes = Files.readAllBytes(Paths.get(photo.filepath));
            
            String contentType = filename.endsWith(".jpg") ? "image/jpeg" : "image/png";
            
            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .body(imageBytes);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Delete a camera photo
     */
    @DeleteMapping("/delete/{deviceId}/{filename}")
    public ResponseEntity<Map<String, Object>> deleteCameraPhoto(
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
            
            // Find and delete photo
            List<CameraPhoto> photos = devicePhotos.getOrDefault(deviceId, new ArrayList<>());
            CameraPhoto photo = photos.stream()
                .filter(s -> s.filename.equals(filename))
                .findFirst()
                .orElse(null);
            
            if (photo == null) {
                response.put("success", false);
                response.put("error", "Camera photo not found");
                return ResponseEntity.status(404).body(response);
            }
            
            photos.remove(photo);
            new File(photo.filepath).delete();
            
            response.put("success", true);
            response.put("message", "Camera photo deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete camera photo: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Camera photo metadata class
     */
    private static class CameraPhoto {
        final String filename;
        final String filepath;
        final String deviceId;
        final String userEmail;
        final LocalDateTime timestamp;
        final long size;
        final String source;
        final String note;
        
        CameraPhoto(String filename, String filepath, String deviceId, String userEmail, LocalDateTime timestamp, long size, String source, String note) {
            this.filename = filename;
            this.filepath = filepath;
            this.deviceId = deviceId;
            this.userEmail = userEmail;
            this.timestamp = timestamp;
            this.size = size;
            this.source = source;
            this.note = note;
        }
    }
}