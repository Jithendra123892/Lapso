package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.LocationHistory;
import com.example.demo.repository.LocationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

@Service
public class TheftDetectionService {

    @Autowired
    private LocationHistoryRepository locationHistoryRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Analyze device behavior and detect potential theft
     * Returns threat level: 0 (safe) to 10 (critical)
     */
    public TheftAnalysisResult analyzeDeviceBehavior(Device device) {
        TheftAnalysisResult result = new TheftAnalysisResult();
        result.setDeviceId(device.getDeviceId());
        result.setThreatLevel(0);
        List<String> suspiciousPatterns = new ArrayList<>();

        // 1. Sudden location jump detection
        if (device.getLatitude() != null && device.getLongitude() != null) {
            double locationJumpScore = detectLocationJump(device);
            if (locationJumpScore > 0) {
                suspiciousPatterns.add("Sudden location jump detected (" + 
                    String.format("%.1f", locationJumpScore) + " km)");
                result.setThreatLevel(result.getThreatLevel() + 3);
            }
        }

        // 2. Unusual time pattern detection
        if (detectUnusualTimePattern(device)) {
            suspiciousPatterns.add("Device active during unusual hours");
            result.setThreatLevel(result.getThreatLevel() + 2);
        }

        // 3. Multiple failed location attempts
        if (detectLocationSpoofing(device)) {
            suspiciousPatterns.add("Possible GPS spoofing detected");
            result.setThreatLevel(result.getThreatLevel() + 4);
        }

        // 4. Rapid battery drain
        if (device.getBatteryLevel() != null && detectRapidBatteryDrain(device)) {
            suspiciousPatterns.add("Rapid battery drain detected");
            result.setThreatLevel(result.getThreatLevel() + 1);
        }

        // 5. Device went offline in suspicious location
        if (!device.getIsOnline() && detectSuspiciousOfflineLocation(device)) {
            suspiciousPatterns.add("Device offline in unfamiliar area");
            result.setThreatLevel(result.getThreatLevel() + 3);
        }

        // 6. High speed movement detection
        double maxSpeed = detectHighSpeedMovement(device);
        if (maxSpeed > 120) { // > 120 km/h might indicate device in vehicle
            suspiciousPatterns.add("High-speed movement detected (" + 
                String.format("%.0f", maxSpeed) + " km/h)");
            result.setThreatLevel(result.getThreatLevel() + 2);
        }

        result.setSuspiciousPatterns(suspiciousPatterns);
        result.setIsTheftDetected(result.getThreatLevel() >= 5);
        
        // Auto-lock if critical threat
        if (result.getThreatLevel() >= 8) {
            result.setRecommendedAction("IMMEDIATE_LOCK");
        } else if (result.getThreatLevel() >= 5) {
            result.setRecommendedAction("ALERT_USER");
        } else {
            result.setRecommendedAction("MONITOR");
        }

        return result;
    }

    private double detectLocationJump(Device device) {
        List<LocationHistory> recent = locationHistoryRepository
            .findByDeviceAndTimestampAfterOrderByTimestampDesc(
                device, LocalDateTime.now().minusHours(1));

        if (recent.size() < 2) return 0;

        LocationHistory latest = recent.get(0);
        LocationHistory previous = recent.get(1);

        Duration timeDiff = Duration.between(previous.getTimestamp(), latest.getTimestamp());
        double timeHours = timeDiff.toMinutes() / 60.0;

        if (timeHours < 0.01) return 0; // Too close in time

        double distance = haversineDistance(
            previous.getLatitude(), previous.getLongitude(),
            latest.getLatitude(), latest.getLongitude()
        );

        double speed = distance / timeHours; // km/h

        // Sudden jump > 500km/h is suspicious (faster than commercial jet)
        if (speed > 500) {
            return distance;
        }

        return 0;
    }

    private boolean detectUnusualTimePattern(Device device) {
        LocalDateTime lastSeen = device.getLastSeen();
        if (lastSeen == null) return false;

        int hour = lastSeen.getHour();
        // Suspicious if device active between 2 AM - 5 AM
        return hour >= 2 && hour <= 5;
    }

