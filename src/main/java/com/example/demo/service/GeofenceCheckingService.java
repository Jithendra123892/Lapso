package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.Geofence;
import com.example.demo.repository.GeofenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeofenceCheckingService {

    @Autowired
    private GeofenceRepository geofenceRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Track which geofences each device was inside (for detecting entry/exit)
    private final Map<String, Map<Long, Boolean>> deviceGeofenceStatus = new ConcurrentHashMap<>();

    /**
     * Check device location against all user's active geofences
     * Triggers alerts on entry/exit events
     */
    public void checkDeviceGeofences(Device device) {
        if (device.getLatitude() == null || device.getLongitude() == null) {
            return;
        }

        String deviceId = device.getDeviceId();
        String username = device.getUser().getEmail();

        // Get all active geofences for user
        List<Geofence> activeGeofences = geofenceRepository.findByUserIdAndIsActiveTrue(username);

        // Initialize device status map if not exists
        deviceGeofenceStatus.putIfAbsent(deviceId, new ConcurrentHashMap<>());
        Map<Long, Boolean> previousStatus = deviceGeofenceStatus.get(deviceId);

        for (Geofence geofence : activeGeofences) {
            boolean isInsideNow = geofence.containsPoint(device.getLatitude(), device.getLongitude());
            boolean wasInsideBefore = previousStatus.getOrDefault(geofence.getId(), false);

            // Detect entry event
            if (isInsideNow && !wasInsideBefore) {
                handleGeofenceEntry(device, geofence);
            }

            // Detect exit event
            if (!isInsideNow && wasInsideBefore) {
                handleGeofenceExit(device, geofence);
            }

            // Update status
            previousStatus.put(geofence.getId(), isInsideNow);
        }
    }

    /**
     * Handle device entering a geofence
     */
    private void handleGeofenceEntry(Device device, Geofence geofence) {
        if (geofence.getAlertOnEntry() == null || !geofence.getAlertOnEntry()) {
            return; // No alert configured
        }

        String message = String.format("Device '%s' has entered geofence '%s'",
                device.getDeviceName(), geofence.getName());

        // Send notification
        sendGeofenceAlert(device, geofence, "ENTRY", message);

        // Log event
        System.out.println("[GEOFENCE ENTRY] " + message);
    }

    /**
     * Handle device exiting a geofence
     */
    private void handleGeofenceExit(Device device, Geofence geofence) {
        if (geofence.getAlertOnExit() == null || !geofence.getAlertOnExit()) {
            return; // No alert configured
        }

        String message = String.format("Device '%s' has left geofence '%s'",
                device.getDeviceName(), geofence.getName());

        // Send notification
        sendGeofenceAlert(device, geofence, "EXIT", message);

        // Auto-lock on exit if configured
        if (geofence.getAutoLockOnExit() != null && geofence.getAutoLockOnExit()) {
            // TODO: Trigger auto-lock command
            System.out.println("[GEOFENCE AUTO-LOCK] Device " + device.getDeviceName() + " locked on exit");
        }

        // Log event
        System.out.println("[GEOFENCE EXIT] " + message);
    }

    /**
     * Send geofence alert notifications
     */
    private void sendGeofenceAlert(Device device, Geofence geofence, String eventType, String message) {
        String username = device.getUser().getEmail();

        // Send WebSocket notification
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("type", "GEOFENCE_" + eventType);
        alertData.put("deviceId", device.getDeviceId());
        alertData.put("deviceName", device.getDeviceName());
        alertData.put("geofenceId", geofence.getId());
        alertData.put("geofenceName", geofence.getName());
        alertData.put("geofenceType", geofence.getFenceType());
        alertData.put("message", message);
        alertData.put("timestamp", java.time.LocalDateTime.now());
        alertData.put("location", Map.of(
                "latitude", device.getLatitude(),
                "longitude", device.getLongitude()
        ));

        messagingTemplate.convertAndSendToUser(username, "/queue/geofence-alerts", alertData);

        // Send email notification if configured
        try {
            String emailSubject = String.format("Geofence Alert: %s - %s", geofence.getName(), eventType);
            String emailBody = String.format(
                    "Hello,\n\n" +
                    "%s\n\n" +
                    "Geofence Details:\n" +
                    "- Name: %s\n" +
                    "- Type: %s\n" +
                    "- Event: %s\n\n" +
                    "Device Location:\n" +
                    "- Latitude: %.6f\n" +
                    "- Longitude: %.6f\n\n" +
                    "View on map: https://your-domain.com/map?device=%s\n\n" +
                    "Best regards,\n" +
                    "LAPSO Team",
                    message, geofence.getName(), geofence.getFenceType(), eventType,
                    device.getLatitude(), device.getLongitude(), device.getDeviceId()
            );

            notificationService.sendEmailNotification(
                    device.getUser().getEmail(),
                    emailSubject,
                    emailBody
            );
        } catch (Exception e) {
            System.err.println("Failed to send geofence email: " + e.getMessage());
        }
    }

    /**
     * Get current geofence status for a device
     */
    public Map<String, Object> getDeviceGeofenceStatus(Device device) {
        if (device.getLatitude() == null || device.getLongitude() == null) {
            return Map.of("status", "NO_LOCATION");
        }

        String username = device.getUser().getEmail();
        List<Geofence> activeGeofences = geofenceRepository.findByUserIdAndIsActiveTrue(username);
        List<Geofence> insideGeofences = geofenceRepository.findGeofencesContainingPoint(
                username, device.getLatitude(), device.getLongitude());

        return Map.of(
                "totalActiveGeofences", activeGeofences.size(),
                "insideGeofences", insideGeofences.size(),
                "geofences", insideGeofences,
                "isInsideAny", !insideGeofences.isEmpty()
        );
    }

    /**
     * Clear status for a device (useful when device is removed)
     */
    public void clearDeviceStatus(String deviceId) {
        deviceGeofenceStatus.remove(deviceId);
    }

    /**
     * Clear status for a geofence (useful when geofence is deleted)
     */
    public void clearGeofenceStatus(Long geofenceId) {
        deviceGeofenceStatus.values().forEach(map -> map.remove(geofenceId));
    }
}

