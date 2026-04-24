package com.example.demo.client.platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

/**
 * Windows-specific implementation of the PlatformAdapter
 */
public class WindowsPlatformAdapter implements PlatformAdapter {

    @Override
    public String getComputerName() {
        return System.getenv("COMPUTERNAME");
    }

    @Override
    public String getManufacturer() {
        String result = executeCommand("wmic computersystem get manufacturer");
        if (result.contains("\n")) {
            String[] lines = result.split("\n");
            if (lines.length > 1) {
                return lines[1].trim();
            }
        }
        return "Unknown";
    }

    @Override
    public String getModel() {
        String result = executeCommand("wmic computersystem get model");
        if (result.contains("\n")) {
            String[] lines = result.split("\n");
            if (lines.length > 1) {
                return lines[1].trim();
            }
        }
        return "Unknown";
    }

    @Override
    public String getDetailedOS() {
        String version = executeCommand("wmic os get Caption, Version /value");
        String build = executeCommand("wmic os get BuildNumber /value");
        return version.trim() + " " + build.trim();
    }

    @Override
    public String getWiFiSSID() {
        String result = executeCommand("netsh wlan show interfaces");
        Pattern ssidPattern = Pattern.compile("\\s+SSID\\s+:\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = ssidPattern.matcher(result);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Unknown";
    }

    @Override
    public int getCPUUsage() {
        try {
            String result = executeCommand("wmic cpu get LoadPercentage");
            Pattern cpuPattern = Pattern.compile("(\\d+)");
            Matcher matcher = cpuPattern.matcher(result);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // Fallback to a default value
        }
        return 0;
    }

    @Override
    public int getBatteryLevel() {
        try {
            String result = executeCommand("WMIC PATH Win32_Battery Get EstimatedChargeRemaining");
            Pattern batteryPattern = Pattern.compile("(\\d+)");
            Matcher matcher = batteryPattern.matcher(result);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // Fallback to a default value
        }
        return 100;
    }

    @Override
    public boolean isCharging() {
        try {
            String result = executeCommand("WMIC PATH Win32_Battery Get BatteryStatus");
            Pattern statusPattern = Pattern.compile("(\\d+)");
            Matcher matcher = statusPattern.matcher(result);
            if (matcher.find()) {
                int status = Integer.parseInt(matcher.group(1));
                // BatteryStatus: 1 = Discharging, 2 = AC Power, other values exist too
                return status == 2;
            }
        } catch (Exception e) {
            // Fallback to a default value
        }
        return true;
    }

    @Override
    public Map<String, Double> getLocationFromIP() {
        Map<String, Double> location = new HashMap<>();
        location.put("latitude", 0.0);
        location.put("longitude", 0.0);
        
        try {
            URL url = new URL("https://ipinfo.io/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            if (jsonResponse.has("loc")) {
                String[] coordinates = jsonResponse.getString("loc").split(",");
                location.put("latitude", Double.parseDouble(coordinates[0]));
                location.put("longitude", Double.parseDouble(coordinates[1]));
            }
        } catch (Exception e) {
            // Use default values
        }
        
        return location;
    }

    @Override
    public String getAddressFromLocation(double latitude, double longitude) {
        try {
            URL url = new URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + 
                              latitude + "&lon=" + longitude);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "LaptopTracker/1.0");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            if (jsonResponse.has("display_name")) {
                return jsonResponse.getString("display_name");
            }
        } catch (Exception e) {
            // Use default value
        }
        
        return "Unknown Location";
    }
}
