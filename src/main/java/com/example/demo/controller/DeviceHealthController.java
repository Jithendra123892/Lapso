package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.service.DeviceHealthService;
import com.example.demo.service.DeviceService;
import com.example.demo.service.PerfectAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/device-health")
public class DeviceHealthController {

    @Autowired
    private DeviceHealthService deviceHealthService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private PerfectAuthService authService;

    /**
     * Get comprehensive health report for a device
     * GET /api/device-health/{deviceId}
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getDeviceHealth(@PathVariable String deviceId) {
        try {
            // Get authenticated user
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            // Get device and verify ownership
            Device device = deviceService.getDeviceByDeviceId(deviceId);
            if (device == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Device not found"));
            }

            if (!device.getUser().getEmail().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Analyze device health
            DeviceHealthService.DeviceHealthReport report = deviceHealthService.analyzeDeviceHealth(device);

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to analyze device health: " + e.getMessage()));
        }
    }

    /**
     * Get quick health status for all user devices
     * GET /api/device-health/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getHealthSummary() {
        try {
            // Get authenticated user
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            // Get all devices for user
            var devices = deviceService.getDevicesByOwnerEmail(username);

            // Generate quick summary for each
            var summaries = devices.stream().map(device -> {
                DeviceHealthService.DeviceHealthReport report = deviceHealthService.analyzeDeviceHealth(device);
                return Map.of(
                        "deviceId", device.getDeviceId(),
                        "deviceName", device.getDeviceName(),
                        "overallScore", report.getOverallScore(),
                        "status", report.getStatus(),
                        "isOnline", device.getIsOnline(),
                        "batteryLevel", device.getBatteryLevel() != null ? device.getBatteryLevel() : 0,
                        "criticalIssues", report.getIssues().stream()
                                .filter(issue -> issue.startsWith("Critical:"))
                                .count()
                );
            }).toList();

            return ResponseEntity.ok(Map.of("devices", summaries));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get health summary: " + e.getMessage()));
        }
    }

    /**
     * Get battery health trends (placeholder - would need historical data)
     * GET /api/device-health/{deviceId}/battery-trends
     */
    @GetMapping("/{deviceId}/battery-trends")
    public ResponseEntity<?> getBatteryTrends(@PathVariable String deviceId) {
        try {
            // Get authenticated user
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            // Get device and verify ownership
            Device device = deviceService.getDeviceByDeviceId(deviceId);
            if (device == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Device not found"));
            }

            if (!device.getUser().getEmail().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // TODO: Implement historical battery tracking
            // For now, return current state
            return ResponseEntity.ok(Map.of(
                    "currentLevel", device.getBatteryLevel() != null ? device.getBatteryLevel() : 0,
                    "isCharging", device.getIsCharging() != null && device.getIsCharging(),
                    "message", "Historical battery trends coming soon"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get battery trends: " + e.getMessage()));
        }
    }

    /**
     * Get performance trends (placeholder - would need historical data)
     * GET /api/device-health/{deviceId}/performance-trends
     */
    @GetMapping("/{deviceId}/performance-trends")
    public ResponseEntity<?> getPerformanceTrends(@PathVariable String deviceId) {
        try {
            // Get authenticated user
            String username = authService.getCurrentUser();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            // Get device and verify ownership
            Device device = deviceService.getDeviceByDeviceId(deviceId);
            if (device == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Device not found"));
            }

            if (!device.getUser().getEmail().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // TODO: Implement historical performance tracking
            // For now, return current state
            return ResponseEntity.ok(Map.of(
                    "cpuUsage", device.getCpuUsage() != null ? device.getCpuUsage() : 0,
                    "memoryUsage", device.getMemoryUsagePercent() != null ? device.getMemoryUsagePercent() : 0,
                    "diskUsage", device.getDiskUsagePercent() != null ? device.getDiskUsagePercent() : 0,
                    "message", "Historical performance trends coming soon"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get performance trends: " + e.getMessage()));
        }
    }
}