    private boolean detectLocationSpoofing(Device device) {
        List<LocationHistory> recent = locationHistoryRepository
            .findByDeviceAndTimestampAfterOrderByTimestampDesc(
                device, LocalDateTime.now().minusMinutes(30));

        if (recent.size() < 3) return false;

        // Check for impossible zigzag patterns
        int impossibleMovements = 0;
        for (int i = 0; i < recent.size() - 2; i++) {
            LocationHistory p1 = recent.get(i);
            LocationHistory p2 = recent.get(i + 1);
            LocationHistory p3 = recent.get(i + 2);

            double dist12 = haversineDistance(
                p1.getLatitude(), p1.getLongitude(),
                p2.getLatitude(), p2.getLongitude()
            );

            double dist23 = haversineDistance(
                p2.getLatitude(), p2.getLongitude(),
                p3.getLatitude(), p3.getLongitude()
            );

            double dist13 = haversineDistance(
                p1.getLatitude(), p1.getLongitude(),
                p3.getLatitude(), p3.getLongitude()
            );

            // If device went far and came back impossibly fast
            if (dist12 > 50 && dist23 > 50 && dist13 < 5) {
                impossibleMovements++;
            }
        }

        return impossibleMovements >= 2;
    }

    private boolean detectRapidBatteryDrain(Device device) {
        // This would need battery history - simplified version
        Integer battery = device.getBatteryLevel();
        if (battery == null) return false;

        // If battery drops below 15% suddenly, might indicate theft
        // (thief might be using device heavily)
        return battery < 15 && device.getIsOnline();
    }

    private boolean detectSuspiciousOfflineLocation(Device device) {
        if (device.getIsOnline()) return false;

        List<LocationHistory> history = locationHistoryRepository
            .findByDeviceOrderByTimestampDesc(device);

        if (history.size() < 10) return false;

        // Check if current location is far from usual locations
        LocationHistory current = history.get(0);
        double minDistanceToUsual = Double.MAX_VALUE;

        for (int i = 1; i < Math.min(10, history.size()); i++) {
            LocationHistory usual = history.get(i);
            double distance = haversineDistance(
                current.getLatitude(), current.getLongitude(),
                usual.getLatitude(), usual.getLongitude()
            );
            minDistanceToUsual = Math.min(minDistanceToUsual, distance);
        }

        // If device is >100km from any usual location
        return minDistanceToUsual > 100;
    }

    private double detectHighSpeedMovement(Device device) {
        List<LocationHistory> recent = locationHistoryRepository
            .findByDeviceAndTimestampAfterOrderByTimestampDesc(
                device, LocalDateTime.now().minusMinutes(30));

        double maxSpeed = 0;

        for (int i = 0; i < recent.size() - 1; i++) {
            LocationHistory current = recent.get(i);
            LocationHistory next = recent.get(i + 1);

            Duration timeDiff = Duration.between(next.getTimestamp(), current.getTimestamp());
            double timeHours = timeDiff.toMinutes() / 60.0;

            if (timeHours < 0.01) continue;

            double distance = haversineDistance(
                current.getLatitude(), current.getLongitude(),
                next.getLatitude(), next.getLongitude()
            );

            double speed = distance / timeHours;
            maxSpeed = Math.max(maxSpeed, speed);
        }

        return maxSpeed;
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Result class
    public static class TheftAnalysisResult {
        private String deviceId;
        private int threatLevel; // 0-10
        private boolean isTheftDetected;
        private List<String> suspiciousPatterns;
        private String recommendedAction;

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public int getThreatLevel() { return threatLevel; }
        public void setThreatLevel(int threatLevel) { this.threatLevel = threatLevel; }

        public boolean getIsTheftDetected() { return isTheftDetected; }
        public void setIsTheftDetected(boolean isTheftDetected) { this.isTheftDetected = isTheftDetected; }

        public List<String> getSuspiciousPatterns() { return suspiciousPatterns; }
        public void setSuspiciousPatterns(List<String> suspiciousPatterns) { this.suspiciousPatterns = suspiciousPatterns; }

        public String getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    }
}
