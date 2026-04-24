package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_share_links")
public class LocationShareLink {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnore
    private Device device;
    
    @Column(name = "token", unique = true, nullable = false)
    private String token;
    
    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Column(name = "is_password_protected")
    private Boolean isPasswordProtected = false;
    
    @Column(name = "expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    @Column(name = "max_views")
    private Integer maxViews;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "share_type")
    @Enumerated(EnumType.STRING)
    private ShareType shareType = ShareType.REAL_TIME;
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "last_accessed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessedAt;
    
    public enum ShareType {
        REAL_TIME,      // Shows live location updates
        SNAPSHOT,       // Shows only location at time of link creation
        LAST_KNOWN      // Shows last known location (for offline devices)
    }
    
    // Constructors
    public LocationShareLink() {
        this.createdAt = LocalDateTime.now();
        this.token = java.util.UUID.randomUUID().toString().replace("-", "");
    }
    
    public LocationShareLink(Device device, String ownerEmail, LocalDateTime expiresAt) {
        this();
        this.device = device;
        this.ownerEmail = ownerEmail;
        this.expiresAt = expiresAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public Boolean getIsPasswordProtected() { return isPasswordProtected; }
    public void setIsPasswordProtected(Boolean isPasswordProtected) { this.isPasswordProtected = isPasswordProtected; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Integer getMaxViews() { return maxViews; }
    public void setMaxViews(Integer maxViews) { this.maxViews = maxViews; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public ShareType getShareType() { return shareType; }
    public void setShareType(ShareType shareType) { this.shareType = shareType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
    
    // Utility methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isMaxViewsReached() {
        return maxViews != null && viewCount >= maxViews;
    }
    
    public boolean isValid() {
        return isActive && !isExpired() && !isMaxViewsReached();
    }
    
    public void incrementViewCount() {
        this.viewCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public String getShareUrl(String baseUrl) {
        return baseUrl + "/share/" + token;
    }
}
