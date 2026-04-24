package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class DeviceActionService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    public boolean lockDevice(Long deviceId, String userEmail) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId)
            .filter(device -> device.getUserEmail().equals(userEmail));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setIsLocked(true);
            device.setLastAction("LOCKED");
            device.setLastActionTime(LocalDateTime.now());
            deviceRepository.save(device);
            
            notificationService.sendDeviceAction(device, "LOCK");
            return true;
        }
        return false;
    }

    public boolean unlockDevice(Long deviceId, String userEmail) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId)
            .filter(device -> device.getUserEmail().equals(userEmail));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setIsLocked(false);
            device.setLastAction("UNLOCKED");
            device.setLastActionTime(LocalDateTime.now());
            deviceRepository.save(device);
            
            notificationService.sendDeviceAction(device, "UNLOCK");
            return true;
        }
        return false;
    }

    public boolean playSound(Long deviceId, String userEmail) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId)
            .filter(device -> device.getUserEmail().equals(userEmail));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setLastAction("PLAY_SOUND");
            device.setLastActionTime(LocalDateTime.now());
            deviceRepository.save(device);
            
            notificationService.sendDeviceAction(device, "PLAY_SOUND");
            return true;
        }
        return false;
    }

    public boolean markAsStolen(Long deviceId, String userEmail) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId)
            .filter(device -> device.getUserEmail().equals(userEmail));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setIsStolen(true);
            device.setTheftDetected(true);
            device.setLastAction("MARKED_STOLEN");
            device.setLastActionTime(LocalDateTime.now());
            deviceRepository.save(device);
            
            notificationService.sendSecurityAlert(device, "Device marked as stolen");
            return true;
        }
        return false;
    }

    @Async
    public CompletableFuture<Boolean> sendRemoteCommand(Long deviceId, String userEmail, String command) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId)
                .filter(device -> device.getUserEmail().equals(userEmail));
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                
                // Log the command
                System.out.println("Sending " + command + " command to device: " + device.getDeviceName());
                
                // In a real implementation, this sends the command to the device agent
                // For now, we simulate the action
                device.setLastAction(command);
                device.setLastActionTime(LocalDateTime.now());
                deviceRepository.save(device);
                
                // Notify via WebSocket if available
                if (notificationService != null) {
                    notificationService.sendDeviceAction(device, command);
                }
                
                return true;
            }
            return false;
        });
    }

    public List<Device> getDevicesNeedingAttention(String userEmail) {
        // Find devices that need attention (low battery, offline for too long, etc.)
        return deviceRepository.findByUserEmail(userEmail).stream()
            .filter(device -> 
                (device.getBatteryLevel() != null && device.getBatteryLevel() < 20) ||
                (device.getTheftDetected() != null && device.getTheftDetected()) ||
                (!device.getIsOnline())
            )
            .toList();
    }

    public boolean isDeviceOnline(Long deviceId, String userEmail) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId)
            .filter(device -> device.getUserEmail().equals(userEmail));
        return deviceOpt.map(Device::getIsOnline).orElse(false);
    }

    public void updateDeviceStatus(Long deviceId, String userEmail, boolean isOnline) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId)
            .filter(device -> device.getUserEmail().equals(userEmail));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setIsOnline(isOnline);
            device.setLastSeen(LocalDateTime.now());
            deviceRepository.save(device);
        }
    }

    // ========================================
    // 24/7 CONTINUOUS OPERATION METHODS
    // ========================================
    
    /**
     * Perform automatic device check for 24/7 monitoring
     */
    public void performAutomaticDeviceCheck() {
        try {
            List<Device> allDevices = deviceRepository.findAll();
            
            for (Device device : allDevices) {
                // Check device status and update if needed
                checkDeviceHealth(device);
                
                // Update last check time (using existing field)
                device.setLastSeen(LocalDateTime.now());
                deviceRepository.save(device);
            }
            
        } catch (Exception e) {
            // Log error but don't throw to maintain continuous operation
            System.err.println("Error in automatic device check: " + e.getMessage());
        }
    }
    
    /**
     * Get total device count for monitoring
     */
    public long getDeviceCount() {
        try {
            return deviceRepository.count();
        } catch (Exception e) {
            System.err.println("Error getting device count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Clear service cache for recovery
     */
    public void clearCache() {
        try {
            // Clear any cached data
            System.gc(); // Suggest garbage collection
        } catch (Exception e) {
            System.err.println("Error clearing cache: " + e.getMessage());
        }
    }
    
    /**
     * Sync all devices for 24/7 operation
     */
    public void syncAllDevices() {
        try {
            List<Device> devices = deviceRepository.findAll();
            
            for (Device device : devices) {
                // Perform sync operations
                syncDeviceData(device);
            }
            
        } catch (Exception e) {
            System.err.println("Error syncing devices: " + e.getMessage());
        }
    }
    
    /**
     * Backup device configurations
     */
    public void backupDeviceConfigurations() {
        try {
            List<Device> devices = deviceRepository.findAll();
            
            // Simple backup - in production, implement proper backup strategy
            for (Device device : devices) {
                // Log device configuration for backup
                System.out.println("Backing up device: " + device.getDeviceName() + " - " + device.getDeviceType());
            }
            
        } catch (Exception e) {
            System.err.println("Error backing up configurations: " + e.getMessage());
        }
    }
    
    /**
     * Check individual device health
     */
    private void checkDeviceHealth(Device device) {
        try {
            // Update device status based on last seen time
            LocalDateTime lastSeen = device.getLastSeen();
            if (lastSeen != null) {
                LocalDateTime now = LocalDateTime.now();
                long minutesSinceLastSeen = java.time.Duration.between(lastSeen, now).toMinutes();
                
                if (minutesSinceLastSeen > 60) {
                    device.setIsOnline(false);
                } else if (minutesSinceLastSeen > 30) {
                    // Warning state - still online but concerning
                    device.setIsOnline(true);
                } else {
                    device.setIsOnline(true);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error checking device health: " + e.getMessage());
        }
    }
    
    /**
     * Sync individual device data
     */
    private void syncDeviceData(Device device) {
        try {
            // Update sync timestamp (using existing field)
            device.setUpdatedAt(LocalDateTime.now());
            deviceRepository.save(device);
            
        } catch (Exception e) {
            System.err.println("Error syncing device data: " + e.getMessage());
        }
    }
    
    /**
     * Initialize Device Action Service
     */
    public void initialize() {
        System.out.println("✅ Device Action Service initialized");
    }
    

}
