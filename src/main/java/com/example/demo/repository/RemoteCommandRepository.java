package com.example.demo.repository;

import com.example.demo.model.RemoteCommand;
import com.example.demo.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RemoteCommandRepository extends JpaRepository<RemoteCommand, Long> {
    
    // Find pending commands for a device
    List<RemoteCommand> findByDeviceAndStatusOrderByPriorityDescCreatedAtAsc(Device device, String status);
    
    // Find all commands for a device
    List<RemoteCommand> findByDeviceOrderByCreatedAtDesc(Device device);
    
    // Find commands by device ID
    List<RemoteCommand> findByDevice_DeviceIdOrderByCreatedAtDesc(String deviceId);
    
    // Find expired commands
    List<RemoteCommand> findByStatusAndExpiresAtBefore(String status, LocalDateTime now);
    
    // Count pending commands for device
    Long countByDeviceAndStatus(Device device, String status);
}
