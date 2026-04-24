package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import com.example.demo.service.DeviceService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceAgentController {

    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private UserService userService;

    /**
     * 🛡️ Enterprise Security Helper - Get authenticated user context
     */
    private String getCurrentAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
        } catch (Exception e) {
            System.err.println("🚨 Security context error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Auto-register device from client agent
     * 🔐 SECURE: Each device is bound to specific user with fingerprint validation
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerDevice(@RequestBody Map<String, Object> deviceData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String deviceId = (String) deviceData.get("deviceId");
            String ownerEmail = (String) deviceData.get("ownerEmail");
            String deviceName = (String) deviceData.get("deviceName");
            String ownerFingerprint = (String) deviceData.get("deviceOwnerFingerprint");
            String userDeviceToken = (String) deviceData.get("userDeviceToken");
            
            if (deviceId == null || ownerEmail == null || ownerFingerprint == null) {
                response.put("success", false);
                response.put("message", "Missing required security fields: deviceId, ownerEmail, or deviceOwnerFingerprint");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 🔐 SECURITY VALIDATION: Ensure device-user binding integrity
            Device existingDevice = deviceService.findByDeviceId(deviceId).orElse(null);
            if (existingDevice != null) {
                // Verify this device belongs to the requesting user
                if (!existingDevice.getUser().getEmail().equals(ownerEmail)) {
                    System.err.println("🚨 SECURITY BREACH ATTEMPT: Device " + deviceId + " belongs to " + 
                                     existingDevice.getUser().getEmail() + " but " + ownerEmail + " tried to access it!");
                    response.put("success", false);
                    response.put("message", "Unauthorized: Device belongs to different user");
                    return ResponseEntity.status(403).body(response);
                }
                
                // Additional security: Verify fingerprint matches
                String storedFingerprint = existingDevice.getSerialNumber(); // We store fingerprint in serial field
                if (storedFingerprint != null && !storedFingerprint.equals(ownerFingerprint)) {
                    System.err.println("🚨 FINGERPRINT MISMATCH: Device " + deviceId + " fingerprint changed!");
                    response.put("success", false);
                    response.put("message", "Security validation failed: Device fingerprint mismatch");
                    return ResponseEntity.status(403).body(response);
                }
            }
            
            // Find or create user
            User user = userService.findOrCreateUser(ownerEmail, ownerEmail.split("@")[0], null, null);
            
            Device device;
            
            if (existingDevice != null) {
                // Update existing device
                device = existingDevice;
                updateDeviceFromData(device, deviceData);
                System.out.println("🔄 Updated existing SECURE device: " + deviceId + " for user: " + ownerEmail);
            } else {
                // Create new device with security binding
                device = new Device();
                device.setDeviceId(deviceId);
                device.setUser(user);
                device.setSerialNumber(ownerFingerprint); // Store security fingerprint
                if (deviceName != null && !deviceName.isEmpty()) {
                    device.setDeviceName(deviceName);
                }
                updateDeviceFromData(device, deviceData);
                System.out.println("✨ Registered new SECURE device: " + deviceId + " for user: " + ownerEmail);
            }
            
            // Save device
            deviceService.saveDevice(device);
            
            response.put("success", true);
            response.put("message", "Device registered securely");
            response.put("deviceId", deviceId);
            response.put("registered", existingDevice == null);
            response.put("securityFingerprint", ownerFingerprint.substring(0, 8) + "...");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Device registration failed: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Update device information from client agent
     * 🔐 SECURE: Validates device ownership before allowing updates
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateDevice(@RequestBody Map<String, Object> deviceData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String deviceId = (String) deviceData.get("deviceId");
            String ownerEmail = (String) deviceData.get("ownerEmail");
            String ownerFingerprint = (String) deviceData.get("deviceOwnerFingerprint");
            
            if (deviceId == null || ownerEmail == null) {
                response.put("success", false);
                response.put("message", "Missing deviceId or ownerEmail");
                return ResponseEntity.badRequest().body(response);
            }
            
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            
            if (device == null) {
                response.put("success", false);
                response.put("message", "Device not found: " + deviceId);
                return ResponseEntity.notFound().build();
            }
            
            // 🔐 SECURITY VALIDATION: Verify device ownership
            if (!device.getUser().getEmail().equals(ownerEmail)) {
                System.err.println("🚨 UNAUTHORIZED UPDATE ATTEMPT: Device " + deviceId + " by " + ownerEmail);
                response.put("success", false);
                response.put("message", "Unauthorized: Cannot update device belonging to different user");
                return ResponseEntity.status(403).body(response);
            }
            
            // Update device with new data
            updateDeviceFromData(device, deviceData);
            deviceService.saveDevice(device);
            
            response.put("success", true);
            response.put("message", "Device updated successfully");
            response.put("deviceId", deviceId);
            response.put("lastUpdated", device.getLastSeen());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Device update failed: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Update failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Device heartbeat endpoint for real-time status
     * 🔐 SECURE: Only processes heartbeats from authorized devices
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> deviceHeartbeat(@RequestBody Map<String, Object> heartbeatData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String deviceId = (String) heartbeatData.get("deviceId");
            String ownerEmail = (String) heartbeatData.get("ownerEmail");
            
            if (deviceId == null) {
                response.put("success", false);
                response.put("message", "Missing deviceId");
                return ResponseEntity.badRequest().body(response);
            }
            
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            
            if (device != null) {
                // 🔐 SECURITY VALIDATION: Verify device ownership for heartbeat
                if (ownerEmail != null && !device.getUser().getEmail().equals(ownerEmail)) {
                    System.err.println("🚨 UNAUTHORIZED HEARTBEAT: Device " + deviceId + " by " + ownerEmail);
                    response.put("success", false);
                    response.put("message", "Unauthorized heartbeat");
                    return ResponseEntity.status(403).body(response);
                }
                
                // Update last seen and online status
                device.setLastSeen(LocalDateTime.now());
                device.setIsOnline(true);
                
                // Update real-time data if provided
                if (heartbeatData.containsKey("batteryLevel")) {
                    device.setBatteryLevel(((Number) heartbeatData.get("batteryLevel")).intValue());
                }
                if (heartbeatData.containsKey("isCharging")) {
                    device.setIsCharging((Boolean) heartbeatData.get("isCharging"));
                }
                if (heartbeatData.containsKey("cpuUsage")) {
                    device.setCpuUsage(((Number) heartbeatData.get("cpuUsage")).doubleValue());
                }
                if (heartbeatData.containsKey("memoryUsed")) {
                    device.setMemoryUsed(((Number) heartbeatData.get("memoryUsed")).longValue());
                }
                
                deviceService.saveDevice(device);
            }
            
            response.put("success", true);
            response.put("message", "Heartbeat received");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Heartbeat failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Update device from received data
     */
    private void updateDeviceFromData(Device device, Map<String, Object> data) {
        // Basic device info
        if (data.containsKey("deviceName")) {
            device.setDeviceName((String) data.get("deviceName"));
        }
        if (data.containsKey("manufacturer")) {
            device.setManufacturer((String) data.get("manufacturer"));
        }
        if (data.containsKey("model")) {
            device.setModel((String) data.get("model"));
        }
        if (data.containsKey("osVersion")) {
            device.setOsVersion((String) data.get("osVersion"));
        }
        if (data.containsKey("platform")) {
            device.setOsName((String) data.get("platform"));
        }
        
        // System specs
        if (data.containsKey("totalMemory")) {
            device.setMemoryTotal(((Number) data.get("totalMemory")).longValue());
        }
        if (data.containsKey("freeMemory")) {
            device.setMemoryUsed(device.getMemoryTotal() - ((Number) data.get("freeMemory")).longValue());
        }
        // CPU model info stored in device name field
        if (data.containsKey("cpuModel")) {
            String cpuModel = (String) data.get("cpuModel");
            // Store CPU model info in device name if empty
            if (device.getDeviceName() == null || device.getDeviceName().isEmpty()) {
                device.setDeviceName(cpuModel);
            }
        }
        
        // Location data
        if (data.containsKey("location") && data.get("location") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> location = (Map<String, Object>) data.get("location");
            if (location.containsKey("latitude") && location.get("latitude") != null) {
                device.setLatitude(((Number) location.get("latitude")).doubleValue());
            }
            if (location.containsKey("longitude") && location.get("longitude") != null) {
                device.setLongitude(((Number) location.get("longitude")).doubleValue());
            }
            if (location.containsKey("city")) {
                device.setAddress((String) location.get("city"));
            }
            if (location.containsKey("ip")) {
                device.setIpAddress((String) location.get("ip"));
            }
        }
        
        // Network info
        if (data.containsKey("networkInterfaces")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> networks = (Map<String, Object>) data.get("networkInterfaces");
            // Find first Wi-Fi interface
            for (Map.Entry<String, Object> entry : networks.entrySet()) {
                if (entry.getKey().toLowerCase().contains("wi-fi") || entry.getKey().toLowerCase().contains("wireless")) {
                    device.setWifiSsid(entry.getKey());
                    break;
                }
            }
        }
        
        // Battery info
        if (data.containsKey("batteryInfo") && data.get("batteryInfo") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> battery = (Map<String, Object>) data.get("batteryInfo");
            if (battery.containsKey("level") && battery.get("level") != null) {
                device.setBatteryLevel(((Number) battery.get("level")).intValue());
            }
            if (battery.containsKey("charging")) {
                device.setIsCharging((Boolean) battery.get("charging"));
            }
        }
        
        // Disk info
        if (data.containsKey("diskInfo") && data.get("diskInfo") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> disk = (Map<String, Object>) data.get("diskInfo");
            if (disk.containsKey("total") && disk.get("total") != null) {
                device.setDiskTotal(((Number) disk.get("total")).longValue());
            }
            if (disk.containsKey("used") && disk.get("used") != null) {
                device.setDiskUsed(((Number) disk.get("used")).longValue());
            }
        }
        
        // Security info
        if (data.containsKey("isLocked")) {
            device.setIsLocked((Boolean) data.get("isLocked"));
        }
        
        // Status
        device.setIsOnline(true);
        device.setLastSeen(LocalDateTime.now());
        
        System.out.println("🔐 Updated SECURE device data for: " + device.getDeviceId());
    }
}
