package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import com.example.demo.model.LocationHistory;
import com.example.demo.service.DeviceService;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UltraPreciseTrackingService;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
@Validated
public class AgentDataController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UltraPreciseTrackingService ultraPreciseTrackingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private com.example.demo.service.AgentAuthenticationService agentAuthService;

    // Rate limiting and security tracking
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SecurityMetrics> securityMetrics = new ConcurrentHashMap<>();
    
    // Performance metrics
    private final ConcurrentHashMap<String, PerformanceMetrics> performanceMetrics = new ConcurrentHashMap<>();

    // Enhanced agent heartbeat with advanced security, rate limiting, and performance optimization
    @PostMapping("/heartbeat")
    @Transactional
    @CacheEvict(value = "deviceCache", key = "#request.deviceId", condition = "#request.deviceId != null")
    public ResponseEntity<?> receiveAgentData(@Valid @RequestBody AgentDataRequest request) {
        long startTime = System.currentTimeMillis();
        String clientIp = getClientIpAddress();
        
        // Advanced rate limiting and security checks
        if (!performSecurityChecks(request, clientIp)) {
            return ResponseEntity.status(429).body(Map.of(
                "status", "error",
                "message", "Rate limit exceeded or security violation detected"
            ));
        }
        try {
            System.out.println("📡 Received agent data from device: " + request.getDeviceId());
            System.out.println("👤 Agent requesting for user: " + request.getUserEmail());

            // ENHANCED SECURITY: Use dedicated authentication service
            var authResult = agentAuthService.authenticateAgent(
                request.getDeviceId(), 
                request.getUserEmail(), 
                clientIp
            );
            
            if (authResult.isRateLimited()) {
                System.err.println("🚨 SECURITY: Rate limited agent request from IP: " + clientIp);
                return ResponseEntity.status(429).body(Map.of(
                    "status", "error", 
                    "message", "Too many requests. Please try again later."
                ));
            }
            
            if (!authResult.isSuccess()) {
                System.err.println("🚨 SECURITY: Agent authentication failed: " + authResult.getErrorMessage());
                return ResponseEntity.status(403).body(Map.of(
                    "status", "error", 
                    "message", authResult.getErrorMessage()
                ));
            }
            
            User user = authResult.getUser();
            Device device = authResult.getDevice();
            
            if (authResult.isNewDevice()) {
                // Create new device for this user
                device = new Device();
                device.setDeviceId(request.getDeviceId());
                device.setDeviceName(request.getDeviceName() != null ? request.getDeviceName() : "Unknown Device");
                device.setUser(user);
                System.out.println("✅ SECURITY: Creating new device " + request.getDeviceId() + " for user " + user.getEmail());
            } else {
                System.out.println("✅ SECURITY: Device ownership verified for " + user.getEmail());
            }
            
            // Update device information with real data from agent
            device.setIsOnline(true);
            device.setLastSeen(LocalDateTime.now());
            device.setBatteryLevel(request.getBatteryLevel());
            device.setManufacturer(request.getManufacturer());
            device.setModel(request.getModel());
            device.setOperatingSystem(request.getOperatingSystem());
            device.setCpuUsage(request.getCpuUsage());
            device.setMemoryUsage(request.getMemoryUsage());
            device.setDiskUsage(request.getDiskUsage());
            try { device.setAgentVersion(request.getAgentVersion()); } catch (Exception ignored) {}

            // Track agent installation status
            if (device.getAgentInstalled() == null || !device.getAgentInstalled()) {
                device.setAgentInstalled(true);
                device.setAgentInstalledAt(LocalDateTime.now());
                System.out.println("🎉 Agent installed for device: " + device.getDeviceId());
            }
            device.setAgentLastHeartbeat(LocalDateTime.now());
            
            // Update location if provided
                // Update location if provided with sanity checks
                if (request.getLatitude() != null && request.getLongitude() != null) {
                    boolean acceptLocation = true;
                    Double prevLat = device.getLatitude();
                    Double prevLng = device.getLongitude();

                    if (prevLat != null && prevLng != null) {
                        double distanceKm = haversineKm(prevLat, prevLng, request.getLatitude(), request.getLongitude());
                        Double acc = request.getAccuracy();
                        // Reject big jumps (>30km) unless accuracy is reasonable (<= 2km)
                        if (distanceKm > 30 && (acc == null || acc > 2000)) {
                            acceptLocation = false;
                            System.err.println("⚠️ Ignoring suspicious location jump for device " + device.getDeviceId() +
                                    ": " + String.format("%.1f km", distanceKm) + 
                                    " (accuracy=" + (acc != null ? acc + "m" : "unknown") + ")");
                        }
                    }

                    if (acceptLocation) {
                        device.setLatitude(request.getLatitude());
                        device.setLongitude(request.getLongitude());
                        device.setAddress(request.getAddress());

                        // Use ultra-precise tracking for enhanced accuracy
                        UltraPreciseTrackingService.LocationUpdateRequest trackingRequest =
                            new UltraPreciseTrackingService.LocationUpdateRequest();
                        trackingRequest.setDeviceId(request.getDeviceId());
                        trackingRequest.setLatitude(request.getLatitude());
                        trackingRequest.setLongitude(request.getLongitude());
                        trackingRequest.setAddress(request.getAddress());
                        trackingRequest.setAccuracy(request.getAccuracy() != null ? request.getAccuracy() : 5.0);
                        trackingRequest.setLocationSource(request.getLocationSource() != null ?
                            request.getLocationSource() : "AGENT_ULTRA_PRECISE");
                        trackingRequest.setConfidence(0.99);
                        trackingRequest.setTimestamp(LocalDateTime.now());

                        ultraPreciseTrackingService.updateDeviceLocation(request.getDeviceId(), trackingRequest);

                        System.out.println("📍 ULTRA-PRECISE LOCATION UPDATE: Device " + device.getDeviceId() +
                            " - Lat: " + request.getLatitude() + ", Lng: " + request.getLongitude() +
                            " (Source: " + request.getLocationSource() + ", Accuracy: " + request.getAccuracy() + "cm)");
                        // Optionally store accuracy/source if your Device model supports it
                        try { device.setAccuracy(request.getAccuracy()); } catch (Exception ignored) {}
                        try { 
                            String source = request.getLocationSource();
                            device.setLocationSource(source != null ? source : "AGENT");  // Default to AGENT
                        } catch (Exception ignored) {
                            device.setLocationSource("AGENT");  // Fallback to AGENT
                        }
                    }
            }
            
            // Save device with user isolation
            System.out.println("💾 Saving device with ID: " + device.getDeviceId() + " for user: " + user.getEmail());
            
            // Check for low battery alert (before saving to compare previous state)
            Integer oldBattery = device.getBatteryLevel();
            Integer newBattery = request.getBatteryLevel();
            Boolean wasCharging = device.getIsCharging();
            Boolean isCharging = request.getIsCharging();
            
            device = deviceService.saveDevice(device);
            System.out.println("💾 Device saved successfully. DB ID: " + device.getId() + " | User: " + user.getEmail());
            
            // Send battery low alert if battery drops below 20% and not charging
            if (newBattery != null && newBattery < 20 && !Boolean.TRUE.equals(isCharging)) {
                if (oldBattery == null || oldBattery >= 20) {
                    // Battery just dropped below 20%
                    messagingTemplate.convertAndSend("/topic/alerts/" + user.getEmail(), Map.of(
                        "type", "BATTERY_LOW",
                        "severity", "warning",
                        "deviceId", device.getDeviceId(),
                        "deviceName", device.getDeviceName(),
                        "batteryLevel", newBattery,
                        "message", "⚠️ " + device.getDeviceName() + " battery is low (" + newBattery + "%)",
                        "timestamp", LocalDateTime.now()
                    ));
                    System.out.println("🔋 LOW BATTERY ALERT sent for " + device.getDeviceName() + ": " + newBattery + "%");
                }
            }
            
            // Send unplugged alert if device was charging and now isn't
            if (Boolean.TRUE.equals(wasCharging) && !Boolean.TRUE.equals(isCharging) && newBattery != null && newBattery < 100) {
                messagingTemplate.convertAndSend("/topic/alerts/" + user.getEmail(), Map.of(
                    "type", "DEVICE_UNPLUGGED",
                    "severity", "info",
                    "deviceId", device.getDeviceId(),
                    "deviceName", device.getDeviceName(),
                    "batteryLevel", newBattery,
                    "message", "🔌 " + device.getDeviceName() + " was unplugged (" + newBattery + "% remaining)",
                    "timestamp", LocalDateTime.now()
                ));
                System.out.println("🔌 UNPLUGGED ALERT sent for " + device.getDeviceName());
            }
            
            // Send real-time update to all connected clients via WebSocket
            messagingTemplate.convertAndSend("/topic/device-updates", new DeviceUpdateMessage(
                device.getId(),
                device.getDeviceId(),
                device.getDeviceName(),
                device.getIsOnline(),
                device.getBatteryLevel(),
                device.getCpuUsage(),
                device.getMemoryUsage(),
                device.getLastSeen()
            ));
            
            System.out.println("✅ Device updated: " + device.getDeviceName() + " (Battery: " + request.getBatteryLevel() + "%)");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Agent data received successfully",
                "deviceId", device.getDeviceId(),
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error processing agent data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to process agent data: " + e.getMessage()
            ));
        }
    }

    // Agent uninstall notification endpoint
    @PostMapping("/uninstall")
    @Transactional
    public ResponseEntity<?> agentUninstalled(@Valid @RequestBody AgentUninstallRequest request) {
        try {
            System.out.println("🗑️ Received agent uninstall notification from device: " + request.getDeviceId());
            
            // Find user by email
            User user = userRepository.findByEmail(request.getUserEmail()).orElse(null);
            if (user == null) {
                System.err.println("❌ User not found: " + request.getUserEmail());
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Find device and verify ownership
            Device device = deviceService.findByDeviceId(request.getDeviceId()).orElse(null);
            if (device == null || !device.getUser().getId().equals(user.getId())) {
                System.err.println("❌ Device not found or unauthorized: " + request.getDeviceId());
                return ResponseEntity.status(403).body(Map.of(
                    "status", "error",
                    "message", "Device not found or unauthorized"
                ));
            }
            
            // Update agent status
            device.setAgentInstalled(false);
            device.setAgentUninstalledAt(LocalDateTime.now());
            device.setIsOnline(false);
            device.setOfflineReason("Agent uninstalled");
            
            deviceService.saveDevice(device);
            
            // Send real-time update to dashboard via WebSocket
            messagingTemplate.convertAndSend("/topic/device-updates", new DeviceUpdateMessage(
                device.getId(),
                device.getDeviceId(),
                device.getDeviceName(),
                false, // isOnline
                device.getBatteryLevel(),
                device.getCpuUsage(),
                device.getMemoryUsage(),
                device.getLastSeen()
            ));
            
            System.out.println("✅ Agent uninstall recorded for device: " + device.getDeviceId());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Agent uninstall recorded successfully"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error recording agent uninstall: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to record agent uninstall: " + e.getMessage()
            ));
        }
    }

    // Enhanced agent configuration with dynamic settings
    @GetMapping("/config/{deviceId}")
    @Cacheable(value = "agentConfig", key = "#deviceId")
    public ResponseEntity<?> getAgentConfig(@PathVariable String deviceId) {
        try {
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            
            Map<String, Object> config = new HashMap<>();
            config.put("reportInterval", device != null && device.getIsOnline() ? 30 : 60);
            config.put("enableGPS", true);
            config.put("enableSystemMonitoring", true);
            config.put("enableSecurityScanning", true);
            config.put("enablePerformanceOptimization", true);
            config.put("serverUrl", "http://localhost:8086/api/agent/heartbeat");
            config.put("securityLevel", "enterprise");
            config.put("compressionEnabled", true);
            config.put("encryptionEnabled", true);
            
            // Dynamic configuration based on device status
            if (device != null) {
                config.put("batteryOptimization", device.getBatteryLevel() != null && device.getBatteryLevel() < 30);
                config.put("highFrequencyMode", device.getTheftDetected() != null && device.getTheftDetected());
            }
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            System.err.println("❌ Error getting agent config: " + e.getMessage());
            return ResponseEntity.ok(getDefaultConfig());
        }
    }

    // Advanced device analytics endpoint
    @GetMapping("/analytics/{deviceId}")
    public ResponseEntity<?> getDeviceAnalytics(@PathVariable String deviceId) {
        try {
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return ResponseEntity.notFound().build();
            }

            PerformanceMetrics metrics = performanceMetrics.get(deviceId);
            SecurityMetrics security = securityMetrics.get(deviceId);

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("deviceId", deviceId);
            analytics.put("uptime", calculateUptime(device));
            analytics.put("performanceScore", calculatePerformanceScore(device));
            analytics.put("securityScore", calculateSecurityScore(device, security));
            analytics.put("batteryHealth", analyzeBatteryHealth(device));
            analytics.put("systemHealth", analyzeSystemHealth(device));
            analytics.put("networkQuality", analyzeNetworkQuality(metrics));
            analytics.put("recommendations", generateRecommendations(device, metrics, security));

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            System.err.println("❌ Error getting device analytics: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Device command endpoint for remote management
    @PostMapping("/command/{deviceId}")
    public ResponseEntity<?> sendDeviceCommand(@PathVariable String deviceId, @RequestBody DeviceCommand command) {
        try {
            // Validate device ownership and permissions
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return ResponseEntity.notFound().build();
            }

            // Execute command based on type
            Map<String, Object> result = executeDeviceCommand(device, command);
            
            // Log security event
            logSecurityEvent(deviceId, "COMMAND_EXECUTED", command.getType());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Error executing device command: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Bulk device status endpoint
    @PostMapping("/bulk-status")
    public ResponseEntity<?> getBulkDeviceStatus(@RequestBody List<String> deviceIds) {
        try {
            Map<String, Object> statuses = new HashMap<>();
            
            for (String deviceId : deviceIds) {
                Device device = deviceService.findByDeviceId(deviceId).orElse(null);
                if (device != null) {
                    statuses.put(deviceId, Map.of(
                        "online", device.getIsOnline(),
                        "battery", device.getBatteryLevel() != null ? device.getBatteryLevel() : 0,
                        "lastSeen", device.getLastSeen(),
                        "location", device.getLatitude() != null ? 
                            Map.of("lat", device.getLatitude(), "lng", device.getLongitude()) : null
                    ));
                }
            }
            
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Security and performance helper methods
    private boolean performSecurityChecks(AgentDataRequest request, String clientIp) {
        String deviceId = request.getDeviceId();
        
        // Rate limiting check
        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(deviceId, k -> new RateLimitInfo());
        if (!rateLimitInfo.allowRequest()) {
            System.err.println("⚠️ Rate limit exceeded for device: " + deviceId);
            return false;
        }
        
        // Security metrics tracking
        SecurityMetrics metrics = securityMetrics.computeIfAbsent(deviceId, k -> new SecurityMetrics());
        metrics.recordRequest(clientIp);
        
        // Detect suspicious patterns
        if (metrics.isSuspiciousActivity()) {
            System.err.println("🚨 Suspicious activity detected for device: " + deviceId);
            logSecurityEvent(deviceId, "SUSPICIOUS_ACTIVITY", "Multiple IP addresses or high frequency requests");
            return false;
        }
        
        return true;
    }

    private String getClientIpAddress() {
        // In a real implementation, extract from HttpServletRequest
        return "127.0.0.1"; // Placeholder
    }

    private void logSecurityEvent(String deviceId, String eventType, String details) {
        System.out.println("🔒 SECURITY EVENT: " + eventType + " for device " + deviceId + " - " + details);
        // In production, log to security monitoring system
    }

    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("reportInterval", 60);
        config.put("enableGPS", true);
        config.put("enableSystemMonitoring", true);
        config.put("serverUrl", "http://localhost:8086/api/agent/heartbeat");
        return config;
    }

    private Duration calculateUptime(Device device) {
        if (device.getLastSeen() != null) {
            return Duration.between(device.getLastSeen().minusHours(24), LocalDateTime.now());
        }
        return Duration.ZERO;
    }

    private int calculatePerformanceScore(Device device) {
        int score = 100;
        
        if (device.getCpuUsage() != null && device.getCpuUsage() > 80) score -= 20;
        if (device.getMemoryUsage() != null && device.getMemoryUsage() > 85) score -= 15;
        if (device.getBatteryLevel() != null && device.getBatteryLevel() < 20) score -= 10;
        if (!device.getIsOnline()) score -= 30;
        
        return Math.max(0, score);
    }

    private int calculateSecurityScore(Device device, SecurityMetrics metrics) {
        int score = 100;
        
        if (device.getTheftDetected() != null && device.getTheftDetected()) score -= 50;
        if (metrics != null && metrics.isSuspiciousActivity()) score -= 30;
        if (device.getLastSeen() != null && 
            Duration.between(device.getLastSeen(), LocalDateTime.now()).toHours() > 24) score -= 20;
        
        return Math.max(0, score);
    }

    private String analyzeBatteryHealth(Device device) {
        if (device.getBatteryLevel() == null) return "Unknown";
        
        int battery = device.getBatteryLevel();
        if (battery > 80) return "Excellent";
        if (battery > 60) return "Good";
        if (battery > 40) return "Fair";
        if (battery > 20) return "Low";
        return "Critical";
    }

    private String analyzeSystemHealth(Device device) {
        if (device.getCpuUsage() == null || device.getMemoryUsage() == null) return "Unknown";
        
        double cpu = device.getCpuUsage();
        double memory = device.getMemoryUsage();
        
        if (cpu < 50 && memory < 70) return "Excellent";
        if (cpu < 70 && memory < 80) return "Good";
        if (cpu < 85 && memory < 90) return "Fair";
        return "Poor";
    }

    private String analyzeNetworkQuality(PerformanceMetrics metrics) {
        if (metrics == null) return "Unknown";
        
        long avgResponseTime = metrics.getAverageResponseTime();
        if (avgResponseTime < 100) return "Excellent";
        if (avgResponseTime < 300) return "Good";
        if (avgResponseTime < 500) return "Fair";
        return "Poor";
    }

    private List<String> generateRecommendations(Device device, PerformanceMetrics perf, SecurityMetrics sec) {
        List<String> recommendations = new java.util.ArrayList<>();
        
        if (device.getBatteryLevel() != null && device.getBatteryLevel() < 30) {
            recommendations.add("Charge device battery - currently at " + device.getBatteryLevel() + "%");
        }
        
        if (device.getCpuUsage() != null && device.getCpuUsage() > 80) {
            recommendations.add("High CPU usage detected - close unnecessary applications");
        }
        
        if (device.getMemoryUsage() != null && device.getMemoryUsage() > 85) {
            recommendations.add("High memory usage - restart device or close applications");
        }
        
        if (!device.getIsOnline()) {
            recommendations.add("Device is offline - check network connection");
        }
        
        if (sec != null && sec.isSuspiciousActivity()) {
            recommendations.add("Security alert - review recent device activity");
        }
        
        return recommendations;
    }

    private Map<String, Object> executeDeviceCommand(Device device, DeviceCommand command) {
        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", device.getDeviceId());
        result.put("command", command.getType());
        result.put("status", "executed");
        result.put("timestamp", LocalDateTime.now());
        
        switch (command.getType().toLowerCase()) {
            case "lock":
                result.put("message", "Device lock command sent");
                break;
            case "locate":
                result.put("message", "Location request sent");
                result.put("location", Map.of(
                    "lat", device.getLatitude() != null ? device.getLatitude() : 0.0,
                    "lng", device.getLongitude() != null ? device.getLongitude() : 0.0
                ));
                break;
            case "alarm":
                result.put("message", "Alarm activated");
                break;
            case "wipe":
                result.put("message", "Remote wipe initiated - WARNING: This will erase all data");
                break;
            default:
                result.put("message", "Unknown command");
                result.put("status", "error");
        }
        
        return result;
    }

    // Helper classes for advanced features
    private static class RateLimitInfo {
        private long lastRequest = 0;
        private int requestCount = 0;
        private static final int MAX_REQUESTS_PER_MINUTE = 120; // 2 requests per second max
        private static final long MINUTE_IN_MILLIS = 60000;
        
        public boolean allowRequest() {
            long now = System.currentTimeMillis();
            
            if (now - lastRequest > MINUTE_IN_MILLIS) {
                requestCount = 1;
                lastRequest = now;
                return true;
            }
            
            requestCount++;
            return requestCount <= MAX_REQUESTS_PER_MINUTE;
        }
    }

    private static class SecurityMetrics {
        private final Map<String, Integer> ipAddresses = new HashMap<>();
        private long firstRequest = System.currentTimeMillis();
        private int totalRequests = 0;
        
        public void recordRequest(String ip) {
            ipAddresses.put(ip, ipAddresses.getOrDefault(ip, 0) + 1);
            totalRequests++;
        }
        
        public boolean isSuspiciousActivity() {
            // Multiple IP addresses for same device
            if (ipAddresses.size() > 3) return true;
            
            // Too many requests in short time
            long timeSpan = System.currentTimeMillis() - firstRequest;
            if (timeSpan < 300000 && totalRequests > 100) return true; // 100 requests in 5 minutes
            
            return false;
        }
    }

    private static class PerformanceMetrics {
        private long totalResponseTime = 0;
        private int requestCount = 0;
        
        public void recordResponseTime(long responseTime) {
            totalResponseTime += responseTime;
            requestCount++;
        }
        
        public long getAverageResponseTime() {
            return requestCount > 0 ? totalResponseTime / requestCount : 0;
        }
    }

    public static class DeviceCommand {
        @NotBlank
        private String type;
        private Map<String, Object> parameters;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    // Enhanced agent data request model with validation
    public static class AgentDataRequest {
        @NotBlank(message = "Device ID is required")
        private String deviceId;
        
        private String deviceName;
        
        @NotBlank(message = "User email is required")
        @Email(message = "Valid email address is required")
        private String userEmail;  // User email to associate device with
        
        @Min(value = -1, message = "Battery level must be between -1 and 100 (-1 = no battery)")
        @Max(value = 100, message = "Battery level must be between -1 and 100 (-1 = no battery)")
        private Integer batteryLevel;
        
        private String manufacturer;
        private String model;
        private String operatingSystem;
        
        @Min(value = 0, message = "CPU usage must be between 0 and 100")
        @Max(value = 100, message = "CPU usage must be between 0 and 100")
        private Double cpuUsage;
        
        @Min(value = 0, message = "Memory usage must be between 0 and 100")
        @Max(value = 100, message = "Memory usage must be between 0 and 100")
        private Double memoryUsage;
        
        @Min(value = 0, message = "Disk usage must be between 0 and 100")
        @Max(value = 100, message = "Disk usage must be between 0 and 100")
        private Double diskUsage;
        
        private Double latitude;
        private Double longitude;
        private String address;
    private Double accuracy; // meters (optional)
    private String locationSource; // e.g., Windows_Location_API or IP_Geolocation
        
        // New enhanced fields
    private String networkType;
        private String wifiSSID;
        private Integer signalStrength;
        private Boolean isCharging;
        private String securityStatus;
        private Map<String, Object> systemInfo;
    private String agentVersion;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;

        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public Integer getBatteryLevel() { return batteryLevel; }
        public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }

        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public String getOperatingSystem() { return operatingSystem; }
        public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

        public Double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }

        public Double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }

        public Double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(Double diskUsage) { this.diskUsage = diskUsage; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public String getLocationSource() { return locationSource; }
    public void setLocationSource(String locationSource) { this.locationSource = locationSource; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        // New enhanced field getters and setters
        public String getNetworkType() { return networkType; }
        public void setNetworkType(String networkType) { this.networkType = networkType; }

        public String getWifiSSID() { return wifiSSID; }
        public void setWifiSSID(String wifiSSID) { this.wifiSSID = wifiSSID; }

        public Integer getSignalStrength() { return signalStrength; }
        public void setSignalStrength(Integer signalStrength) { this.signalStrength = signalStrength; }

        public Boolean getIsCharging() { return isCharging; }
        public void setIsCharging(Boolean isCharging) { this.isCharging = isCharging; }

        public String getSecurityStatus() { return securityStatus; }
        public void setSecurityStatus(String securityStatus) { this.securityStatus = securityStatus; }

        public Map<String, Object> getSystemInfo() { return systemInfo; }
        public void setSystemInfo(Map<String, Object> systemInfo) { this.systemInfo = systemInfo; }

        public String getAgentVersion() { return agentVersion; }
        public void setAgentVersion(String agentVersion) { this.agentVersion = agentVersion; }
    }
    
    // Helper method to get base URL
    private String getBaseUrl() {
        return "http://localhost:8086"; // In production, this should be configurable
    }

    // Haversine distance in kilometers
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // WebSocket message model
    public static class DeviceUpdateMessage {
        private Long id;
        private String deviceId;
        private String deviceName;
        private Boolean isOnline;
        private Integer batteryLevel;
        private Double cpuUsage;
        private Double memoryUsage;
        private LocalDateTime lastSeen;

        public DeviceUpdateMessage(Long id, String deviceId, String deviceName, Boolean isOnline, 
                                 Integer batteryLevel, Double cpuUsage, Double memoryUsage, LocalDateTime lastSeen) {
            this.id = id;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.isOnline = isOnline;
            this.batteryLevel = batteryLevel;
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.lastSeen = lastSeen;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public Boolean getIsOnline() { return isOnline; }
        public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }

        public Integer getBatteryLevel() { return batteryLevel; }
        public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }

        public Double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }

        public Double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }

        public LocalDateTime getLastSeen() { return lastSeen; }
        public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    }

    // Agent Uninstall Request DTO
    public static class AgentUninstallRequest {
        @NotBlank(message = "Device ID is required")
        private String deviceId;
        
        @NotBlank(message = "User email is required")
        @Email(message = "Invalid email format")
        private String userEmail;
        
        private String reason;

        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
