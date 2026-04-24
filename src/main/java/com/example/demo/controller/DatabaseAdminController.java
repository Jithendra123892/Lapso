package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import com.example.demo.model.DeviceShare;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.DeviceShareRepository;
import com.example.demo.service.DeviceService;
import com.example.demo.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/database")
@Transactional(readOnly = true)
public class DatabaseAdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private DeviceShareRepository deviceShareRepository;
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private SecurityService securityService;
    
    /**
     * SECURITY FIXED: User-specific database view - Only current user's data
     * GET /api/database/user-view
     */
    @GetMapping("/user-view")
    public ResponseEntity<Map<String, Object>> getUserDatabaseView() {
        Map<String, Object> databaseView = new HashMap<>();
        
        try {
            // SECURITY FIX: Only return current user's data
            String currentUserEmail = securityService.getCurrentUserEmail();
            if (currentUserEmail == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }
            
            // Get current user information only
            User currentUser = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", currentUser.getId());
            userInfo.put("email", currentUser.getEmail());
            userInfo.put("name", currentUser.getName());
            userInfo.put("registrationDate", currentUser.getCreatedAt());
            userInfo.put("lastLogin", currentUser.getLastLoginAt());
            userInfo.put("isActive", currentUser.getIsActive());
            userInfo.put("authMethod", currentUser.getGoogleId() != null ? "Google OAuth" : "Email/Password");
            userInfo.put("deviceCount", currentUser.getDevices().size());
            
            // Get only current user's devices using secure method
            List<Device> devices = deviceService.getCurrentUserDevices();
            List<Map<String, Object>> deviceData = devices.stream().map(device -> {
                Map<String, Object> deviceInfo = new HashMap<>();
                deviceInfo.put("id", device.getId());
                deviceInfo.put("deviceId", device.getDeviceId());
                deviceInfo.put("deviceName", device.getDeviceName());
                deviceInfo.put("ownerEmail", device.getUser().getEmail());
                deviceInfo.put("manufacturer", device.getManufacturer());
                deviceInfo.put("model", device.getModel());
                deviceInfo.put("operatingSystem", device.getOsName() + " " + device.getOsVersion());
                deviceInfo.put("currentStatus", device.getIsOnline() ? "Online" : "Offline");
                deviceInfo.put("lastSeen", device.getLastSeen());
                deviceInfo.put("location", Map.of(
                    "latitude", device.getLatitude(),
                    "longitude", device.getLongitude(),
                    "address", device.getAddress()
                ));
                deviceInfo.put("systemInfo", Map.of(
                    "batteryLevel", device.getBatteryLevel(),
                    "isCharging", device.getIsCharging(),
                    "cpuUsage", device.getCpuUsage(),
                    "memoryUsed", device.getMemoryUsed(),
                    "memoryTotal", device.getMemoryTotal()
                ));
                deviceInfo.put("networkInfo", Map.of(
                    "ipAddress", device.getIpAddress(),
                    "wifiNetwork", device.getWifiSsid()
                ));
                deviceInfo.put("securityStatus", Map.of(
                    "isLocked", device.getIsLocked(),
                    "theftDetected", device.getTheftDetected()
                ));
                return deviceInfo;
            }).collect(Collectors.toList());
            
            // Get only current user's device shares 
            List<DeviceShare> shares = deviceShareRepository.findSharesByOwner(currentUser);
            List<Map<String, Object>> shareData = shares.stream().map(share -> {
                Map<String, Object> shareInfo = new HashMap<>();
                shareInfo.put("id", share.getId());
                shareInfo.put("deviceName", share.getDevice().getDeviceName());
                shareInfo.put("sharedWith", share.getSharedWith().getEmail());
                shareInfo.put("permissionLevel", share.getPermissionLevel());
                shareInfo.put("isActive", share.getIsActive());
                shareInfo.put("createdAt", share.getCreatedAt());
                shareInfo.put("expiresAt", share.getExpiresAt());
                shareInfo.put("isExpired", share.isExpired());
                return shareInfo;
            }).collect(Collectors.toList());
            
            // User-specific summary statistics
            Map<String, Object> summary = new HashMap<>();
            summary.put("myDevices", devices.size());
            summary.put("myShares", shares.size());
            summary.put("onlineDevices", devices.stream().mapToLong(d -> d.getIsOnline() != null && d.getIsOnline() ? 1 : 0).sum());
            summary.put("offlineDevices", devices.stream().mapToLong(d -> d.getIsOnline() == null || !d.getIsOnline() ? 1 : 0).sum());
            summary.put("secureDevices", devices.stream().mapToLong(d -> d.getTheftDetected() == null || !d.getTheftDetected() ? 1 : 0).sum());
            summary.put("averageBattery", devices.stream()
                .filter(d -> d.getBatteryLevel() != null)
                .mapToInt(Device::getBatteryLevel)
                .average().orElse(0));
            
            // Build secure response with only user's data
            databaseView.put("summary", summary);
            databaseView.put("userInfo", userInfo);
            databaseView.put("devices", deviceData);
            databaseView.put("deviceShares", shareData);
            databaseView.put("status", "success");
            databaseView.put("timestamp", java.time.LocalDateTime.now());
            databaseView.put("securityNote", "Data filtered for current user only - Privacy Protected");
            
            return ResponseEntity.ok(databaseView);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Database access failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Database statistics for monitoring
     * GET /api/database/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getDatabaseStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("userCount", userRepository.count());
            stats.put("deviceCount", deviceRepository.count());
            stats.put("shareCount", deviceShareRepository.count());
            stats.put("onlineDevices", deviceRepository.countByIsOnlineTrue());
            stats.put("offlineDevices", deviceRepository.countByIsOnlineFalse());
            stats.put("databaseStatus", "Connected");
            stats.put("lastChecked", java.time.LocalDateTime.now());
            stats.put("status", "success");
            
        } catch (Exception e) {
            stats.put("status", "error");
            stats.put("message", e.getMessage());
            stats.put("databaseStatus", "Error");
        }
        
        return stats;
    }
}
