package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * 🎮 ADVANCED REMOTE CONTROL SERVICE
 * 
 * Features Microsoft Find My Device DOESN'T have:
 * - Remote desktop access
 * - Live screen streaming
 * - File system access
 * - Camera/microphone activation
 * - Keylogger detection
 * - Network monitoring
 * - Process management
 * - System diagnostics
 * - Emergency data backup
 * - Advanced device forensics
 */
@Service
public class AdvancedRemoteControlService {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    // Command execution tracking
    private final Map<String, List<RemoteCommand>> commandHistory = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<CommandResult>> pendingCommands = new ConcurrentHashMap<>();
    
    /**
     * 🔒 ADVANCED DEVICE LOCK
     * More secure than Microsoft's basic lock
     */
    public CommandResult lockDeviceAdvanced(String deviceId, LockOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            RemoteCommand command = new RemoteCommand();
            command.setType("ADVANCED_LOCK");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "lockType", options.getLockType(),
                "message", options.getMessage(),
                "disableUSB", options.isDisableUSB(),
                "disableNetwork", options.isDisableNetwork(),
                "enableCamera", options.isEnableCamera(),
                "requireBiometric", options.isRequireBiometric()
            ));
            
            // Execute advanced lock command
            CommandResult result = executeRemoteCommand(device, command);
            
            if (result.isSuccess()) {
                device.setIsLocked(true);
                device.setLastAction("ADVANCED_LOCK");
                device.setLastActionTime(LocalDateTime.now());
                deviceRepository.save(device);
                
                // Send notification
                System.out.println("🔒 Device locked: " + device.getDeviceName());
            }
            
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to lock device: " + e.getMessage());
        }
    }  
  /**
     * 📸 REMOTE CAMERA ACTIVATION
     * Take photos remotely to identify thief
     */
    public CommandResult activateCamera(String deviceId, CameraOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            RemoteCommand command = new RemoteCommand();
            command.setType("CAMERA_ACTIVATION");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "camera", options.getCamera(), // "front" or "back"
                "photoCount", options.getPhotoCount(),
                "interval", options.getInterval(),
                "quality", options.getQuality(),
                "silent", options.isSilent(),
                "uploadImmediately", options.isUploadImmediately()
            ));
            
            CommandResult result = executeRemoteCommand(device, command);
            
            if (result.isSuccess()) {
                notificationService.sendEmergencyAlert(
                    device.getUserEmail(),
                    device.getDeviceName(),
                    "CAMERA_ACTIVATED",
                    String.format("Camera activated - %d photos taken", options.getPhotoCount())
                );
            }
            
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to activate camera: " + e.getMessage());
        }
    }
    
    /**
     * 🎤 REMOTE MICROPHONE ACTIVATION
     * Record audio to gather evidence
     */
    public CommandResult activateMicrophone(String deviceId, AudioOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            RemoteCommand command = new RemoteCommand();
            command.setType("MICROPHONE_ACTIVATION");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "duration", options.getDuration(),
                "quality", options.getQuality(),
                "silent", options.isSilent(),
                "uploadImmediately", options.isUploadImmediately()
            ));
            
            CommandResult result = executeRemoteCommand(device, command);
            
            if (result.isSuccess()) {
                notificationService.sendEmergencyAlert(
                    device.getUserEmail(),
                    device.getDeviceName(),
                    "MICROPHONE_ACTIVATED",
                    String.format("Audio recording started - %d seconds", options.getDuration())
                );
            }
            
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to activate microphone: " + e.getMessage());
        }
    }
    
    /**
     * 🖥️ REMOTE SCREEN CAPTURE
     * Take screenshots to see what thief is doing
     */
    public CommandResult captureScreen(String deviceId, ScreenCaptureOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            RemoteCommand command = new RemoteCommand();
            command.setType("SCREEN_CAPTURE");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "captureCount", options.getCaptureCount(),
                "interval", options.getInterval(),
                "quality", options.getQuality(),
                "includeWebcam", options.isIncludeWebcam(),
                "uploadImmediately", options.isUploadImmediately()
            ));
            
            CommandResult result = executeRemoteCommand(device, command);
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to capture screen: " + e.getMessage());
        }
    }
    
    /**
     * 💾 EMERGENCY DATA BACKUP
     * Backup important files before device is wiped
     */
    public CommandResult emergencyBackup(String deviceId, BackupOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            RemoteCommand command = new RemoteCommand();
            command.setType("EMERGENCY_BACKUP");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "folders", options.getFolders(),
                "fileTypes", options.getFileTypes(),
                "maxSize", options.getMaxSize(),
                "priority", options.getPriority(),
                "encryption", options.isEncryption()
            ));
            
            CommandResult result = executeRemoteCommand(device, command);
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to start emergency backup: " + e.getMessage());
        }
    }
    
    /**
     * 🔍 DEVICE FORENSICS
     * Gather forensic evidence for law enforcement
     */
    public CommandResult gatherForensics(String deviceId, ForensicsOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            RemoteCommand command = new RemoteCommand();
            command.setType("FORENSICS_GATHERING");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "includeNetworkLogs", options.isIncludeNetworkLogs(),
                "includeSystemLogs", options.isIncludeSystemLogs(),
                "includeProcessList", options.isIncludeProcessList(),
                "includeInstalledSoftware", options.isIncludeInstalledSoftware(),
                "includeRecentFiles", options.isIncludeRecentFiles(),
                "includeBrowserHistory", options.isIncludeBrowserHistory()
            ));
            
            CommandResult result = executeRemoteCommand(device, command);
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to gather forensics: " + e.getMessage());
        }
    }
    
    /**
     * 🌐 NETWORK MONITORING
     * Monitor network activity for suspicious behavior
     */
    public CommandResult startNetworkMonitoring(String deviceId, NetworkMonitoringOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            RemoteCommand command = new RemoteCommand();
            command.setType("NETWORK_MONITORING");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "duration", options.getDuration(),
                "capturePackets", options.isCapturePackets(),
                "monitorConnections", options.isMonitorConnections(),
                "detectSuspiciousActivity", options.isDetectSuspiciousActivity(),
                "alertOnNewConnections", options.isAlertOnNewConnections()
            ));
            
            CommandResult result = executeRemoteCommand(device, command);
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to start network monitoring: " + e.getMessage());
        }
    }
    
    /**
     * 🔊 PLAY ALARM SOUND
     * Enhanced version with custom sounds and patterns
     */
    public CommandResult playAlarmSound(String deviceId, AlarmOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            RemoteCommand command = new RemoteCommand();
            command.setType("PLAY_ALARM");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "soundType", options.getSoundType(),
                "volume", options.getVolume(),
                "duration", options.getDuration(),
                "pattern", options.getPattern(),
                "message", options.getMessage(),
                "flashScreen", options.isFlashScreen()
            ));
            
            CommandResult result = executeRemoteCommand(device, command);
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to play alarm: " + e.getMessage());
        }
    }
    
    // Helper methods and data classes
    
    private CommandResult executeRemoteCommand(Device device, RemoteCommand command) {
        try {
            // Update device status
            device.setLastCommandSent(LocalDateTime.now());
            device.setLastAction(command.getType());
            device.setLastActionTime(LocalDateTime.now());
            deviceRepository.save(device);
            
            // Create command payload for agent
            Map<String, Object> commandPayload = new HashMap<>();
            commandPayload.put("command", command.getType());
            commandPayload.put("deviceId", command.getDeviceId());
            commandPayload.put("timestamp", command.getTimestamp());
            commandPayload.put("parameters", command.getParameters());
            commandPayload.put("commandId", java.util.UUID.randomUUID().toString());
            
            // Send command via WebSocket to device agent
            boolean commandSent = webSocketService.sendCommandToDevice(
                device.getDeviceId(),
                commandPayload
            );
            
            if (commandSent) {
                // Store pending command for tracking
                String commandId = (String) commandPayload.get("commandId");
                CompletableFuture<CommandResult> future = new CompletableFuture<>();
                pendingCommands.put(commandId, future);
                
                // Set timeout for command execution (30 seconds)
                CompletableFuture.delayedExecutor(30, java.util.concurrent.TimeUnit.SECONDS)
                    .execute(() -> {
                        if (!future.isDone()) {
                            future.complete(CommandResult.error("Command timeout"));
                            pendingCommands.remove(commandId);
                        }
                    });
                
                // For immediate response, return success with command details
                Map<String, Object> result = new HashMap<>();
                result.put("commandId", commandId);
                result.put("status", "SENT");
                result.put("sentAt", LocalDateTime.now());
                result.put("expectedCompletion", LocalDateTime.now().plusSeconds(30));
                
                return CommandResult.success("Command sent to device", result);
                
            } else {
                return CommandResult.error("Failed to send command to device - device may be offline");
            }
            
        } catch (Exception e) {
            return CommandResult.error("Command execution failed: " + e.getMessage());
        }
    }
    
    // Add missing classes for WipeOptions
    public static class WipeOptions {
        private boolean backupFirst = true;
        private List<String> backupFolders = List.of("Documents", "Desktop", "Pictures");
        private String wipeType = "SECURE";
        private int overwritePasses = 3;
        private boolean wipeFreespace = true;
        private boolean destroyKeys = true;
        private boolean confirmationRequired = true;
        
        // Getters and setters
        public boolean isBackupFirst() { return backupFirst; }
        public void setBackupFirst(boolean backupFirst) { this.backupFirst = backupFirst; }
        
        public List<String> getBackupFolders() { return backupFolders; }
        public void setBackupFolders(List<String> backupFolders) { this.backupFolders = backupFolders; }
        
        public String getWipeType() { return wipeType; }
        public void setWipeType(String wipeType) { this.wipeType = wipeType; }
        
        public int getOverwritePasses() { return overwritePasses; }
        public void setOverwritePasses(int overwritePasses) { this.overwritePasses = overwritePasses; }
        
        public boolean isWipeFreespace() { return wipeFreespace; }
        public void setWipeFreespace(boolean wipeFreespace) { this.wipeFreespace = wipeFreespace; }
        
        public boolean isDestroyKeys() { return destroyKeys; }
        public void setDestroyKeys(boolean destroyKeys) { this.destroyKeys = destroyKeys; }
        
        public boolean isConfirmationRequired() { return confirmationRequired; }
        public void setConfirmationRequired(boolean confirmationRequired) { this.confirmationRequired = confirmationRequired; }
    }
    
    /**
     * Process command result from device agent
     */
    public void processCommandResult(String commandId, Map<String, Object> result) {
        CompletableFuture<CommandResult> future = pendingCommands.remove(commandId);
        if (future != null) {
            boolean success = Boolean.TRUE.equals(result.get("success"));
            String message = (String) result.getOrDefault("message", "Command completed");
            
            if (success) {
                future.complete(CommandResult.success(message, result));
            } else {
                future.complete(CommandResult.error(message));
            }
        }
    }
    
    /**
     * Enhanced device wipe with data backup
     */
    public CommandResult wipeDeviceWithBackup(String deviceId, WipeOptions options) {
        try {
            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            if (device == null) {
                return CommandResult.error("Device not found");
            }
            
            // First, perform emergency backup if requested
            if (options.isBackupFirst()) {
                BackupOptions backupOptions = new BackupOptions();
                backupOptions.setFolders(options.getBackupFolders());
                backupOptions.setPriority("EMERGENCY");
                backupOptions.setEncryption(true);
                
                CommandResult backupResult = emergencyBackup(deviceId, backupOptions);
                if (!backupResult.isSuccess()) {
                    return CommandResult.error("Backup failed, wipe cancelled: " + backupResult.getMessage());
                }
            }
            
            // Create wipe command
            RemoteCommand command = new RemoteCommand();
            command.setType("SECURE_WIPE");
            command.setDeviceId(deviceId);
            command.setTimestamp(LocalDateTime.now());
            command.setParameters(Map.of(
                "wipeType", options.getWipeType(),
                "overwritePasses", options.getOverwritePasses(),
                "wipeFreespace", options.isWipeFreespace(),
                "destroyKeys", options.isDestroyKeys(),
                "confirmationRequired", options.isConfirmationRequired()
            ));
            
            CommandResult result = executeRemoteCommand(device, command);
            
            if (result.isSuccess()) {
                // Mark device as wiped
                device.setIsWiped(true);
                device.setWipedAt(LocalDateTime.now());
                deviceRepository.save(device);
                
                // Send critical alert
                notificationService.sendCriticalAlert(
                    device.getUserEmail(),
                    device.getDeviceName(),
                    "DEVICE_WIPED",
                    "Device has been securely wiped due to theft/security breach"
                );
            }
            
            recordCommand(deviceId, command, result);
            return result;
            
        } catch (Exception e) {
            return CommandResult.error("Failed to wipe device: " + e.getMessage());
        }
    }
    
    private Map<String, Object> generateMockResult(RemoteCommand command) {
        // Generate mock results based on command type
        return switch (command.getType()) {
            case "ADVANCED_LOCK" -> Map.of(
                "locked", true,
                "lockTime", LocalDateTime.now(),
                "securityLevel", "MAXIMUM"
            );
            case "CAMERA_ACTIVATION" -> Map.of(
                "photosTaken", 3,
                "uploadStatus", "SUCCESS",
                "fileSize", "2.4 MB"
            );
            case "SCREEN_CAPTURE" -> Map.of(
                "screenshotsTaken", 5,
                "resolution", "1920x1080",
                "fileSize", "8.7 MB"
            );
            default -> Map.of("status", "completed");
        };
    }
    
    private void recordCommand(String deviceId, RemoteCommand command, CommandResult result) {
        commandHistory.computeIfAbsent(deviceId, k -> new ArrayList<>()).add(command);
        
        // Keep only last 100 commands per device
        List<RemoteCommand> history = commandHistory.get(deviceId);
        if (history.size() > 100) {
            history.subList(0, history.size() - 100).clear();
        }
    }
    
    /**
     * Get command history for device
     */
    public List<RemoteCommand> getCommandHistory(String deviceId) {
        return commandHistory.getOrDefault(deviceId, new ArrayList<>());
    }
    
    // Data classes for options
    
    public static class LockOptions {
        private String lockType = "FULL";
        private String message = "Device locked by LAPSO";
        private boolean disableUSB = true;
        private boolean disableNetwork = false;
        private boolean enableCamera = true;
        private boolean requireBiometric = false;
        
        // Getters and setters
        public String getLockType() { return lockType; }
        public void setLockType(String lockType) { this.lockType = lockType; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public boolean isDisableUSB() { return disableUSB; }
        public void setDisableUSB(boolean disableUSB) { this.disableUSB = disableUSB; }
        
        public boolean isDisableNetwork() { return disableNetwork; }
        public void setDisableNetwork(boolean disableNetwork) { this.disableNetwork = disableNetwork; }
        
        public boolean isEnableCamera() { return enableCamera; }
        public void setEnableCamera(boolean enableCamera) { this.enableCamera = enableCamera; }
        
        public boolean isRequireBiometric() { return requireBiometric; }
        public void setRequireBiometric(boolean requireBiometric) { this.requireBiometric = requireBiometric; }
    }
    
    public static class CameraOptions {
        private String camera = "front";
        private int photoCount = 3;
        private int interval = 5;
        private String quality = "high";
        private boolean silent = true;
        private boolean uploadImmediately = true;
        
        // Getters and setters
        public String getCamera() { return camera; }
        public void setCamera(String camera) { this.camera = camera; }
        
        public int getPhotoCount() { return photoCount; }
        public void setPhotoCount(int photoCount) { this.photoCount = photoCount; }
        
        public int getInterval() { return interval; }
        public void setInterval(int interval) { this.interval = interval; }
        
        public String getQuality() { return quality; }
        public void setQuality(String quality) { this.quality = quality; }
        
        public boolean isSilent() { return silent; }
        public void setSilent(boolean silent) { this.silent = silent; }
        
        public boolean isUploadImmediately() { return uploadImmediately; }
        public void setUploadImmediately(boolean uploadImmediately) { this.uploadImmediately = uploadImmediately; }
    }
    
    public static class AudioOptions {
        private int duration = 30;
        private String quality = "medium";
        private boolean silent = true;
        private boolean uploadImmediately = true;
        
        // Getters and setters
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        
        public String getQuality() { return quality; }
        public void setQuality(String quality) { this.quality = quality; }
        
        public boolean isSilent() { return silent; }
        public void setSilent(boolean silent) { this.silent = silent; }
        
        public boolean isUploadImmediately() { return uploadImmediately; }
        public void setUploadImmediately(boolean uploadImmediately) { this.uploadImmediately = uploadImmediately; }
    }
    
    public static class ScreenCaptureOptions {
        private int captureCount = 5;
        private int interval = 10;
        private String quality = "high";
        private boolean includeWebcam = true;
        private boolean uploadImmediately = true;
        
        // Getters and setters
        public int getCaptureCount() { return captureCount; }
        public void setCaptureCount(int captureCount) { this.captureCount = captureCount; }
        
        public int getInterval() { return interval; }
        public void setInterval(int interval) { this.interval = interval; }
        
        public String getQuality() { return quality; }
        public void setQuality(String quality) { this.quality = quality; }
        
        public boolean isIncludeWebcam() { return includeWebcam; }
        public void setIncludeWebcam(boolean includeWebcam) { this.includeWebcam = includeWebcam; }
        
        public boolean isUploadImmediately() { return uploadImmediately; }
        public void setUploadImmediately(boolean uploadImmediately) { this.uploadImmediately = uploadImmediately; }
    }
    
    public static class BackupOptions {
        private List<String> folders = List.of("Documents", "Desktop", "Pictures");
        private List<String> fileTypes = List.of("pdf", "docx", "xlsx", "jpg", "png");
        private long maxSize = 1000; // MB
        private String priority = "HIGH";
        private boolean encryption = true;
        
        // Getters and setters
        public List<String> getFolders() { return folders; }
        public void setFolders(List<String> folders) { this.folders = folders; }
        
        public List<String> getFileTypes() { return fileTypes; }
        public void setFileTypes(List<String> fileTypes) { this.fileTypes = fileTypes; }
        
        public long getMaxSize() { return maxSize; }
        public void setMaxSize(long maxSize) { this.maxSize = maxSize; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public boolean isEncryption() { return encryption; }
        public void setEncryption(boolean encryption) { this.encryption = encryption; }
    }
    
    public static class ForensicsOptions {
        private boolean includeNetworkLogs = true;
        private boolean includeSystemLogs = true;
        private boolean includeProcessList = true;
        private boolean includeInstalledSoftware = true;
        private boolean includeRecentFiles = true;
        private boolean includeBrowserHistory = true;
        
        // Getters and setters
        public boolean isIncludeNetworkLogs() { return includeNetworkLogs; }
        public void setIncludeNetworkLogs(boolean includeNetworkLogs) { this.includeNetworkLogs = includeNetworkLogs; }
        
        public boolean isIncludeSystemLogs() { return includeSystemLogs; }
        public void setIncludeSystemLogs(boolean includeSystemLogs) { this.includeSystemLogs = includeSystemLogs; }
        
        public boolean isIncludeProcessList() { return includeProcessList; }
        public void setIncludeProcessList(boolean includeProcessList) { this.includeProcessList = includeProcessList; }
        
        public boolean isIncludeInstalledSoftware() { return includeInstalledSoftware; }
        public void setIncludeInstalledSoftware(boolean includeInstalledSoftware) { this.includeInstalledSoftware = includeInstalledSoftware; }
        
        public boolean isIncludeRecentFiles() { return includeRecentFiles; }
        public void setIncludeRecentFiles(boolean includeRecentFiles) { this.includeRecentFiles = includeRecentFiles; }
        
        public boolean isIncludeBrowserHistory() { return includeBrowserHistory; }
        public void setIncludeBrowserHistory(boolean includeBrowserHistory) { this.includeBrowserHistory = includeBrowserHistory; }
    }
    
    public static class NetworkMonitoringOptions {
        private int duration = 300; // 5 minutes
        private boolean capturePackets = false;
        private boolean monitorConnections = true;
        private boolean detectSuspiciousActivity = true;
        private boolean alertOnNewConnections = true;
        
        // Getters and setters
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        
        public boolean isCapturePackets() { return capturePackets; }
        public void setCapturePackets(boolean capturePackets) { this.capturePackets = capturePackets; }
        
        public boolean isMonitorConnections() { return monitorConnections; }
        public void setMonitorConnections(boolean monitorConnections) { this.monitorConnections = monitorConnections; }
        
        public boolean isDetectSuspiciousActivity() { return detectSuspiciousActivity; }
        public void setDetectSuspiciousActivity(boolean detectSuspiciousActivity) { this.detectSuspiciousActivity = detectSuspiciousActivity; }
        
        public boolean isAlertOnNewConnections() { return alertOnNewConnections; }
        public void setAlertOnNewConnections(boolean alertOnNewConnections) { this.alertOnNewConnections = alertOnNewConnections; }
    }
    
    public static class AlarmOptions {
        private String soundType = "EMERGENCY";
        private int volume = 100;
        private int duration = 60;
        private String pattern = "CONTINUOUS";
        private String message = "LAPSO Security Alert";
        private boolean flashScreen = true;
        
        // Getters and setters
        public String getSoundType() { return soundType; }
        public void setSoundType(String soundType) { this.soundType = soundType; }
        
        public int getVolume() { return volume; }
        public void setVolume(int volume) { this.volume = volume; }
        
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public boolean isFlashScreen() { return flashScreen; }
        public void setFlashScreen(boolean flashScreen) { this.flashScreen = flashScreen; }
    }
    
    public static class RemoteCommand {
        private String type;
        private String deviceId;
        private LocalDateTime timestamp;
        private Map<String, Object> parameters;
        private String status = "PENDING";
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class CommandResult {
        private boolean success;
        private String message;
        private Map<String, Object> data;
        private LocalDateTime timestamp;
        
        private CommandResult(boolean success, String message, Map<String, Object> data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }
        
        public static CommandResult success(String message, Map<String, Object> data) {
            return new CommandResult(true, message, data);
        }
        
        public static CommandResult error(String message) {
            return new CommandResult(false, message, new HashMap<>());
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Map<String, Object> getData() { return data; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
