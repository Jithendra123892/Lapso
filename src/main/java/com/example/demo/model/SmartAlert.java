package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "smart_alerts")
public class SmartAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "alert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;
    
    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertPriority priority;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "location_latitude")
    private Double locationLatitude;
    
    @Column(name = "location_longitude")
    private Double locationLongitude;
    
    @Column(name = "geofence_id")
    private Long geofenceId;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "is_acknowledged")
    private Boolean isAcknowledged = false;
    
    @Column(name = "auto_action_taken")
    private String autoActionTaken;
    
    @Column(name = "battery_level")
    private Integer batteryLevel;
    
    @Column(name = "threat_level")
    private Integer threatLevel = 1; // 1-10 scale
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "acknowledged_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acknowledgedAt;
    
    public enum AlertType {
        GEOFENCE_EXIT("Device left safe zone"),
        GEOFENCE_ENTRY("Device entered restricted area"),
        LOW_BATTERY("Battery critically low"),
        THEFT_DETECTED("Suspicious activity detected"),
        UNAUTHORIZED_ACCESS("Unauthorized login attempt"),
        DEVICE_OFFLINE("Device went offline"),
        PERFORMANCE_ISSUE("Performance degradation detected"),
        SECURITY_BREACH("Security threat identified"),
        MAINTENANCE_DUE("Maintenance required"),
        UNUSUAL_LOCATION("Device in unusual location"),
        RAPID_MOVEMENT("Rapid movement detected"),
        TAMPER_DETECTED("Physical tampering detected");
        
        private final String description;
        
        AlertType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    public enum AlertPriority {
        LOW("📘"),
        MEDIUM("🟡"),
        HIGH("🟠"),
        CRITICAL("🔴"),
        EMERGENCY("🚨");
        
        private final String emoji;
        
        AlertPriority(String emoji) {
            this.emoji = emoji;
        }
        
        public String getEmoji() { return emoji; }
    }
    
    // Constructors
    public SmartAlert() {
        this.createdAt = LocalDateTime.now();
    }
    
    public SmartAlert(String deviceId, String userId, AlertType alertType, 
                     AlertPriority priority, String title, String message) {
        this();
        this.deviceId = deviceId;
        this.userId = userId;
        this.alertType = alertType;
        this.priority = priority;
        this.title = title;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    
    public AlertPriority getPriority() { return priority; }
    public void setPriority(AlertPriority priority) { this.priority = priority; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Double getLocationLatitude() { return locationLatitude; }
    public void setLocationLatitude(Double locationLatitude) { this.locationLatitude = locationLatitude; }
    
    public Double getLocationLongitude() { return locationLongitude; }
    public void setLocationLongitude(Double locationLongitude) { this.locationLongitude = locationLongitude; }
    
    public Long getGeofenceId() { return geofenceId; }
    public void setGeofenceId(Long geofenceId) { this.geofenceId = geofenceId; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    public Boolean getIsAcknowledged() { return isAcknowledged; }
    public void setIsAcknowledged(Boolean isAcknowledged) { 
        this.isAcknowledged = isAcknowledged;
        if (isAcknowledged && this.acknowledgedAt == null) {
            this.acknowledgedAt = LocalDateTime.now();
        }
    }
    
    public String getAutoActionTaken() { return autoActionTaken; }
    public void setAutoActionTaken(String autoActionTaken) { this.autoActionTaken = autoActionTaken; }
    
    public Integer getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }
    
    public Integer getThreatLevel() { return threatLevel; }
    public void setThreatLevel(Integer threatLevel) { this.threatLevel = threatLevel; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    
    // Utility methods
    public String getFormattedAlert() {
        return String.format("%s %s - %s", 
                           priority.getEmoji(), 
                           alertType.getDescription(), 
                           title);
    }
    
    public boolean isCritical() {
        return priority == AlertPriority.CRITICAL || priority == AlertPriority.EMERGENCY;
    }
    
    public boolean requiresImmediateAction() {
        return alertType == AlertType.THEFT_DETECTED || 
               alertType == AlertType.SECURITY_BREACH ||
               alertType == AlertType.TAMPER_DETECTED ||
               (alertType == AlertType.LOW_BATTERY && batteryLevel != null && batteryLevel < 5);
    }
    
    @Override
    public String toString() {
        return String.format("SmartAlert{id=%d, type=%s, priority=%s, device=%s, acknowledged=%b}", 
                           id, alertType, priority, deviceId, isAcknowledged);
    }
}
