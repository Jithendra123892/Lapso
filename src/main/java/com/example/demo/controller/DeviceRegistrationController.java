package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import com.example.demo.service.DeviceService;
import com.example.demo.service.PerfectAuthService;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/device")
public class DeviceRegistrationController {

    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private PerfectAuthService authService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Register a new device for the current user - SECURE: Only authenticated users can register devices
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerDevice(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // SECURITY CHECK: Verify user authentication
            if (!authService.isLoggedIn()) {
                response.put("success", false);
                response.put("error", "Authentication required to register devices");
                System.err.println("🚨 SECURITY: Unauthorized device registration attempt");
                return ResponseEntity.status(401).body(response);
            }
            
            String userEmail = authService.getLoggedInUser();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.status(404).body(response);
            }
            
            User user = userOpt.get();
            
            // Extract device information
            String deviceName = (String) request.getOrDefault("deviceName", "My Device");
            String manufacturer = (String) request.getOrDefault("manufacturer", "Unknown");
            String model = (String) request.getOrDefault("model", "Unknown");
            String osName = (String) request.getOrDefault("osName", "Unknown");
            String osVersion = (String) request.getOrDefault("osVersion", "");
            String deviceType = (String) request.getOrDefault("deviceType", "LAPTOP");
            
            // Register the device
            Device device = deviceService.registerDevice(user, deviceName, manufacturer, model, osName);
            device.setOsVersion(osVersion);
            device.setDeviceType(deviceType);
            device.setCreatedAt(LocalDateTime.now());
            device.setLastSeen(LocalDateTime.now());
            
            Device savedDevice = deviceService.saveDevice(device);
            
            // Prepare response
            response.put("success", true);
            response.put("message", "Device registered successfully");
            response.put("deviceId", savedDevice.getDeviceId());
            response.put("deviceName", savedDevice.getDeviceName());
            response.put("registrationTime", savedDevice.getCreatedAt());
            
            // Generate download URLs for agents
            Map<String, String> downloadUrls = new HashMap<>();
            downloadUrls.put("windows", "/agents/windows/lapso-agent-" + savedDevice.getDeviceId() + ".exe");
            downloadUrls.put("macos", "/agents/macos/lapso-agent-" + savedDevice.getDeviceId() + ".dmg");
            downloadUrls.put("linux", "/agents/linux/lapso-agent-" + savedDevice.getDeviceId() + ".deb");
            
            response.put("downloadUrls", downloadUrls);
            
            // Setup instructions
            Map<String, Object> setupInstructions = new HashMap<>();
            setupInstructions.put("deviceId", savedDevice.getDeviceId());
            setupInstructions.put("userEmail", userEmail);
            setupInstructions.put("serverUrl", "http://localhost:8080");
            setupInstructions.put("updateInterval", 30); // seconds
            
            response.put("setupInstructions", setupInstructions);
            
            System.out.println("✅ Device registered: " + savedDevice.getDeviceId() + " for user: " + userEmail);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Registration failed: " + e.getMessage());
            System.err.println("❌ Device registration failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get device registration status
     */
    @GetMapping("/registration-status/{deviceId}")
    public ResponseEntity<Map<String, Object>> getRegistrationStatus(@PathVariable String deviceId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Device> deviceOpt = deviceService.findByDeviceId(deviceId);
            
            if (deviceOpt.isEmpty()) {
                response.put("registered", false);
                response.put("error", "Device not found");
                return ResponseEntity.status(404).body(response);
            }
            
            Device device = deviceOpt.get();
            
            response.put("registered", true);
            response.put("deviceId", device.getDeviceId());
            response.put("deviceName", device.getDeviceName());
            response.put("isOnline", device.getIsOnline());
            response.put("lastSeen", device.getLastSeen());
            response.put("registrationTime", device.getCreatedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("registered", false);
            response.put("error", "Status check failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Generate agent configuration for a device
     */
    @GetMapping("/agent-config/{deviceId}")
    public ResponseEntity<Map<String, Object>> getAgentConfig(@PathVariable String deviceId) {
        Map<String, Object> config = new HashMap<>();
        
        try {
            Optional<Device> deviceOpt = deviceService.findByDeviceId(deviceId);
            
            if (deviceOpt.isEmpty()) {
                config.put("error", "Device not found");
                return ResponseEntity.status(404).body(config);
            }
            
            Device device = deviceOpt.get();
            
            // Agent configuration
            config.put("deviceId", device.getDeviceId());
            config.put("deviceName", device.getDeviceName());
            config.put("userEmail", device.getUserEmail());
            config.put("serverUrl", "http://localhost:8080");
            config.put("apiEndpoint", "/api/agent/update");
            config.put("updateInterval", 30); // seconds
            config.put("enableLocationTracking", true);
            config.put("enableSystemMonitoring", true);
            config.put("enableNetworkMonitoring", true);
            config.put("agentVersion", "1.0.0");
            
            // Security settings
            Map<String, Object> security = new HashMap<>();
            security.put("enableEncryption", true);
            security.put("enableTamperDetection", true);
            security.put("enableRemoteWipe", true);
            security.put("enableRemoteLock", true);
            
            config.put("security", security);
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            config.put("error", "Config generation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(config);
        }
    }
    
    /**
     * Get current user's devices
     */
    @GetMapping("/my-devices")
    public ResponseEntity<Map<String, Object>> getMyDevices() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!authService.isLoggedIn()) {
                response.put("success", false);
                response.put("error", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }
            
            String userEmail = authService.getLoggedInUser();
            var devices = deviceService.getUserDevices(userEmail);
            
            response.put("success", true);
            response.put("devices", devices);
            response.put("totalDevices", devices.size());
            response.put("onlineDevices", devices.stream().mapToLong(d -> d.getIsOnline() != null && d.getIsOnline() ? 1 : 0).sum());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get devices: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
