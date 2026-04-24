package com.example.demo.controller;

import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.UserService;
import com.example.demo.model.User;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/api/test")
public class QuickTestController {
    
    @Autowired
    private PerfectAuthService authService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestParam String email, 
            @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.findByEmail(email.trim().toLowerCase());
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found: " + email);
                return ResponseEntity.ok(response);
            }
            
            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "Password reset successfully for " + email);
            response.put("email", email);
            
            System.out.println("✅ Password reset for: " + email);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            System.err.println("❌ Password reset error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test-login")
    public ResponseEntity<Map<String, Object>> testLogin(@RequestParam String email, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = authService.performLogin(email, password);
            response.put("success", success);
            response.put("message", success ? "Login successful" : "Login failed");
            response.put("email", email);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "LAPSO is running");
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/cleanup-demo-devices")
    @Transactional
    public ResponseEntity<Map<String, Object>> cleanupDemoDevices() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // List of demo device IDs and names to remove
            var demoDeviceIds = Arrays.asList("DEMO-LAPTOP-001", "register");
            var demoDeviceNames = Arrays.asList("Demo MacBook Pro", "Test Laptop");
            
            // Delete by device_id
            int deletedByIds = deviceRepository.deleteByDeviceIdIn(demoDeviceIds);
            
            // Delete by device_name
            int deletedByNames = deviceRepository.deleteByDeviceNameIn(demoDeviceNames);
            
            int totalDeleted = deletedByIds + deletedByNames;
            
            response.put("success", true);
            response.put("message", "Demo devices cleaned up successfully");
            response.put("deletedCount", totalDeleted);
            response.put("deletedByIds", deletedByIds);
            response.put("deletedByNames", deletedByNames);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error cleaning up demo devices: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}