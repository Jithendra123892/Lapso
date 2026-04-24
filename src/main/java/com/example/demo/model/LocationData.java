package com.example.demo.model;

import java.time.LocalDateTime;

public class LocationData {
    private String deviceId;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private String source;
    private String address;
    private LocalDateTime timestamp;
    
    // Advanced location data fields
    private Double confidence;
    private Integer satelliteCount;
    private Integer signalStrength;
    private Integer accessPointCount;
    private Integer beaconCount;
    private Integer cellTowerCount;
    private Double speed;
    private String transportationMode;
    private Boolean isIndoor;
    private Double altitude;
    
    // Constructors
    public LocationData() {}
    
    public LocationData(String deviceId, Double latitude, Double longitude, Double accuracy, String source, String address, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.source = source;
        this.address = address;
        this.timestamp = timestamp;
        this.confidence = 0.8; // Default confidence
        this.isIndoor = false; // Default outdoor
    }
    
    // Builder pattern for easy construction
    public static LocationDataBuilder builder() {
        return new LocationDataBuilder();
    }
    
    public static class LocationDataBuilder {
        private String deviceId;
        private Double latitude;
        private Double longitude;
        private Double accuracy;
        private String source;
        private String address;
        private LocalDateTime timestamp;
        private Double confidence;
        private Integer satelliteCount;
        private Integer signalStrength;
        
        public LocationDataBuilder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }
        
        public LocationDataBuilder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }
        
        public LocationDataBuilder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }
        
        public LocationDataBuilder accuracy(Double accuracy) {
            this.accuracy = accuracy;
            return this;
        }
        
        public LocationDataBuilder source(String source) {
            this.source = source;
            return this;
        }
        
        public LocationDataBuilder address(String address) {
            this.address = address;
            return this;
        }
        
        public LocationDataBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public LocationDataBuilder confidence(Double confidence) {
            this.confidence = confidence;
            return this;
        }
        
        public LocationDataBuilder satelliteCount(Integer satelliteCount) {
            this.satelliteCount = satelliteCount;
            return this;
        }
        
        public LocationDataBuilder signalStrength(Integer signalStrength) {
            this.signalStrength = signalStrength;
            return this;
        }
        
        public LocationData build() {
            LocationData data = new LocationData(deviceId, latitude, longitude, accuracy, source, address, timestamp);
            data.setConfidence(confidence);
            data.setSatelliteCount(satelliteCount);
            data.setSignalStrength(signalStrength);
            return data;
        }
    }
    
    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    // Advanced getters and setters
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public Integer getSatelliteCount() { return satelliteCount; }
    public void setSatelliteCount(Integer satelliteCount) { this.satelliteCount = satelliteCount; }
    
    public Integer getSignalStrength() { return signalStrength; }
    public void setSignalStrength(Integer signalStrength) { this.signalStrength = signalStrength; }
    
    public Integer getAccessPointCount() { return accessPointCount; }
    public void setAccessPointCount(Integer accessPointCount) { this.accessPointCount = accessPointCount; }
    
    public Integer getBeaconCount() { return beaconCount; }
    public void setBeaconCount(Integer beaconCount) { this.beaconCount = beaconCount; }
    
    public Integer getCellTowerCount() { return cellTowerCount; }
    public void setCellTowerCount(Integer cellTowerCount) { this.cellTowerCount = cellTowerCount; }
    
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    
    public String getTransportationMode() { return transportationMode; }
    public void setTransportationMode(String transportationMode) { this.transportationMode = transportationMode; }
    
    public Boolean getIsIndoor() { return isIndoor; }
    public void setIsIndoor(Boolean isIndoor) { this.isIndoor = isIndoor; }
    
    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }
}
