package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service to automatically clean up fake devices from the system
 */
@Service
public class FakeDeviceCleanupService {
    
    @Autowired
    private DeviceService deviceService;
    
    /**
     * Run cleanup every hour (3600000 ms = 1 hour)
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupFakeDevices() {
        try {
            System.out.println("🔍 Running automatic fake device cleanup...");
            deviceService.cleanupFakeDevices();
            System.out.println("✅ Automatic fake device cleanup completed");
        } catch (Exception e) {
            System.err.println("❌ Error during fake device cleanup: " + e.getMessage());
        }
    }
}