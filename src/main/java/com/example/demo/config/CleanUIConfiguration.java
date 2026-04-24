package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CleanUIConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "app.ui")
    public CleanUIProperties cleanUIProperties() {
        return new CleanUIProperties();
    }

    public static class CleanUIProperties {
        private String theme = "clean-modern";
        private boolean enableAnimations = true;
        private boolean enableHoverEffects = true;
        private String fontFamily = "Inter";

        // Getters and setters
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }

        public boolean isEnableAnimations() { return enableAnimations; }
        public void setEnableAnimations(boolean enableAnimations) { this.enableAnimations = enableAnimations; }

        public boolean isEnableHoverEffects() { return enableHoverEffects; }
        public void setEnableHoverEffects(boolean enableHoverEffects) { this.enableHoverEffects = enableHoverEffects; }

        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    }
}
