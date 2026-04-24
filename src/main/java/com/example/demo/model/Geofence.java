package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "geofences")
public class Geofence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "center_latitude", nullable = false)
    private Double centerLatitude;
    
    @Column(name = "center_longitude", nullable = false)
    private Double centerLongitude;
    
    @Column(name = "radius_meters", nullable = false)
    private Double radiusMeters;
    
    @Column(name = "fence_type")
    @Enumerated(EnumType.STRING)
    private GeofenceType fenceType = GeofenceType.SAFE_ZONE;
    
    @Column(name = "alert_on_entry")
    private Boolean alertOnEntry = false;
    
    @Column(name = "alert_on_exit")
    private Boolean alertOnExit = true;
    
    @Column(name = "auto_lock_on_exit")
    private Boolean autoLockOnExit = false;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public enum GeofenceType {
        SAFE_ZONE,      // Home, Office - alert when leaving
        RESTRICTED_ZONE, // Areas device shouldn't be - alert when entering
        WORK_ZONE,      // Track work hours and presence
        SCHOOL_ZONE     // Educational institution tracking
    }
    
    // Constructors
    public Geofence() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Geofence(String name, Double centerLatitude, Double centerLongitude, 
                   Double radiusMeters, String userId) {
        this();
        this.name = name;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.radiusMeters = radiusMeters;
        this.userId = userId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getCenterLatitude() { return centerLatitude; }
    public void setCenterLatitude(Double centerLatitude) { this.centerLatitude = centerLatitude; }
    
    public Double getCenterLongitude() { return centerLongitude; }
    public void setCenterLongitude(Double centerLongitude) { this.centerLongitude = centerLongitude; }
    
    public Double getRadiusMeters() { return radiusMeters; }
    public void setRadiusMeters(Double radiusMeters) { this.radiusMeters = radiusMeters; }
    
    public GeofenceType getFenceType() { return fenceType; }
    public void setFenceType(GeofenceType fenceType) { this.fenceType = fenceType; }
    
    public Boolean getAlertOnEntry() { return alertOnEntry; }
    public void setAlertOnEntry(Boolean alertOnEntry) { this.alertOnEntry = alertOnEntry; }
    
    public Boolean getAlertOnExit() { return alertOnExit; }
    public void setAlertOnExit(Boolean alertOnExit) { this.alertOnExit = alertOnExit; }
    
    public Boolean getAutoLockOnExit() { return autoLockOnExit; }
    public void setAutoLockOnExit(Boolean autoLockOnExit) { this.autoLockOnExit = autoLockOnExit; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Utility method to check if a point is within this geofence
    public boolean containsPoint(double latitude, double longitude) {
        double distance = calculateDistance(this.centerLatitude, this.centerLongitude, 
                                          latitude, longitude);
        return distance <= this.radiusMeters;
    }
    
    // Calculate distance between two points using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    @Override
    public String toString() {
        return String.format("Geofence{id=%d, name='%s', type=%s, center=[%f,%f], radius=%fm}", 
                           id, name, fenceType, centerLatitude, centerLongitude, radiusMeters);
    }
}
