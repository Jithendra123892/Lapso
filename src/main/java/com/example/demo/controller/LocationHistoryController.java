package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.LocationHistory;
import com.example.demo.repository.LocationHistoryRepository;
import com.example.demo.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/location-history")
@CrossOrigin(origins = "*")
public class LocationHistoryController {

    @Autowired
    private LocationHistoryRepository locationHistoryRepository;

    @Autowired
    private DeviceService deviceService;

    // Get location history for a device
    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getLocationHistory(
            @PathVariable String deviceId,
            @RequestParam(required = false) Integer hours) {
        
        try {
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Device not found"
                ));
            }

            // Get authenticated user and verify ownership
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String email = auth.getName();
                if (!device.getUser().getEmail().equals(email)) {
                    return ResponseEntity.status(403).body(Map.of(
                        "status", "error",
                        "message", "Unauthorized access"
                    ));
                }
            }

            LocalDateTime since = hours != null ? 
                LocalDateTime.now().minusHours(hours) : 
                LocalDateTime.now().minusDays(30);

            List<LocationHistory> history = locationHistoryRepository
                .findByDeviceAndTimestampAfterOrderByTimestampDesc(device, since);

            List<Map<String, Object>> locations = history.stream()
                .map(loc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("latitude", loc.getLatitude());
                    map.put("longitude", loc.getLongitude());
                    map.put("timestamp", loc.getTimestamp().toString());
                    map.put("accuracy", loc.getAccuracy());
                    map.put("address", loc.getAddress());
                    map.put("speed", loc.getSpeed());
                    map.put("batteryLevel", loc.getBatteryLevel());
                    map.put("isOnline", loc.getIsOnline());
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "deviceId", deviceId,
                "locations", locations,
                "count", locations.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to get location history: " + e.getMessage()
            ));
        }
    }

    // Get heatmap data (aggregated locations)
    @GetMapping("/{deviceId}/heatmap")
    public ResponseEntity<?> getHeatmapData(@PathVariable String deviceId) {
        try {
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Device not found"
                ));
            }

            // Get last 30 days of location data
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<LocationHistory> history = locationHistoryRepository
                .findByDeviceAndTimestampAfterOrderByTimestampDesc(device, since);

            // Group nearby locations (within 100m) to create heatmap points
            List<Map<String, Object>> heatmapPoints = new ArrayList<>();
            Map<String, Integer> locationCounts = new HashMap<>();

            for (LocationHistory loc : history) {
                String key = String.format("%.3f,%.3f", loc.getLatitude(), loc.getLongitude());
                locationCounts.put(key, locationCounts.getOrDefault(key, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : locationCounts.entrySet()) {
                String[] coords = entry.getKey().split(",");
                heatmapPoints.add(Map.of(
                    "latitude", Double.parseDouble(coords[0]),
                    "longitude", Double.parseDouble(coords[1]),
                    "weight", entry.getValue()
                ));
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "heatmapPoints", heatmapPoints
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to get heatmap data: " + e.getMessage()
            ));
        }
    }

    // Get statistics
    @GetMapping("/{deviceId}/stats")
    public ResponseEntity<?> getLocationStats(@PathVariable String deviceId) {
        try {
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Device not found"
                ));
            }

            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<LocationHistory> history = locationHistoryRepository
                .findByDeviceAndTimestampAfterOrderByTimestampDesc(device, since);

            // Calculate statistics
            double totalDistance = 0;
            double maxSpeed = 0;
            int uniqueLocations = new HashSet<>(
                history.stream()
                    .map(loc -> String.format("%.3f,%.3f", loc.getLatitude(), loc.getLongitude()))
                    .collect(Collectors.toList())
            ).size();

            for (int i = 0; i < history.size() - 1; i++) {
                LocationHistory current = history.get(i);
                LocationHistory next = history.get(i + 1);
                
                double distance = haversineDistance(
                    current.getLatitude(), current.getLongitude(),
                    next.getLatitude(), next.getLongitude()
                );
                totalDistance += distance;

                if (current.getSpeed() != null && current.getSpeed() > maxSpeed) {
                    maxSpeed = current.getSpeed();
                }
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "totalLocations", history.size(),
                "uniqueLocations", uniqueLocations,
                "totalDistanceKm", String.format("%.2f", totalDistance),
                "maxSpeedKmh", String.format("%.2f", maxSpeed),
                "avgLocationPerDay", history.size() / 30.0
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to get statistics: " + e.getMessage()
            ));
        }
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
