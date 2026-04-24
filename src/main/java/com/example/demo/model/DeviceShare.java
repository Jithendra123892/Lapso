package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_shares")
public class DeviceShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_id", nullable = false)
    private User sharedWith;
    
    @Column(name = "permission_level")
    @Enumerated(EnumType.STRING)
    private PermissionLevel permissionLevel = PermissionLevel.VIEW_ONLY;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "shared_message")
    private String sharedMessage;
    
    public enum PermissionLevel {
        VIEW_ONLY,      // Can only view location and status
        TRACK_CONTROL,  // Can track and send commands (lock, alarm)
        FULL_ADMIN      // Can modify settings and revoke access
    }
    
    // Constructors
    public DeviceShare() {
        this.createdAt = LocalDateTime.now();
    }
    
    public DeviceShare(Device device, User owner, User sharedWith, PermissionLevel permissionLevel) {
        this();
        this.device = device;
        this.owner = owner;
        this.sharedWith = sharedWith;
        this.permissionLevel = permissionLevel;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    
    public User getSharedWith() { return sharedWith; }
    public void setSharedWith(User sharedWith) { this.sharedWith = sharedWith; }
    
    public PermissionLevel getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(PermissionLevel permissionLevel) { this.permissionLevel = permissionLevel; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getSharedMessage() { return sharedMessage; }
    public void setSharedMessage(String sharedMessage) { this.sharedMessage = sharedMessage; }
    
    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public boolean isValid() {
        return isActive && !isExpired();
    }
}
