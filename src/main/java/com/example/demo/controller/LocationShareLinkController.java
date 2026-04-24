package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.LocationShareLink;
import com.example.demo.repository.LocationShareLinkRepository;
import com.example.demo.service.DeviceService;
import com.example.demo.service.PerfectAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/share-links")
public class LocationShareLinkController {

    @Autowired
    private LocationShareLinkRepository shareLinkRepository;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private PerfectAuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create a new location sharing link
     * POST /api/share-links/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createShareLink(@RequestBody CreateLinkRequest request) {
        try {
            String currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            // Validate input
            if (request.getDeviceId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Device ID is required"));
            }

            // Get device and verify ownership
            Device device = deviceService.getDeviceByDeviceId(request.getDeviceId());
            if (device == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Device not found"));
            }

            if (!device.getUser().getEmail().equals(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only device owner can create share links"));
            }

            // Create share link
            LocationShareLink link = new LocationShareLink();
            link.setDevice(device);
            link.setOwnerEmail(currentUser);
            link.setTitle(request.getTitle());
            link.setDescription(request.getDescription());
            link.setShareType(LocationShareLink.ShareType.valueOf(
                    request.getShareType() != null ? request.getShareType() : "REAL_TIME"));

            // Set expiration
            if (request.getExpiresInHours() != null && request.getExpiresInHours() > 0) {
                link.setExpiresAt(LocalDateTime.now().plusHours(request.getExpiresInHours()));
            } else if (request.getExpiresAt() != null) {
                link.setExpiresAt(request.getExpiresAt());
            }

            // Set max views
            if (request.getMaxViews() != null && request.getMaxViews() > 0) {
                link.setMaxViews(request.getMaxViews());
            }

            // Set password if provided
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                link.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                link.setIsPasswordProtected(true);
            }

            LocationShareLink saved = shareLinkRepository.save(link);

            // Build response with share URL
            String shareUrl = "https://your-domain.com/share/" + saved.getToken();

            return ResponseEntity.ok(Map.of(
                    "message", "Share link created successfully",
                    "link", saved,
                    "shareUrl", shareUrl,
                    "token", saved.getToken()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create share link: " + e.getMessage()));
        }
    }

    /**
     * Get all share links for current user's devices
     * GET /api/share-links/my-links
     */
    @GetMapping("/my-links")
    public ResponseEntity<?> getMyLinks(@RequestParam(required = false) String deviceId,
                                       @RequestParam(required = false) Boolean activeOnly) {
        try {
            String currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            List<LocationShareLink> links;
            if (deviceId != null) {
                Device device = deviceService.getDeviceByDeviceId(deviceId);
                if (device == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Device not found"));
                }
                if (!device.getUser().getEmail().equals(currentUser)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Access denied"));
                }
                links = shareLinkRepository.findByDeviceAndIsActiveTrue(device);
            } else {
                if (activeOnly != null && activeOnly) {
                    links = shareLinkRepository.findByOwnerEmailAndIsActiveTrue(currentUser);
                } else {
                    links = shareLinkRepository.findByOwnerEmail(currentUser);
                }
            }

            // Add share URLs and status
            List<Map<String, Object>> result = links.stream().map(link -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", link.getId());
                map.put("token", link.getToken());
                map.put("deviceId", link.getDevice().getDeviceId());
                map.put("deviceName", link.getDevice().getDeviceName());
                map.put("title", link.getTitle());
                map.put("shareType", link.getShareType());
                map.put("viewCount", link.getViewCount());
                map.put("maxViews", link.getMaxViews());
                map.put("expiresAt", link.getExpiresAt());
                map.put("isPasswordProtected", link.getIsPasswordProtected());
                map.put("isActive", link.getIsActive());
                map.put("isExpired", link.isExpired());
                map.put("isValid", link.isValid());
                map.put("shareUrl", link.getShareUrl("https://your-domain.com"));
                map.put("createdAt", link.getCreatedAt());
                map.put("lastAccessedAt", link.getLastAccessedAt());
                return map;
            }).toList();

            return ResponseEntity.ok(Map.of("links", result));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get share links: " + e.getMessage()));
        }
    }

    /**
     * Validate and access a share link (public endpoint)
     * POST /api/share-links/access/{token}
     */
    @PostMapping("/access/{token}")
    public ResponseEntity<?> accessShareLink(@PathVariable String token,
                                            @RequestBody(required = false) Map<String, String> request) {
        try {
            LocationShareLink link = shareLinkRepository.findByToken(token);
            if (link == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Share link not found"));
            }

            // Check if valid
            if (!link.isValid()) {
                String reason = link.isExpired() ? "expired" :
                               link.isMaxViewsReached() ? "max views reached" : "inactive";
                return ResponseEntity.status(HttpStatus.GONE)
                        .body(Map.of("error", "Share link is " + reason));
            }

            // Check password if protected
            if (link.getIsPasswordProtected()) {
                String password = request != null ? request.get("password") : null;
                if (password == null || !passwordEncoder.matches(password, link.getPasswordHash())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Invalid password", "requiresPassword", true));
                }
            }

            // Increment view count
            link.incrementViewCount();
            shareLinkRepository.save(link);

            // Get device location
            Device device = link.getDevice();
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("deviceName", device.getDeviceName());
            locationData.put("latitude", device.getLatitude());
            locationData.put("longitude", device.getLongitude());
            locationData.put("lastUpdated", device.getLastSeen());
            locationData.put("isOnline", device.getIsOnline());
            locationData.put("batteryLevel", device.getBatteryLevel());
            locationData.put("shareType", link.getShareType());
            locationData.put("title", link.getTitle());
            locationData.put("description", link.getDescription());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "location", locationData,
                    "expiresAt", link.getExpiresAt(),
                    "viewCount", link.getViewCount(),
                    "maxViews", link.getMaxViews()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to access share link: " + e.getMessage()));
        }
    }

    /**
     * Deactivate a share link
     * DELETE /api/share-links/{linkId}
     */
    @DeleteMapping("/{linkId}")
    public ResponseEntity<?> deactivateLink(@PathVariable Long linkId) {
        try {
            String currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            LocationShareLink link = shareLinkRepository.findById(linkId).orElse(null);
            if (link == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Share link not found"));
            }

            if (!link.getOwnerEmail().equals(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only link owner can deactivate it"));
            }

            link.setIsActive(false);
            shareLinkRepository.save(link);

            return ResponseEntity.ok(Map.of("message", "Share link deactivated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deactivate link: " + e.getMessage()));
        }
    }

    /**
     * Get statistics for a share link
     * GET /api/share-links/stats/{linkId}
     */
    @GetMapping("/stats/{linkId}")
    public ResponseEntity<?> getLinkStats(@PathVariable Long linkId) {
        try {
            String currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            LocationShareLink link = shareLinkRepository.findById(linkId).orElse(null);
            if (link == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Share link not found"));
            }

            if (!link.getOwnerEmail().equals(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalViews", link.getViewCount());
            stats.put("maxViews", link.getMaxViews());
            stats.put("remainingViews", link.getMaxViews() != null ? 
                     link.getMaxViews() - link.getViewCount() : "Unlimited");
            stats.put("createdAt", link.getCreatedAt());
            stats.put("lastAccessedAt", link.getLastAccessedAt());
            stats.put("expiresAt", link.getExpiresAt());
            stats.put("isExpired", link.isExpired());
            stats.put("isValid", link.isValid());
            stats.put("shareUrl", link.getShareUrl("https://your-domain.com"));

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get stats: " + e.getMessage()));
        }
    }

    /**
     * DTO for creating share links
     */
    public static class CreateLinkRequest {
        private String deviceId;
        private String title;
        private String description;
        private String shareType = "REAL_TIME";
        private Integer expiresInHours;
        private LocalDateTime expiresAt;
        private Integer maxViews;
        private String password;

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getShareType() { return shareType; }
        public void setShareType(String shareType) { this.shareType = shareType; }

        public Integer getExpiresInHours() { return expiresInHours; }
        public void setExpiresInHours(Integer expiresInHours) { this.expiresInHours = expiresInHours; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public Integer getMaxViews() { return maxViews; }
        public void setMaxViews(Integer maxViews) { this.maxViews = maxViews; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
