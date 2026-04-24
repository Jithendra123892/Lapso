package com.example.demo.controller;

import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/agents")
public class SecureAgentController {

    @Autowired
    private PerfectAuthService authService;
    
    @Autowired
    private DeviceService deviceService;

    /**
     * SECURE: Serve agent scripts only to authenticated users
     * Prevents unauthorized access to agent installation files
     */
    @GetMapping("/{platform}/{filename}")
    public ResponseEntity<Resource> getAgentFile(
            @PathVariable String platform,
            @PathVariable String filename) {
        
        // SECURITY CHECK: Verify user authentication
        if (!authService.isLoggedIn()) {
            System.err.println("🚨 SECURITY: Unauthorized access attempt to agent file: " + platform + "/" + filename);
            return ResponseEntity.status(401).build();
        }
        
        String currentUser = authService.getLoggedInUser();
        System.out.println("✅ SECURITY: Authenticated user " + currentUser + " accessing agent file: " + platform + "/" + filename);
        
        try {
            // Validate platform
            if (!isValidPlatform(platform)) {
                System.err.println("🚨 SECURITY: Invalid platform requested: " + platform);
                return ResponseEntity.badRequest().build();
            }
            
            // Validate filename to prevent directory traversal
            if (!isValidFilename(filename)) {
                System.err.println("🚨 SECURITY: Invalid filename requested: " + filename);
                return ResponseEntity.badRequest().build();
            }
            
            // Construct safe file path
            String filePath = "static/agents/" + platform + "/" + filename;
            Resource resource = new ClassPathResource(filePath);
            
            if (!resource.exists()) {
                System.err.println("🚨 SECURITY: Requested file not found: " + filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            MediaType contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(contentType)
                    .body(resource);
                    
        } catch (Exception e) {
            System.err.println("🚨 SECURITY: Error serving agent file: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * SECURE: Get agent configuration only for authenticated users
     */
    @GetMapping("/config/{deviceId}")
    public ResponseEntity<String> getAgentConfig(@PathVariable String deviceId) {
        
        // SECURITY CHECK: Verify user authentication
        if (!authService.isLoggedIn()) {
            System.err.println("🚨 SECURITY: Unauthorized access attempt to agent config for device: " + deviceId);
            return ResponseEntity.status(401).build();
        }
        
        String currentUser = authService.getLoggedInUser();
        
        // SECURITY CHECK: Verify user owns the device
        var deviceOpt = deviceService.findByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            System.err.println("🚨 SECURITY: Device not found: " + deviceId);
            return ResponseEntity.notFound().build();
        }
        
        var device = deviceOpt.get();
        if (!device.getUserEmail().equals(currentUser)) {
            System.err.println("🚨 SECURITY: User " + currentUser + " attempted to access config for device " + deviceId + " owned by " + device.getUserEmail());
            return ResponseEntity.status(403).build();
        }
        
        // Generate secure agent configuration
        String config = generateSecureAgentConfig(deviceId, currentUser);
        
        System.out.println("✅ SECURITY: Provided agent config for device " + deviceId + " to user " + currentUser);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(config);
    }
    
    private boolean isValidPlatform(String platform) {
        return platform != null && 
               (platform.equals("windows") || platform.equals("linux") || platform.equals("macos"));
    }
    
    private boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // Prevent directory traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }
        
        // Allow only specific file extensions
        return filename.endsWith(".ps1") || filename.endsWith(".sh") || filename.endsWith(".bat") || 
               filename.endsWith(".exe") || filename.endsWith(".dmg") || filename.endsWith(".deb");
    }
    
    private MediaType getContentType(String filename) {
        if (filename.endsWith(".ps1") || filename.endsWith(".sh") || filename.endsWith(".bat")) {
            return MediaType.TEXT_PLAIN;
        } else if (filename.endsWith(".exe") || filename.endsWith(".dmg") || filename.endsWith(".deb")) {
            return MediaType.APPLICATION_OCTET_STREAM;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
    
    private String generateSecureAgentConfig(String deviceId, String userEmail) {
        return String.format("""
            {
                "deviceId": "%s",
                "userEmail": "%s",
                "serverUrl": "http://localhost:8080",
                "apiEndpoint": "/api/agent/heartbeat",
                "updateInterval": 30,
                "enableLocationTracking": true,
                "enableSystemMonitoring": true,
                "enableNetworkMonitoring": true,
                "agentVersion": "1.0.0",
                "security": {
                    "enableEncryption": true,
                    "enableTamperDetection": true,
                    "enableRemoteWipe": true,
                    "enableRemoteLock": true
                },
                "timestamp": "%s"
            }
            """, deviceId, userEmail, java.time.LocalDateTime.now());
    }
}
