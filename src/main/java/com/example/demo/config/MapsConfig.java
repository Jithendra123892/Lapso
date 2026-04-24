package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapsConfig {
    
    @Value("${mappls.api.key:}")
    private String mapplsApiKey;
    
    public String getMapplsApiKey() {
        return mapplsApiKey;
    }
    
    public boolean isMapplsEnabled() {
        return mapplsApiKey != null && !mapplsApiKey.trim().isEmpty();
    }
}
