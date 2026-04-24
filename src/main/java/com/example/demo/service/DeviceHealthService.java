package com.example.demo.service;

import com.example.demo.model.Device;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceHealthService {

    /**
     * Comprehensive health check for a device
     */
    public DeviceHealthReport analyzeDeviceHealth(Device device) {
        DeviceHealthReport report = new DeviceHealthReport();
        report.setDeviceId(device.getDeviceId());
        report.setDeviceName(device.getDeviceName());
        report.setOverallScore(100); // Start with perfect score
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // 1. Battery Health
        BatteryHealth batteryHealth = analyzeBatteryHealth(device);
        report.setBatteryHealth(batteryHealth);
        report.setOverallScore(report.getOverallScore() - (100 - batteryHealth.getScore()));
        if (batteryHealth.getScore() < 50) {
            issues.add("Critical: Battery health is poor");
            recommendations.add("Consider replacing battery");
        }

        // 2. Disk Health
        DiskHealth diskHealth = analyzeDiskHealth(device);
        report.setDiskHealth(diskHealth);
        report.setOverallScore(report.getOverallScore() - (100 - diskHealth.getScore()) * 0.5);
        if (diskHealth.getUsagePercent() > 90) {
            issues.add("Warning: Disk usage above 90%");
            recommendations.add("Free up disk space or upgrade storage");
        }

        // 3. Performance Health
        PerformanceHealth performanceHealth = analyzePerformanceHealth(device);
        report.setPerformanceHealth(performanceHealth);
        report.setOverallScore(report.getOverallScore() - (100 - performanceHealth.getScore()) * 0.3);
        if (performanceHealth.getScore() < 60) {
            issues.add("Performance degradation detected");
            recommendations.add("Close unused applications or add more RAM");
        }

        // 4. Connectivity Health
        ConnectivityHealth connectivityHealth = analyzeConnectivityHealth(device);
        report.setConnectivityHealth(connectivityHealth);
        if (!connectivityHealth.getIsStable()) {
            issues.add("Unstable network connection");
            recommendations.add("Check network settings and Wi-Fi signal");
        }

        // 5. Security Health
        SecurityHealth securityHealth = analyzeSecurityHealth(device);
        report.setSecurityHealth(securityHealth);
        report.setOverallScore(report.getOverallScore() - (100 - securityHealth.getScore()) * 0.2);
        if (securityHealth.getScore() < 70) {
            issues.add("Security vulnerabilities detected");
            recommendations.add("Update operating system and install security patches");
        }

        // Set status based on overall score
        if (report.getOverallScore() >= 80) {
            report.setStatus("EXCELLENT");
        } else if (report.getOverallScore() >= 60) {
            report.setStatus("GOOD");
        } else if (report.getOverallScore() >= 40) {
            report.setStatus("FAIR");
        } else {
            report.setStatus("POOR");
        }

        report.setIssues(issues);
        report.setRecommendations(recommendations);
        return report;
    }

    private BatteryHealth analyzeBatteryHealth(Device device) {
        BatteryHealth health = new BatteryHealth();
        Integer batteryLevel = device.getBatteryLevel();

        if (batteryLevel == null) {
            health.setScore(50);
            health.setStatus("Unknown");
            return health;
        }

        health.setCurrentLevel(batteryLevel);
        health.setIsCharging(device.getIsCharging() != null && device.getIsCharging());

        // Estimate health based on battery behavior
        if (batteryLevel > 80) {
            health.setScore(90);
            health.setStatus("Excellent");
        } else if (batteryLevel > 50) {
            health.setScore(75);
            health.setStatus("Good");
        } else if (batteryLevel > 20) {
            health.setScore(60);
            health.setStatus("Fair");
        } else {
            health.setScore(40);
            health.setStatus("Poor - Needs charging");
        }

        // Estimate remaining hours (simplified)
        if (!health.getIsCharging()) {
            health.setEstimatedHoursRemaining(batteryLevel / 15.0); // Rough estimate
        }

        return health;
    }

    private DiskHealth analyzeDiskHealth(Device device) {
        DiskHealth health = new DiskHealth();

        Integer diskUsage = device.getDiskUsagePercent();
        if (diskUsage == null) {
            health.setScore(50);
            health.setStatus("Unknown");
            return health;
        }

        health.setUsagePercent(diskUsage);
        health.setTotalGB(device.getDiskTotal() != null ? device.getDiskTotal() / 1_000_000_000.0 : 0);
        health.setUsedGB(device.getDiskUsed() != null ? device.getDiskUsed() / 1_000_000_000.0 : 0);
        health.setFreeGB(health.getTotalGB() - health.getUsedGB());

        if (diskUsage < 50) {
            health.setScore(100);
            health.setStatus("Excellent");
        } else if (diskUsage < 70) {
            health.setScore(80);
            health.setStatus("Good");
        } else if (diskUsage < 85) {
            health.setScore(60);
            health.setStatus("Fair");
        } else if (diskUsage < 95) {
            health.setScore(40);
            health.setStatus("Poor");
        } else {
            health.setScore(20);
            health.setStatus("Critical");
        }

        return health;
    }

    private PerformanceHealth analyzePerformanceHealth(Device device) {
        PerformanceHealth health = new PerformanceHealth();

        Double cpuUsage = device.getCpuUsage();
        Integer memoryUsage = device.getMemoryUsagePercent();

        if (cpuUsage == null && memoryUsage == null) {
            health.setScore(50);
            health.setStatus("Unknown");
            return health;
        }

        health.setCpuUsage(cpuUsage != null ? cpuUsage : 0);
        health.setMemoryUsage(memoryUsage != null ? memoryUsage : 0);

        // Calculate performance score
        int score = 100;
        if (cpuUsage != null && cpuUsage > 80) score -= 30;
        else if (cpuUsage != null && cpuUsage > 60) score -= 15;

        if (memoryUsage != null && memoryUsage > 85) score -= 30;
        else if (memoryUsage != null && memoryUsage > 70) score -= 15;

        health.setScore(Math.max(score, 0));

        if (health.getScore() >= 80) health.setStatus("Excellent");
        else if (health.getScore() >= 60) health.setStatus("Good");
        else if (health.getScore() >= 40) health.setStatus("Fair");
        else health.setStatus("Poor");

        return health;
    }

    private ConnectivityHealth analyzeConnectivityHealth(Device device) {
        ConnectivityHealth health = new ConnectivityHealth();

        health.setIsOnline(device.getIsOnline());
        health.setNetworkType(device.getNetworkName() != null ? "Wi-Fi" : "Unknown");

        if (device.getLastSeen() != null) {
            Duration since = Duration.between(device.getLastSeen(), LocalDateTime.now());
            health.setLastSeenMinutesAgo((int) since.toMinutes());
            health.setIsStable(since.toMinutes() < 5);
        } else {
            health.setIsStable(false);
        }

        health.setSignalStrength(device.getWifiSignalStrength());
        health.setPublicIp(device.getPublicIp());

        return health;
    }

    private SecurityHealth analyzeSecurityHealth(Device device) {
        SecurityHealth health = new SecurityHealth();

        int score = 100;
        List<String> vulnerabilities = new ArrayList<>();

        // Check if agent is installed
        if (device.getAgentInstalled() == null || !device.getAgentInstalled()) {
            score -= 30;
            vulnerabilities.add("LAPSO agent not installed");
        }

        // Check if device is locked when offline
        if (!device.getIsOnline() && (device.getIsLocked() == null || !device.getIsLocked())) {
            score -= 20;
            vulnerabilities.add("Device offline but not locked");
        }

        // Check for theft detection
        if (device.getTheftDetected() != null && device.getTheftDetected()) {
            score -= 40;
            vulnerabilities.add("CRITICAL: Theft detected");
        }

        // Check if OS is old (simplified - would need OS version parsing)
        if (device.getOsVersion() != null && device.getOsVersion().contains("old")) {
            score -= 15;
            vulnerabilities.add("Operating system may be outdated");
        }

        health.setScore(Math.max(score, 0));
        health.setVulnerabilities(vulnerabilities);
        health.setTheftDetected(device.getTheftDetected() != null && device.getTheftDetected());
        health.setIsLocked(device.getIsLocked() != null && device.getIsLocked());

        if (health.getScore() >= 80) health.setStatus("Secure");
        else if (health.getScore() >= 60) health.setStatus("Moderate");
        else health.setStatus("At Risk");

        return health;
    }

    // Inner classes for health reports
    public static class DeviceHealthReport {
        private String deviceId;
        private String deviceName;
        private double overallScore;
        private String status;
        private BatteryHealth batteryHealth;
        private DiskHealth diskHealth;
        private PerformanceHealth performanceHealth;
        private ConnectivityHealth connectivityHealth;
        private SecurityHealth securityHealth;
        private List<String> issues;
        private List<String> recommendations;

        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public BatteryHealth getBatteryHealth() { return batteryHealth; }
        public void setBatteryHealth(BatteryHealth batteryHealth) { this.batteryHealth = batteryHealth; }

        public DiskHealth getDiskHealth() { return diskHealth; }
        public void setDiskHealth(DiskHealth diskHealth) { this.diskHealth = diskHealth; }

        public PerformanceHealth getPerformanceHealth() { return performanceHealth; }
        public void setPerformanceHealth(PerformanceHealth performanceHealth) { this.performanceHealth = performanceHealth; }

        public ConnectivityHealth getConnectivityHealth() { return connectivityHealth; }
        public void setConnectivityHealth(ConnectivityHealth connectivityHealth) { this.connectivityHealth = connectivityHealth; }

        public SecurityHealth getSecurityHealth() { return securityHealth; }
        public void setSecurityHealth(SecurityHealth securityHealth) { this.securityHealth = securityHealth; }

        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    public static class BatteryHealth {
        private int score;
        private String status;
        private Integer currentLevel;
        private Boolean isCharging;
        private Double estimatedHoursRemaining;

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getCurrentLevel() { return currentLevel; }
        public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }
        public Boolean getIsCharging() { return isCharging; }
        public void setIsCharging(Boolean isCharging) { this.isCharging = isCharging; }
        public Double getEstimatedHoursRemaining() { return estimatedHoursRemaining; }
        public void setEstimatedHoursRemaining(Double estimatedHoursRemaining) { this.estimatedHoursRemaining = estimatedHoursRemaining; }
    }

    public static class DiskHealth {
        private int score;
        private String status;
        private Integer usagePercent;
        private Double totalGB;
        private Double usedGB;
        private Double freeGB;

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getUsagePercent() { return usagePercent; }
        public void setUsagePercent(Integer usagePercent) { this.usagePercent = usagePercent; }
        public Double getTotalGB() { return totalGB; }
        public void setTotalGB(Double totalGB) { this.totalGB = totalGB; }
        public Double getUsedGB() { return usedGB; }
        public void setUsedGB(Double usedGB) { this.usedGB = usedGB; }
        public Double getFreeGB() { return freeGB; }
        public void setFreeGB(Double freeGB) { this.freeGB = freeGB; }
    }

    public static class PerformanceHealth {
        private int score;
        private String status;
        private Double cpuUsage;
        private Integer memoryUsage;

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
        public Integer getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(Integer memoryUsage) { this.memoryUsage = memoryUsage; }
    }

    public static class ConnectivityHealth {
        private Boolean isOnline;
        private String networkType;
        private Integer lastSeenMinutesAgo;
        private Boolean isStable;
        private Integer signalStrength;
        private String publicIp;

        public Boolean getIsOnline() { return isOnline; }
        public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
        public String getNetworkType() { return networkType; }
        public void setNetworkType(String networkType) { this.networkType = networkType; }
        public Integer getLastSeenMinutesAgo() { return lastSeenMinutesAgo; }
        public void setLastSeenMinutesAgo(Integer lastSeenMinutesAgo) { this.lastSeenMinutesAgo = lastSeenMinutesAgo; }
        public Boolean getIsStable() { return isStable; }
        public void setIsStable(Boolean isStable) { this.isStable = isStable; }
        public Integer getSignalStrength() { return signalStrength; }
        public void setSignalStrength(Integer signalStrength) { this.signalStrength = signalStrength; }
        public String getPublicIp() { return publicIp; }
        public void setPublicIp(String publicIp) { this.publicIp = publicIp; }
    }

    public static class SecurityHealth {
        private int score;
        private String status;
        private List<String> vulnerabilities;
        private Boolean theftDetected;
        private Boolean isLocked;

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<String> getVulnerabilities() { return vulnerabilities; }
        public void setVulnerabilities(List<String> vulnerabilities) { this.vulnerabilities = vulnerabilities; }
        public Boolean getTheftDetected() { return theftDetected; }
        public void setTheftDetected(Boolean theftDetected) { this.theftDetected = theftDetected; }
        public Boolean getIsLocked() { return isLocked; }
        public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    }
}
