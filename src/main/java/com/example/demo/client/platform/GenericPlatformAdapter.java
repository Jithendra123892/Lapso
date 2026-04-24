package com.example.demo.client.platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * Generic fallback implementation of the PlatformAdapter
 * Used when the OS is not specifically supported
 */
public class GenericPlatformAdapter implements PlatformAdapter {

    @Override
    public String getComputerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    @Override
    public String getManufacturer() {
        return "Unknown";
    }

    @Override
    public String getModel() {
        return "Unknown";
    }

    @Override
    public String getDetailedOS() {
        return System.getProperty("os.name") + " " + 
               System.getProperty("os.version") + " " + 
               System.getProperty("os.arch");
    }

    @Override
    public String getWiFiSSID() {
        return "Unknown";
    }

    @Override
    public int getCPUUsage() {
        return 0;
    }

    @Override
    public int getBatteryLevel() {
        return 100;
    }

    @Override
    public boolean isCharging() {
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
