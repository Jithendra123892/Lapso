package com.example.demo.controller;

import com.example.demo.service.DeviceService;
import com.example.demo.service.PerfectAuthService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/agents")
public class AgentDownloadController {
    
    private final DeviceService deviceService;
    private final PerfectAuthService authService;
    
    public AgentDownloadController(DeviceService deviceService, PerfectAuthService authService) {
        this.deviceService = deviceService;
        this.authService = authService;
    }

    @GetMapping("/download/{platform}/{filename}")
    public ResponseEntity<Resource> downloadAgent(
            @PathVariable String platform,
            @PathVariable String filename) throws IOException {
        
        // Register device before download
        String filePath = "static/agents/" + platform + "/" + filename;
        Resource resource = new ClassPathResource(filePath);
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Determine content type
        String contentType = "application/octet-stream";
        if (filename.endsWith(".ps1")) {
            contentType = "text/plain";
        } else if (filename.endsWith(".sh")) {
            contentType = "application/x-sh";
        } else if (filename.endsWith(".py")) {
            contentType = "text/x-python";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
    
    // Add endpoint for agent updates
    @GetMapping("/update/{platform}/{version}")
    public ResponseEntity<Resource> updateAgent(
            @PathVariable String platform,
            @PathVariable String version) throws IOException {
        
        String filePath = "static/agents/" + platform + "/agent-" + version + ".jar";
        Resource resource = new ClassPathResource(filePath);
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"agent-" + version + ".jar\"")
                .body(resource);
    }
}