package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_alerts")
public class DeviceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laptop_id", nullable = false)
    private Device laptop;
    
    @Column(name = "alert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;
    
    @Column(nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.INFO;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public DeviceAlert() {
        this.createdAt = LocalDateTime.now();
    }
    
    public DeviceAlert(Device laptop, AlertType alertType, String message) {
        this();
        this.laptop = laptop;
        this.alertType = alertType;
        this.message = message;
    }
    
    public DeviceAlert(Device laptop, AlertType alertType, String message, Severity severity) {
        this(laptop, alertType, message);
        this.severity = severity;
    }
    
    // Enums
    public enum AlertType {
        THEFT_DETECTED,
        LOW_BATTERY,
        DEVICE_OFFLINE,
        DEVICE_ONLINE,
        LOCATION_CHANGED,
        UNAUTHORIZED_ACCESS,
        SYSTEM_CRITICAL
    }
    
    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Device getLaptop() { return laptop; }
    public void setLaptop(Device laptop) { this.laptop = laptop; }
    
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
