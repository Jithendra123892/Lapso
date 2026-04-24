package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 🔗 LAPSO Integration Service
 * Connects all components and ensures everything works together seamlessly
 */
@Service
@Transactional
public class LapsoIntegrationService {
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private PerfectAuthService authService;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private EnhancedLocationService locationService;
    
    @Autowired
    private GeofenceService geofenceService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private RealTimeMonitoringService realTimeService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    /**
     * Initialize LAPSO system when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeLapsoSystem() {
        System.out.println("\n🚀 LAPSO Integration Service - Connecting All Components");
        
        try {
            // 1. Initialize admin user if database is empty
            initializeAdminUserIfNeeded();
            
            // 2. Start real-time monitoring
            startRealTimeMonitoring();
            
            // 3. Initialize WebSocket connections
            initializeWebSocketService();
            
            // 4. Start background services
            startBackgroundServices();
            
            System.out.println("✅ LAPSO Integration Complete - All Systems Connected");
            System.out.println("🌐 Access LAPSO at: http://localhost:8080");
            
        } catch (Exception e) {
            System.err.println("❌ LAPSO Integration Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check database status
     */
    private void initializeAdminUserIfNeeded() {
        try {
            long userCount = userRepository.count();
            System.out.println("✅ Database has " + userCount + " users");
        } catch (Exception e) {
            System.err.println("⚠️ Error checking database: " + e.getMessage());
        }
    }
    

    
    /**
     * Start real-time monitoring for all devices
     */
    private void startRealTimeMonitoring() {
        try {
            realTimeService.startMonitoring();
            System.out.println("✅ Real-time monitoring started");
        } catch (Exception e) {
            System.err.println("⚠️ Error starting real-time monitoring: " + e.getMessage());
        }
    }
    
    /**
     * Initialize WebSocket service for live updates
     */
    private void initializeWebSocketService() {
        try {
            webSocketService.initialize();
            System.out.println("✅ WebSocket service initialized");
        } catch (Exception e) {
            System.err.println("⚠️ Error initializing WebSocket service: " + e.getMessage());
        }
    }
    
    /**
     * Start background services
     */
    private void startBackgroundServices() {
        try {
            // Start analytics collection
            analyticsService.startBackgroundAnalytics();
            
            // Start geofence monitoring
            geofenceService.startMonitoring();
            
            // Start notification service
            notificationService.startService();
            
            System.out.println("✅ Background services started");
        } catch (Exception e) {
            System.err.println("⚠️ Error starting background services: " + e.getMessage());
        }
    }
    
    /**
     * Get system health status
     */
    public SystemHealth getSystemHealth() {
        SystemHealth health = new SystemHealth();
        
        try {
            // Check database connectivity
            long userCount = userRepository.count();
            long deviceCount = deviceRepository.count();
            health.setDatabaseConnected(true);
            health.setUserCount(userCount);
            health.setDeviceCount(deviceCount);
            
            // Check services
            health.setAuthServiceActive(authService != null);
            health.setDeviceServiceActive(deviceService != null);
            health.setLocationServiceActive(locationService != null);
            health.setWebSocketActive(webSocketService != null);
            health.setRealTimeActive(realTimeService != null);
            
            // Overall health
            health.setHealthy(health.isDatabaseConnected() && 
                            health.isAuthServiceActive() && 
                            health.isDeviceServiceActive());
            
        } catch (Exception e) {
            health.setHealthy(false);
            health.setErrorMessage(e.getMessage());
        }
        
        return health;
    }
    
    /**
     * Connect a new device to all services
     */
    public void connectDeviceToAllServices(Device device) {
        try {
            // Register with location service
            if (device.getLatitude() != null && device.getLongitude() != null) {
                locationService.updateDeviceLocation(device.getDeviceId(), 
                    device.getLatitude(), device.getLongitude(), device.getAddress());
            }
            
            // Start geofence monitoring
            geofenceService.addDeviceToMonitoring(device.getDeviceId());
            
            // Add to real-time monitoring
            realTimeService.addDevice(device.getDeviceId());
            
            // Send notification
            notificationService.sendDeviceConnectedNotification(device);
            
            // Broadcast via WebSocket
            webSocketService.broadcastDeviceUpdate(device);
            
            System.out.println("✅ Device connected to all services: " + device.getDeviceName());
            
        } catch (Exception e) {
            System.err.println("⚠️ Error connecting device to services: " + e.getMessage());
        }
    }
    
