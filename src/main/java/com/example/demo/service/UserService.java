package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 🔐 PRODUCTION USER SERVICE
 * Handles user management, authentication, password security
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private final SecureRandom secureRandom = new SecureRandom();
    
    // Store verification tokens (in production, use Redis or database)
    private final Map<String, TokenData> verificationTokens = new HashMap<>();
    private final Map<String, TokenData> passwordResetTokens = new HashMap<>();
    
    /**
     * Create new user with encrypted password
     */
    public User createUser(String email, String name, String password) {
        // Hash password with BCrypt
        String hashedPassword = passwordEncoder.encode(password);
        
        User user = new User();
        user.setEmail(email.toLowerCase().trim());
        user.setName(name.trim());
        user.setPassword(hashedPassword);
        user.setIsActive(false); // Require email verification
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        System.out.println("✅ User created: " + email + " (ID: " + savedUser.getId() + ")");
        
        return savedUser;
    }
    
    /**
     * Authenticate user with email and password
     */
    public User authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if account is active
            if (!user.getIsActive()) {
                throw new RuntimeException("Account not verified. Please check your email for verification link.");
            }
            
            // Verify password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Update last login
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                
                System.out.println("✅ User authenticated: " + email);
                return user;
            }
        }
        
        System.out.println("❌ Authentication failed for: " + email);
        return null;
    }
    
    /**
     * Generate email verification token
     */
    public String generateVerificationToken(User user) {
        String token = generateSecureToken();
        TokenData tokenData = new TokenData(user.getId(), LocalDateTime.now().plusHours(24));
        verificationTokens.put(token, tokenData);
        
        System.out.println("📧 Verification token generated for: " + user.getEmail());
        return token;
    }
    
    /**
     * Verify email with token
     */
    public boolean verifyEmail(String token) {
        TokenData tokenData = verificationTokens.get(token);
        
        if (tokenData != null && tokenData.getExpiresAt().isAfter(LocalDateTime.now())) {
            Optional<User> userOpt = userRepository.findById(tokenData.getUserId());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsActive(true);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                // Remove used token
                verificationTokens.remove(token);
                
                System.out.println("✅ Email verified for: " + user.getEmail());
                return true;
            }
        }
        
        System.out.println("❌ Email verification failed for token: " + token);
        return false;
    }
    
    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(User user) {
        String token = generateSecureToken();
        TokenData tokenData = new TokenData(user.getId(), LocalDateTime.now().plusHours(1)); // 1 hour expiry
        passwordResetTokens.put(token, tokenData);
        
        System.out.println("🔑 Password reset token generated for: " + user.getEmail());
        return token;
    }
    
    /**
     * Reset password with token
     */
    public boolean resetPassword(String token, String newPassword) {
        TokenData tokenData = passwordResetTokens.get(token);
        
        if (tokenData != null && tokenData.getExpiresAt().isAfter(LocalDateTime.now())) {
            Optional<User> userOpt = userRepository.findById(tokenData.getUserId());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                // Remove used token
                passwordResetTokens.remove(token);
                
                System.out.println("✅ Password reset for: " + user.getEmail());
                return true;
            }
        }
        
        System.out.println("❌ Password reset failed for token: " + token);
        return false;
    }
    
    /**
     * Change password for authenticated user
     */
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Verify current password
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                System.out.println("✅ Password changed for: " + email);
                return true;
            }
        }
        
        System.out.println("❌ Password change failed for: " + email);
        return false;
    }
    
    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }
    
    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);
    }
    
    /**
     * Get current authenticated user email
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
    
    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return email != null ? findByEmail(email) : null;
    }
    
    /**
     * Initialize user service
     */
    public void initialize() {
        System.out.println("✅ User service initialized");
    }
    
    /**
     * Update user profile
     */
    public User updateUserProfile(String email, String name, String pictureUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(name != null ? name.trim() : user.getName());
            user.setPictureUrl(pictureUrl != null ? pictureUrl.trim() : user.getPictureUrl());
            user.setUpdatedAt(LocalDateTime.now());
            
            User updatedUser = userRepository.save(user);
            System.out.println("✅ Profile updated for: " + email);
            return updatedUser;
        }
        
        return null;
    }
    
    /**
     * Deactivate user account
     */
    public boolean deactivateUser(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            System.out.println("⚠️ User deactivated: " + email);
            return true;
        }
        
        return false;
    }
    
    /**
     * Generate secure random token
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Clean up expired tokens (should be called periodically)
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        verificationTokens.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));
        passwordResetTokens.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));
        
        System.out.println("🧹 Expired tokens cleaned up");
    }
    
    /**
     * Token data class
     */
    private static class TokenData {
        private final Long userId;
        private final LocalDateTime expiresAt;
        
        public TokenData(Long userId, LocalDateTime expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
        
        public Long getUserId() { return userId; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
    }
    
    /**
     * Register manual user
     */
    public User registerManualUser(String email, String password, String name) {
        try {
            // Check if user already exists
            if (userRepository.findByEmail(email.toLowerCase().trim()).isPresent()) {
                System.out.println("⚠️ User already exists: " + email);
                return null;
            }
            
            // Create new user
            User user = new User();
            user.setEmail(email.toLowerCase().trim());
            user.setName(name != null ? name.trim() : "User");
            String hashedPassword = passwordEncoder.encode(password);
            user.setPassword(hashedPassword);  // Set both password fields
            user.setPasswordHash(hashedPassword);
            user.setProvider("manual");
            user.setIsActive(true);
            user.setIsEmailVerified(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            System.out.println("✅ Manual user registered: " + email);
            return savedUser;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to register manual user: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Update last login
     */
    public void updateLastLogin(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastLoginAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to update last login: " + e.getMessage());
        }
    }
    
    /**
     * Find or create user
     */
    public User findOrCreateUser(String email, String name, String provider, String providerId) {
        try {
            Optional<User> existingUser = userRepository.findByEmail(email.toLowerCase().trim());
            if (existingUser.isPresent()) {
                return existingUser.get();
            }
            
            // Create new user
            User user = new User();
            user.setEmail(email.toLowerCase().trim());
            user.setName(name != null ? name.trim() : "User");
            user.setProvider(provider != null ? provider : "manual");
            user.setProviderId(providerId);
            user.setIsActive(true);
            user.setIsEmailVerified(true); // Auto-verify for OAuth users
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            System.out.println("✅ New user created: " + email);
            return savedUser;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to find or create user: " + e.getMessage());
            return null;
        }
    }
}
