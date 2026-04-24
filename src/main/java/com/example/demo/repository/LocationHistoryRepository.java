package com.example.demo.repository;

import com.example.demo.model.LocationHistory;
import com.example.demo.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    List<LocationHistory> findByDeviceDeviceId(String deviceId);
    List<LocationHistory> findByDeviceAndTimestampAfterOrderByTimestampDesc(Device device, LocalDateTime since);
    List<LocationHistory> findByDeviceOrderByTimestampDesc(Device device);
}
