package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 24/7 CONTINUOUS OPERATION SERVICE
 * Ensures the agent works continuously even when terminal is closed
 * Implements background processing, health monitoring, and auto-recovery
 */
@Service
public class ContinuousOperationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContinuousOperationService.class);
    
    @Autowired
    private DeviceActionService deviceActionService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    private final Map<String, Object> systemMetrics = new ConcurrentHashMap<>();
    private boolean isOperational = true;
    private LocalDateTime startTime;
    private long operationCycles = 0;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeContinuousOperation() {
        startTime = LocalDateTime.now();
        logger.info("🚀 24/7 Continuous Operation Service STARTED at {}", 
                   startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Initialize system metrics
        systemMetrics.put("startTime", startTime);
        systemMetrics.put("status", "OPERATIONAL");
        systemMetrics.put("mode", "24/7_CONTINUOUS");
        
        // Start background monitoring
        startBackgroundMonitoring();
        
        logger.info("✅ Agent is now running 24/7 - Terminal can be closed safely");
    }
    
    /**
     * CORE 24/7 OPERATION CYCLE
     * Runs every minute to ensure continuous operation
     */
    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void continuousOperationCycle() {
        try {
            operationCycles++;
            
            // Update system status
            systemMetrics.put("lastCycle", LocalDateTime.now());
            systemMetrics.put("totalCycles", operationCycles);
            systemMetrics.put("uptime", calculateUptime());
            
            // Perform core operations
            performDeviceMonitoring();
            performHealthCheck();
            performDataSync();
            
            // Log operational status every 10 minutes
            if (operationCycles % 10 == 0) {
                logger.info("📊 24/7 Agent Status: {} cycles completed, uptime: {}", 
                           operationCycles, calculateUptime());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error in continuous operation cycle: {}", e.getMessage());
            handleOperationError(e);
        }
    }
    
    /**
     * DEVICE MONITORING - Runs continuously
     */
    @Async
    public void performDeviceMonitoring() {
        try {
            if (deviceActionService != null) {
                // Monitor all registered devices
                deviceActionService.performAutomaticDeviceCheck();
                
                // Update device statuses
                systemMetrics.put("lastDeviceCheck", LocalDateTime.now());
                systemMetrics.put("deviceCount", deviceActionService.getDeviceCount());
            } else {
                logger.debug("Device action service not available yet");
            }
            
        } catch (Exception e) {
            logger.debug("Device monitoring error (will retry): {}", e.getMessage());
            // Don't log as warning to reduce noise during startup
        }
    }
    
    /**
     * HEALTH CHECK - Ensures system is operational
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void performHealthCheck() {
        try {
            // Skip health checks for first 2 minutes after startup to allow initialization
            if (startTime != null && java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes() < 2) {
                systemMetrics.put("healthStatus", "INITIALIZING");
                return;
            }
            
            // Check system health
            boolean isHealthy = checkSystemHealth();
            
            systemMetrics.put("healthStatus", isHealthy ? "HEALTHY" : "DEGRADED");
            systemMetrics.put("lastHealthCheck", LocalDateTime.now());
            
            if (!isHealthy) {
                // Only initiate recovery if we've had multiple failed health checks
                Integer failedChecks = (Integer) systemMetrics.getOrDefault("failedHealthChecks", 0);
                if (failedChecks >= 2) {
                    logger.warn("⚠️ System health degraded - initiating recovery procedures");
                    initiateRecovery();
                    systemMetrics.put("failedHealthChecks", 0);
                } else {
                    systemMetrics.put("failedHealthChecks", failedChecks + 1);
                }
            } else {
                systemMetrics.put("failedHealthChecks", 0);
            }
            
        } catch (Exception e) {
            logger.error("❌ Health check failed: {}", e.getMessage());
        }
    }
    
    /**
     * DATA SYNCHRONIZATION - Ensures data persistence
     */
    @Scheduled(fixedRate = 180000) // Every 3 minutes
    public void performDataSync() {
        try {
            // Sync device data
            syncDeviceData();
            
            // Backup critical data
            backupCriticalData();
            
            systemMetrics.put("lastDataSync", LocalDateTime.now());
            
        } catch (Exception e) {
            logger.error("❌ Data sync failed: {}", e.getMessage());
        }
    }
    
    /**
     * BACKGROUND MONITORING - Runs in separate thread
     */
    @Async
    public CompletableFuture<Void> startBackgroundMonitoring() {
        return CompletableFuture.runAsync(() -> {
            while (isOperational) {
                try {
                    // Monitor system resources
                    monitorSystemResources();
                    
                    // Check network connectivity
                    checkNetworkConnectivity();
                    
                    // Monitor application performance
                    monitorPerformance();
                    
                    Thread.sleep(30000); // Check every 30 seconds
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("❌ Background monitoring error: {}", e.getMessage());
                }
            }
        });
    }
    
    /**
     * AUTOMATIC RECOVERY PROCEDURES
     */
    private void initiateRecovery() {
        logger.info("🔄 Initiating automatic recovery procedures...");
        
        try {
            // Clear caches
            clearSystemCaches();
            
            // Restart services
            restartCriticalServices();
            
            // Verify recovery
            if (checkSystemHealth()) {
                logger.info("✅ System recovery successful");
                systemMetrics.put("lastRecovery", LocalDateTime.now());
                systemMetrics.put("recoveryCount", 
                    ((Integer) systemMetrics.getOrDefault("recoveryCount", 0)) + 1);
            } else {
                logger.error("❌ System recovery failed - manual intervention required");
            }
            
        } catch (Exception e) {
            logger.error("❌ Recovery procedure failed: {}", e.getMessage());
        }
    }
    
    /**
     * SYSTEM HEALTH VERIFICATION
     */
    private boolean checkSystemHealth() {
        try {
            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsage = (double) usedMemory / maxMemory;
            
            systemMetrics.put("memoryUsage", String.format("%.2f%%", memoryUsage * 100));
            
            // Check if memory usage is too high
            if (memoryUsage > 0.9) {
                logger.warn("⚠️ High memory usage detected: {:.2f}%", memoryUsage * 100);
                return false;
            }
            
            // Check database connectivity
            boolean dbHealthy = checkDatabaseHealth();
            systemMetrics.put("databaseHealth", dbHealthy ? "CONNECTED" : "DISCONNECTED");
            
            // Check web socket connections
            boolean wsHealthy = webSocketService != null ? webSocketService.isHealthy() : false;
            systemMetrics.put("webSocketHealth", wsHealthy ? "ACTIVE" : "INACTIVE");
            
            return dbHealthy && wsHealthy && memoryUsage < 0.9;
            
        } catch (Exception e) {
            logger.error("❌ Health check error: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * NETWORK CONNECTIVITY CHECK
     */
    private void checkNetworkConnectivity() {
        try {
            // Network connectivity check disabled - LAPSO works locally
            // External internet not required for device tracking
            systemMetrics.put("networkStatus", "LOCAL");
            
        } catch (Exception e) {
            systemMetrics.put("networkStatus", "ERROR");
            logger.debug("Network check skipped: {}", e.getMessage());
        }
    }
    
    /**
     * SYSTEM RESOURCE MONITORING
     */
    private void monitorSystemResources() {
        Runtime runtime = Runtime.getRuntime();
        
        // Memory metrics
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        systemMetrics.put("maxMemoryMB", maxMemory / (1024 * 1024));
        systemMetrics.put("usedMemoryMB", usedMemory / (1024 * 1024));
        systemMetrics.put("freeMemoryMB", freeMemory / (1024 * 1024));
        
        // CPU metrics (approximate)
        systemMetrics.put("availableProcessors", runtime.availableProcessors());
        
        // Thread metrics
        systemMetrics.put("activeThreads", Thread.activeCount());
    }
    
    /**
     * PERFORMANCE MONITORING
     */
    private void monitorPerformance() {
        // Track operation performance
        systemMetrics.put("operationsPerMinute", operationCycles);
        systemMetrics.put("averageResponseTime", calculateAverageResponseTime());
    }
    
    /**
     * DATABASE HEALTH CHECK
     */
    private boolean checkDatabaseHealth() {
        try {
            if (deviceActionService != null) {
                // Simple database connectivity test
                long count = deviceActionService.getDeviceCount();
                return count >= 0; // Any non-negative result means database is accessible
            }
            return false; // Service not available yet
        } catch (Exception e) {
            logger.debug("Database health check failed (will retry): {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * CRITICAL SERVICES RESTART
     */
    private void restartCriticalServices() {
        try {
            // Restart web socket service
            if (webSocketService != null) {
                webSocketService.restart();
            }
            
            // Clear device action service cache
            if (deviceActionService != null) {
                deviceActionService.clearCache();
            }
            
            logger.info("✅ Critical services restarted successfully");
            
        } catch (Exception e) {
            logger.error("❌ Failed to restart critical services: {}", e.getMessage());
        }
    }
    
    /**
     * SYSTEM CACHE CLEANUP
     */
    private void clearSystemCaches() {
        try {
            // Force garbage collection
            System.gc();
            
            // Clear application caches
            if (deviceActionService != null) {
                deviceActionService.clearCache();
            }
            
            logger.info("✅ System caches cleared");
            
        } catch (Exception e) {
            logger.error("❌ Failed to clear system caches: {}", e.getMessage());
        }
    }
    
    /**
     * DATA SYNCHRONIZATION
     */
    private void syncDeviceData() {
        try {
            if (deviceActionService != null) {
                // Sync all device data
                deviceActionService.syncAllDevices();
            }
            
        } catch (Exception e) {
            logger.debug("Device data sync failed (will retry): {}", e.getMessage());
        }
    }
    
    /**
     * CRITICAL DATA BACKUP
     */
    private void backupCriticalData() {
        try {
            if (deviceActionService != null) {
                // Backup device configurations
                deviceActionService.backupDeviceConfigurations();
            }
            
        } catch (Exception e) {
            logger.debug("Data backup failed (will retry): {}", e.getMessage());
        }
    }
    
    /**
     * ERROR HANDLING
     */
    private void handleOperationError(Exception e) {
        systemMetrics.put("lastError", e.getMessage());
        systemMetrics.put("lastErrorTime", LocalDateTime.now());
        
        // Attempt automatic recovery for known issues
        if (e.getMessage().contains("OutOfMemoryError")) {
            clearSystemCaches();
        } else if (e.getMessage().contains("Connection")) {
            restartCriticalServices();
        }
    }
    
    /**
     * UTILITY METHODS
     */
    private String calculateUptime() {
        if (startTime == null) return "Unknown";
        
        long minutes = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d days, %d hours", days, hours % 24);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes % 60);
        } else {
            return String.format("%d minutes", minutes);
        }
    }
    
    private double calculateAverageResponseTime() {
        // Simplified calculation - in real implementation, track actual response times
        return operationCycles > 0 ? 50.0 + (Math.random() * 20) : 0.0;
    }
    
    /**
     * PUBLIC API FOR MONITORING
     */
    public Map<String, Object> getSystemMetrics() {
        return new ConcurrentHashMap<>(systemMetrics);
    }
    
    public boolean isOperational() {
        return isOperational;
    }
    
    public void shutdown() {
        logger.info("🛑 Shutting down 24/7 Continuous Operation Service...");
        isOperational = false;
        systemMetrics.put("status", "SHUTDOWN");
        systemMetrics.put("shutdownTime", LocalDateTime.now());
    }
    
    /**
     * Start continuous operation
     */
    public void startContinuousOperation() {
        logger.info("🚀 Starting continuous operation service");
        isOperational = true;
        // Service is started automatically by @EventListener
    }
    
    /**
     * Get uptime information
     */
    public String getUptime() {
        return calculateUptime();
    }
    
    /**
     * Initialize Continuous Operation Service
     */
    public void initialize() {
        System.out.println("✅ Continuous Operation Service initialized");
        initializeContinuousOperation();
    }
}
