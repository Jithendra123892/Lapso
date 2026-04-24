package com.example.demo.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * ULTRA-PRECISE TRACKING AGENT SIMULATOR
 * Simulates a high-frequency tracking agent for testing
 */
public class UltraPreciseAgentSimulator {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("Ultra-Precise Tracking Agent Simulator");
        System.out.println("=====================================");

        // Simulate high-frequency location updates
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Simulate location update
                simulateLocationUpdate();
            } catch (Exception e) {
                System.err.println("Error in location simulation: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS); // 5-second intervals for ultra-precise tracking
    }

    private static void simulateLocationUpdate() {
        // Generate random location data for testing
        double latitude = 28.6139 + (random.nextDouble() - 0.5) * 0.01; // Delhi area
        double longitude = 77.2090 + (random.nextDouble() - 0.5) * 0.01;

        System.out.println("📍 Ultra-precise tracking update - Lat: " + latitude + ", Lng: " + longitude +
                         " (Accuracy: 1cm) - " + LocalDateTime.now());
    }
}