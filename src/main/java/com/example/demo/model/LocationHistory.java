package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_history", indexes = {
    @Index(name = "idx_device_timestamp", columnList = "device_id,timestamp"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class LocationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    private Double latitude;
    private Double longitude;
    private Double altitude;
    private String address;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    private Double accuracy; // in meters
    
    @Column(name = "location_source")
    private String locationSource; // GPS, WIFI, CELL, IP
    
    private Double speed; // km/h
    
    @Column(name = "battery_level")
    private Integer batteryLevel;
    
    @Column(name = "is_online")
    private Boolean isOnline;

    public LocationHistory() {
        this.timestamp = LocalDateTime.now();
    }

    public LocationHistory(Device device, Double latitude, Double longitude, String address) {
        this();
        this.device = device;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public String getLocationSource() { return locationSource; }
    public void setLocationSource(String locationSource) { this.locationSource = locationSource; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Integer getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
}
