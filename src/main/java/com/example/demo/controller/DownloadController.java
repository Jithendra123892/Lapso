package com.example.demo.controller;

import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/download")
public class DownloadController {

    @Autowired
    private PerfectAuthService authService;
    
    @Autowired
    private DeviceService deviceService;

    /**
     * Download Windows installer - SECURE: Only authenticated users can download their own device installers
     */
    @GetMapping("/windows")
    public ResponseEntity<Resource> downloadWindowsInstaller(
            @RequestParam String deviceId,
            @RequestParam String userEmail,
            @RequestParam(defaultValue = "http://localhost:8080") String serverUrl) {
        
        // SECURITY CHECK: Verify user authentication
        if (!authService.isLoggedIn()) {
            return ResponseEntity.status(401).build();
        }
        
        // SECURITY CHECK: Verify user can only download their own device installers
        String currentUser = authService.getLoggedInUser();
        if (!currentUser.equals(userEmail)) {
            return ResponseEntity.status(403).build(); // Forbidden - user trying to access another user's device
        }
        
        try {
            // Create customized installer script
            String installerContent = createWindowsInstaller(deviceId, userEmail, serverUrl);
            
            // Create temporary file
            java.nio.file.Path tempFile = Files.createTempFile("lapso-installer-" + deviceId, ".ps1");
            Files.write(tempFile, installerContent.getBytes());
            
            Resource resource = new org.springframework.core.io.FileSystemResource(tempFile.toFile());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lapso-installer-" + deviceId + ".ps1")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Download Linux installer - SECURE: Only authenticated users can download their own device installers
     */
    @GetMapping("/linux")
    public ResponseEntity<Resource> downloadLinuxInstaller(
            @RequestParam String deviceId,
            @RequestParam String userEmail,
            @RequestParam(defaultValue = "http://localhost:8080") String serverUrl) {
        
        // SECURITY CHECK: Verify user authentication
        if (!authService.isLoggedIn()) {
            return ResponseEntity.status(401).build();
        }
        
        // SECURITY CHECK: Verify user can only download their own device installers
        String currentUser = authService.getLoggedInUser();
        if (!currentUser.equals(userEmail)) {
            return ResponseEntity.status(403).build(); // Forbidden - user trying to access another user's device
        }
        
        try {
            // Create customized installer script
            String installerContent = createLinuxInstaller(deviceId, userEmail, serverUrl);
            
            // Create temporary file
            java.nio.file.Path tempFile = Files.createTempFile("lapso-installer-" + deviceId, ".sh");
            Files.write(tempFile, installerContent.getBytes());
            
            Resource resource = new org.springframework.core.io.FileSystemResource(tempFile.toFile());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lapso-installer-" + deviceId + ".sh")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Download macOS installer - SECURE: Only authenticated users can download their own device installers
     */
    @GetMapping("/macos")
    public ResponseEntity<Resource> downloadMacOSInstaller(
            @RequestParam String deviceId,
            @RequestParam String userEmail,
            @RequestParam(defaultValue = "http://localhost:8080") String serverUrl) {
        
        // SECURITY CHECK: Verify user authentication
        if (!authService.isLoggedIn()) {
            return ResponseEntity.status(401).build();
        }
        
        // SECURITY CHECK: Verify user can only download their own device installers
        String currentUser = authService.getLoggedInUser();
        if (!currentUser.equals(userEmail)) {
            return ResponseEntity.status(403).build(); // Forbidden - user trying to access another user's device
        }
        
        try {
            // Create customized installer script
            String installerContent = createMacOSInstaller(deviceId, userEmail, serverUrl);
            
            // Create temporary file
            java.nio.file.Path tempFile = Files.createTempFile("lapso-installer-" + deviceId, ".sh");
            Files.write(tempFile, installerContent.getBytes());
            
            Resource resource = new org.springframework.core.io.FileSystemResource(tempFile.toFile());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lapso-installer-" + deviceId + ".sh")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private String createWindowsInstaller(String deviceId, String userEmail, String serverUrl) {
        return String.format("""
            # LAPSO Windows Agent Installer - Customized for %s
            # Better than Microsoft Find My Device - Completely Free
            
            $DeviceId = "%s"
            $UserEmail = "%s"
            $ServerUrl = "%s"
            
            Write-Host "🛡️ LAPSO Agent Installer" -ForegroundColor Green
            Write-Host "=========================" -ForegroundColor Green
            Write-Host ""
            Write-Host "Device ID: $DeviceId" -ForegroundColor Cyan
            Write-Host "User Email: $UserEmail" -ForegroundColor Cyan
            Write-Host "Server URL: $ServerUrl" -ForegroundColor Cyan
            Write-Host ""
            
            # Check if running as administrator
            if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
                Write-Host "❌ This installer must be run as Administrator!" -ForegroundColor Red
                Write-Host "Right-click PowerShell and select 'Run as Administrator'" -ForegroundColor Yellow
                pause
                exit 1
            }
            
            Write-Host "✅ Running with Administrator privileges" -ForegroundColor Green
            
            # Create LAPSO directory
            $lapsoDir = "C:\\Program Files\\LAPSO"
            if (!(Test-Path $lapsoDir)) {
                New-Item -ItemType Directory -Path $lapsoDir -Force | Out-Null
                Write-Host "✅ Created LAPSO directory: $lapsoDir" -ForegroundColor Green
            }
            
            # Download and install the agent script
            $agentScript = "$lapsoDir\\lapso-agent.ps1"
            $agentUrl = "$ServerUrl/agents/windows/laptop-tracker-agent.ps1"
            
            try {
                Write-Host "📥 Downloading LAPSO agent..." -ForegroundColor Yellow
                Invoke-WebRequest -Uri $agentUrl -OutFile $agentScript -UseBasicParsing
                Write-Host "✅ Agent downloaded successfully" -ForegroundColor Green
            } catch {
                Write-Host "❌ Failed to download agent: $($_.Exception.Message)" -ForegroundColor Red
                pause
                exit 1
            }
            
            # Create configuration file
            $configFile = "$lapsoDir\\config.json"
            $config = @{
                deviceId = $DeviceId
                userEmail = $UserEmail
                serverUrl = $ServerUrl
                updateInterval = 30
                enableLocationTracking = $true
                enableSystemMonitoring = $true
                enableNetworkMonitoring = $true
                agentVersion = "1.0.0"
            } | ConvertTo-Json -Depth 3
            
            $config | Out-File -FilePath $configFile -Encoding UTF8
            Write-Host "✅ Configuration saved: $configFile" -ForegroundColor Green
            
            # Create scheduled task
            $taskName = "LAPSO Agent"
            $serviceScript = "$lapsoDir\\service-wrapper.ps1"
            $serviceScriptContent = @"
# LAPSO Service Wrapper
`$configPath = "C:\\Program Files\\LAPSO\\config.json"
`$agentPath = "C:\\Program Files\\LAPSO\\lapso-agent.ps1"

if (Test-Path `$configPath) {
    `$config = Get-Content `$configPath | ConvertFrom-Json
    & `$agentPath -DeviceId `$config.deviceId -UserEmail `$config.userEmail -ServerUrl `$config.serverUrl
} else {
    Write-EventLog -LogName Application -Source "LAPSO" -EventId 1001 -EntryType Error -Message "LAPSO configuration file not found"
}
"@
            
            $serviceScriptContent | Out-File -FilePath $serviceScript -Encoding UTF8
            
            # Install as scheduled task
            Write-Host "🔧 Installing LAPSO as Windows Service..." -ForegroundColor Yellow
            
            $action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-WindowStyle Hidden -ExecutionPolicy Bypass -File `"$serviceScript`""
            $trigger = New-ScheduledTaskTrigger -AtStartup
            $settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1)
            $principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest
            
            # Remove existing task if it exists
            Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
            
            # Register new task
            Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Settings $settings -Principal $principal -Description "LAPSO free laptop tracking and security service" | Out-Null
            
            Write-Host "✅ LAPSO service installed successfully" -ForegroundColor Green
            
            # Start the service
            Write-Host "🚀 Starting LAPSO agent..." -ForegroundColor Yellow
            Start-ScheduledTask -TaskName $taskName
            
            # Wait and check status
            Start-Sleep -Seconds 3
            $task = Get-ScheduledTask -TaskName $taskName
            if ($task.State -eq "Running") {
                Write-Host "✅ LAPSO agent is running!" -ForegroundColor Green
            } else {
                Write-Host "⚠️ LAPSO agent may not be running. Check Task Scheduler." -ForegroundColor Yellow
            }
            
            # Final instructions
            Write-Host ""
            Write-Host "🎉 LAPSO Installation Complete!" -ForegroundColor Green
            Write-Host "================================" -ForegroundColor Green
            Write-Host ""
            Write-Host "✅ Agent is now protecting your device 24/7" -ForegroundColor Green
            Write-Host "✅ Real-time updates every 30 seconds" -ForegroundColor Green
            Write-Host "✅ Better than Microsoft Find My Device" -ForegroundColor Green
            Write-Host "✅ Completely free and open source" -ForegroundColor Green
            Write-Host ""
            Write-Host "📊 View your device at: $ServerUrl" -ForegroundColor Yellow
            Write-Host ""
            
            pause
            """, deviceId, deviceId, userEmail, serverUrl);
    }
    
    private String createLinuxInstaller(String deviceId, String userEmail, String serverUrl) {
        return String.format("""
            #!/bin/bash
            # LAPSO Linux Agent Installer - Customized for %s
            # Better than Microsoft Find My Device - Completely Free
            
            DEVICE_ID="%s"
            USER_EMAIL="%s"
            SERVER_URL="%s"
            
            # Colors
            GREEN='\\033[0;32m'
            YELLOW='\\033[1;33m'
            CYAN='\\033[0;36m'
            NC='\\033[0m'
            
            echo -e "${GREEN}🛡️ LAPSO Agent Installer${NC}"
            echo -e "${GREEN}=========================${NC}"
            echo ""
            echo -e "${CYAN}Device ID: $DEVICE_ID${NC}"
            echo -e "${CYAN}User Email: $USER_EMAIL${NC}"
            echo -e "${CYAN}Server URL: $SERVER_URL${NC}"
            echo ""
            
            # Create LAPSO directory
            LAPSO_DIR="/opt/lapso"
            sudo mkdir -p "$LAPSO_DIR"
            echo -e "${GREEN}✅ Created LAPSO directory${NC}"
            
            # Download agent
            AGENT_SCRIPT="$LAPSO_DIR/lapso-agent.sh"
            curl -f -s -o "$AGENT_SCRIPT" "$SERVER_URL/agents/linux/laptop-tracker-agent.sh"
            sudo chmod +x "$AGENT_SCRIPT"
            echo -e "${GREEN}✅ Agent downloaded${NC}"
            
            # Create config
            CONFIG_FILE="$LAPSO_DIR/config.json"
            sudo tee "$CONFIG_FILE" > /dev/null <<EOF
{
    "deviceId": "$DEVICE_ID",
    "userEmail": "$USER_EMAIL",
    "serverUrl": "$SERVER_URL",
    "updateInterval": 30,
    "enableLocationTracking": true,
    "enableSystemMonitoring": true,
    "enableNetworkMonitoring": true,
    "agentVersion": "1.0.0"
}
EOF
            
            echo -e "${GREEN}✅ Configuration saved${NC}"
            
            # Install systemd service
            SERVICE_FILE="/etc/systemd/system/lapso-agent.service"
            sudo tee "$SERVICE_FILE" > /dev/null <<EOF
[Unit]
Description=LAPSO Laptop Security Agent
After=network.target
            
[Service]
Type=simple
ExecStart=$AGENT_SCRIPT
Restart=always
RestartSec=10
            
[Install]
WantedBy=multi-user.target
EOF
            
            sudo systemctl daemon-reload
            sudo systemctl enable lapso-agent.service
            sudo systemctl start lapso-agent.service
            
            echo -e "${GREEN}✅ LAPSO service installed and started${NC}"
            echo ""
            echo -e "${GREEN}🎉 Installation Complete!${NC}"
            echo -e "${GREEN}✅ Agent protecting your device 24/7${NC}"
            echo -e "${YELLOW}📊 View at: $SERVER_URL${NC}"
            """, deviceId, deviceId, userEmail, serverUrl);
    }
    
    private String createMacOSInstaller(String deviceId, String userEmail, String serverUrl) {
        return String.format("""
            #!/bin/bash
            # LAPSO macOS Agent Installer - Customized for %s
            # Better than Microsoft Find My Device - Completely Free
            
            DEVICE_ID="%s"
            USER_EMAIL="%s"
            SERVER_URL="%s"
            
            # Colors
            GREEN='\\033[0;32m'
            YELLOW='\\033[1;33m'
            CYAN='\\033[0;36m'
            NC='\\033[0m'
            
            echo -e "${GREEN}🛡️ LAPSO Agent Installer for macOS${NC}"
            echo -e "${GREEN}===================================${NC}"
            echo ""
            echo -e "${CYAN}Device ID: $DEVICE_ID${NC}"
            echo -e "${CYAN}User Email: $USER_EMAIL${NC}"
            echo -e "${CYAN}Server URL: $SERVER_URL${NC}"
            echo ""
            
            # Create LAPSO directory
            LAPSO_DIR="/usr/local/lapso"
            sudo mkdir -p "$LAPSO_DIR"
            echo -e "${GREEN}✅ Created LAPSO directory${NC}"
            
            # Download agent
            AGENT_SCRIPT="$LAPSO_DIR/lapso-agent.sh"
            curl -f -s -o "$AGENT_SCRIPT" "$SERVER_URL/agents/macos/laptop-tracker-agent.sh"
            sudo chmod +x "$AGENT_SCRIPT"
            echo -e "${GREEN}✅ Agent downloaded${NC}"
            
            # Create config
            CONFIG_FILE="$LAPSO_DIR/config.json"
            sudo tee "$CONFIG_FILE" > /dev/null <<EOF
{
    "deviceId": "$DEVICE_ID",
    "userEmail": "$USER_EMAIL",
    "serverUrl": "$SERVER_URL",
    "updateInterval": 30,
    "enableLocationTracking": true,
    "enableSystemMonitoring": true,
    "enableNetworkMonitoring": true,
    "agentVersion": "1.0.0"
}
EOF
            
            echo -e "${GREEN}✅ Configuration saved${NC}"
            
            # Install LaunchDaemon
            PLIST_FILE="/Library/LaunchDaemons/com.lapso.agent.plist"
            sudo tee "$PLIST_FILE" > /dev/null <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.lapso.agent</string>
    <key>ProgramArguments</key>
    <array>
        <string>$AGENT_SCRIPT</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <true/>
</dict>
</plist>
EOF
            
            sudo launchctl load "$PLIST_FILE"
            
            echo -e "${GREEN}✅ LAPSO service installed and started${NC}"
            echo ""
            echo -e "${GREEN}🎉 Installation Complete!${NC}"
            echo -e "${GREEN}✅ Agent protecting your device 24/7${NC}"
            echo -e "${YELLOW}📊 View at: $SERVER_URL${NC}"
            echo -e "${YELLOW}⚠️ Grant location permissions in System Preferences${NC}"
            """, deviceId, deviceId, userEmail, serverUrl);
    }
}
