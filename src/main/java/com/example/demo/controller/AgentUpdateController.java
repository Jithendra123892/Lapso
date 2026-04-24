package com.example.demo.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentUpdateController {

    private static final String AGENT_VERSION = "1.0.0"; // keep in sync with agent script

    @GetMapping("/version")
    public ResponseEntity<?> getAgentVersion(@RequestParam(name = "platform", required = false, defaultValue = "windows") String platform) {
        try {
            String filename = platform.equalsIgnoreCase("windows")
                    ? "laptop-tracker-agent.ps1"
                    : "agent.sh"; // placeholder for other platforms

            String resourcePath = "static/agents/" + platform + "/" + filename;
            Resource resource = new ClassPathResource(resourcePath);

            String sha256 = null;
            if (resource.exists()) {
                try (InputStream in = resource.getInputStream(); DigestInputStream din = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"))) {
                    byte[] buffer = new byte[8192];
                    while (din.read(buffer) != -1) { /* read to compute digest */ }
                    byte[] digest = din.getMessageDigest().digest();
                    StringBuilder sb = new StringBuilder();
                    for (byte b : digest) sb.append(String.format("%02x", b));
                    sha256 = sb.toString();
                }
            }

            Map<String, Object> body = new HashMap<>();
            body.put("version", AGENT_VERSION);
            body.put("platform", platform);
            body.put("filename", filename);
            body.put("url", "/api/agents/download/" + platform + "/" + filename);
            if (sha256 != null) body.put("sha256", sha256);
            body.put("changelog", "Minor improvements, logging, and self-update support.");

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
