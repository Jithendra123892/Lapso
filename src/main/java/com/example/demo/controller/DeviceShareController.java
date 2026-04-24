package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceShare;
import com.example.demo.model.User;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DeviceShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/share")
public class DeviceShareController {
    
    @Autowired
    private DeviceShareService deviceShareService;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Share a device with another user
     * POST /api/share/device
     */
    @PostMapping("/device")
    public ResponseEntity<Map<String, Object>> shareDevice(@RequestBody ShareDeviceRequest request) {
        try {
            String ownerEmail = getCurrentUserEmail();
            if (ownerEmail == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Find owner and device
            Optional<User> ownerOpt = userRepository.findByEmail(ownerEmail);
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(request.getDeviceId());
            
            if (ownerOpt.isEmpty() || deviceOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Device or user not found"));
            }
            
            User owner = ownerOpt.get();
            Device device = deviceOpt.get();
            
            // Check if user owns the device
            if (!device.getUser().getId().equals(owner.getId())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "You can only share devices you own"));
            }
            
            // Parse permission level
            DeviceShare.PermissionLevel permissionLevel = DeviceShare.PermissionLevel.valueOf(
                request.getPermissionLevel().toUpperCase());
            
            // Parse expiration date
            LocalDateTime expiresAt = null;
            if (request.getExpiresInDays() != null && request.getExpiresInDays() > 0) {
                expiresAt = LocalDateTime.now().plusDays(request.getExpiresInDays());
            }
            
            // Share the device
            DeviceShare share = deviceShareService.shareDevice(
                device, owner, request.getShareWithEmail(), 
                permissionLevel, request.getMessage(), expiresAt);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device shared successfully");
            response.put("shareId", share.getId());
            response.put("sharedWith", request.getShareWithEmail());
            response.put("permissionLevel", permissionLevel.toString());
            response.put("expiresAt", expiresAt);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to share device: " + e.getMessage()));
        }
    }
    
    /**
     * Get all devices accessible to the current user (owned + shared)
     * GET /api/share/accessible-devices
     */
    @GetMapping("/accessible-devices")
    public ResponseEntity<Map<String, Object>> getAccessibleDevices() {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
            }
            
            List<Device> accessibleDevices = deviceShareService.getAllAccessibleDevices(userOpt.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("devices", accessibleDevices);
            response.put("totalCount", accessibleDevices.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to get accessible devices: " + e.getMessage()));
        }
    }
    
    /**
     * Get shares owned by current user
     * GET /api/share/my-shares
     */
    @GetMapping("/my-shares")
    public ResponseEntity<Map<String, Object>> getMyShares() {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
            }
            
            List<DeviceShare> shares = deviceShareService.getSharesByOwner(userOpt.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shares", shares);
            response.put("totalCount", shares.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to get shares: " + e.getMessage()));
        }
    }
    
    /**
     * Get shares received by current user
     * GET /api/share/shared-with-me
     */
    @GetMapping("/shared-with-me")
    public ResponseEntity<Map<String, Object>> getSharedWithMe() {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
            }
            
            List<DeviceShare> shares = deviceShareService.getSharesForUser(userOpt.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shares", shares);
            response.put("totalCount", shares.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to get shared devices: " + e.getMessage()));
        }
    }
    
    /**
     * Revoke a device share
     * DELETE /api/share/{shareId}
     */
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Map<String, Object>> revokeShare(@PathVariable Long shareId) {
        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
            }
            
            deviceShareService.revokeShare(shareId, userOpt.get());
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Share revoked successfully"));
            
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to revoke share: " + e.getMessage()));
        }
    }
    
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof OidcUser) {
                return ((OidcUser) principal).getEmail();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        }
        return null;
    }
    
    // Request DTOs
    public static class ShareDeviceRequest {
        private String deviceId;
        private String shareWithEmail;
        private String permissionLevel = "VIEW_ONLY";
        private String message;
        private Integer expiresInDays;
        
        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public String getShareWithEmail() { return shareWithEmail; }
        public void setShareWithEmail(String shareWithEmail) { this.shareWithEmail = shareWithEmail; }
        
        public String getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Integer getExpiresInDays() { return expiresInDays; }
        public void setExpiresInDays(Integer expiresInDays) { this.expiresInDays = expiresInDays; }
    }
}
