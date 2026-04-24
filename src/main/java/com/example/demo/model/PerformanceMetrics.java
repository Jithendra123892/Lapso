package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_metrics")
public class PerformanceMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "cpu_usage")
    private Double cpuUsage;
    
    @Column(name = "memory_usage_percent")
    private Double memoryUsagePercent;
    
    @Column(name = "memory_used_bytes")
    private Long memoryUsedBytes;
    
    @Column(name = "memory_total_bytes")
    private Long memoryTotalBytes;
    
    @Column(name = "disk_usage_percent")
    private Double diskUsagePercent;
    
    @Column(name = "disk_used_bytes")
    private Long diskUsedBytes;
    
    @Column(name = "disk_total_bytes")
    private Long diskTotalBytes;
    
    @Column(name = "battery_level")
    private Integer batteryLevel;
    
    @Column(name = "is_charging")
    private Boolean isCharging;
    
    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;
    
    @Column(name = "network_speed_mbps")
    private Double networkSpeedMbps;
    
    @Column(name = "active_processes")
    private Integer activeProcesses;
    
    @Column(name = "uptime_hours")
    private Double uptimeHours;
    
    @Column(name = "performance_score")
    private Integer performanceScore; // 1-100 calculated score
    
    @Column(name = "health_status")
    @Enumerated(EnumType.STRING)
    private HealthStatus healthStatus = HealthStatus.GOOD;
    
    @Column(name = "recorded_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;
    
    public enum HealthStatus {
        EXCELLENT(90, "🟢"),
        GOOD(70, "🟡"),
        WARNING(50, "🟠"),
        CRITICAL(30, "🔴"),
        POOR(0, "⚫");
        
        private final int minScore;
        private final String emoji;
        
        HealthStatus(int minScore, String emoji) {
            this.minScore = minScore;
            this.emoji = emoji;
        }
        
        public int getMinScore() { return minScore; }
        public String getEmoji() { return emoji; }
        
        public static HealthStatus fromScore(int score) {
            if (score >= 90) return EXCELLENT;
            if (score >= 70) return GOOD;
            if (score >= 50) return WARNING;
            if (score >= 30) return CRITICAL;
            return POOR;
        }
    }
    
    // Constructors
    public PerformanceMetrics() {
        this.recordedAt = LocalDateTime.now();
    }
    
    public PerformanceMetrics(String deviceId, String userId) {
        this();
        this.deviceId = deviceId;
        this.userId = userId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
    
    public Double getMemoryUsagePercent() { return memoryUsagePercent; }
    public void setMemoryUsagePercent(Double memoryUsagePercent) { this.memoryUsagePercent = memoryUsagePercent; }
    
    public Long getMemoryUsedBytes() { return memoryUsedBytes; }
    public void setMemoryUsedBytes(Long memoryUsedBytes) { this.memoryUsedBytes = memoryUsedBytes; }
    
    public Long getMemoryTotalBytes() { return memoryTotalBytes; }
    public void setMemoryTotalBytes(Long memoryTotalBytes) { this.memoryTotalBytes = memoryTotalBytes; }
    
    public Double getDiskUsagePercent() { return diskUsagePercent; }
    public void setDiskUsagePercent(Double diskUsagePercent) { this.diskUsagePercent = diskUsagePercent; }
    
    public Long getDiskUsedBytes() { return diskUsedBytes; }
    public void setDiskUsedBytes(Long diskUsedBytes) { this.diskUsedBytes = diskUsedBytes; }
    
    public Long getDiskTotalBytes() { return diskTotalBytes; }
    public void setDiskTotalBytes(Long diskTotalBytes) { this.diskTotalBytes = diskTotalBytes; }
    
    public Integer getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }
    
    public Boolean getIsCharging() { return isCharging; }
    public void setIsCharging(Boolean isCharging) { this.isCharging = isCharging; }
    
    public Double getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(Double temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }
    
    public Double getNetworkSpeedMbps() { return networkSpeedMbps; }
    public void setNetworkSpeedMbps(Double networkSpeedMbps) { this.networkSpeedMbps = networkSpeedMbps; }
    
    public Integer getActiveProcesses() { return activeProcesses; }
    public void setActiveProcesses(Integer activeProcesses) { this.activeProcesses = activeProcesses; }
    
    public Double getUptimeHours() { return uptimeHours; }
    public void setUptimeHours(Double uptimeHours) { this.uptimeHours = uptimeHours; }
    
    public Integer getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(Integer performanceScore) { 
        this.performanceScore = performanceScore;
        if (performanceScore != null) {
            this.healthStatus = HealthStatus.fromScore(performanceScore);
        }
    }
    
    public HealthStatus getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatus healthStatus) { this.healthStatus = healthStatus; }
    
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    
    /**
     * Calculate performance score based on multiple metrics
     */
    public void calculatePerformanceScore() {
        int score = 100;
        
        // CPU usage penalty (0-40 points)
        if (cpuUsage != null) {
            if (cpuUsage > 90) score -= 40;
            else if (cpuUsage > 70) score -= 25;
            else if (cpuUsage > 50) score -= 15;
            else if (cpuUsage > 30) score -= 5;
        }
        
        // Memory usage penalty (0-30 points)
        if (memoryUsagePercent != null) {
            if (memoryUsagePercent > 95) score -= 30;
            else if (memoryUsagePercent > 85) score -= 20;
            else if (memoryUsagePercent > 70) score -= 10;
            else if (memoryUsagePercent > 60) score -= 5;
        }
        
        // Disk usage penalty (0-20 points)
        if (diskUsagePercent != null) {
            if (diskUsagePercent > 95) score -= 20;
            else if (diskUsagePercent > 85) score -= 15;
            else if (diskUsagePercent > 75) score -= 10;
            else if (diskUsagePercent > 65) score -= 5;
        }
        
        // Battery penalty (0-10 points)
        if (batteryLevel != null && !Boolean.TRUE.equals(isCharging)) {
            if (batteryLevel < 10) score -= 10;
            else if (batteryLevel < 20) score -= 5;
        }
        
        this.performanceScore = Math.max(0, Math.min(100, score));
        this.healthStatus = HealthStatus.fromScore(this.performanceScore);
    }
    
    /**
     * Get formatted health status with emoji
     */
    public String getFormattedHealthStatus() {
        return String.format("%s %s (%d/100)", 
                           healthStatus.getEmoji(), 
                           healthStatus.name(), 
                           performanceScore != null ? performanceScore : 0);
    }
    
    /**
     * Get optimization suggestions based on current metrics
     */
    public String getOptimizationSuggestion() {
        if (cpuUsage != null && cpuUsage > 80) {
            return "High CPU usage detected. Consider closing unnecessary applications or running a system scan.";
        }
        if (memoryUsagePercent != null && memoryUsagePercent > 85) {
            return "Memory usage is high. Restart applications or add more RAM for better performance.";
        }
        if (diskUsagePercent != null && diskUsagePercent > 90) {
            return "Disk space is running low. Consider cleaning temporary files or moving data to cloud storage.";
        }
        if (batteryLevel != null && batteryLevel < 15 && !Boolean.TRUE.equals(isCharging)) {
            return "Battery is critically low. Connect charger immediately to prevent data loss.";
        }
        if (performanceScore != null && performanceScore >= 90) {
            return "System is running optimally. Great job maintaining your device!";
        }
        return "System performance is stable. Regular maintenance recommended.";
    }
    
    @Override
    public String toString() {
        return String.format("PerformanceMetrics{device=%s, score=%d, health=%s, cpu=%.1f%%, memory=%.1f%%}", 
                           deviceId, performanceScore, healthStatus, cpuUsage, memoryUsagePercent);
    }
}
