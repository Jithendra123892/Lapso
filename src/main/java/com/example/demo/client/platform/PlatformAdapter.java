package com.example.demo.client.platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Platform adapter interface for cross-platform compatibility
 * Provides platform-specific implementations for device information collection
 */
public interface PlatformAdapter {
    
    /**
     * Get the platform-specific implementation based on current OS
     * @return The appropriate platform adapter
     */
    static PlatformAdapter getInstance() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return new WindowsPlatformAdapter();
        } else if (os.contains("mac")) {
            return new MacOSPlatformAdapter();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return new LinuxPlatformAdapter();
        } else {
            // Fallback to generic implementation
            return new GenericPlatformAdapter();
        }
    }
    
    /**
     * Get the computer name
     * @return Computer name
     */
    String getComputerName();
    
    /**
     * Get the manufacturer of the device
     * @return Manufacturer name
     */
    String getManufacturer();
    
    /**
     * Get the model of the device
     * @return Model name
     */
    String getModel();
    
    /**
     * Get detailed OS information
     * @return OS details
     */
    String getDetailedOS();
    
    /**
     * Get the current WiFi SSID
     * @return WiFi SSID
     */
    String getWiFiSSID();
    
    /**
     * Get the current CPU usage percentage
     * @return CPU usage as percentage
     */
    int getCPUUsage();
    
    /**
     * Get the current battery level percentage
     * @return Battery level as percentage
     */
    int getBatteryLevel();
    
    /**
     * Check if the device is currently charging
     * @return true if charging, false otherwise
     */
    boolean isCharging();
    
    /**
     * Get location information based on IP
     * @return Map containing latitude and longitude
     */
    Map<String, Double> getLocationFromIP();
    
    /**
     * Get address from latitude and longitude
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Address as string
     */
    String getAddressFromLocation(double latitude, double longitude);
    
    /**
     * Execute a command and return the output
     * @param command Command to execute
     * @return Output of the command
     */
    default String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            process.waitFor();
            reader.close();
        } catch (IOException | InterruptedException e) {
            return "";
        }
        
        return output.toString().trim();
    }
}
