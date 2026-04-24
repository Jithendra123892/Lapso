package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.Geofence;
import com.example.demo.repository.GeofenceRepository;
import com.example.demo.service.DeviceService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.PerfectAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geofences")
public class GeofenceController {

    @Autowired
    private GeofenceRepository geofenceRepository;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PerfectAuthService authService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Create a new geofence
     * POST /api/geofences
     */
    @PostMapping
    public ResponseEntity<?> createGeofence(@RequestBody GeofenceRequest request) {
        try {
            // Get authenticated user
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            // Validate input
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Geofence name is required"));
            }
            if (request.getCenterLatitude() == null || request.getCenterLongitude() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Center coordinates are required"));
            }
            if (request.getRadiusMeters() == null || request.getRadiusMeters() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid radius is required"));
            }

            // Create geofence
            Geofence geofence = new Geofence();
            geofence.setName(request.getName());
            geofence.setDescription(request.getDescription());
            geofence.setCenterLatitude(request.getCenterLatitude());
            geofence.setCenterLongitude(request.getCenterLongitude());
            geofence.setRadiusMeters(request.getRadiusMeters());
            geofence.setUserId(username);
            geofence.setIsActive(true);
            geofence.setAlertOnEntry(request.getAlertOnEntry() != null ? request.getAlertOnEntry() : false);
            geofence.setAlertOnExit(request.getAlertOnExit() != null ? request.getAlertOnExit() : true);
            geofence.setAutoLockOnExit(request.getAutoLockOnExit() != null ? request.getAutoLockOnExit() : false);
            
            if (request.getFenceType() != null) {
                geofence.setFenceType(Geofence.GeofenceType.valueOf(request.getFenceType()));
            }

            Geofence saved = geofenceRepository.save(geofence);

            // Send WebSocket notification
            messagingTemplate.convertAndSendToUser(username, "/queue/geofence-updates",
                    Map.of("action", "created", "geofence", saved));

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create geofence: " + e.getMessage()));
        }
    }

    /**
     * Get all geofences for authenticated user
     * GET /api/geofences
     */
    @GetMapping
    public ResponseEntity<?> getUserGeofences(@RequestParam(required = false) Boolean activeOnly) {
        try {
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            List<Geofence> geofences;
            if (activeOnly != null && activeOnly) {
                geofences = geofenceRepository.findByUserIdAndIsActiveTrue(username);
            } else {
                geofences = geofenceRepository.findByUserId(username);
            }

            return ResponseEntity.ok(geofences);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get geofences: " + e.getMessage()));
        }
    }

    /**
     * Get a specific geofence by ID
     * GET /api/geofences/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGeofence(@PathVariable Long id) {
        try {
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            Geofence geofence = geofenceRepository.findById(id).orElse(null);
            if (geofence == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Geofence not found"));
            }

            if (!geofence.getUserId().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            return ResponseEntity.ok(geofence);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get geofence: " + e.getMessage()));
        }
    }

    /**
     * Update an existing geofence
     * PUT /api/geofences/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGeofence(@PathVariable Long id, @RequestBody GeofenceRequest request) {
        try {
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            Geofence geofence = geofenceRepository.findById(id).orElse(null);
            if (geofence == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Geofence not found"));
            }

            if (!geofence.getUserId().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Update fields
            if (request.getName() != null) geofence.setName(request.getName());
            if (request.getDescription() != null) geofence.setDescription(request.getDescription());
            if (request.getCenterLatitude() != null) geofence.setCenterLatitude(request.getCenterLatitude());
            if (request.getCenterLongitude() != null) geofence.setCenterLongitude(request.getCenterLongitude());
            if (request.getRadiusMeters() != null) geofence.setRadiusMeters(request.getRadiusMeters());
            if (request.getAlertOnEntry() != null) geofence.setAlertOnEntry(request.getAlertOnEntry());
            if (request.getAlertOnExit() != null) geofence.setAlertOnExit(request.getAlertOnExit());
            if (request.getAutoLockOnExit() != null) geofence.setAutoLockOnExit(request.getAutoLockOnExit());
            if (request.getFenceType() != null) {
                geofence.setFenceType(Geofence.GeofenceType.valueOf(request.getFenceType()));
            }

            geofence.setUpdatedAt(LocalDateTime.now());
            Geofence updated = geofenceRepository.save(geofence);

            // Send WebSocket notification
            messagingTemplate.convertAndSendToUser(username, "/queue/geofence-updates",
                    Map.of("action", "updated", "geofence", updated));

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update geofence: " + e.getMessage()));
        }
    }

    /**
     * Delete a geofence
     * DELETE /api/geofences/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGeofence(@PathVariable Long id) {
        try {
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            Geofence geofence = geofenceRepository.findById(id).orElse(null);
            if (geofence == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Geofence not found"));
            }

            if (!geofence.getUserId().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            geofenceRepository.delete(geofence);

            // Send WebSocket notification
            messagingTemplate.convertAndSendToUser(username, "/queue/geofence-updates",
                    Map.of("action", "deleted", "geofenceId", id));

            return ResponseEntity.ok(Map.of("message", "Geofence deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete geofence: " + e.getMessage()));
        }
    }

    /**
     * Check if a device is currently inside any geofences
     * GET /api/geofences/check/{deviceId}
     */
    @GetMapping("/check/{deviceId}")
    public ResponseEntity<?> checkDeviceGeofences(@PathVariable String deviceId) {
        try {
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            Device device = deviceService.getDeviceByDeviceId(deviceId);
            if (device == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Device not found"));
            }

            if (!device.getUser().getEmail().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            if (device.getLatitude() == null || device.getLongitude() == null) {
                return ResponseEntity.ok(Map.of(
                        "insideGeofences", List.of(),
                        "message", "Device location not available"
                ));
            }

            // Find geofences containing device location
            List<Geofence> containingGeofences = geofenceRepository.findGeofencesContainingPoint(
                    username, device.getLatitude(), device.getLongitude());

            return ResponseEntity.ok(Map.of(
                    "deviceId", deviceId,
                    "deviceLocation", Map.of("latitude", device.getLatitude(), "longitude", device.getLongitude()),
                    "insideGeofences", containingGeofences,
                    "isInsideAnyGeofence", !containingGeofences.isEmpty()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check geofences: " + e.getMessage()));
        }
    }

    /**
     * Toggle geofence active status
     * POST /api/geofences/{id}/toggle
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggleGeofence(@PathVariable Long id) {
        try {
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            Geofence geofence = geofenceRepository.findById(id).orElse(null);
            if (geofence == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Geofence not found"));
            }

            if (!geofence.getUserId().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            geofence.setIsActive(!geofence.getIsActive());
            geofence.setUpdatedAt(LocalDateTime.now());
            Geofence updated = geofenceRepository.save(geofence);

            // Send WebSocket notification
            messagingTemplate.convertAndSendToUser(username, "/queue/geofence-updates",
                    Map.of("action", "toggled", "geofence", updated));

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to toggle geofence: " + e.getMessage()));
        }
    }

    /**
     * DTO for geofence creation/update requests
     */
    public static class GeofenceRequest {
        private String name;
        private String description;
        private Double centerLatitude;
        private Double centerLongitude;
        private Double radiusMeters;
        private String fenceType;
        private Boolean alertOnEntry;
        private Boolean alertOnExit;
        private Boolean autoLockOnExit;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getCenterLatitude() { return centerLatitude; }
        public void setCenterLatitude(Double centerLatitude) { this.centerLatitude = centerLatitude; }

        public Double getCenterLongitude() { return centerLongitude; }
        public void setCenterLongitude(Double centerLongitude) { this.centerLongitude = centerLongitude; }

        public Double getRadiusMeters() { return radiusMeters; }
        public void setRadiusMeters(Double radiusMeters) { this.radiusMeters = radiusMeters; }

        public String getFenceType() { return fenceType; }
        public void setFenceType(String fenceType) { this.fenceType = fenceType; }

        public Boolean getAlertOnEntry() { return alertOnEntry; }
        public void setAlertOnEntry(Boolean alertOnEntry) { this.alertOnEntry = alertOnEntry; }

        public Boolean getAlertOnExit() { return alertOnExit; }
        public void setAlertOnExit(Boolean alertOnExit) { this.alertOnExit = alertOnExit; }

        public Boolean getAutoLockOnExit() { return autoLockOnExit; }
        public void setAutoLockOnExit(Boolean autoLockOnExit) { this.autoLockOnExit = autoLockOnExit; }
    }
}

