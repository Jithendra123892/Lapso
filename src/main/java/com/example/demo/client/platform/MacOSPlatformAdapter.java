package com.example.demo.client.platform;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

/**
 * macOS-specific implementation of the PlatformAdapter
 */
public class MacOSPlatformAdapter implements PlatformAdapter {

    @Override
    public String getComputerName() {
        return executeCommand("scutil --get ComputerName").trim();
    }

    @Override
    public String getManufacturer() {
        return "Apple Inc.";
    }

    @Override
    public String getModel() {
        String result = executeCommand("system_profiler SPHardwareDataType | grep 'Model Name'");
        if (!result.isEmpty()) {
            return result.split(":")[1].trim();
        }
        return "Mac";
    }

    @Override
    public String getDetailedOS() {
        String version = executeCommand("sw_vers -productVersion");
        String build = executeCommand("sw_vers -buildVersion");
        return "macOS " + version.trim() + " (" + build.trim() + ")";
    }

    @Override
    public String getWiFiSSID() {
        String result = executeCommand("/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport -I | grep ' SSID'");
        if (!result.isEmpty() && result.contains(":")) {
            return result.split(":")[1].trim();
        }
        return "Unknown";
    }

    @Override
    public int getCPUUsage() {
        try {
            String result = executeCommand("top -l 1 | grep 'CPU usage'");
            Pattern cpuPattern = Pattern.compile("(\\d+\\.\\d+)% user");
            Matcher matcher = cpuPattern.matcher(result);
            if (matcher.find()) {
                return (int) Double.parseDouble(matcher.group(1));
            }
        } catch (Exception e) {
            // Fallback to a default value
        }
        return 0;
    }

    @Override
    public int getBatteryLevel() {
        try {
            String result = executeCommand("pmset -g batt | grep -o '[0-9]*%'");
            if (!result.isEmpty()) {
                return Integer.parseInt(result.replace("%", "").trim());
            }
        } catch (Exception e) {
            // Fallback to a default value
        }
        return 100;
    }

    @Override
    public boolean isCharging() {
        try {
            String result = executeCommand("pmset -g batt");
            return result.contains("AC Power") || result.contains("charging");
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
