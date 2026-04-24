package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * Get comprehensive analytics for user's devices
     */
    public Map<String, Object> getDeviceAnalytics(String userEmail) {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            // Get all devices for the user
            List<Device> devices = deviceRepository.findByUserEmail(userEmail);
            
            // Basic statistics
            analytics.put("totalDevices", devices.size());
            analytics.put("onlineDevices", devices.stream().mapToInt(d -> d.getIsOnline() ? 1 : 0).sum());
            analytics.put("offlineDevices", devices.stream().mapToInt(d -> !d.getIsOnline() ? 1 : 0).sum());
            
            // Device types
            Map<String, Long> deviceTypes = devices.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getDeviceType() != null ? d.getDeviceType() : "Unknown",
                    Collectors.counting()
                ));
            analytics.put("deviceTypes", deviceTypes);
            
            // Operating systems
            Map<String, Long> operatingSystems = devices.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getOsName() != null ? d.getOsName() : "Unknown",
                    Collectors.counting()
                ));
            analytics.put("operatingSystems", operatingSystems);
            
            // Manufacturer distribution
            Map<String, Long> manufacturers = devices.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getManufacturer() != null ? d.getManufacturer() : "Unknown",
                    Collectors.counting()
                ));
            analytics.put("manufacturers", manufacturers);
            
            // Recent activity
            List<Map<String, Object>> recentActivity = devices.stream()
                .filter(d -> d.getLastSeen() != null)
                .sorted((a, b) -> b.getLastSeen().compareTo(a.getLastSeen()))
                .limit(10)
                .map(this::deviceToActivityMap)
                .collect(Collectors.toList());
            analytics.put("recentActivity", recentActivity);
            
            // Performance metrics
            analytics.put("performanceMetrics", getPerformanceMetrics(devices));
            
            // Security insights
            analytics.put("securityInsights", getSecurityInsights(devices));
            
        } catch (Exception e) {
            analytics.put("error", "Failed to get device analytics: " + e.getMessage());
        }
        
        return analytics;
    }

    /**
     * Get analytics for a specific device
     */
    public Map<String, Object> getDeviceAnalytics(Long deviceId, String userEmail) {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                
                // Verify ownership
                if (!device.getUserEmail().equals(userEmail)) {
                    analytics.put("error", "Access denied");
                    return analytics;
                }
                
                analytics.put("deviceInfo", deviceToMap(device));
                analytics.put("locationHistory", getLocationHistory(device));
                analytics.put("performanceHistory", getPerformanceHistory(device));
                analytics.put("securityEvents", getSecurityEvents(device));
                
            } else {
                analytics.put("error", "Device not found");
            }
            
        } catch (Exception e) {
            analytics.put("error", "Failed to get device analytics: " + e.getMessage());
        }
        
        return analytics;
    }

    /**
     * Get system-wide analytics (admin only)
     */
    public Map<String, Object> getSystemAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            List<Device> allDevices = deviceRepository.findAll();
            
            analytics.put("totalDevices", allDevices.size());
            analytics.put("totalUsers", allDevices.stream()
                .map(Device::getUserEmail)
                .distinct()
                .count());
            
            analytics.put("onlineDevices", allDevices.stream()
                .mapToInt(d -> d.getIsOnline() ? 1 : 0).sum());
            
            analytics.put("devicesByType", allDevices.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getDeviceType() != null ? d.getDeviceType() : "Unknown",
                    Collectors.counting()
                )));
            
            analytics.put("devicesByOS", allDevices.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getOsName() != null ? d.getOsName() : "Unknown",
                    Collectors.counting()
                )));
            
        } catch (Exception e) {
            analytics.put("error", "Failed to get system analytics: " + e.getMessage());
        }
        
        return analytics;
    }

    private Map<String, Object> deviceToActivityMap(Device device) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("deviceId", device.getId());
        activity.put("deviceName", device.getDeviceName());
        activity.put("lastSeen", device.getLastSeen());
        activity.put("isOnline", device.getIsOnline());
        activity.put("location", device.getAddress());
        return activity;
    }

    private Map<String, Object> deviceToMap(Device device) {
        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put("id", device.getId());
        deviceMap.put("name", device.getDeviceName());
        deviceMap.put("type", device.getDeviceType());
        deviceMap.put("os", device.getOsName());
        deviceMap.put("manufacturer", device.getManufacturer());
        deviceMap.put("model", device.getModel());
        deviceMap.put("isOnline", device.getIsOnline());
        deviceMap.put("lastSeen", device.getLastSeen());
        deviceMap.put("batteryLevel", device.getBatteryLevel());
        return deviceMap;
    }

    private Map<String, Object> getPerformanceMetrics(List<Device> devices) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Average battery level
        double avgBattery = devices.stream()
            .filter(d -> d.getBatteryLevel() != null)
            .mapToInt(Device::getBatteryLevel)
            .average()
            .orElse(0.0);
        metrics.put("averageBatteryLevel", Math.round(avgBattery));
        
        // Low battery devices
        long lowBatteryCount = devices.stream()
            .filter(d -> d.getBatteryLevel() != null && d.getBatteryLevel() < 20)
            .count();
        metrics.put("lowBatteryDevices", lowBatteryCount);
        
        return metrics;
    }

    private Map<String, Object> getSecurityInsights(List<Device> devices) {
        Map<String, Object> insights = new HashMap<>();
        
        // Locked devices
        long lockedDevices = devices.stream()
            .filter(d -> d.getIsLocked() != null && d.getIsLocked())
            .count();
        insights.put("lockedDevices", lockedDevices);
        
        // Theft detected
        long theftDetected = devices.stream()
            .filter(d -> d.getTheftDetected() != null && d.getTheftDetected())
            .count();
        insights.put("theftDetectedDevices", theftDetected);
        
        return insights;
    }

    private List<Map<String, Object>> getLocationHistory(Device device) {
        // In a real implementation, this would query location history from database
        List<Map<String, Object>> history = new ArrayList<>();
        
        if (device.getLatitude() != null && device.getLongitude() != null) {
            Map<String, Object> location = new HashMap<>();
            location.put("latitude", device.getLatitude());
            location.put("longitude", device.getLongitude());
            location.put("address", device.getAddress());
            location.put("timestamp", device.getLastSeen());
            history.add(location);
        }
        
        return history;
    }

    private List<Map<String, Object>> getPerformanceHistory(Device device) {
        // Mock performance history
        List<Map<String, Object>> history = new ArrayList<>();
        
        Map<String, Object> performance = new HashMap<>();
        performance.put("cpuUsage", device.getCpuUsage());
        performance.put("memoryUsage", device.getMemoryUsage());
        performance.put("diskUsage", device.getDiskUsage());
        performance.put("batteryLevel", device.getBatteryLevel());
        performance.put("timestamp", device.getLastSeen());
        history.add(performance);
        
        return history;
    }

    private List<Map<String, Object>> getSecurityEvents(Device device) {
        // Mock security events
        List<Map<String, Object>> events = new ArrayList<>();
        
        if (device.getIsLocked() != null && device.getIsLocked()) {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "DEVICE_LOCKED");
            event.put("timestamp", device.getLastCommandSent());
            event.put("description", "Device was locked remotely");
            events.add(event);
        }
        
        return events;
    }
    
    /**
     * Get dashboard analytics
     */
    public Map<String, Object> getDashboardAnalytics(String userEmail) {
        return getDeviceAnalytics(userEmail);
    }
    
    /**
     * Record device update
     */
    public void recordDeviceUpdate(Device device) {
        // Log device update for analytics
        System.out.println("📊 Device update recorded: " + device.getDeviceName());
    }
    
    /**
     * Start background analytics
     */
    public void startBackgroundAnalytics() {
        System.out.println("📊 Background analytics started");
    }
}
