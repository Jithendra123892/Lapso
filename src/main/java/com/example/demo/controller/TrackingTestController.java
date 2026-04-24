package com.example.demo.controller;

import com.example.demo.service.UltraPreciseTrackingService;
import com.example.demo.model.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TrackingTestController {

    @Autowired
    private UltraPreciseTrackingService ultraPreciseTrackingService;

    @PostMapping("/track")
    public ResponseEntity<?> testTracking(@RequestBody TrackingTestRequest request) {
        try {
            UltraPreciseTrackingService.LocationUpdateRequest trackingRequest =
                new UltraPreciseTrackingService.LocationUpdateRequest();
            trackingRequest.setDeviceId(request.getDeviceId());
            trackingRequest.setLatitude(request.getLatitude());
            trackingRequest.setLongitude(request.getLongitude());
            trackingRequest.setAccuracy(1.0); // 1cm accuracy
            trackingRequest.setLocationSource("TEST_ULTRA_PRECISE");
            trackingRequest.setConfidence(0.999);
            trackingRequest.setTimestamp(java.time.LocalDateTime.now());

            boolean success = ultraPreciseTrackingService.updateDeviceLocation(
                request.getDeviceId(), trackingRequest);

            if (success) {
                return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "Ultra-precise tracking test successful",
                    "accuracy", "1cm"
                ));
            } else {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                    "status", "error",
                    "message", "Ultra-precise tracking test failed"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "status", "error",
                "message", "Exception: " + e.getMessage()
            ));
        }
    }

    public static class TrackingTestRequest {
        private String deviceId;
        private double latitude;
        private double longitude;

        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }
}