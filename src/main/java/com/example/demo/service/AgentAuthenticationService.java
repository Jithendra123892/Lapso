package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentAuthenticationService {

    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Store device authentication tokens (in production, use Redis or database)
    private final Map<String, DeviceAuthToken> deviceTokens = new ConcurrentHashMap<>();
    
    // Rate limiting for authentication attempts
    private final Map<String, AuthAttempt> authAttempts = new ConcurrentHashMap<>();
    
    private static final int MAX_AUTH_ATTEMPTS = 5;
    private static final long AUTH_LOCKOUT_MINUTES = 15;

    /**
     * Authenticate agent request with device ID and user email
     * SECURITY: Ensures only legitimate devices can send data
     */
    public AuthenticationResult authenticateAgent(String deviceId, String userEmail, String clientIp) {
        try {
            // Rate limiting check
            if (isRateLimited(clientIp)) {
                System.err.println("🚨 SECURITY: Rate limited IP: " + clientIp);
                return AuthenticationResult.rateLimited();
            }
            
            // Validate input parameters
            if (deviceId == null || deviceId.trim().isEmpty()) {
                recordFailedAttempt(clientIp);
                return AuthenticationResult.failed("Device ID is required");
            }
            
            if (userEmail == null || userEmail.trim().isEmpty()) {
                recordFailedAttempt(clientIp);
                return AuthenticationResult.failed("User email is required");
            }
            
            // Find user
            Optional<User> userOpt = userRepository.findByEmail(userEmail.trim());
            if (userOpt.isEmpty()) {
                recordFailedAttempt(clientIp);
                System.err.println("🚨 SECURITY: Authentication failed - User not found: " + userEmail);
                return AuthenticationResult.failed("User not found");
            }
            
            User user = userOpt.get();
            
            // Find device
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
            if (deviceOpt.isEmpty()) {
                // Device doesn't exist - this might be a new device registration
                System.out.println("ℹ️ SECURITY: New device registration attempt: " + deviceId + " for user: " + userEmail);
                return AuthenticationResult.newDevice(user);
            }
            
            Device device = deviceOpt.get();
            
            // CRITICAL: Verify device ownership
            if (!device.getUser().getId().equals(user.getId())) {
                recordFailedAttempt(clientIp);
                System.err.println("🚨 SECURITY VIOLATION: Device " + deviceId + 
                                 " belongs to " + device.getUser().getEmail() + 
                                 " but agent claims to be " + userEmail);
                return AuthenticationResult.failed("Device ownership violation");
            }
            
            // Authentication successful
            clearFailedAttempts(clientIp);
            System.out.println("✅ SECURITY: Agent authenticated - Device: " + deviceId + ", User: " + userEmail);
            return AuthenticationResult.success(user, device);
            
        } catch (Exception e) {
            recordFailedAttempt(clientIp);
            System.err.println("🚨 SECURITY: Agent authentication error: " + e.getMessage());
            return AuthenticationResult.failed("Authentication error");
        }
    }
    
    /**
     * Generate secure authentication token for device
     */
    public String generateDeviceToken(String deviceId, String userEmail) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] tokenBytes = new byte[32];
            random.nextBytes(tokenBytes);
            
            String token = Base64.getEncoder().encodeToString(tokenBytes);
            
            // Store token with expiration
            DeviceAuthToken authToken = new DeviceAuthToken(
                token, deviceId, userEmail, LocalDateTime.now().plusHours(24)
            );
            
            deviceTokens.put(deviceId, authToken);
            
            System.out.println("🔐 SECURITY: Generated auth token for device: " + deviceId);
            return token;
            
        } catch (Exception e) {
            System.err.println("🚨 SECURITY: Token generation failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate device authentication token
     */
    public boolean validateDeviceToken(String deviceId, String token) {
        try {
            DeviceAuthToken authToken = deviceTokens.get(deviceId);
            if (authToken == null) {
                System.err.println("🚨 SECURITY: No auth token found for device: " + deviceId);
                return false;
            }
            
            if (authToken.isExpired()) {
                deviceTokens.remove(deviceId);
                System.err.println("🚨 SECURITY: Expired auth token for device: " + deviceId);
                return false;
            }
            
            if (!authToken.getToken().equals(token)) {
                System.err.println("🚨 SECURITY: Invalid auth token for device: " + deviceId);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("🚨 SECURITY: Token validation error: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isRateLimited(String clientIp) {
        AuthAttempt attempt = authAttempts.get(clientIp);
        if (attempt == null) {
            return false;
        }
        
        // Check if lockout period has expired
        if (attempt.isLockoutExpired()) {
            authAttempts.remove(clientIp);
            return false;
        }
        
        return attempt.getFailedAttempts() >= MAX_AUTH_ATTEMPTS;
    }
    
    private void recordFailedAttempt(String clientIp) {
        AuthAttempt attempt = authAttempts.computeIfAbsent(clientIp, k -> new AuthAttempt());
        attempt.recordFailedAttempt();
        
        if (attempt.getFailedAttempts() >= MAX_AUTH_ATTEMPTS) {
            System.err.println("🚨 SECURITY: IP " + clientIp + " locked out due to too many failed attempts");
        }
    }
    
    private void clearFailedAttempts(String clientIp) {
        authAttempts.remove(clientIp);
    }
    
    // Inner classes for authentication results and tracking
    public static class AuthenticationResult {
        private final boolean success;
        private final boolean newDevice;
        private final boolean rateLimited;
        private final String errorMessage;
        private final User user;
        private final Device device;
        
        private AuthenticationResult(boolean success, boolean newDevice, boolean rateLimited, 
                                   String errorMessage, User user, Device device) {
            this.success = success;
            this.newDevice = newDevice;
            this.rateLimited = rateLimited;
            this.errorMessage = errorMessage;
            this.user = user;
            this.device = device;
        }
        
        public static AuthenticationResult success(User user, Device device) {
            return new AuthenticationResult(true, false, false, null, user, device);
        }
        
        public static AuthenticationResult newDevice(User user) {
            return new AuthenticationResult(true, true, false, null, user, null);
        }
        
        public static AuthenticationResult failed(String errorMessage) {
            return new AuthenticationResult(false, false, false, errorMessage, null, null);
        }
        
        public static AuthenticationResult rateLimited() {
            return new AuthenticationResult(false, false, true, "Rate limited", null, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public boolean isNewDevice() { return newDevice; }
        public boolean isRateLimited() { return rateLimited; }
        public String getErrorMessage() { return errorMessage; }
        public User getUser() { return user; }
        public Device getDevice() { return device; }
    }
    
    private static class DeviceAuthToken {
        private final String token;
        private final String deviceId;
        private final String userEmail;
        private final LocalDateTime expiresAt;
        
        public DeviceAuthToken(String token, String deviceId, String userEmail, LocalDateTime expiresAt) {
            this.token = token;
            this.deviceId = deviceId;
            this.userEmail = userEmail;
            this.expiresAt = expiresAt;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
        
        public String getToken() { return token; }
        public String getDeviceId() { return deviceId; }
        public String getUserEmail() { return userEmail; }
    }
    
    private static class AuthAttempt {
        private int failedAttempts = 0;
        private LocalDateTime lockoutUntil;
        
        public void recordFailedAttempt() {
            failedAttempts++;
            if (failedAttempts >= MAX_AUTH_ATTEMPTS) {
                lockoutUntil = LocalDateTime.now().plusMinutes(AUTH_LOCKOUT_MINUTES);
            }
        }
        
        public boolean isLockoutExpired() {
            return lockoutUntil == null || LocalDateTime.now().isAfter(lockoutUntil);
        }
        
        public int getFailedAttempts() { return failedAttempts; }
    }
    
    /**
     * Initialize Agent Authentication Service
     */
    public void initialize() {
        System.out.println("✅ Agent Authentication Service initialized");
    }
}
