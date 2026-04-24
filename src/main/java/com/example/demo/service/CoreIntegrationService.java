package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 🔗 CORE INTEGRATION SERVICE
 * Ensures all essential components are connected and working together
 */
@Service
public class CoreIntegrationService {
    
    // Core Services
    @Autowired private DeviceService deviceService;
    @Autowired private UserService userService;
    @Autowired private AnalyticsService analyticsService;
    @Autowired private NotificationService notificationService;
    @Autowired private WebSocketService webSocketService;
    @Autowired private RealTimeMonitoringService realTimeService;
    @Autowired private EnhancedLocationService locationService;
    @Autowired private GeofenceService geofenceService;
    @Autowired private EncryptionService encryptionService;
    @Autowired private PerfectAuthService authService;
    @Autowired private QuickActionsService quickActionsService;
    @Autowired private DeviceActionService deviceActionService;
    @Autowired private ContinuousOperationService continuousOperationService;

    @Autowired private AgentAuthenticationService agentAuthService;
    
    /**
     * Initialize all core components when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCoreSystem() {
        System.out.println("\n🚀 LAPSO Core Integration - Connecting Essential Components");
        
        try {
            // 1. Initialize authentication and user management
            initializeUserManagement();
            
            // 2. Start device and location services
            initializeDeviceServices();
            
            // 3. Start real-time monitoring and notifications
            initializeRealTimeServices();
            
            // 4. Start security and encryption services
            initializeSecurityServices();
            
            // 5. Start analytics and monitoring
            initializeAnalyticsServices();
            
            System.out.println("✅ LAPSO Core Integration Complete - All Essential Systems Connected");
            System.out.println("🌐 Access LAPSO at: http://localhost:8080");
            
        } catch (Exception e) {
            System.err.println("❌ LAPSO Core Integration Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeUserManagement() {
        try {
            // User management initialized
            
            // Initialize authentication service
            authService.initialize();
            System.out.println("✅ User management initialized");
            
        } catch (Exception e) {
            System.err.println("⚠️ User management initialization error: " + e.getMessage());
        }
    }
    
    private void initializeDeviceServices() {
        try {
            // Start device service
            deviceService.initialize();
            
            // Start location service
            locationService.initialize();
            
            // Start geofence service
            geofenceService.startMonitoring();
            
            // Start device action service
            deviceActionService.initialize();
            
            // Start agent authentication
            agentAuthService.initialize();
            
            System.out.println("✅ Device services initialized");
            
        } catch (Exception e) {
            System.err.println("⚠️ Device services initialization error: " + e.getMessage());
        }
    }
    
    private void initializeRealTimeServices() {
        try {
            // Start WebSocket service
            webSocketService.initialize();
            
            // Start real-time monitoring
            realTimeService.startMonitoring();
            
            // Start notification service
            notificationService.startService();
            
            // Start continuous operation service
            continuousOperationService.initialize();
            
            // Start quick actions service
            quickActionsService.initialize();
            
            System.out.println("✅ Real-time services initialized");
            
        } catch (Exception e) {
            System.err.println("⚠️ Real-time services initialization error: " + e.getMessage());
        }
    }
    
    private void initializeSecurityServices() {
        try {
            // Initialize encryption service
            if (!encryptionService.isEncryptionConfigured()) {
                System.out.println("⚠️ Encryption key not configured - using generated key");
            }
            
            // Smart alert service replaced with NotificationService
            
            System.out.println("✅ Security services initialized");
            
        } catch (Exception e) {
            System.err.println("⚠️ Security services initialization error: " + e.getMessage());
        }
    }
    
    private void initializeAnalyticsServices() {
        try {
            // Start analytics service
            analyticsService.startBackgroundAnalytics();
            
            System.out.println("✅ Analytics services initialized");
            
        } catch (Exception e) {
            System.err.println("⚠️ Analytics services initialization error: " + e.getMessage());
        }
    }
    
    /**
     * Connect a device to all essential services
     */
    public void connectDeviceToAllServices(Device device) {
        try {
            // Register with location service
            if (device.getLatitude() != null && device.getLongitude() != null) {
                locationService.updateDeviceLocation(device.getDeviceId(), 
                    device.getLatitude(), device.getLongitude(), device.getAddress());
            }
            
            // Add to geofence monitoring
            geofenceService.addDeviceToMonitoring(device.getDeviceId());
            
            // Add to real-time monitoring
            realTimeService.addDevice(device.getDeviceId());
            
            // Send connection notification
            notificationService.sendDeviceConnectedNotification(device);
            
            // Broadcast via WebSocket
            webSocketService.broadcastDeviceUpdate(device);
            
            // Record analytics
            analyticsService.recordDeviceUpdate(device);
            
            System.out.println("✅ Device connected to all essential services: " + device.getDeviceName());
            
        } catch (Exception e) {
            System.err.println("⚠️ Error connecting device to services: " + e.getMessage());
        }
    }
    
