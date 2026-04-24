# LAPSO - Comprehensive Documentation

## Table of Contents
1. [Overview](#overview)
2. [Installation](#installation)
3. [Remote Control Features](#remote-control-features)
4. [Agent Updates](#agent-updates)
5. [Uninstalling Agents](#uninstalling-agents)
6. [Technical Details](#technical-details)
7. [Security Features](#security-features)
8. [Emergency Procedures](#emergency-procedures)

## Overview

LAPSO is a free, open-source laptop tracking system that helps you locate and secure your devices in case of theft or loss. It provides real-time tracking with 3-5 meter GPS accuracy and offers comprehensive remote control capabilities.

## Installation

### Server Setup

1. Install Java 17 or higher
2. Clone this repository
3. Run with: `./mvnw spring-boot:run`
4. Access at: http://localhost:8080

### Device Agent Installation

#### Windows
```powershell
# Download and run the installer
$base = 'http://localhost:8080'
$ProgressPreference='SilentlyContinue'
Invoke-WebRequest -UseBasicParsing -Uri "$base/api/agents/download/windows/lapso-installer.ps1" -OutFile 'lapso-installer.ps1'
PowerShell -ExecutionPolicy Bypass -File .\lapso-installer.ps1
```

#### macOS
```bash
curl -fsSL 'http://localhost:8080/api/agents/download/macos/lapso-installer.sh' -o lapso-installer.sh
chmod +x lapso-installer.sh
sudo ./lapso-installer.sh
```

#### Linux
```bash
curl -fsSL 'http://localhost:8080/api/agents/download/linux/lapso-installer.sh' -o lapso-installer.sh
chmod +x lapso-installer.sh
sudo ./lapso-installer.sh
```

## Remote Control Features

All agents support these remote commands:

### 🔒 Remote Lock
Instantly lock device screen with PIN/Password required to unlock.
- **Windows**: Uses multiple methods (rundll32, VBS script, Win32 API, Scheduled Task)
- **macOS**: Uses pmset, AppleScript, and screen saver methods
- **Linux**: Uses loginctl, GNOME/KDE screensavers, and xset methods

### 🚨 Sound Alarm
Play loud beeping sound (10 seconds) to help locate device.
- **Windows**: Plays alternating beeps at 800Hz and 1200Hz
- **macOS**: Plays system sound using afplay
- **Linux**: Uses paplay or console beep

### 📸 Screenshot
Capture current screen and upload to dashboard.
- **Windows**: Uses .NET Graphics API
- **macOS**: Uses screencapture command
- **Linux**: Uses gnome-screenshot, scrot, or ImageMagick

### 📷 Camera Photo
Capture photo from device camera (falls back to screenshot if no camera).
- **Windows**: Uses Win32 API for webcam capture
- **macOS**: Uses imagesnap, ffmpeg, or fswebcam
- **Linux**: Uses fswebcam, ffmpeg, or streamer

### 🗑️ Emergency Wipe
Securely erase all user data with 3 overwrite passes (DANGEROUS).
- **All platforms**: Overwrites files with random data, zeros, and ones before deletion
- **Windows**: Also clears browser data and system caches
- **macOS**: Also clears keychains and credentials
- **Linux**: Also clears system credentials and keys

### 📍 Locate Device
Get current GPS/IP-based location with 3-5 meter accuracy.
- **Windows**: Uses Windows Location API with fallback to IP geolocation
- **macOS**: Uses Core Location with fallback to IP geolocation
- **Linux**: Uses GPS/GNSS with fallback to IP geolocation

## Agent Updates

Agents automatically check for updates every 30 minutes. To manually update:

1. Download the latest agent from the web interface
2. Stop your current agent (Ctrl+C in terminal)
3. Run the new agent with your device ID and email
4. Verify you see "Agent is running - sending heartbeats and polling for commands"

## Uninstalling Agents

### Windows
Run PowerShell as Administrator:
```powershell
& "C:\Program Files\LAPSO\uninstall.ps1"
```

### macOS
Run in Terminal:
```bash
sudo /usr/local/lapso/uninstall.sh
```

### Linux
Run in Terminal:
```bash
sudo /opt/lapso/uninstall.sh
```

### Manual Removal (if uninstall script fails)
- **Windows**: Stop LAPSO service, delete C:\Program Files\LAPSO folder, remove scheduled task
- **macOS**: Delete /usr/local/lapso directory, remove launch daemon
- **Linux**: Delete /opt/lapso directory, remove systemd service
- **All platforms**: Remove device from your dashboard

## Technical Details

### Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Device Agent  │───▶│  LAPSO Server   │───▶│    Database     │
│  (Cross-platform)│    │ (Spring Boot)   │    │  (H2/PostgreSQL)│
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │  Web Dashboard  │
                       │    (Vaadin)     │
                       └─────────────────┘
```

### Agent Capabilities

**Windows Agent (PowerShell):**
- Multiple lock methods (rundll32, VBS script, Win32 API, Scheduled Task)
- Webcam capture using Win32 API
- Secure data wiping with 3 overwrite passes
- Self-updating mechanism

**macOS Agent (Bash):**
- Multiple lock methods (pmset, AppleScript, screencapture)
- Camera capture using imagesnap/ffmpeg/fswebcam
- Secure data wiping with 3 overwrite passes
- Keychain and credential clearing

**Linux Agent (Bash):**
- Multiple lock methods (loginctl, GNOME/KDE screensavers, xset)
- Camera capture using fswebcam/ffmpeg/streamer
- Secure data wiping with 3 overwrite passes
- System credential clearing

## Security Features

- All data transmission encrypted
- Secure device authentication
- Multiple overwrite passes for data wiping
- PIN/password required for device unlock
- Automatic agent updates for security patches

## Emergency Procedures

If your device is stolen:

1. **Immediately lock** the device remotely
2. **Play sound alarm** to help locate it nearby
3. **Take screenshot** to see what the thief is doing
4. **Capture camera photo** to potentially identify the thief
5. **Set geofence** alerts for location monitoring
6. **As last resort, wipe** all data securely