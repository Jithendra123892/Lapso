package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceShare;
import com.example.demo.model.User;
import com.example.demo.repository.DeviceShareRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeviceShareService {
    
    @Autowired
    private DeviceShareRepository deviceShareRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Share a device with another user by email
     */
    public DeviceShare shareDevice(Device device, User owner, String shareWithEmail, 
                                 DeviceShare.PermissionLevel permissionLevel, 
                                 String message, LocalDateTime expiresAt) {
        
        // Find the user to share with
        Optional<User> shareWithUser = userRepository.findByEmail(shareWithEmail);
        if (shareWithUser.isEmpty()) {
            throw new IllegalArgumentException("User with email " + shareWithEmail + " not found");
        }
        
        // Check if already shared
        Optional<DeviceShare> existingShare = deviceShareRepository.findShareBetweenUsers(
            device, owner, shareWithUser.get());
        
        if (existingShare.isPresent()) {
            // Update existing share
            DeviceShare share = existingShare.get();
            share.setPermissionLevel(permissionLevel);
            share.setSharedMessage(message);
            share.setExpiresAt(expiresAt);
            share.setIsActive(true);
            return deviceShareRepository.save(share);
        } else {
            // Create new share
            DeviceShare newShare = new DeviceShare(device, owner, shareWithUser.get(), permissionLevel);
            newShare.setSharedMessage(message);
            newShare.setExpiresAt(expiresAt);
            return deviceShareRepository.save(newShare);
        }
    }
    
    /**
     * Get all devices accessible to a user (owned + shared)
     */
    public List<Device> getAllAccessibleDevices(User user) {
        // Get user's own devices
        List<Device> ownDevices = user.getDevices();
        
        // Get shared devices
        List<Device> sharedDevices = deviceShareRepository.findAccessibleDevices(user);
        
        // Combine and return unique devices
        List<Device> allDevices = new java.util.ArrayList<>(ownDevices);
        for (Device sharedDevice : sharedDevices) {
            if (!allDevices.contains(sharedDevice)) {
                allDevices.add(sharedDevice);
            }
        }
        return allDevices;
    }
    
    /**
     * Get all shares for devices owned by a user
     */
    public List<DeviceShare> getSharesByOwner(User owner) {
        return deviceShareRepository.findSharesByOwner(owner);
    }
    
    /**
     * Get all shares where user is the recipient
     */
    public List<DeviceShare> getSharesForUser(User user) {
        return deviceShareRepository.findActiveSharesForUser(user);
    }
    
    /**
     * Check if user has access to a device
     */
    public boolean hasAccessToDevice(User user, Device device) {
        // Check if user owns the device
        if (device.getUser().getId().equals(user.getId())) {
            return true;
        }
        
        // Check if device is shared with user
        return deviceShareRepository.hasAccessToDevice(user, device);
    }
    
    /**
     * Get user's permission level for a device
     */
    public DeviceShare.PermissionLevel getPermissionLevel(User user, Device device) {
        // If user owns the device, they have full admin access
        if (device.getUser().getId().equals(user.getId())) {
            return DeviceShare.PermissionLevel.FULL_ADMIN;
        }
        
        // Check shared permissions
        List<DeviceShare> shares = deviceShareRepository.findActiveSharesForUser(user);
        Optional<DeviceShare> deviceShare = shares.stream()
            .filter(share -> share.getDevice().getId().equals(device.getId()))
            .findFirst();
        
        return deviceShare.map(DeviceShare::getPermissionLevel)
            .orElse(null); // No access
    }
    
    /**
     * Revoke device share
     */
    public void revokeShare(Long shareId, User requestingUser) {
        Optional<DeviceShare> shareOpt = deviceShareRepository.findById(shareId);
        if (shareOpt.isEmpty()) {
            throw new IllegalArgumentException("Share not found");
        }
        
        DeviceShare share = shareOpt.get();
        
        // Only owner or recipient can revoke
        if (!share.getOwner().getId().equals(requestingUser.getId()) && 
            !share.getSharedWith().getId().equals(requestingUser.getId())) {
            throw new SecurityException("Not authorized to revoke this share");
        }
        
        share.setIsActive(false);
        deviceShareRepository.save(share);
    }
    
    /**
     * Cleanup expired shares
     */
    @Transactional
    public void cleanupExpiredShares() {
        List<DeviceShare> expiredShares = deviceShareRepository.findExpiredShares();
        for (DeviceShare share : expiredShares) {
            share.setIsActive(false);
        }
        deviceShareRepository.saveAll(expiredShares);
    }
    
    /**
     * Get share details by ID
     */
    public Optional<DeviceShare> getShareById(Long shareId) {
        return deviceShareRepository.findById(shareId);
    }
    
    /**
     * Get all shares for a specific device
     */
    public List<DeviceShare> getSharesForDevice(Device device) {
        return deviceShareRepository.findSharesByDevice(device);
    }
}
