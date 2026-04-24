#!/bin/bash

# LAPSO Linux Agent Installer
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

# Check if running as root
if [[ $EUID -eq 0 ]]; then
    print_error "This installer should not be run as root!"
    print_info "Run as a regular user with sudo privileges"
    exit 1
fi

# Auto-detect device ID and prompt for email if not provided
# Device ID: try system UUID or hostname
DEVICE_ID=$(sudo dmidecode -s system-uuid 2>/dev/null | tr '[:lower:]' '[:upper:]')
if [[ -z "$DEVICE_ID" || "$DEVICE_ID" == "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF" ]]; then
    DEVICE_ID=$(hostname)
fi

# Server URL (default to local)
SERVER_URL="${SERVER_URL:-http://localhost:8080}"

# User email: accept as $1 for non-interactive; otherwise prompt
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

echo -e "${GREEN}🛡️ LAPSO Agent Installer${NC}"
echo -e "${GREEN}=========================${NC}"
echo ""

print_info "Device ID: $DEVICE_ID"
print_info "User Email: $USER_EMAIL"
print_info "Server URL: $SERVER_URL"
echo ""

# Check for required tools
print_info "Checking system requirements..."

if ! command -v curl &> /dev/null; then
    print_error "curl is required but not installed"
    print_info "Install with: sudo apt-get install curl (Ubuntu/Debian) or sudo yum install curl (CentOS/RHEL)"
    exit 1
fi

if ! command -v systemctl &> /dev/null; then
    print_warning "systemctl not found. Will use alternative service management."
fi

print_status "System requirements met"

# Create LAPSO directory
LAPSO_DIR="/opt/lapso"
sudo mkdir -p "$LAPSO_DIR"
print_status "Created LAPSO directory: $LAPSO_DIR"

# Download agent script
print_info "Downloading LAPSO agent..."
AGENT_SCRIPT="$LAPSO_DIR/lapso-agent.sh"
AGENT_URL="$SERVER_URL/api/agents/download/linux/laptop-tracker-agent.sh"

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

# Create systemd service
if command -v systemctl &> /dev/null; then
    print_info "Installing systemd service..."
    
    SERVICE_FILE="/etc/systemd/system/lapso-agent.service"
    sudo tee "$SERVICE_FILE" > /dev/null <<EOF
[Unit]
Description=LAPSO Laptop Security Agent
Documentation=https://github.com/lapso-project/lapso
After=network.target
Wants=network.target

[Service]
Type=simple
User=root
Group=root
ExecStart=$AGENT_SCRIPT
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=lapso-agent

# Security settings
NoNewPrivileges=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=$LAPSO_DIR /var/log

[Install]
WantedBy=multi-user.target
EOF

    # Reload systemd and enable service
    sudo systemctl daemon-reload
    sudo systemctl enable lapso-agent.service
    print_status "Systemd service installed"
    
    # Start the service
    print_info "Starting LAPSO agent..."
    sudo systemctl start lapso-agent.service
    
    # Check status
    sleep 3
    if sudo systemctl is-active --quiet lapso-agent.service; then
        print_status "LAPSO agent is running!"
    else
        print_warning "LAPSO agent may not be running. Check with: sudo systemctl status lapso-agent"
    fi
    
else
    # Fallback for systems without systemd
    print_info "Installing as init script..."
    
    INIT_SCRIPT="/etc/init.d/lapso-agent"
    sudo tee "$INIT_SCRIPT" > /dev/null <<EOF
#!/bin/bash
### BEGIN INIT INFO
# Provides:          lapso-agent
# Required-Start:    \$network \$remote_fs \$syslog
# Required-Stop:     \$network \$remote_fs \$syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: LAPSO Laptop Security Agent
# Description:       Free laptop tracking and security service
### END INIT INFO

DAEMON="$AGENT_SCRIPT"
PIDFILE="/var/run/lapso-agent.pid"

case "\$1" in
    start)
        echo "Starting LAPSO agent..."
        start-stop-daemon --start --quiet --pidfile \$PIDFILE --exec \$DAEMON --background --make-pidfile
        ;;
    stop)
        echo "Stopping LAPSO agent..."
        start-stop-daemon --stop --quiet --pidfile \$PIDFILE
        rm -f \$PIDFILE
        ;;
    restart)
        \$0 stop
        sleep 2
        \$0 start
        ;;
    status)
        if [ -f \$PIDFILE ]; then
            echo "LAPSO agent is running (PID: \$(cat \$PIDFILE))"
        else
            echo "LAPSO agent is not running"
        fi
        ;;
    *)
        echo "Usage: \$0 {start|stop|restart|status}"
        exit 1
        ;;
esac

exit 0
EOF

    sudo chmod +x "$INIT_SCRIPT"
    
    # Enable service
    if command -v update-rc.d &> /dev/null; then
        sudo update-rc.d lapso-agent defaults
    elif command -v chkconfig &> /dev/null; then
        sudo chkconfig --add lapso-agent
        sudo chkconfig lapso-agent on
    fi
    
    # Start service
    sudo "$INIT_SCRIPT" start
    print_status "Init script installed and started"
fi

# Create uninstaller
UNINSTALLER="$LAPSO_DIR/uninstall.sh"
sudo tee "$UNINSTALLER" > /dev/null <<EOF
#!/bin/bash
echo "🗑️ Uninstalling LAPSO Agent..."

# Read config to notify server
CONFIG_FILE="/opt/lapso/config.json"
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

# Stop service
if command -v systemctl &> /dev/null; then
    sudo systemctl stop lapso-agent.service 2>/dev/null || true
    sudo systemctl disable lapso-agent.service 2>/dev/null || true
    sudo rm -f /etc/systemd/system/lapso-agent.service
    sudo systemctl daemon-reload
else
    sudo /etc/init.d/lapso-agent stop 2>/dev/null || true
    sudo rm -f /etc/init.d/lapso-agent
    if command -v update-rc.d &> /dev/null; then
        sudo update-rc.d lapso-agent remove
    elif command -v chkconfig &> /dev/null; then
        sudo chkconfig --del lapso-agent
    fi
fi

# Remove files
sudo rm -rf /opt/lapso

echo "✅ LAPSO Agent uninstalled successfully"
EOF

sudo chmod +x "$UNINSTALLER"

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
if command -v systemctl &> /dev/null; then
    echo -e "${YELLOW}   sudo systemctl status lapso-agent${NC}"
    echo -e "${YELLOW}   sudo systemctl restart lapso-agent${NC}"
else
    echo -e "${YELLOW}   sudo /etc/init.d/lapso-agent status${NC}"
    echo -e "${YELLOW}   sudo /etc/init.d/lapso-agent restart${NC}"
fi
echo -e "${YELLOW}🗑️ To uninstall: sudo $UNINSTALLER${NC}"
echo ""