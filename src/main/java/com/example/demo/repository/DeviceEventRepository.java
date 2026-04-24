package com.example.demo.repository;

import com.example.demo.model.DeviceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceEventRepository extends JpaRepository<DeviceEvent, Long> {
    List<DeviceEvent> findByDeviceDeviceId(String deviceId);
}
