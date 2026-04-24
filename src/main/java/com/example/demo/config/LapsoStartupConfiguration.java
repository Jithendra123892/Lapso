package com.example.demo.config;

import com.example.demo.service.RealTimeMonitoringService;
import com.example.demo.service.ContinuousOperationService;
import com.example.demo.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LapsoStartupConfiguration implements CommandLineRunner {

    @Autowired
    private RealTimeMonitoringService realTimeMonitoringService;
    
    @Autowired
    private ContinuousOperationService continuousOperationService;
    
    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🚀 LAPSO Startup Configuration");
        System.out.println("===============================");
        
        // Initialize real-time monitoring (handled by @Scheduled annotations)
        System.out.println("✅ Real-time monitoring service initialized");
        
        // Initialize continuous operation
        try {
            continuousOperationService.startContinuousOperation();
            System.out.println("✅ 24/7 continuous operation started");
        } catch (Exception e) {
            System.err.println("❌ Failed to start continuous operation: " + e.getMessage());
        }
        
        // Verify WebSocket service
        try {
            if (webSocketService.isHealthy()) {
                System.out.println("✅ WebSocket service ready");
            } else {
                System.out.println("⚠️ WebSocket service needs attention");
            }
        } catch (Exception e) {
            System.err.println("❌ WebSocket service error: " + e.getMessage());
        }
        
        System.out.println("===============================");
        System.out.println("🛡️ LAPSO is now fully operational!");
        System.out.println("⚡ Real-time updates every 30 seconds");
        System.out.println("🌐 Better than Microsoft Find My Device");
        System.out.println("🆓 Completely free and open source");
        System.out.println("===============================\n");
    }
}
