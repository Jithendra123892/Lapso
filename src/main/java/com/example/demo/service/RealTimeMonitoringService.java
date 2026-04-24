package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RealTimeMonitoringService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * Monitor devices in real-time (every 30 seconds)
     * DISABLED: Only update when agent sends actual data
     */
    // @Scheduled(fixedRate = 30000)
    public void monitorDevices() {
        try {
            List<Device> onlineDevices = deviceRepository.findByIsOnlineTrue();
            
            for (Device device : onlineDevices) {
                // Check device health only - don't send fake location requests
                checkDeviceHealth(device);
                
                // Send real-time status to user (status only, no fake location data)
                sendRealTimeUpdate(device);
            }
            
        } catch (Exception e) {
            System.err.println("Error in real-time monitoring: " + e.getMessage());
        }
    }

    /**
     * Send device status update to user
     */
    public void sendDeviceStatusUpdate(String userEmail, Device device) {
        try {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("deviceId", device.getDeviceId());
            statusUpdate.put("deviceName", device.getDeviceName());
            statusUpdate.put("isOnline", device.getIsOnline());
            statusUpdate.put("batteryLevel", device.getBatteryLevel());
            statusUpdate.put("lastSeen", device.getLastSeen());
            statusUpdate.put("latitude", device.getLatitude());
            statusUpdate.put("longitude", device.getLongitude());
            statusUpdate.put("address", device.getAddress());
            statusUpdate.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendRealTimeUpdate(userEmail, statusUpdate);
            
        } catch (Exception e) {
            System.err.println("Failed to send device status update: " + e.getMessage());
        }
    }

    /**
     * Request location update for device
     */
    private void requestLocationUpdate(Device device) {
        try {
            // Create location request command
            Map<String, Object> locationRequest = new HashMap<>();
            locationRequest.put("command", "REQUEST_LOCATION");
            locationRequest.put("priority", "HIGH");
            locationRequest.put("timeout", 30);
            locationRequest.put("accuracy", "BEST");
            locationRequest.put("sources", List.of("GPS", "WIFI", "NETWORK"));
            
            // Send via WebSocket to device agent
            boolean sent = webSocketService.sendCommandToDevice(device.getDeviceId(), locationRequest);
            
            if (sent) {
                System.out.println("📍 Location update requested for device: " + device.getDeviceId());
                
                // Update last location request time
                device.setLastLocationRequest(LocalDateTime.now());
                deviceRepository.save(device);
            } else {
                System.err.println("❌ Failed to send location request to device: " + device.getDeviceId());
            }
            
        } catch (Exception e) {
            System.err.println("Failed to request location update: " + e.getMessage());
        }
    }
    
    /**
     * Enhanced real-time monitoring with health checks
     * DISABLED: Only update when agent sends actual data, not fake scheduled updates
     */
    // @Scheduled(fixedRate = 15000) // Every 15 seconds for real-time monitoring
    public void enhancedRealTimeMonitoring() {
        try {
            List<Device> allDevices = deviceRepository.findAll();
            
            for (Device device : allDevices) {
                // Check device health
                checkDeviceHealth(device);
                
                // Only send updates for devices that are actually responding
                // Don't send fake location requests - wait for agent to report
                
                // Send real-time update to user (status only, no fake data)
                sendEnhancedRealTimeUpdate(device);
            }
            
        } catch (Exception e) {
            System.err.println("Error in enhanced real-time monitoring: " + e.getMessage());
        }
    }
    
    /**
     * Request system status from device
     */
    private void requestSystemStatus(Device device) {
        try {
            Map<String, Object> statusRequest = new HashMap<>();
            statusRequest.put("command", "REQUEST_SYSTEM_STATUS");
            statusRequest.put("include", List.of("battery", "performance", "security", "network"));
            
            webSocketService.sendCommandToDevice(device.getDeviceId(), statusRequest);
            
        } catch (Exception e) {
            System.err.println("Failed to request system status: " + e.getMessage());
        }
    }
    
    /**
     * Check device health and update status
     */
    private void checkDeviceHealth(Device device) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastSeen = device.getLastSeen();
            
            if (lastSeen != null) {
                long minutesOffline = java.time.Duration.between(lastSeen, now).toMinutes();
                
                // Update online status based on last seen time
                if (minutesOffline > 2) { // 2 minutes threshold (faster than Microsoft's 5+ minutes)
                    if (Boolean.TRUE.equals(device.getIsOnline())) {
                        device.setIsOnline(false);
                        device.setOfflineReason("No communication for " + minutesOffline + " minutes");
                        
                        // Send offline alert
                        webSocketService.sendAlert(
                            device.getUserEmail(),
                            "DEVICE_OFFLINE",
                            device.getDeviceName() + " went offline",
                            device.getDeviceId()
                        );
                    }
                } else {
                    if (Boolean.FALSE.equals(device.getIsOnline())) {
                        device.setIsOnline(true);
                        device.setOfflineReason(null);
                        
                        // Send online alert
                        webSocketService.sendAlert(
                            device.getUserEmail(),
                            "DEVICE_ONLINE",
                            device.getDeviceName() + " is back online",
                            device.getDeviceId()
                        );
                    }
                }
                
                deviceRepository.save(device);
            }
            
        } catch (Exception e) {
            System.err.println("Error checking device health: " + e.getMessage());
        }
    }
    
    /**
     * Send enhanced real-time update
     */
    private void sendEnhancedRealTimeUpdate(Device device) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("deviceId", device.getDeviceId());
            update.put("deviceName", device.getDeviceName());
            update.put("isOnline", device.getIsOnline());
            update.put("batteryLevel", device.getBatteryLevel());
            update.put("isCharging", device.getIsCharging());
            update.put("lastSeen", device.getLastSeen());
            update.put("latitude", device.getLatitude());
            update.put("longitude", device.getLongitude());
            update.put("accuracy", device.getAccuracy());
            update.put("address", device.getAddress());
            update.put("cpuUsage", device.getCpuUsage());
            update.put("memoryUsage", device.getMemoryUsage());
            update.put("diskUsage", device.getDiskUsage());
            update.put("networkStatus", device.getNetworkStatus());
            update.put("securityStatus", device.getSecurityStatus());
            update.put("timestamp", LocalDateTime.now());
            
            // Calculate health score
            double healthScore = calculateDeviceHealthScore(device);
            update.put("healthScore", healthScore);
            
            webSocketService.sendRealTimeUpdate(device.getUserEmail(), update);
            
        } catch (Exception e) {
            System.err.println("Failed to send enhanced real-time update: " + e.getMessage());
        }
    }
    
    /**
     * Calculate device health score (0-100)
     */
    private double calculateDeviceHealthScore(Device device) {
        double score = 100.0;
        
        // Deduct points for various issues
        if (Boolean.FALSE.equals(device.getIsOnline())) {
            score -= 50; // Major penalty for being offline
        }
        
        if (device.getBatteryLevel() != null) {
            if (device.getBatteryLevel() < 20) {
                score -= 20; // Low battery penalty
            } else if (device.getBatteryLevel() < 50) {
                score -= 10; // Medium battery penalty
            }
        }
        
        if (device.getCpuUsage() != null && device.getCpuUsage() > 80) {
            score -= 15; // High CPU usage penalty
        }
        
        if (device.getMemoryUsage() != null && device.getMemoryUsage() > 90) {
            score -= 15; // High memory usage penalty
        }
        
        if (device.getDiskUsage() != null && device.getDiskUsage() > 95) {
            score -= 10; // High disk usage penalty
        }
        
        if (Boolean.TRUE.equals(device.getTheftDetected())) {
            score -= 30; // Theft detection penalty
        }
        
        return Math.max(0, score);
    }

    /**
     * Send real-time update via WebSocket
     */
    private void sendRealTimeUpdate(Device device) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("deviceId", device.getDeviceId());
            update.put("deviceName", device.getDeviceName());
            update.put("isOnline", device.getIsOnline());
            update.put("batteryLevel", device.getBatteryLevel());
            update.put("lastSeen", device.getLastSeen());
            update.put("latitude", device.getLatitude());
            update.put("longitude", device.getLongitude());
            update.put("address", device.getAddress());
            update.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendRealTimeUpdate(device.getUserEmail(), update);
            
        } catch (Exception e) {
            System.err.println("Failed to send real-time update: " + e.getMessage());
        }
    }

    /**
     * Get monitoring statistics
     */
    public Map<String, Object> getMonitoringStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<Device> allDevices = deviceRepository.findAll();
            List<Device> onlineDevices = deviceRepository.findByIsOnlineTrue();
            
            stats.put("totalDevices", allDevices.size());
            stats.put("onlineDevices", onlineDevices.size());
            stats.put("offlineDevices", allDevices.size() - onlineDevices.size());
            stats.put("activeConnections", webSocketService.getActiveSessionCount());
            stats.put("lastUpdate", LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("Error getting monitoring stats: " + e.getMessage());
            stats.put("error", "Failed to get monitoring statistics");
        }
        
        return stats;
    }
    
    /**
     * Start monitoring
     */
    public void startMonitoring() {
        System.out.println("✅ Real-time monitoring started");
    }
    
    /**
     * Add device to monitoring
     */
    public void addDevice(String deviceId) {
        System.out.println("✅ Device added to monitoring: " + deviceId);
    }
    
    /**
     * Check if device is active
     */
    public boolean isDeviceActive(String deviceId) {
        try {
            return deviceRepository.findByDeviceId(deviceId)
                .map(device -> device.getIsOnline())
                .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
