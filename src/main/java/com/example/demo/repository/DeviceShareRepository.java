package com.example.demo.repository;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceShare;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceShareRepository extends JpaRepository<DeviceShare, Long> {
    
    // Find all active shares for a user (where they are the recipient)
    @Query("SELECT ds FROM DeviceShare ds WHERE ds.sharedWith = :user AND ds.isActive = true AND (ds.expiresAt IS NULL OR ds.expiresAt > CURRENT_TIMESTAMP)")
    List<DeviceShare> findActiveSharesForUser(@Param("user") User user);
    
    // Find all shares owned by a user (where they are the owner)
    @Query("SELECT ds FROM DeviceShare ds WHERE ds.owner = :owner AND ds.isActive = true")
    List<DeviceShare> findSharesByOwner(@Param("owner") User owner);
    
    // Find all shares for a specific device
    @Query("SELECT ds FROM DeviceShare ds WHERE ds.device = :device AND ds.isActive = true")
    List<DeviceShare> findSharesByDevice(@Param("device") Device device);
    
    // Find specific share between users for a device
    @Query("SELECT ds FROM DeviceShare ds WHERE ds.device = :device AND ds.owner = :owner AND ds.sharedWith = :sharedWith AND ds.isActive = true")
    Optional<DeviceShare> findShareBetweenUsers(@Param("device") Device device, @Param("owner") User owner, @Param("sharedWith") User sharedWith);
    
    // Check if a user has access to a device (either as owner or through sharing)
    @Query("SELECT COUNT(ds) > 0 FROM DeviceShare ds WHERE ds.device = :device AND ds.sharedWith = :user AND ds.isActive = true AND (ds.expiresAt IS NULL OR ds.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasAccessToDevice(@Param("user") User user, @Param("device") Device device);
    
    // Find all devices a user can access (including shared ones)
    @Query("SELECT DISTINCT ds.device FROM DeviceShare ds WHERE ds.sharedWith = :user AND ds.isActive = true AND (ds.expiresAt IS NULL OR ds.expiresAt > CURRENT_TIMESTAMP)")
    List<Device> findAccessibleDevices(@Param("user") User user);
    
    // Find expired shares for cleanup
    @Query("SELECT ds FROM DeviceShare ds WHERE ds.expiresAt IS NOT NULL AND ds.expiresAt < CURRENT_TIMESTAMP AND ds.isActive = true")
    List<DeviceShare> findExpiredShares();
}