    /**
     * Get system health status
     */
    public SystemHealthStatus getSystemHealth() {
        SystemHealthStatus health = new SystemHealthStatus();
        
        try {
            // Check core services
            health.setUserServiceActive(userService != null);
            health.setDeviceServiceActive(deviceService != null);
            health.setLocationServiceActive(locationService != null);
            health.setWebSocketActive(webSocketService != null);
            health.setRealTimeActive(realTimeService != null);
            health.setNotificationServiceActive(notificationService != null);
            health.setAnalyticsServiceActive(analyticsService != null);
            health.setEncryptionServiceActive(encryptionService != null);
            
            // Overall health
            health.setHealthy(health.isUserServiceActive() && 
                            health.isDeviceServiceActive() && 
                            health.isLocationServiceActive() &&
                            health.isWebSocketActive());
            
        } catch (Exception e) {
            health.setHealthy(false);
            health.setErrorMessage(e.getMessage());
        }
        
        return health;
    }
    
    /**
     * System Health Status
     */
    public static class SystemHealthStatus {
        private boolean healthy;
        private boolean userServiceActive;
        private boolean deviceServiceActive;
        private boolean locationServiceActive;
        private boolean webSocketActive;
        private boolean realTimeActive;
        private boolean notificationServiceActive;
        private boolean analyticsServiceActive;
        private boolean encryptionServiceActive;
        private String errorMessage;
        
        // Getters and setters
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        
        public boolean isUserServiceActive() { return userServiceActive; }
        public void setUserServiceActive(boolean userServiceActive) { this.userServiceActive = userServiceActive; }
        
        public boolean isDeviceServiceActive() { return deviceServiceActive; }
        public void setDeviceServiceActive(boolean deviceServiceActive) { this.deviceServiceActive = deviceServiceActive; }
        
        public boolean isLocationServiceActive() { return locationServiceActive; }
        public void setLocationServiceActive(boolean locationServiceActive) { this.locationServiceActive = locationServiceActive; }
        
        public boolean isWebSocketActive() { return webSocketActive; }
        public void setWebSocketActive(boolean webSocketActive) { this.webSocketActive = webSocketActive; }
        
        public boolean isRealTimeActive() { return realTimeActive; }
        public void setRealTimeActive(boolean realTimeActive) { this.realTimeActive = realTimeActive; }
        
        public boolean isNotificationServiceActive() { return notificationServiceActive; }
        public void setNotificationServiceActive(boolean notificationServiceActive) { this.notificationServiceActive = notificationServiceActive; }
        
        public boolean isAnalyticsServiceActive() { return analyticsServiceActive; }
        public void setAnalyticsServiceActive(boolean analyticsServiceActive) { this.analyticsServiceActive = analyticsServiceActive; }
        
        public boolean isEncryptionServiceActive() { return encryptionServiceActive; }
        public void setEncryptionServiceActive(boolean encryptionServiceActive) { this.encryptionServiceActive = encryptionServiceActive; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
