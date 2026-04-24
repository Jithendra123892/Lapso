#!/bin/bash

# LAPSO macOS Agent Installer
# Better than Microsoft Find My Device - Completely Free

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️ $1${NC}"
}

# Auto-detect Device ID and prompt for email if not provided
DEVICE_ID=$(system_profiler SPHardwareDataType | awk -F': ' '/Serial Number/{print $2}')
if [[ -z "$DEVICE_ID" ]]; then
    DEVICE_ID=$(scutil --get ComputerName 2>/dev/null)
fi

# Server URL (default)
SERVER_URL="${SERVER_URL:-http://localhost:8080}"

# Accept email as $1 for non-interactive; otherwise prompt
if [[ -n "$1" ]]; then
    USER_EMAIL="$1"
fi
if [[ -z "$USER_EMAIL" ]]; then
    read -r -p "Enter your LAPSO account email: " USER_EMAIL
fi
if [[ -z "$USER_EMAIL" ]]; then
    print_error "User email is required"
    exit 1
fi

echo -e "${GREEN}🛡️ LAPSO Agent Installer for macOS${NC}"
echo -e "${GREEN}===================================${NC}"
echo ""

print_info "Device ID: $DEVICE_ID"
print_info "User Email: $USER_EMAIL"
print_info "Server URL: $SERVER_URL"
echo ""

# Check for required tools
print_info "Checking system requirements..."

if ! command -v curl &> /dev/null; then
    print_error "curl is required but not installed"
    print_info "Install Xcode Command Line Tools: xcode-select --install"
    exit 1
fi

print_status "System requirements met"

# Request necessary permissions
print_info "LAPSO requires the following permissions:"
print_info "• Location Services (for device tracking)"
print_info "• Full Disk Access (for system monitoring)"
print_info "• Accessibility (for security features)"
echo ""
print_warning "Please grant these permissions when prompted by macOS"
echo ""

# Create LAPSO directory
LAPSO_DIR="/usr/local/lapso"
sudo mkdir -p "$LAPSO_DIR"
print_status "Created LAPSO directory: $LAPSO_DIR"

# Download agent script
print_info "Downloading LAPSO agent..."
AGENT_SCRIPT="$LAPSO_DIR/lapso-agent.sh"
AGENT_URL="$SERVER_URL/api/agents/download/macos/laptop-tracker-agent.sh"

if curl -f -s -o "$AGENT_SCRIPT" "$AGENT_URL"; then
    sudo chmod +x "$AGENT_SCRIPT"
    print_status "Agent downloaded successfully"
else
    print_error "Failed to download agent from $AGENT_URL"
    exit 1
fi

# Create configuration file
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

print_status "Configuration saved: $CONFIG_FILE"

# Create LaunchDaemon for system-wide service
print_info "Installing LaunchDaemon..."

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
    <dict>
        <key>SuccessfulExit</key>
        <false/>
    </dict>
    <key>StandardOutPath</key>
    <string>/var/log/lapso-agent.log</string>
    <key>StandardErrorPath</key>
    <string>/var/log/lapso-agent-error.log</string>
    <key>WorkingDirectory</key>
    <string>$LAPSO_DIR</string>
    <key>UserName</key>
    <string>root</string>
    <key>GroupName</key>
    <string>wheel</string>
    <key>ThrottleInterval</key>
    <integer>10</integer>
</dict>
</plist>
EOF

# Set proper permissions
sudo chown root:wheel "$PLIST_FILE"
sudo chmod 644 "$PLIST_FILE"

print_status "LaunchDaemon installed"

# Load and start the service
print_info "Starting LAPSO agent..."
sudo launchctl load "$PLIST_FILE"

# Check if service is running
sleep 3
if sudo launchctl list | grep -q "com.lapso.agent"; then
    print_status "LAPSO agent is running!"
else
    print_warning "LAPSO agent may not be running. Check logs: tail -f /var/log/lapso-agent.log"
fi

# Create menu bar app (optional)
print_info "Creating menu bar application..."

MENU_APP_DIR="/Applications/LAPSO.app"
sudo mkdir -p "$MENU_APP_DIR/Contents/MacOS"
sudo mkdir -p "$MENU_APP_DIR/Contents/Resources"

# Create Info.plist
sudo tee "$MENU_APP_DIR/Contents/Info.plist" > /dev/null <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>LAPSO</string>
    <key>CFBundleIdentifier</key>
    <string>com.lapso.menubar</string>
    <key>CFBundleName</key>
    <string>LAPSO</string>
    <key>CFBundleVersion</key>
    <string>1.0.0</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>LSUIElement</key>
    <true/>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# Create simple menu bar script
