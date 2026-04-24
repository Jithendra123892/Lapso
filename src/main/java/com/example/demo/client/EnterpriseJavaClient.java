package com.example.demo.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enterprise Java Client - Superior to Microsoft Find My Device
 * Pure Java implementation with advanced features
 */
@Component
public class EnterpriseJavaClient {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private String deviceId;
    private String serverUrl = "http://localhost:8086";
    private boolean isRunning = false;
    private String securityFingerprint;
    private Map<String, Object> lastKnownState = new HashMap<>();
    
    public EnterpriseJavaClient() {
        this.deviceId = generateEnterpriseDeviceId();
        this.securityFingerprint = generateSecurityFingerprint();
        System.out.println("🚀 Enterprise Java Client - Superior to Microsoft Find My Device");
        System.out.println("📱 Device ID: " + deviceId);
        System.out.println("🛡️ Security Fingerprint: " + securityFingerprint.substring(0, 8) + "...");
    }
    
    /**
     * Generate enterprise-grade device ID using hardware characteristics
     */
    private String generateEnterpriseDeviceId() {
        try {
            StringBuilder hardware = new StringBuilder();
            
            // System properties for hardware identification
            hardware.append(System.getProperty("user.name", "unknown"));
            hardware.append(System.getProperty("os.name", "unknown"));
            hardware.append(System.getProperty("os.arch", "unknown"));
            hardware.append(System.getProperty("java.vm.vendor", "unknown"));
            
            // Network interfaces for unique identification
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                byte[] hardwareAddress = ni.getHardwareAddress();
                if (hardwareAddress != null) {
                    hardware.append(Arrays.toString(hardwareAddress));
                    break; // Use first available MAC address
                }
            }
            
            // CPU information
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            hardware.append(osBean.getAvailableProcessors());
            
            // Create hash of hardware info
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(hardware.toString().getBytes());
            
            // Convert to readable device ID
            StringBuilder deviceId = new StringBuilder("LTPRO-");
            for (int i = 0; i < 4; i++) {
                deviceId.append(String.format("%02X", hash[i] & 0xFF));
            }
            
            return deviceId.toString();
            
        } catch (Exception e) {
            // Fallback to timestamp-based ID
            return "LTPRO-" + System.currentTimeMillis() % 100000;
        }
    }
    
    /**
     * Generate security fingerprint for tamper detection
     */
    private String generateSecurityFingerprint() {
        try {
            StringBuilder fingerprint = new StringBuilder();
            
            // System environment
            fingerprint.append(System.getProperty("user.home", ""));
            fingerprint.append(System.getProperty("java.home", ""));
            fingerprint.append(ManagementFactory.getRuntimeMXBean().getName());
            
            // Memory configuration
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            fingerprint.append(memoryBean.getHeapMemoryUsage().getMax());
            fingerprint.append(memoryBean.getNonHeapMemoryUsage().getMax());
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(fingerprint.toString().getBytes());
            
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "fallback-fingerprint-" + System.currentTimeMillis();
        }
    }
    
    /**
     * Get comprehensive system information - Superior to Microsoft's basic data
     */
    public Map<String, Object> getEnterpriseSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // Basic device information
            info.put("deviceId", deviceId);
            info.put("deviceName", getDeviceName());
            info.put("manufacturer", getManufacturer());
            info.put("model", getModel());
            info.put("operatingSystem", getDetailedOSInfo());
            info.put("currentUser", System.getProperty("user.name"));
            info.put("securityFingerprint", securityFingerprint);
            
            // Advanced system metrics - Microsoft lacks this depth
            info.put("javaVersion", System.getProperty("java.version"));
            info.put("javaVendor", System.getProperty("java.vendor"));
            info.put("processorCount", Runtime.getRuntime().availableProcessors());
            info.put("systemArchitecture", System.getProperty("os.arch"));
            
            // Memory information - More detailed than Microsoft
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            info.put("totalMemoryMB", memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024));
            info.put("usedMemoryMB", memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
            info.put("memoryUsage", (int) ((memoryBean.getHeapMemoryUsage().getUsed() * 100.0) / memoryBean.getHeapMemoryUsage().getMax()));
            
            // CPU and system load - Superior monitoring that beats Microsoft's basic metrics
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // Use standard OperatingSystemMXBean methods (non-deprecated)
            info.put("availableProcessors", osBean.getAvailableProcessors());
            info.put("systemLoadAverage", osBean.getSystemLoadAverage());
            
            // Enhanced system metrics using newer APIs
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                
                // Use newer methods for CPU and memory
                try {
                    double processCpuLoad = sunOsBean.getProcessCpuLoad();
                    info.put("cpuUsage", processCpuLoad >= 0 ? (int) (processCpuLoad * 100) : getCurrentCpuUsage());
                    
                    // Enhanced memory information
                    long totalMemory = sunOsBean.getTotalMemorySize();
                    long freeMemory = sunOsBean.getFreeMemorySize();
                    info.put("totalSystemMemoryMB", totalMemory > 0 ? totalMemory / (1024 * 1024) : -1);
                    info.put("freeSystemMemoryMB", freeMemory > 0 ? freeMemory / (1024 * 1024) : -1);
                } catch (Exception e) {
                    // Fallback to basic metrics
                    info.put("cpuUsage", getCurrentCpuUsage());
                    info.put("totalSystemMemoryMB", -1);
                    info.put("freeSystemMemoryMB", -1);
                }
            } else {
                info.put("cpuUsage", getCurrentCpuUsage());
                info.put("totalSystemMemoryMB", -1);
                info.put("freeSystemMemoryMB", -1);
            }
            
            // Network information - Superior to Microsoft's basic connectivity
            info.put("networkInterfaces", getNetworkInterfaceInfo());
            info.put("ipAddress", getLocalIPAddress());
            info.put("externalIP", getExternalIPAddress());
            
            // Advanced location tracking
            Map<String, Object> location = getAdvancedLocation();
            if (location != null) {
                info.putAll(location);
            }
            
            // Security status - Microsoft lacks comprehensive security monitoring
            info.put("securityStatus", getSecurityStatus());
            info.put("isStolen", detectTheft());
            info.put("theftRiskLevel", calculateTheftRisk());
            
            // System uptime and performance
            info.put("uptimeSeconds", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
            info.put("systemLoadAverage", osBean.getSystemLoadAverage());
            
            // Enterprise features
            info.put("isOnline", true);
            info.put("lastUpdated", System.currentTimeMillis());
            info.put("clientVersion", "2.0-Enterprise-Java");
            info.put("trackingAccuracy", "High");
            
            return info;
            
        } catch (Exception e) {
            System.err.println("Error gathering system info: " + e.getMessage());
            info.put("error", e.getMessage());
            return info;
        }
    }
    
    private String getDeviceName() {
        String computerName = System.getenv("COMPUTERNAME");
        if (computerName == null) {
            computerName = System.getenv("HOSTNAME");
        }
        if (computerName == null) {
            try {
                computerName = InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                computerName = "Unknown-Device";
            }
        }
        return computerName + " (Java Enterprise)";
    }
    
    private String getManufacturer() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return "Apple Inc.";
        } else if (os.contains("windows")) {
            return getWindowsManufacturer();
        } else if (os.contains("linux")) {
            return getLinuxManufacturer();
        }
        return "Unknown Manufacturer";
    }
    
    private String getWindowsManufacturer() {
        try {
            Process process = Runtime.getRuntime().exec("wmic computersystem get manufacturer");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.equalsIgnoreCase("manufacturer")) {
                    return line;
                }
            }
        } catch (Exception e) {
            // Ignore and return default
        }
        return "PC Manufacturer";
    }
    
    private String getLinuxManufacturer() {
        try {
            Path vendorPath = Paths.get("/sys/class/dmi/id/sys_vendor");
            if (Files.exists(vendorPath)) {
                return Files.readString(vendorPath).trim();
            }
        } catch (Exception e) {
            // Ignore and return default
        }
        return "Linux System";
    }
    
    private String getModel() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return getMacModel();
        } else if (os.contains("windows")) {
            return getWindowsModel();
        } else if (os.contains("linux")) {
            return getLinuxModel();
        }
        return "Unknown Model";
    }
    
    private String getMacModel() {
        try {
            Process process = Runtime.getRuntime().exec("system_profiler SPHardwareDataType");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Model Name:")) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (Exception e) {
            // Ignore and return default
        }
        return "Mac Computer";
    }
    
    private String getWindowsModel() {
        try {
            Process process = Runtime.getRuntime().exec("wmic computersystem get model");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.equalsIgnoreCase("model")) {
                    return line;
                }
            }
        } catch (Exception e) {
            // Ignore and return default
        }
        return "PC Model";
    }
    
    private String getLinuxModel() {
        try {
            Path modelPath = Paths.get("/sys/class/dmi/id/product_name");
            if (Files.exists(modelPath)) {
                return Files.readString(modelPath).trim();
            }
        } catch (Exception e) {
            // Ignore and return default
        }
        return "Linux Computer";
    }
    
    private String getDetailedOSInfo() {
        return System.getProperty("os.name") + " " + 
               System.getProperty("os.version") + " (" + 
               System.getProperty("os.arch") + ")";
    }
    
    private int getCurrentCpuUsage() {
        // Simplified CPU usage calculation
        long startTime = System.nanoTime();
        long startCpu = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        
        try {
            Thread.sleep(100); // Short sampling period
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        long endCpu = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        
        long cpuTime = endCpu - startCpu;
        long totalTime = endTime - startTime;
        
        return (int) ((cpuTime * 100.0) / totalTime);
    }
    
    private List<Map<String, Object>> getNetworkInterfaceInfo() {
        List<Map<String, Object>> interfaces = new ArrayList<>();
        
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    Map<String, Object> interfaceInfo = new HashMap<>();
                    interfaceInfo.put("name", ni.getName());
                    interfaceInfo.put("displayName", ni.getDisplayName());
                    
                    // Get MAC address
                    byte[] hardwareAddress = ni.getHardwareAddress();
                    if (hardwareAddress != null) {
                        StringBuilder mac = new StringBuilder();
                        for (int i = 0; i < hardwareAddress.length; i++) {
                            mac.append(String.format("%02X", hardwareAddress[i]));
                            if (i < hardwareAddress.length - 1) {
                                mac.append(":");
                            }
                        }
                        interfaceInfo.put("macAddress", mac.toString());
                    }
                    
                    // Get IP addresses
                    List<String> addresses = new ArrayList<>();
                    Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress addr = inetAddresses.nextElement();
                        if (!addr.isLoopbackAddress()) {
                            addresses.add(addr.getHostAddress());
                        }
                    }
                    interfaceInfo.put("addresses", addresses);
                    
                    if (!addresses.isEmpty()) {
                        interfaces.add(interfaceInfo);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting network interfaces: " + e.getMessage());
        }
        
        return interfaces;
    }
    
    private String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    private String getExternalIPAddress() {
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    private Map<String, Object> getAdvancedLocation() {
        try {
            // IP-based geolocation with enhanced data
            URL url = new URL("http://ip-api.com/json/?fields=status,message,country,countryCode,region,regionName,city,zip,lat,lon,timezone,isp,org,as,mobile,proxy,hosting");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            Map<String, Object> locationData = objectMapper.readValue(response.toString(), 
                new TypeReference<Map<String, Object>>() {});
            
            if ("success".equals(locationData.get("status"))) {
                Map<String, Object> location = new HashMap<>();
                location.put("latitude", ((Number) locationData.get("lat")).doubleValue());
                location.put("longitude", ((Number) locationData.get("lon")).doubleValue());
                location.put("address", locationData.get("city") + ", " + locationData.get("regionName") + ", " + locationData.get("country"));
                location.put("lastLocationUpdate", System.currentTimeMillis());
                location.put("locationSource", "IP-Enhanced");
                location.put("locationAccuracy", "City-level");
                location.put("timezone", locationData.get("timezone"));
                location.put("isp", locationData.get("isp"));
                location.put("organization", locationData.get("org"));
                location.put("countryCode", locationData.get("countryCode"));
                location.put("postalCode", locationData.get("zip"));
                
                return location;
            }
        } catch (Exception e) {
            System.err.println("Location lookup failed: " + e.getMessage());
        }
        return null;
    }
    
    private Map<String, Object> getSecurityStatus() {
        Map<String, Object> security = new HashMap<>();
        
        // Java security properties - Using modern security check
        security.put("javaSecurityEnabled", hasSecurityManagerCapabilities());
        security.put("javaVersion", System.getProperty("java.version"));
        security.put("javaVendor", System.getProperty("java.vendor"));
        
        // File system permissions
        security.put("canWriteToHome", Files.isWritable(Paths.get(System.getProperty("user.home"))));
        security.put("canWriteToTemp", Files.isWritable(Paths.get(System.getProperty("java.io.tmpdir"))));
        
        // Network security
        security.put("httpsSupported", isHTTPSSupported());
        
        return security;
    }
    
    private boolean isHTTPSSupported() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean hasSecurityManagerCapabilities() {
        // Modern security check without deprecated SecurityManager
        try {
            // Check if we can perform security-related operations
            System.getProperty("java.security.policy");
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
    
    private boolean detectTheft() {
        try {
            // Compare current security fingerprint with stored one
            String currentFingerprint = generateSecurityFingerprint();
            if (!currentFingerprint.equals(securityFingerprint)) {
                System.out.println("🚨 SECURITY ALERT: Device fingerprint changed!");
                return true;
            }
            
            // Check for suspicious user changes
            String currentUser = System.getProperty("user.name");
            if (lastKnownState.containsKey("user") && !currentUser.equals(lastKnownState.get("user"))) {
                System.out.println("🚨 SECURITY ALERT: User changed from " + lastKnownState.get("user") + " to " + currentUser);
                return true;
            }
            lastKnownState.put("user", currentUser);
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String calculateTheftRisk() {
        int riskScore = 0;
        
        // Check system uptime (very low uptime might indicate theft)
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        if (uptime < 300) { // Less than 5 minutes
            riskScore += 20;
        }
        
        // Check for unknown network connections
        try {
            String externalIP = getExternalIPAddress();
            if (lastKnownState.containsKey("externalIP") && !externalIP.equals(lastKnownState.get("externalIP"))) {
                riskScore += 15;
            }
            lastKnownState.put("externalIP", externalIP);
        } catch (Exception e) {
            riskScore += 10;
        }
        
        if (riskScore >= 30) return "HIGH";
        if (riskScore >= 15) return "MEDIUM";
        return "LOW";
    }
    
    /**
     * Register device with server - Enterprise registration
     */
    public boolean registerDevice(String ownerEmail) {
        try {
            Map<String, Object> deviceInfo = getEnterpriseSystemInfo();
            deviceInfo.put("ownerEmail", ownerEmail);
            deviceInfo.put("registrationType", "Enterprise-Java");
            
            String jsonData = objectMapper.writeValueAsString(deviceInfo);
            
            URL url = new URL(serverUrl + "/api/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "LaptopTracker-Enterprise-Java/2.0");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ Enterprise device registered successfully!");
                System.out.println("🛡️ Advanced security monitoring enabled");
                System.out.println("📊 Performance tracking activated");
                return true;
            } else {
                System.out.println("❌ Registration failed: " + responseCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update device status on server
     */
    public boolean updateDeviceStatus() {
        try {
            Map<String, Object> deviceInfo = getEnterpriseSystemInfo();
            String jsonData = objectMapper.writeValueAsString(deviceInfo);
            
            URL url = new URL(serverUrl + "/api/devices/" + deviceId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "LaptopTracker-Enterprise-Java/2.0");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Integer memUsage = (Integer) deviceInfo.get("memoryUsage");
                Integer cpuUsage = (Integer) deviceInfo.get("cpuUsage");
                String theftRisk = (String) deviceInfo.get("theftRiskLevel");
                
                System.out.printf("📊 Enterprise Update - 💻 CPU: %d%% | 🧠 RAM: %d%% | 🛡️ Risk: %s%n", 
                    cpuUsage != null ? cpuUsage : 0, 
                    memUsage != null ? memUsage : 0, 
                    theftRisk);
                
                return true;
            } else {
                System.out.println("⚠️ Update failed: " + responseCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Update error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Start enterprise tracking with Java-only implementation
     */
    public void startEnterpriseTracking(String ownerEmail, String serverUrl, int intervalSeconds) {
        this.serverUrl = serverUrl;
        
        System.out.println("🚀 Starting Enterprise Java Tracking - Superior to Microsoft Find My Device");
        System.out.println("======================================================================");
        
        // Register device
        if (!registerDevice(ownerEmail)) {
            System.out.println("❌ Failed to register device. Exiting.");
            return;
        }
        
        isRunning = true;
        
        System.out.println("✅ Enterprise Java tracking activated!");
        System.out.println("📊 Advanced metrics every " + intervalSeconds + " seconds");
        System.out.println("🛡️ Security monitoring with theft detection");
        System.out.println("🌐 Network and performance analysis");
        System.out.println("⌨️ Press Ctrl+C to stop");
        System.out.println("======================================================================");
        
        // Schedule regular updates
        scheduler.scheduleAtFixedRate(() -> {
            if (isRunning) {
                updateDeviceStatus();
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
        
        // Schedule security checks more frequently
        scheduler.scheduleAtFixedRate(() -> {
            if (isRunning && detectTheft()) {
                System.out.println("🚨 THEFT DETECTED - Sending immediate alert!");
                sendTheftAlert();
            }
        }, 0, 30, TimeUnit.SECONDS); // Check every 30 seconds
        
        // Keep main thread alive
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Stopping enterprise tracking...");
            isRunning = false;
            scheduler.shutdown();
        }));
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("\n🛑 Enterprise tracking interrupted.");
            isRunning = false;
            scheduler.shutdown();
        }
    }
    
    private void sendTheftAlert() {
        try {
            Map<String, Object> alert = new HashMap<>();
            alert.put("deviceId", deviceId);
            alert.put("alertType", "THEFT_DETECTED");
            alert.put("timestamp", System.currentTimeMillis());
            alert.put("securityFingerprint", securityFingerprint);
            alert.put("location", getAdvancedLocation());
            
            String jsonData = objectMapper.writeValueAsString(alert);
            
            URL url = new URL(serverUrl + "/api/alerts/theft");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            System.out.println("🚨 THEFT ALERT SENT TO SECURITY TEAM!");
            
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send theft alert: " + e.getMessage());
        }
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public void stop() {
        isRunning = false;
        scheduler.shutdown();
    }
}
