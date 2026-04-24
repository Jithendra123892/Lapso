package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "laptops")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)  // Changed to EAGER to prevent proxy session errors
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;
    
    @Column(name = "device_name", nullable = false)
    private String deviceName;
    
    private String manufacturer;
    private String model;
    
    @Column(name = "serial_number")
    private String serialNumber;
    
    @Column(name = "os_name")
    private String osName;
    
    @Column(name = "os_version")
    private String osVersion;
    
    // Location tracking
    private Double latitude;
    private Double longitude;
    private Double altitude;
    
    @Column(name = "location_accuracy")
    private Double locationAccuracy;
    
    private String address;
    
    // Device status
    @Column(name = "is_online")
    private Boolean isOnline = false;
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    // Enhanced monitoring fields
    @Column(name = "last_location_request")
    private LocalDateTime lastLocationRequest;
    
    @Column(name = "last_command_sent")
    private LocalDateTime lastCommandSent;
    
    @Column(name = "last_action")
    private String lastAction;
    
    @Column(name = "last_action_time")
    private LocalDateTime lastActionTime;
    
    @Column(name = "offline_reason")
    private String offlineReason;
    
    @Column(name = "agent_version")
    private String agentVersion;
    
    @Column(name = "accuracy")
    private Double accuracy;
    
    @Column(name = "location_source")
    private String locationSource;
    
    @Column(name = "network_status")
    private String networkStatus;
    
    @Column(name = "security_status")
    private String securityStatus;
    
    @Column(name = "is_wiped")
    private Boolean isWiped = false;
    
    @Column(name = "wiped_at")
    private LocalDateTime wipedAt;
    
    @Column(name = "battery_level")
    private Integer batteryLevel;
    
    @Column(name = "is_charging")
    private Boolean isCharging = false;
    
    // System performance fields
    @Column(name = "cpu_usage")
    private Double cpuUsage;
    
    @Column(name = "memory_total")
    private Long memoryTotal;
    
    @Column(name = "memory_used")
    private Long memoryUsed;
    
    @Column(name = "disk_total")
    private Long diskTotal;
    
    @Column(name = "disk_used")
    private Long diskUsed;
    
    // Network information
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "wifi_ssid")
    private String wifiSsid;
    
    @Column(name = "wifi_signal_strength")
    private Integer wifiSignalStrength;
    
    // Security features
    @Column(name = "is_locked")
    private Boolean isLocked = false;
    
    @Column(name = "theft_detected")
    private Boolean theftDetected = false;

    @Column(name = "is_stolen")
    private Boolean isStolen = false;

    @Column(name = "device_type")
    private String deviceType; // LAPTOP, DESKTOP, PHONE, TABLET

    @Column(name = "network_name")
    private String networkName;

    @Column(name = "public_ip")
    private String publicIp;

    @Column(name = "disk_usage")
    private Integer diskUsage;

    @Column(name = "memory_usage")
    private Integer memoryUsage;

    @Column(name = "uptime_hours")
    private Long uptimeHours;

    @Column(name = "agent_installed")
    private Boolean agentInstalled = false;

    @Column(name = "agent_installed_at")
    private LocalDateTime agentInstalledAt;

    @Column(name = "agent_uninstalled_at")
    private LocalDateTime agentUninstalledAt;

    @Column(name = "agent_last_heartbeat")
    private LocalDateTime agentLastHeartbeat;

    // Getters and setters
    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public Boolean getIsStolen() { return isStolen; }
    public void setIsStolen(Boolean isStolen) { this.isStolen = isStolen; }

    public String getLastAction() { return lastAction; }
    public void setLastAction(String lastAction) { this.lastAction = lastAction; }

    public LocalDateTime getLastActionTime() { return lastActionTime; }
    public void setLastActionTime(LocalDateTime lastActionTime) { this.lastActionTime = lastActionTime; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getNetworkName() { return networkName; }
    public void setNetworkName(String networkName) { this.networkName = networkName; }

    public String getPublicIp() { return publicIp; }
    public void setPublicIp(String publicIp) { this.publicIp = publicIp; }

    public Integer getDiskUsagePercent() { return diskUsage; }
    public void setDiskUsagePercent(Integer diskUsage) { this.diskUsage = diskUsage; }

    public Integer getMemoryUsagePercent() { return memoryUsage; }
    public void setMemoryUsagePercent(Integer memoryUsage) { this.memoryUsage = memoryUsage; }

    // Backward compatibility methods
    public Double getMemoryUsage() {
        if (memoryUsage != null) {
            return memoryUsage.doubleValue();
        }
        return getCalculatedMemoryUsage();
    }
    
    public void setMemoryUsage(Double memoryUsage) {
        if (memoryUsage != null) {
            this.memoryUsage = memoryUsage.intValue();
        }
    }
    
    public Double getDiskUsage() {
        if (diskUsage != null) {
            return diskUsage.doubleValue();
        }
        return getCalculatedDiskUsage();
    }
    
    public void setDiskUsage(Double diskUsage) {
        if (diskUsage != null) {
            this.diskUsage = diskUsage.intValue();
        }
    }

    public Long getUptimeHours() { return uptimeHours; }
    public void setUptimeHours(Long uptimeHours) { this.uptimeHours = uptimeHours; }

    public String getAgentVersion() { return agentVersion; }
    public void setAgentVersion(String agentVersion) { this.agentVersion = agentVersion; }
    
    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "operating_system")
    private String operatingSystem;
    
    // Advanced location and movement tracking
    @Column(name = "speed")
    private Double speed;
    
    @Column(name = "transportation_mode")
    private String transportationMode;
    
    @Column(name = "theft_detected_at")
    private LocalDateTime theftDetectedAt;
    
    @Column(name = "location_confidence")
    private Double locationConfidence;
    
    @Column(name = "satellite_count")
    private Integer satelliteCount;
    
    @Column(name = "signal_strength")
    private Integer signalStrength;
    
    // Constructors
    public Device() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isOnline = false;
        this.theftDetected = false;
        this.isLocked = false;
        this.isCharging = false;
        this.isWiped = false;
    }
    
    public Device(String deviceId, User user, String deviceName) {
        this();
        this.deviceId = deviceId;
        this.user = user;
        this.deviceName = deviceName;
    }
    
    // Update timestamp when modified
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }
    
    // Alias for osName for compatibility
    public String getOsType() { return osName; }
    public void setOsType(String osType) { this.osName = osType; }
    
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }
    
    public Double getLocationAccuracy() { return locationAccuracy; }
    public void setLocationAccuracy(Double locationAccuracy) { this.locationAccuracy = locationAccuracy; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
    
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    
    public Integer getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }
    
    public Boolean getIsCharging() { return isCharging; }
    public void setIsCharging(Boolean isCharging) { this.isCharging = isCharging; }
    
    public Double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
    
    public Long getMemoryTotal() { return memoryTotal; }
    public void setMemoryTotal(Long memoryTotal) { this.memoryTotal = memoryTotal; }
    
    public Long getMemoryUsed() { return memoryUsed; }
    public void setMemoryUsed(Long memoryUsed) { this.memoryUsed = memoryUsed; }
    
    public Long getDiskTotal() { return diskTotal; }
    public void setDiskTotal(Long diskTotal) { this.diskTotal = diskTotal; }
    
    public Long getDiskUsed() { return diskUsed; }
    public void setDiskUsed(Long diskUsed) { this.diskUsed = diskUsed; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getWifiSsid() { return wifiSsid; }
    public void setWifiSsid(String wifiSsid) { this.wifiSsid = wifiSsid; }
    
    public Integer getWifiSignalStrength() { return wifiSignalStrength; }
    public void setWifiSignalStrength(Integer wifiSignalStrength) { this.wifiSignalStrength = wifiSignalStrength; }
    
    public Boolean getTheftDetected() { return theftDetected; }
    public void setTheftDetected(Boolean theftDetected) { this.theftDetected = theftDetected; }

    public Boolean getIsWiped() { return isWiped; }
    public void setIsWiped(Boolean isWiped) { this.isWiped = isWiped; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Convenience methods
    public String getOwnerEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    public String getOperatingSystem() { 
        return operatingSystem != null ? operatingSystem : (osName + (osVersion != null ? " " + osVersion : ""));
    }
    
    public void setOperatingSystem(String operatingSystem) { 
        this.operatingSystem = operatingSystem; 
    }
    
    // Memory and disk usage as calculated percentage
    public Double getCalculatedMemoryUsage() {
        if (memoryTotal != null && memoryUsed != null && memoryTotal > 0) {
            return (double) ((memoryUsed * 100) / memoryTotal);
        }
        return null;
    }
    
    public void setCalculatedMemoryUsage(Double memoryUsage) {
        // This is a calculated field, but we can store the raw percentage if needed
        if (memoryUsage != null && memoryTotal != null) {
            this.memoryUsed = (long) ((memoryUsage * memoryTotal) / 100);
        }
    }
    
    public Double getCalculatedDiskUsage() {
        if (diskTotal != null && diskUsed != null && diskTotal > 0) {
            return (double) ((diskUsed * 100) / diskTotal);
        }
        return null;
    }
    
    public void setCalculatedDiskUsage(Double diskUsage) {
        // This is a calculated field, but we can store the raw percentage if needed
        if (diskUsage != null && diskTotal != null) {
            this.diskUsed = (long) ((diskUsage * diskTotal) / 100);
        }
    }
    

    

    
    public LocalDateTime getLastLocationUpdate() {
        return lastSeen;
    }
    
    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
        this.lastSeen = lastLocationUpdate;
    }
    
    public LocalDateTime getLastUpdated() {
        return updatedAt;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.updatedAt = lastUpdated;
    }
    
    /**
     * Get user email for this device
     */
    public String getUserEmail() {
        return user != null ? user.getEmail() : "unknown@user.com";
    }
    
    /**
     * Get device name
     */
    public String getName() {
        return deviceName != null ? deviceName : "Unknown Device";
    }
    
    /**
     * Last command sent timestamp
     */
    public LocalDateTime getLastCommandSent() { return lastCommandSent; }
    public void setLastCommandSent(LocalDateTime lastCommandSent) { this.lastCommandSent = lastCommandSent; }
    
    /**
     * Location accuracy
     */
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    
    /**
     * Advanced location and movement fields
     */
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    
    public String getTransportationMode() { return transportationMode; }
    public void setTransportationMode(String transportationMode) { this.transportationMode = transportationMode; }
    
    public LocalDateTime getTheftDetectedAt() { return theftDetectedAt; }
    public void setTheftDetectedAt(LocalDateTime theftDetectedAt) { this.theftDetectedAt = theftDetectedAt; }
    
    public Double getLocationConfidence() { return locationConfidence; }
    public void setLocationConfidence(Double locationConfidence) { this.locationConfidence = locationConfidence; }
    
    public String getLocationSource() { return locationSource; }
    public void setLocationSource(String locationSource) { this.locationSource = locationSource; }
    
    public Integer getSatelliteCount() { return satelliteCount; }
    public void setSatelliteCount(Integer satelliteCount) { this.satelliteCount = satelliteCount; }
    
    public Integer getSignalStrength() { return signalStrength; }
    public void setSignalStrength(Integer signalStrength) { this.signalStrength = signalStrength; }
    
    // Additional enhanced monitoring fields
    public LocalDateTime getLastLocationRequest() { return lastLocationRequest; }
    public void setLastLocationRequest(LocalDateTime lastLocationRequest) { this.lastLocationRequest = lastLocationRequest; }
    
    public String getOfflineReason() { return offlineReason; }
    public void setOfflineReason(String offlineReason) { this.offlineReason = offlineReason; }
    
    public String getNetworkStatus() { return networkStatus; }
    public void setNetworkStatus(String networkStatus) { this.networkStatus = networkStatus; }
    
    public String getSecurityStatus() { return securityStatus; }
    public void setSecurityStatus(String securityStatus) { this.securityStatus = securityStatus; }
    
    public LocalDateTime getWipedAt() { return wipedAt; }
    public void setWipedAt(LocalDateTime wipedAt) { this.wipedAt = wipedAt; }

    // Agent status tracking
    public Boolean getAgentInstalled() { return agentInstalled; }
    public void setAgentInstalled(Boolean agentInstalled) { this.agentInstalled = agentInstalled; }

    public LocalDateTime getAgentInstalledAt() { return agentInstalledAt; }
    public void setAgentInstalledAt(LocalDateTime agentInstalledAt) { this.agentInstalledAt = agentInstalledAt; }

    public LocalDateTime getAgentUninstalledAt() { return agentUninstalledAt; }
    public void setAgentUninstalledAt(LocalDateTime agentUninstalledAt) { this.agentUninstalledAt = agentUninstalledAt; }

    public LocalDateTime getAgentLastHeartbeat() { return agentLastHeartbeat; }
    public void setAgentLastHeartbeat(LocalDateTime agentLastHeartbeat) { this.agentLastHeartbeat = agentLastHeartbeat; }
}