    /**
     * Update device across all services
     */
    public void updateDeviceAcrossServices(Device device) {
        try {
            // Update in device service
            deviceService.saveDevice(device);
            
            // Update location if available
            if (device.getLatitude() != null && device.getLongitude() != null) {
                locationService.updateDeviceLocation(device.getDeviceId(), 
                    device.getLatitude(), device.getLongitude(), device.getAddress());
            }
            
            // Check geofences
            geofenceService.checkGeofenceViolations(device.getDeviceId(), 
                device.getLatitude(), device.getLongitude());
            
            // Update analytics
            analyticsService.recordDeviceUpdate(device);
            
            // Broadcast update
            webSocketService.broadcastDeviceUpdate(device);
            
        } catch (Exception e) {
            System.err.println("⚠️ Error updating device across services: " + e.getMessage());
        }
    }
    
    /**
     * Get comprehensive device status
     */
    public DeviceStatus getComprehensiveDeviceStatus(String deviceId) {
        DeviceStatus status = new DeviceStatus();
        
        try {
            // Get device from service
            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                status.setFound(false);
                return status;
            }
            
            status.setFound(true);
            status.setDevice(device);
            
            // Get location status
            status.setLocationAvailable(device.getLatitude() != null && device.getLongitude() != null);
            
            // Get geofence status
            status.setGeofenceViolations(geofenceService.getActiveViolations(deviceId));
            
            // Get real-time status
            status.setRealTimeActive(realTimeService.isDeviceActive(deviceId));
            
            // Get analytics
            status.setAnalytics(analyticsService.getDeviceAnalytics(deviceId));
            
        } catch (Exception e) {
            status.setError(e.getMessage());
        }
        
        return status;
    }
    
    /**
     * System Health Status
     */
    public static class SystemHealth {
        private boolean healthy;
        private boolean databaseConnected;
        private boolean authServiceActive;
        private boolean deviceServiceActive;
        private boolean locationServiceActive;
        private boolean webSocketActive;
        private boolean realTimeActive;
        private long userCount;
        private long deviceCount;
        private String errorMessage;
        
        // Getters and setters
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        
        public boolean isDatabaseConnected() { return databaseConnected; }
        public void setDatabaseConnected(boolean databaseConnected) { this.databaseConnected = databaseConnected; }
        
        public boolean isAuthServiceActive() { return authServiceActive; }
        public void setAuthServiceActive(boolean authServiceActive) { this.authServiceActive = authServiceActive; }
        
        public boolean isDeviceServiceActive() { return deviceServiceActive; }
        public void setDeviceServiceActive(boolean deviceServiceActive) { this.deviceServiceActive = deviceServiceActive; }
        
        public boolean isLocationServiceActive() { return locationServiceActive; }
        public void setLocationServiceActive(boolean locationServiceActive) { this.locationServiceActive = locationServiceActive; }
        
        public boolean isWebSocketActive() { return webSocketActive; }
        public void setWebSocketActive(boolean webSocketActive) { this.webSocketActive = webSocketActive; }
        
        public boolean isRealTimeActive() { return realTimeActive; }
        public void setRealTimeActive(boolean realTimeActive) { this.realTimeActive = realTimeActive; }
        
        public long getUserCount() { return userCount; }
        public void setUserCount(long userCount) { this.userCount = userCount; }
        
        public long getDeviceCount() { return deviceCount; }
        public void setDeviceCount(long deviceCount) { this.deviceCount = deviceCount; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    /**
     * Device Status
     */
    public static class DeviceStatus {
        private boolean found;
        private Device device;
        private boolean locationAvailable;
        private boolean realTimeActive;
        private List<String> geofenceViolations;
        private Object analytics;
        private String error;
        
        // Getters and setters
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
        
        public Device getDevice() { return device; }
        public void setDevice(Device device) { this.device = device; }
        
        public boolean isLocationAvailable() { return locationAvailable; }
        public void setLocationAvailable(boolean locationAvailable) { this.locationAvailable = locationAvailable; }
        
        public boolean isRealTimeActive() { return realTimeActive; }
        public void setRealTimeActive(boolean realTimeActive) { this.realTimeActive = realTimeActive; }
        
        public List<String> getGeofenceViolations() { return geofenceViolations; }
        public void setGeofenceViolations(List<String> geofenceViolations) { this.geofenceViolations = geofenceViolations; }
        
        public Object getAnalytics() { return analytics; }
        public void setAnalytics(Object analytics) { this.analytics = analytics; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