sudo tee "$MENU_APP_DIR/Contents/MacOS/LAPSO" > /dev/null <<EOF
#!/bin/bash
# Simple menu bar app for LAPSO
osascript -e "
tell application \"System Events\"
    display dialog \"🛡️ LAPSO Agent Status\\n\\nDevice ID: $DEVICE_ID\\nUser: $USER_EMAIL\\nStatus: Running\\n\\nView dashboard: $SERVER_URL\" buttons {\"OK\"} default button \"OK\"
end tell
"
EOF

sudo chmod +x "$MENU_APP_DIR/Contents/MacOS/LAPSO"

print_status "Menu bar app created"

# Create uninstaller
UNINSTALLER="$LAPSO_DIR/uninstall.sh"
sudo tee "$UNINSTALLER" > /dev/null <<EOF
#!/bin/bash
echo "🗑️ Uninstalling LAPSO Agent..."

# Read config to notify server
CONFIG_FILE="/usr/local/lapso/config.json"
if [ -f "\$CONFIG_FILE" ]; then
    DEVICE_ID=\$(grep -o '"deviceId": *"[^"]*"' "\$CONFIG_FILE" | sed 's/"deviceId": *"\([^"]*\)"/\1/')
    USER_EMAIL=\$(grep -o '"userEmail": *"[^"]*"' "\$CONFIG_FILE" | sed 's/"userEmail": *"\([^"]*\)"/\1/')
    SERVER_URL=\$(grep -o '"serverUrl": *"[^"]*"' "\$CONFIG_FILE" | sed 's/"serverUrl": *"\([^"]*\)"/\1/')
    
    if [ -n "\$DEVICE_ID" ] && [ -n "\$USER_EMAIL" ] && [ -n "\$SERVER_URL" ]; then
        curl -X POST "\$SERVER_URL/api/agent/uninstall" \\
            -H "Content-Type: application/json" \\
            -d "{\"deviceId\":\"\$DEVICE_ID\",\"userEmail\":\"\$USER_EMAIL\",\"reason\":\"User uninstalled\"}" \\
            --silent --show-error || echo "⚠️ Could not notify server (continuing with uninstall)"
        echo "✅ Notified server of uninstall"
    fi
fi

# Stop and unload LaunchDaemon
sudo launchctl unload /Library/LaunchDaemons/com.lapso.agent.plist 2>/dev/null || true
sudo rm -f /Library/LaunchDaemons/com.lapso.agent.plist

# Remove files
sudo rm -rf /usr/local/lapso
sudo rm -rf /Applications/LAPSO.app
sudo rm -f /var/log/lapso-agent*.log

echo "✅ LAPSO Agent uninstalled successfully"
echo "Note: You may need to manually revoke permissions in System Preferences > Security & Privacy"
EOF

sudo chmod +x "$UNINSTALLER"

# Request location permission
print_info "Requesting location permission..."
osascript -e 'tell application "System Events" to display dialog "LAPSO needs location access to track your device. Please allow location access in System Preferences > Security & Privacy > Privacy > Location Services." buttons {"OK"} default button "OK"' 2>/dev/null || true

# Final instructions
echo ""
echo -e "${GREEN}🎉 LAPSO Installation Complete!${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo -e "${CYAN}Device ID: $DEVICE_ID${NC}"
echo -e "${CYAN}User Email: $USER_EMAIL${NC}"
echo -e "${CYAN}Server URL: $SERVER_URL${NC}"
echo ""
print_status "Agent is now protecting your device 24/7"
print_status "Real-time updates every 30 seconds"
print_status "Better than Microsoft Find My Device"
print_status "Completely free and open source"
echo ""
echo -e "${YELLOW}📊 View your device at: $SERVER_URL${NC}"
echo -e "${YELLOW}🔧 Service management:${NC}"
echo -e "${YELLOW}   sudo launchctl list | grep lapso${NC}"
echo -e "${YELLOW}   sudo launchctl unload /Library/LaunchDaemons/com.lapso.agent.plist${NC}"
echo -e "${YELLOW}   sudo launchctl load /Library/LaunchDaemons/com.lapso.agent.plist${NC}"
echo -e "${YELLOW}📱 Menu bar app: /Applications/LAPSO.app${NC}"
echo -e "${YELLOW}🗑️ To uninstall: sudo $UNINSTALLER${NC}"
echo ""
print_warning "Important: Grant location and system permissions in System Preferences > Security & Privacy"
echo ""