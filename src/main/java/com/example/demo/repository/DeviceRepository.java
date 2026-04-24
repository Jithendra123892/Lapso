package com.example.demo.repository;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    List<Device> findByUser(User user);
    
    @Query("SELECT d FROM Device d JOIN FETCH d.user WHERE d.user.email = :email")
    List<Device> findByUserEmail(@Param("email") String email);
    
    Optional<Device> findByDeviceId(String deviceId);
    Optional<Device> findByDeviceIdAndUser(String deviceId, User user);
    List<Device> findByIsOnlineTrue();
    List<Device> findByIsOnlineFalse();
    List<Device> findByTheftDetectedTrue();
    
    @Query("SELECT d FROM Device d JOIN FETCH d.user WHERE d.user.email = :email AND d.isOnline = :online")
    List<Device> findByUserEmailAndOnlineStatus(@Param("email") String email, @Param("online") Boolean online);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.user.email = :email")
    Long countByUserEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.user.email = :email AND d.isOnline = true")
    Long countOnlineByUserEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.user.email = :email AND d.theftDetected = true")
    Long countStolenByUserEmail(@Param("email") String email);
    
    @Query("SELECT d FROM Device d WHERE d.latitude IS NOT NULL AND d.longitude IS NOT NULL")
    List<Device> findDevicesWithLocation();
    
    @Query("SELECT d FROM Device d JOIN FETCH d.user WHERE d.user.email = :email AND d.latitude IS NOT NULL AND d.longitude IS NOT NULL")
    List<Device> findByUserEmailWithLocation(@Param("email") String email);
    
    @Query("SELECT d FROM Device d WHERE d.lastSeen < :threshold")
    List<Device> findOfflineDevices(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT d FROM Device d WHERE d.batteryLevel < :level AND d.batteryLevel IS NOT NULL")
    List<Device> findLowBatteryDevices(@Param("level") Integer level);
    
    // Count methods for statistics
    long countByIsOnlineTrue();
    long countByIsOnlineFalse();
    
    // Admin cleanup methods
    @Modifying
    @Transactional
    @Query("DELETE FROM Device d WHERE d.deviceId LIKE CONCAT(:prefix, '%')")
    long deleteByDeviceIdStartingWith(@Param("prefix") String prefix);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Device d WHERE d.deviceId IN :deviceIds")
    int deleteByDeviceIdIn(@Param("deviceIds") List<String> deviceIds);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Device d WHERE d.deviceName IN :deviceNames")
    int deleteByDeviceNameIn(@Param("deviceNames") List<String> deviceNames);
}
