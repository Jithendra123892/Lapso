package com.example.demo.config;

import com.example.demo.service.UserService;
import com.example.demo.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

/**
 * Simple Spring Security configuration that works with Vaadin
 * Avoids VaadinWebSecurity conflicts by using standard SecurityFilterChain
 */
@Configuration
@EnableWebSecurity  
public class VaadinSecurityConfiguration {

    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;

    public VaadinSecurityConfiguration(UserService userService, CustomUserDetailsService customUserDetailsService) {
        this.userService = userService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable CSRF completely to avoid 403 errors
        http.csrf(csrf -> csrf.disable());
        
        // Disable X-Frame-Options to allow iframe/frame display
        http.headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.disable())
        );
        
        http.authorizeHttpRequests(authz -> authz
            // Allow ALL Vaadin internal paths (critical for Vaadin to work!)
            .requestMatchers(
                "/login", "/",
                // Static resources
                "/static/**", "/js/**", "/css/**", "/webjars/**", "/favicon.ico",
                "/*.html", 
                "/VAADIN/**", "/vaadinServlet/**", "/frontend/**", 
                "/sw.js", "/manifest.webmanifest", "/icons/**", "/images/**",
                // Critical Vaadin paths that were missing:
                "/?v-r=**",  // Vaadin request routing
                "/vaadinServlet/UIDL/**",  // Vaadin UIDL communication
                "/vaadinServlet/HEARTBEAT/**",  // Vaadin heartbeat
                "/connect/**",  // Vaadin connect
                "/@vaadin/**"  // Vaadin internal resources
            ).permitAll()
            // Allow access to all routes without authentication
            .requestMatchers("/add-device", "/dashboard", "/map", "/analytics").permitAll()
            // API endpoints
            .requestMatchers("/api/**").permitAll()
            .anyRequest().permitAll()
        );
        
        // Configure form login for Vaadin
        http.formLogin(form -> form
            .loginPage("/login")
            .permitAll()
        );
        
        // Configure logout to work with Vaadin
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll()
        );
        
        return http.build();
    }
    
}