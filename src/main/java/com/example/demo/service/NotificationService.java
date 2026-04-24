package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Autowired
    private WebSocketService webSocketService;
    
    // Store notification preferences (in production, use database)
    private final Map<String, NotificationPreferences> userPreferences = new ConcurrentHashMap<>();
    
    // Track notification history to prevent spam
    private final Map<String, LocalDateTime> lastNotificationTime = new ConcurrentHashMap<>();
    
    private static final int MIN_NOTIFICATION_INTERVAL_MINUTES = 5;

    /**
     * Send device offline notification
     */
    public void sendDeviceOfflineNotification(Device device) {
        String userEmail = device.getUserEmail();
        
        if (shouldSendNotification(userEmail, "device_offline")) {
            // Email notification
            sendEmailNotification(
                userEmail,
                "🔴 LAPSO Alert: Device Offline",
                String.format(
                    "Your device '%s' has gone offline.\n\n" +
                    "Last seen: %s\n" +
                    "Last location: %s\n\n" +
                    "View details: http://localhost:8080\n\n" +
                    "LAPSO - Free Laptop Security",
                    device.getDeviceName(),
                    device.getLastSeen() != null ? device.getLastSeen().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Unknown",
                    device.getAddress() != null ? device.getAddress() : "Unknown location"
                )
            );
            
            // WebSocket notification
            webSocketService.sendAlert(
                userEmail,
                "device_offline",
                String.format("Device '%s' went offline", device.getDeviceName()),
                device.getDeviceId()
            );
            
            recordNotification(userEmail, "device_offline");
            System.out.println("📧 Notification sent: Device offline for " + device.getDeviceName());
        }
    }
    
    /**
     * Send device online notification
     */
    public void sendDeviceOnlineNotification(Device device) {
        String userEmail = device.getUserEmail();
        
        if (shouldSendNotification(userEmail, "device_online")) {
            // Email notification
            sendEmailNotification(
                userEmail,
                "🟢 LAPSO Alert: Device Back Online",
                String.format(
                    "Your device '%s' is back online!\n\n" +
                    "Current location: %s\n" +
                    "Battery level: %s\n" +
                    "Connected at: %s\n\n" +
                    "View details: http://localhost:8080\n\n" +
                    "LAPSO - Free Laptop Security",
                    device.getDeviceName(),
                    device.getAddress() != null ? device.getAddress() : "Updating location...",
                    device.getBatteryLevel() != null ? device.getBatteryLevel() + "%" : "Unknown",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                )
            );
            
            // WebSocket notification
            webSocketService.sendAlert(
                userEmail,
                "device_online",
                String.format("Device '%s' is back online", device.getDeviceName()),
                device.getDeviceId()
            );
            
            recordNotification(userEmail, "device_online");
            System.out.println("📧 Notification sent: Device online for " + device.getDeviceName());
        }
    }
    
    /**
     * Send low battery notification
     */
    public void sendLowBatteryNotification(Device device) {
        String userEmail = device.getUserEmail();
        
        if (shouldSendNotification(userEmail, "low_battery")) {
            String batteryLevel = device.getBatteryLevel() != null ? device.getBatteryLevel() + "%" : "Unknown";
            
            // Email notification
            sendEmailNotification(
                userEmail,
                "🔋 LAPSO Alert: Low Battery Warning",
                String.format(
                    "Your device '%s' has low battery!\n\n" +
                    "Battery level: %s\n" +
                    "Current location: %s\n" +
                    "Charging: %s\n\n" +
                    "Consider charging your device to maintain tracking.\n\n" +
                    "View details: http://localhost:8080\n\n" +
                    "LAPSO - Free Laptop Security",
                    device.getDeviceName(),
                    batteryLevel,
                    device.getAddress() != null ? device.getAddress() : "Unknown location",
                    device.getIsCharging() != null && device.getIsCharging() ? "Yes" : "No"
                )
            );
            
            // WebSocket notification
            webSocketService.sendAlert(
                userEmail,
                "low_battery",
                String.format("Device '%s' has low battery (%s)", device.getDeviceName(), batteryLevel),
                device.getDeviceId()
            );
            
            recordNotification(userEmail, "low_battery");
            System.out.println("📧 Notification sent: Low battery for " + device.getDeviceName());
        }
    }
    
    /**
     * Send geofence violation notification
     */
    public void sendGeofenceViolationNotification(Device device, String geofenceName, String violationType) {
        String userEmail = device.getUserEmail();
        
        if (shouldSendNotification(userEmail, "geofence_violation")) {
            // Email notification
            sendEmailNotification(
                userEmail,
                "🚨 LAPSO Alert: Geofence Violation",
                String.format(
                    "Geofence violation detected!\n\n" +
                    "Device: %s\n" +
                    "Geofence: %s\n" +
                    "Violation: %s\n" +
                    "Current location: %s\n" +
                    "Time: %s\n\n" +
                    "This could indicate unauthorized movement of your device.\n\n" +
                    "View details: http://localhost:8080\n\n" +
                    "LAPSO - Free Laptop Security",
                    device.getDeviceName(),
                    geofenceName,
                    violationType,
                    device.getAddress() != null ? device.getAddress() : "Unknown location",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                )
            );
            
            // WebSocket notification
            webSocketService.sendAlert(
                userEmail,
                "geofence_violation",
                String.format("Device '%s' %s geofence '%s'", device.getDeviceName(), violationType.toLowerCase(), geofenceName),
                device.getDeviceId()
            );
            
            recordNotification(userEmail, "geofence_violation");
            System.out.println("📧 Notification sent: Geofence violation for " + device.getDeviceName());
        }
    }
    
    /**
     * Send theft detection notification
     */
    public void sendTheftDetectionNotification(Device device) {
        String userEmail = device.getUserEmail();
        
        // Always send theft notifications (override rate limiting)
        // Email notification
        sendEmailNotification(
            userEmail,
            "🚨 LAPSO URGENT: Theft Detected",
            String.format(
                "URGENT: Potential theft detected!\n\n" +
                "Device: %s\n" +
                "Current location: %s\n" +
                "Detection time: %s\n\n" +
                "IMMEDIATE ACTIONS RECOMMENDED:\n" +
                "1. Check if you moved the device\n" +
                "2. Contact local authorities if stolen\n" +
                "3. Use remote lock/wipe if necessary\n\n" +
                "View live tracking: http://localhost:8080\n\n" +
                "LAPSO - Free Laptop Security\n" +
                "Emergency Contact: [Your emergency contact]",
                device.getDeviceName(),
                device.getAddress() != null ? device.getAddress() : "Unknown location",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
            )
        );
        
        // WebSocket notification
        webSocketService.sendAlert(
            userEmail,
            "theft_detected",
            String.format("URGENT: Potential theft detected for device '%s'", device.getDeviceName()),
            device.getDeviceId()
        );
        
        recordNotification(userEmail, "theft_detected");
        System.out.println("🚨 URGENT notification sent: Theft detected for " + device.getDeviceName());
    }
    
    /**
     * Send welcome notification for new users
     */
    public void sendWelcomeNotification(User user) {
        sendEmailNotification(
            user.getEmail(),
            "🛡️ Welcome to LAPSO - Free Laptop Security",
            String.format(
                "Welcome to LAPSO, %s!\n\n" +
                "Thank you for choosing LAPSO for your laptop security needs.\n\n" +
                "LAPSO Features:\n" +
                "✅ Real-time location tracking (every 30 seconds)\n" +
                "✅ Smart geofencing with instant alerts\n" +
                "✅ Cross-platform support (Windows, macOS, Linux)\n" +
                "✅ 24/7 continuous monitoring\n" +
                "✅ Completely free and open source\n\n" +
                "Getting Started:\n" +
                "1. Visit your dashboard: http://localhost:8080\n" +
                "2. Click 'Add Device' to register your first laptop\n" +
                "3. Download and install the agent\n" +
                "4. Start tracking immediately!\n\n" +
                "Need help? Check our documentation or contact support.\n\n" +
                "LAPSO - Better than Microsoft Find My Device, completely free!\n\n" +
                "Best regards,\n" +
                "The LAPSO Team",
                user.getName() != null ? user.getName() : "User"
            )
        );
        
        System.out.println("📧 Welcome notification sent to " + user.getEmail());
    }
    
    /**
     * Send email notification
     */
    public void sendEmailNotification(String to, String subject, String body) {
        if (mailSender == null) {
            System.out.println("📧 Email service not configured, notification logged: " + subject);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@lapso.local");
            
            mailSender.send(message);
            System.out.println("📧 Email sent successfully to " + to);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
        }
    }
    
    /**
     * Send device action notification
     */
    public void sendDeviceAction(Device device, String action) {
        try {
            String message = "Device action performed: " + action + " on device " + device.getName();
            sendWebSocketNotification("device-action", message);
            System.out.println("📱 Device action notification sent: " + action);
        } catch (Exception e) {
            System.err.println("❌ Failed to send device action notification: " + e.getMessage());
        }
    }
    
    /**
     * Send security alert notification
     */
    public void sendSecurityAlert(Device device, String alert) {
        try {
            String message = "Security alert for device " + device.getName() + ": " + alert;
            sendWebSocketNotification("security-alert", message);
            System.out.println("🚨 Security alert sent: " + alert);
        } catch (Exception e) {
            System.err.println("❌ Failed to send security alert: " + e.getMessage());
        }
    }
    
    /**
     * Send WebSocket notification
     */
    private void sendWebSocketNotification(String type, String message) {
        try {
            if (webSocketService != null) {
                webSocketService.sendNotification(type, message);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebSocket notification: " + e.getMessage());
        }
    }

    /**
     * Check if notification should be sent (rate limiting)
     */
    private boolean shouldSendNotification(String userEmail, String notificationType) {
        String key = userEmail + ":" + notificationType;
        LocalDateTime lastSent = lastNotificationTime.get(key);
        
        if (lastSent == null) {
            return true;
        }
        
        // Check if enough time has passed
        return lastSent.plusMinutes(MIN_NOTIFICATION_INTERVAL_MINUTES).isBefore(LocalDateTime.now());
    }
    
    /**
     * Record notification to prevent spam
     */
    private void recordNotification(String userEmail, String notificationType) {
        String key = userEmail + ":" + notificationType;
        lastNotificationTime.put(key, LocalDateTime.now());
    }
    
    /**
     * Get user notification preferences
     */
    public NotificationPreferences getUserPreferences(String userEmail) {
        return userPreferences.getOrDefault(userEmail, new NotificationPreferences());
    }
    
    /**
     * Update user notification preferences
     */
    public void updateUserPreferences(String userEmail, NotificationPreferences preferences) {
        userPreferences.put(userEmail, preferences);
        System.out.println("📧 Updated notification preferences for " + userEmail);
    }
    
    /**
     * Start notification service
     */
    public void startService() {
        System.out.println("✅ Notification service started - ready to send alerts");
        // Initialize any background tasks or cleanup old notifications
    }
    
    /**
     * Initialize notification service
     */
    public void initialize() {
        startService();
    }
    
    /**
     * Send device connected notification
     */
    public void sendDeviceConnectedNotification(Device device) {
        sendDeviceOnlineNotification(device); // Use existing method
    }
    
    /**
     * Notification preferences class
     */
    public static class NotificationPreferences {
        private boolean emailEnabled = true;
        private boolean webSocketEnabled = true;
        private boolean deviceOfflineEnabled = true;
        private boolean deviceOnlineEnabled = true;
        private boolean lowBatteryEnabled = true;
        private boolean geofenceViolationEnabled = true;
        private boolean theftDetectionEnabled = true;
        
        // Getters and setters
        public boolean isEmailEnabled() { return emailEnabled; }
        public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
        
        public boolean isWebSocketEnabled() { return webSocketEnabled; }
        public void setWebSocketEnabled(boolean webSocketEnabled) { this.webSocketEnabled = webSocketEnabled; }
        
        public boolean isDeviceOfflineEnabled() { return deviceOfflineEnabled; }
        public void setDeviceOfflineEnabled(boolean deviceOfflineEnabled) { this.deviceOfflineEnabled = deviceOfflineEnabled; }
        
        public boolean isDeviceOnlineEnabled() { return deviceOnlineEnabled; }
        public void setDeviceOnlineEnabled(boolean deviceOnlineEnabled) { this.deviceOnlineEnabled = deviceOnlineEnabled; }
        
        public boolean isLowBatteryEnabled() { return lowBatteryEnabled; }
        public void setLowBatteryEnabled(boolean lowBatteryEnabled) { this.lowBatteryEnabled = lowBatteryEnabled; }
        
        public boolean isGeofenceViolationEnabled() { return geofenceViolationEnabled; }
        public void setGeofenceViolationEnabled(boolean geofenceViolationEnabled) { this.geofenceViolationEnabled = geofenceViolationEnabled; }
        
        public boolean isTheftDetectionEnabled() { return theftDetectionEnabled; }
        public void setTheftDetectionEnabled(boolean theftDetectionEnabled) { this.theftDetectionEnabled = theftDetectionEnabled; }
    }
    
    /**
     * Send verification email
     */
    public void sendVerificationEmail(String email, String name, String verificationToken) {
        try {
            String subject = "Verify your LAPSO account";
            String body = "Hello " + name + ",\n\n" +
                         "Please verify your LAPSO account by clicking the link below:\n" +
                         "http://localhost:8080/verify?token=" + verificationToken + "\n\n" +
                         "Best regards,\nLAPSO Team";
            
            sendEmailNotification(email, subject, body);
            System.out.println("📧 Verification email sent to " + email);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String name, String resetToken) {
        try {
            String subject = "Reset your LAPSO password";
            String body = "Hello " + name + ",\n\n" +
                         "You requested to reset your LAPSO password. Click the link below:\n" +
                         "http://localhost:8080/reset-password?token=" + resetToken + "\n\n" +
                         "If you didn't request this, please ignore this email.\n\n" +
                         "Best regards,\nLAPSO Team";
            
            sendEmailNotification(email, subject, body);
            System.out.println("📧 Password reset email sent to " + email);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email: " + e.getMessage());
        }
    }
    
    /**
     * Send notification with three parameters
     */
    public void sendNotification(String userEmail, String title, String message) {
        try {
            if (webSocketService != null) {
                webSocketService.sendNotification(userEmail, title, message);
            }
            System.out.println("📧 Notification sent to " + userEmail + ": " + title);
        } catch (Exception e) {
            System.err.println("❌ Failed to send notification: " + e.getMessage());
        }
    }
    
    /**
     * Send emergency alert - REAL IMPLEMENTATION
     */
    public void sendEmergencyAlert(String userEmail, String deviceName, String alertType, String message) {
        try {
            String subject = "🚨 LAPSO EMERGENCY: " + alertType;
            String body = "EMERGENCY ALERT\n\n" +
                         "Device: " + deviceName + "\n" +
                         "Alert Type: " + alertType + "\n" +
                         "Message: " + message + "\n\n" +
                         "Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n" +
                         "Please take immediate action.\n\n" +
                         "LAPSO Security Team";
            
            sendEmailNotification(userEmail, subject, body);
            
            if (webSocketService != null) {
                webSocketService.sendAlert(userEmail, "emergency", message, null);
            }
            
            System.out.println("🚨 Emergency alert sent to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send emergency alert: " + e.getMessage());
        }
    }
    
    /**
     * Send critical alert - REAL IMPLEMENTATION
     */
    public void sendCriticalAlert(String userEmail, String deviceName, String alertType, String message) {
        try {
            String subject = "🔴 LAPSO CRITICAL: " + alertType;
            String body = "CRITICAL ALERT\n\n" +
                         "Device: " + deviceName + "\n" +
                         "Alert Type: " + alertType + "\n" +
                         "Message: " + message + "\n\n" +
                         "Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n" +
                         "Immediate attention required.\n\n" +
                         "LAPSO Security Team";
            
            sendEmailNotification(userEmail, subject, body);
            
            if (webSocketService != null) {
                webSocketService.sendAlert(userEmail, "critical", message, null);
            }
            
            System.out.println("🔴 Critical alert sent to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send critical alert: " + e.getMessage());
        }
    }
    
    /**
     * Send theft alert - REAL IMPLEMENTATION
     */
    public void sendTheftAlert(String userEmail, String message) {
        try {
            String subject = "🚨 LAPSO THEFT DETECTION ALERT";
            String body = "THEFT DETECTED!\n\n" +
                         message + "\n\n" +
                         "Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n" +
                         "IMMEDIATE ACTION REQUIRED:\n" +
                         "1. Verify if you moved the device\n" +
                         "2. Contact authorities if stolen\n" +
                         "3. Use remote lock/wipe if necessary\n\n" +
                         "LAPSO Security Team";

            sendEmailNotification(userEmail, subject, body);

            if (webSocketService != null) {
                webSocketService.sendAlert(userEmail, "theft", message, null);
            }

            System.out.println("🚨 Theft alert sent to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send theft alert: " + e.getMessage());
        }
    }

    /**
     * Send AI-powered theft alert with detailed analysis
     */
    public void sendAITheftAlert(String userEmail, TheftRiskPrediction prediction) {
        try {
            String subject = "🚨 LAPSO AI THEFT ALERT: " + prediction.getRiskPercentage() + "% RISK";
            String body = "AI-POWERED THEFT RISK ALERT!\n\n" +
                         "Device: " + prediction.getDeviceName() + "\n" +
                         "Risk Level: " + prediction.getRiskPercentage() + "%\n" +
                         "Analysis Time: " + prediction.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n" +
                         "Risk Factors:\n" +
                         "📍 Location Anomaly: " + prediction.getLocationAnomalyScore() + "%\n" +
                         "⏰ Time Anomaly: " + prediction.getTimeAnomalyScore() + "%\n" +
                         "🚗 Movement Pattern: " + prediction.getMovementPatternScore() + "%\n" +
                         "🔋 Battery Pattern: " + prediction.getBatteryPatternScore() + "%\n" +
                         "📶 Network Anomaly: " + prediction.getNetworkAnomalyScore() + "%\n\n" +
                         "AI Recommendations:\n" +
                         "1. " + prediction.getRecommendations().get(0) + "\n" +
                         "2. " + prediction.getRecommendations().get(1) + "\n" +
                         "3. " + prediction.getRecommendations().get(2) + "\n\n" +
                         "LAPSO AI Security Team";

            sendEmailNotification(userEmail, subject, body);

            if (webSocketService != null) {
                webSocketService.sendAlert(userEmail, "ai_theft", "AI theft risk detected: " + prediction.getRiskPercentage() + "%", null);
            }

            System.out.println("🚨 AI theft alert sent to: " + userEmail + " (Risk: " + prediction.getRiskPercentage() + "%)");
        } catch (Exception e) {
            System.err.println("Failed to send AI theft alert: " + e.getMessage());
        }
    }
}