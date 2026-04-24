package com.example.demo.controller;

import com.example.demo.service.QuickActionsService;
import com.example.demo.service.PerfectAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/quick-actions")
public class QuickActionsController {

    @Autowired
    private QuickActionsService quickActionsService;
    
    @Autowired
    private PerfectAuthService authService;

    /**
     * Lock device remotely
     */
    @PostMapping("/lock/{deviceId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> lockDevice(@PathVariable String deviceId) {
        
        if (!authService.isLoggedIn()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"))
            );
        }
        
        String userEmail = authService.getLoggedInUser();
        
        return quickActionsService.lockDevice(deviceId, userEmail)
            .thenApply(result -> {
                if ((Boolean) result.get("success")) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.badRequest().body(result);
                }
            });
    }
    
    /**
     * Unlock device remotely
     */
    @PostMapping("/unlock/{deviceId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> unlockDevice(@PathVariable String deviceId) {
        
        if (!authService.isLoggedIn()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"))
            );
        }
        
        String userEmail = authService.getLoggedInUser();
        
        return quickActionsService.unlockDevice(deviceId, userEmail)
            .thenApply(result -> {
                if ((Boolean) result.get("success")) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.badRequest().body(result);
                }
            });
    }
    
    /**
     * Wipe device remotely (DANGEROUS)
     */
    @PostMapping("/wipe/{deviceId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> wipeDevice(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        
        if (!authService.isLoggedIn()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"))
            );
        }
        
        String userEmail = authService.getLoggedInUser();
        String confirmationCode = request.get("confirmationCode");
        
        return quickActionsService.wipeDevice(deviceId, userEmail, confirmationCode)
            .thenApply(result -> {
                if ((Boolean) result.get("success")) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.badRequest().body(result);
                }
            });
    }
    
    /**
     * Play sound on device
     */
    @PostMapping("/play-sound/{deviceId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> playSound(@PathVariable String deviceId) {
        
        if (!authService.isLoggedIn()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"))
            );
        }
        
        String userEmail = authService.getLoggedInUser();
        
        return quickActionsService.playSoundOnDevice(deviceId, userEmail)
            .thenApply(result -> {
                if ((Boolean) result.get("success")) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.badRequest().body(result);
                }
            });
    }
    
    /**
     * Take screenshot
     */
    @PostMapping("/screenshot/{deviceId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> takeScreenshot(@PathVariable String deviceId) {
        
        if (!authService.isLoggedIn()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"))
            );
        }
        
        String userEmail = authService.getLoggedInUser();
        
        return quickActionsService.takeScreenshot(deviceId, userEmail)
            .thenApply(result -> {
                if ((Boolean) result.get("success")) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.badRequest().body(result);
                }
            });
    }
    
    /**
     * Take camera photo
     */
    @PostMapping("/camera/{deviceId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> takeCameraPhoto(@PathVariable String deviceId) {
        
        if (!authService.isLoggedIn()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"))
            );
        }
        
        String userEmail = authService.getLoggedInUser();
        
        return quickActionsService.takeCameraPhoto(deviceId, userEmail)
            .thenApply(result -> {
                if ((Boolean) result.get("success")) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.badRequest().body(result);
                }
            });
    }

    /**
     * Get current location
     */
    @GetMapping("/location/{deviceId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getCurrentLocation(@PathVariable String deviceId) {
        
        if (!authService.isLoggedIn()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"))
            );
        }
        
        String userEmail = authService.getLoggedInUser();
        
        return quickActionsService.getCurrentLocation(deviceId, userEmail)
            .thenApply(result -> {
                if ((Boolean) result.get("success")) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.badRequest().body(result);
                }
            });
    }
    
    /**
     * Get available quick actions for a device
     */
    @GetMapping("/available/{deviceId}")
    public ResponseEntity<Map<String, Object>> getAvailableActions(@PathVariable String deviceId) {
        
        if (!authService.isLoggedIn()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"));
        }
        
        Map<String, Object> actions = new HashMap<>();
        
        // Basic actions available for all devices
        actions.put("lock", Map.of(
            "name", "Lock Device",
            "description", "Remotely lock the device screen",
            "icon", "🔒",
            "endpoint", "/api/quick-actions/lock/" + deviceId,
            "method", "POST",
            "requiresConfirmation", false
        ));
        
        actions.put("unlock", Map.of(
            "name", "Unlock Device",
            "description", "Remotely unlock the device screen",
            "icon", "🔓",
            "endpoint", "/api/quick-actions/unlock/" + deviceId,
            "method", "POST",
            "requiresConfirmation", false
        ));
        
        actions.put("playSound", Map.of(
            "name", "Play Sound",
            "description", "Play a sound to help locate the device",
            "icon", "🔊",
            "endpoint", "/api/quick-actions/play-sound/" + deviceId,
            "method", "POST",
            "requiresConfirmation", false,
            "requiresOnline", true
        ));
        
        actions.put("screenshot", Map.of(
            "name", "Take Screenshot",
            "description", "Capture a screenshot of the device screen",
            "icon", "📸",
            "endpoint", "/api/quick-actions/screenshot/" + deviceId,
            "method", "POST",
            "requiresConfirmation", false,
            "requiresOnline", true
        ));
        
        actions.put("camera", Map.of(
            "name", "Take Camera Photo",
            "description", "Capture a photo from the device camera",
            "icon", "📷",
            "endpoint", "/api/quick-actions/camera/" + deviceId,
            "method", "POST",
            "requiresConfirmation", false,
            "requiresOnline", true
        ));

        actions.put("location", Map.of(
            "name", "Get Location",
            "description", "Get current or last known location",
            "icon", "📍",
            "endpoint", "/api/quick-actions/location/" + deviceId,
            "method", "GET",
            "requiresConfirmation", false
        ));
        
        actions.put("wipe", Map.of(
            "name", "Emergency Wipe",
            "description", "DANGER: Permanently delete all data on device",
            "icon", "💣",
            "endpoint", "/api/quick-actions/wipe/" + deviceId,
            "method", "POST",
            "requiresConfirmation", true,
            "confirmationCode", "WIPE_CONFIRMED",
            "warning", "This action cannot be undone and will permanently delete all data"
        ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deviceId", deviceId);
        response.put("actions", actions);
        response.put("totalActions", actions.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Execute multiple actions in sequence
     */
    @PostMapping("/batch/{deviceId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> executeBatchActions(
            @PathVariable String deviceId,
            @RequestBody Map<String, Object> request) {
        
        if (!authService.isLoggedIn()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(401).body(Map.of("success", false, "error", "Authentication required"))
            );
        }
        
        String userEmail = authService.getLoggedInUser();
        
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            
            try {
                // This would execute multiple actions in sequence
                // For example: lock device, take screenshot, get location
                
                result.put("success", true);
                result.put("message", "Batch actions feature coming soon");
                result.put("deviceId", deviceId);
                
            } catch (Exception e) {
                result.put("success", false);
                result.put("error", "Batch execution failed: " + e.getMessage());
            }
            
            return ResponseEntity.ok(result);
        });
    }
}
