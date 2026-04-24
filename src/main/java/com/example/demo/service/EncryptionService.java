package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 🔐 PRODUCTION ENCRYPTION SERVICE
 * AES-256-GCM encryption for sensitive data (location, personal info)
 */
@Service
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    @Value("${app.encryption.key:}")
    private String encryptionKeyBase64;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Get or generate encryption key
     */
    private SecretKey getEncryptionKey() {
        if (encryptionKeyBase64 != null && !encryptionKeyBase64.isEmpty()) {
            byte[] keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);
            return new SecretKeySpec(keyBytes, ALGORITHM);
        }
        
        // Generate new key if not configured
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SecretKey key = keyGenerator.generateKey();
            
            // Log the key for configuration (in production, store securely)
            String keyBase64 = Base64.getEncoder().encodeToString(key.getEncoded());
            System.out.println("🔑 Generated encryption key (add to config): " + keyBase64);
            
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
    
    /**
     * Encrypt sensitive data
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        try {
            SecretKey key = getEncryptionKey();
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            
            // Encrypt data
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            System.err.println("❌ Encryption failed: " + e.getMessage());
            return plaintext; // Return original if encryption fails
        }
    }
    
    /**
     * Decrypt sensitive data
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        
        try {
            SecretKey key = getEncryptionKey();
            
            // Decode base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            
            // Decrypt data
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            System.err.println("❌ Decryption failed: " + e.getMessage());
            return encryptedData; // Return original if decryption fails
        }
    }
    
    /**
     * Encrypt location coordinates
     */
    public String encryptLocation(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        
        String locationString = latitude + "," + longitude;
        return encrypt(locationString);
    }
    
    /**
     * Decrypt location coordinates
     */
    public Double[] decryptLocation(String encryptedLocation) {
        if (encryptedLocation == null || encryptedLocation.isEmpty()) {
            return null;
        }
        
        try {
            String decrypted = decrypt(encryptedLocation);
            String[] parts = decrypted.split(",");
            if (parts.length == 2) {
                return new Double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
            }
        } catch (Exception e) {
            System.err.println("❌ Location decryption failed: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Hash sensitive data (one-way)
     */
    public String hash(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            System.err.println("❌ Hashing failed: " + e.getMessage());
            return data;
        }
    }
    
    /**
     * Generate secure random token
     */
    public String generateSecureToken(int length) {
        byte[] tokenBytes = new byte[length];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Check if encryption is properly configured
     */
    public boolean isEncryptionConfigured() {
        return encryptionKeyBase64 != null && !encryptionKeyBase64.isEmpty();
    }
}
