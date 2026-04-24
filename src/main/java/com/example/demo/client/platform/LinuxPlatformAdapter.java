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
 * Linux-specific implementation of the PlatformAdapter
 */
public class LinuxPlatformAdapter implements PlatformAdapter {

    @Override
    public String getComputerName() {
        return executeCommand("hostname").trim();
    }

    @Override
    public String getManufacturer() {
        String result = executeCommand("cat /sys/devices/virtual/dmi/id/sys_vendor");
        if (result.isEmpty()) {
            result = executeCommand("dmidecode -s system-manufacturer");
        }
        return result.isEmpty() ? "Unknown" : result.trim();
    }

    @Override
    public String getModel() {
        String result = executeCommand("cat /sys/devices/virtual/dmi/id/product_name");
        if (result.isEmpty()) {
            result = executeCommand("dmidecode -s system-product-name");
        }
        return result.isEmpty() ? "Unknown" : result.trim();
    }

    @Override
    public String getDetailedOS() {
        String distro = executeCommand("cat /etc/os-release | grep PRETTY_NAME");
        if (!distro.isEmpty() && distro.contains("=")) {
            distro = distro.split("=")[1].replace("\"", "").trim();
        }
        String kernel = executeCommand("uname -r").trim();
        return distro + " (Kernel: " + kernel + ")";
    }

    @Override
    public String getWiFiSSID() {
        String result = executeCommand("iwgetid -r");
        return result.isEmpty() ? "Unknown" : result.trim();
    }

    @Override
    public int getCPUUsage() {
        try {
            String result = executeCommand("top -bn1 | grep 'Cpu(s)' | awk '{print $2 + $4}'");
            return (int) Double.parseDouble(result.trim());
        } catch (Exception e) {
            // Fallback to a default value
        }
        return 0;
    }

    @Override
    public int getBatteryLevel() {
        try {
            // Try upower first
            String result = executeCommand("upower -i $(upower -e | grep BAT) | grep percentage");
            if (!result.isEmpty()) {
                Pattern pattern = Pattern.compile("(\\d+)%");
                Matcher matcher = pattern.matcher(result);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
            
            // Try acpi as fallback
            result = executeCommand("acpi -b | grep -P -o '[0-9]+(?=%)'");
            if (!result.isEmpty()) {
                return Integer.parseInt(result.trim());
            }
        } catch (Exception e) {
            // Fallback to a default value
        }
        return 100;
    }

    @Override
    public boolean isCharging() {
        try {
            // Try upower first
            String result = executeCommand("upower -i $(upower -e | grep BAT) | grep state");
            if (!result.isEmpty()) {
                return result.contains("charging");
            }
            
            // Try acpi as fallback
            result = executeCommand("acpi -b");
            if (!result.isEmpty()) {
                return result.contains("Charging");
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
