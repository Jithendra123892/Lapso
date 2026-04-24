package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.RemoteCommand;
import com.example.demo.model.User;
import com.example.demo.repository.RemoteCommandRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/remote-commands")
@CrossOrigin(origins = "*")
public class RemoteCommandController {

    @Autowired
    private RemoteCommandRepository commandRepository;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Send a remote command to a device
    @PostMapping("/send")
    @Transactional
    public ResponseEntity<?> sendCommand(@Valid @RequestBody SendCommandRequest request) {
        try {
            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Not authenticated"));
            }

            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(403).body(Map.of("status", "error", "message", "User not found"));
            }

            // Find device and verify ownership
            Device device = deviceService.findByDeviceId(request.getDeviceId()).orElse(null);
            if (device == null || !device.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "status", "error",
                    "message", "Device not found or unauthorized"
                ));
            }

            // Validate command type
            String[] validCommands = {"LOCK", "UNLOCK", "WIPE", "SCREENSHOT", "CAMERA", "ALARM", "MESSAGE", "LOCATE"};
            if (!Arrays.asList(validCommands).contains(request.getCommandType().toUpperCase())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid command type"
                ));
            }

            // Create command
            RemoteCommand command = new RemoteCommand();
            command.setDevice(device);
            command.setUser(user);
            command.setCommandType(request.getCommandType().toUpperCase());
            command.setCommandParams(request.getCommandParams());
            command.setStatus("PENDING");
            command.setPriority(request.getPriority() != null ? request.getPriority() : 5);
            command.setCreatedAt(LocalDateTime.now());
            
            // Set expiration (default 24 hours)
            int expiryHours = request.getExpiryHours() != null ? request.getExpiryHours() : 24;
            command.setExpiresAt(LocalDateTime.now().plusHours(expiryHours));

            commandRepository.save(command);

            // Send WebSocket notification
            messagingTemplate.convertAndSend("/topic/commands/" + device.getDeviceId(), Map.of(
                "commandId", command.getId(),
                "commandType", command.getCommandType(),
                "status", "PENDING"
            ));

            System.out.println("✅ Remote command " + command.getCommandType() + " created for device: " + device.getDeviceId());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Command sent successfully",
                "commandId", command.getId(),
                "commandType", command.getCommandType()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error sending command: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to send command: " + e.getMessage()
            ));
        }
    }

    // Agent polls for pending commands
    @GetMapping("/poll/{deviceId}")
    public ResponseEntity<?> pollCommands(@PathVariable String deviceId, @RequestParam String userEmail) {
        try {
            // Verify user and device
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(403).body(Map.of("status", "error", "message", "User not found"));
            }

            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null || !device.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "status", "error",
                    "message", "Device not found or unauthorized"
                ));
            }

            // Get pending commands
            List<RemoteCommand> pendingCommands = commandRepository
                .findByDeviceAndStatusOrderByPriorityDescCreatedAtAsc(device, "PENDING");

            // Filter expired commands
            LocalDateTime now = LocalDateTime.now();
            List<Map<String, Object>> commands = new ArrayList<>();
            
            for (RemoteCommand cmd : pendingCommands) {
                if (cmd.getExpiresAt() != null && cmd.getExpiresAt().isBefore(now)) {
                    cmd.setStatus("EXPIRED");
                    commandRepository.save(cmd);
                    continue;
                }

                commands.add(Map.of(
                    "id", cmd.getId(),
                    "commandType", cmd.getCommandType(),
                    "commandParams", cmd.getCommandParams() != null ? cmd.getCommandParams() : "",
                    "priority", cmd.getPriority(),
                    "createdAt", cmd.getCreatedAt().toString()
                ));

                // Mark as SENT
                cmd.setStatus("SENT");
                cmd.setSentAt(LocalDateTime.now());
                commandRepository.save(cmd);
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "commands", commands
            ));

        } catch (Exception e) {
            System.err.println("❌ Error polling commands: " + e.getMessage());
            return ResponseEntity.ok(Map.of("status", "success", "commands", List.of()));
        }
    }

    // Agent reports command execution result
    @PostMapping("/result")
    @Transactional
    public ResponseEntity<?> reportResult(@Valid @RequestBody CommandResultRequest request) {
        try {
            RemoteCommand command = commandRepository.findById(request.getCommandId()).orElse(null);
            if (command == null) {
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Command not found"));
            }

            command.setStatus(request.getSuccess() ? "COMPLETED" : "FAILED");
            command.setExecutedAt(LocalDateTime.now());
            command.setCompletedAt(LocalDateTime.now());
            command.setResult(request.getResult());
            
            if (request.getScreenshotUrl() != null) {
                command.setScreenshotUrl(request.getScreenshotUrl());
            }

            commandRepository.save(command);

            // Send WebSocket notification
            messagingTemplate.convertAndSend("/topic/commands/" + command.getDevice().getDeviceId(), Map.of(
                "commandId", command.getId(),
                "commandType", command.getCommandType(),
                "status", command.getStatus(),
                "result", request.getResult()
            ));

            System.out.println("✅ Command " + command.getId() + " " + command.getStatus() + ": " + request.getResult());

            return ResponseEntity.ok(Map.of("status", "success", "message", "Result recorded"));

        } catch (Exception e) {
            System.err.println("❌ Error recording command result: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to record result: " + e.getMessage()
            ));
        }
    }

    // Get command history for a device
    @GetMapping("/history/{deviceId}")
    public ResponseEntity<?> getCommandHistory(@PathVariable String deviceId) {
        try {
            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Not authenticated"));
            }

            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(403).body(Map.of("status", "error", "message", "User not found"));
            }

            Device device = deviceService.findByDeviceId(deviceId).orElse(null);
            if (device == null || !device.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "status", "error",
                    "message", "Device not found or unauthorized"
                ));
            }

            List<RemoteCommand> commands = commandRepository.findByDeviceOrderByCreatedAtDesc(device);

            List<Map<String, Object>> history = new ArrayList<>();
            for (RemoteCommand cmd : commands) {
                history.add(Map.of(
                    "id", cmd.getId(),
                    "commandType", cmd.getCommandType(),
                    "status", cmd.getStatus(),
                    "createdAt", cmd.getCreatedAt().toString(),
                    "completedAt", cmd.getCompletedAt() != null ? cmd.getCompletedAt().toString() : "",
                    "result", cmd.getResult() != null ? cmd.getResult() : "",
                    "screenshotUrl", cmd.getScreenshotUrl() != null ? cmd.getScreenshotUrl() : ""
                ));
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "commands", history
            ));

        } catch (Exception e) {
            System.err.println("❌ Error getting command history: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to get history: " + e.getMessage()
            ));
        }
    }

    // DTOs
    public static class SendCommandRequest {
        @NotBlank(message = "Device ID is required")
        private String deviceId;
        
        @NotBlank(message = "Command type is required")
        private String commandType;
        
        private String commandParams;
        private Integer priority;
        private Integer expiryHours;

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getCommandType() { return commandType; }
        public void setCommandType(String commandType) { this.commandType = commandType; }

        public String getCommandParams() { return commandParams; }
        public void setCommandParams(String commandParams) { this.commandParams = commandParams; }

        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }

        public Integer getExpiryHours() { return expiryHours; }
        public void setExpiryHours(Integer expiryHours) { this.expiryHours = expiryHours; }
    }

    public static class CommandResultRequest {
        private Long commandId;
        private Boolean success;
        private String result;
        private String screenshotUrl;

        public Long getCommandId() { return commandId; }
        public void setCommandId(Long commandId) { this.commandId = commandId; }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getScreenshotUrl() { return screenshotUrl; }
        public void setScreenshotUrl(String screenshotUrl) { this.screenshotUrl = screenshotUrl; }
    }
}
