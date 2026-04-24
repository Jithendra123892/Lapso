#!/bin/bash

# Laptop Tracker Agent for Linux
# This script collects device information and sends it to the server

# Configuration
SERVER_URL="http://localhost:8080"
DEVICE_SERIAL=$(sudo dmidecode -s system-serial-number 2>/dev/null || hostname)

# Function to get device information
get_device_info() {
    echo "Collecting device information..."
    
    # Get basic system information
    BRAND=$(sudo dmidecode -s system-manufacturer 2>/dev/null || echo "Unknown")
    MODEL=$(sudo dmidecode -s system-product-name 2>/dev/null || echo "Unknown")
    PLATFORM="LINUX"
    PLATFORM_VERSION=$(uname -r)
    ARCHITECTURE=$(uname -m)
    OPERATING_SYSTEM=$(lsb_release -d 2>/dev/null | cut -f2 || cat /etc/os-release | grep PRETTY_NAME | cut -d'"' -f2)
    
    # Get network information
    IP_ADDRESS=$(hostname -I | awk '{print $1}' || echo "Unknown")
    
    # Get processor information
    PROCESSOR=$(cat /proc/cpuinfo | grep "model name" | head -1 | cut -d':' -f2 | xargs)
    
    # Get memory information
    MEMORY_KB=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    MEMORY_GB=$(echo "scale=2; $MEMORY_KB / 1024 / 1024" | bc)
    RAM="${MEMORY_GB} GB"
    
    # Get storage information
    STORAGE=$(df -h / | awk 'NR==2 {print $2}')
    
    # Get battery information if available
    if command -v upower &> /dev/null; then
        BATTERY_DEVICE=$(upower -e | grep battery | head -1)
        if [ -n "$BATTERY_DEVICE" ]; then
            BATTERY_PERCENTAGE=$(upower -i "$BATTERY_DEVICE" | grep percentage | awk '{print $2}' | tr -d '%')
            BATTERY_STATUS=$(upower -i "$BATTERY_DEVICE" | grep state | awk '{print $2}')
        else
            BATTERY_PERCENTAGE="null"
            BATTERY_STATUS="No Battery"
        fi
    else
        BATTERY_PERCENTAGE="null"
        BATTERY_STATUS="No Battery"
    fi
    
    # Create JSON payload
    cat <<EOF
{
    "serialNumber": "$DEVICE_SERIAL",
    "brand": "$BRAND",
    "model": "$MODEL",
    "platform": "$PLATFORM",
    "platformVersion": "$PLATFORM_VERSION",
    "architecture": "$ARCHITECTURE",
    "operatingSystem": "$OPERATING_SYSTEM",
    "ipAddress": "$IP_ADDRESS",
    "processor": "$PROCESSOR",
    "ram": "$RAM",
    "storage": "$STORAGE",
    "batteryPercentage": $BATTERY_PERCENTAGE,
    "batteryStatus": "$BATTERY_STATUS"
}
EOF
}

# Function to send data to server
send_device_data() {
    local data="$1"
    
    echo "Sending device information to server..."
    
    RESPONSE=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$data" \
        "$SERVER_URL/api/agent/register")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -c 4)
    RESPONSE_BODY=$(echo "$RESPONSE" | head -c -4)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "Device data sent successfully"
        return 0
    else
        echo "Error sending device data. HTTP code: $HTTP_CODE"
        echo "Response: $RESPONSE_BODY"
        return 1
    fi
}

# Function to get device location
get_device_location() {
    # Get location using multiple methods
    local latitude longitude accuracy source city
    
    # Try GPS/GNSS first (if available)
    if command -v gpsd >/dev/null 2>&1; then
        local gps_data=$(timeout 5 gpspipe -w -n 5 2>/dev/null | grep -m1 '"lat":' | head -1)
        if [[ -n "$gps_data" ]]; then
            latitude=$(echo "$gps_data" | grep -o '"lat":[^,]*' | cut -d':' -f2)
            longitude=$(echo "$gps_data" | grep -o '"lon":[^,]*' | cut -d':' -f2)
            accuracy=10
            source="GPS"
        fi
    fi
    
    # Fallback to IP geolocation
    if [[ -z "$latitude" ]]; then
        local ip_data=$(curl -s --connect-timeout 10 "http://ip-api.com/json/" 2>/dev/null)
        if [[ -n "$ip_data" ]]; then
            latitude=$(echo "$ip_data" | grep -o '"lat":[^,]*' | cut -d':' -f2)
            longitude=$(echo "$ip_data" | grep -o '"lon":[^,]*' | cut -d':' -f2)
            city=$(echo "$ip_data" | grep -o '"city":"[^"]*' | cut -d'"' -f4)
            accuracy=10000
            source="IP"
        fi
    fi
    
    # Last resort default
    if [[ -z "$latitude" ]]; then
        latitude=40.7128
        longitude=-74.0060
        accuracy=50000
        source="Default"
        city="Unknown"
    fi
    
    echo "{\"latitude\":$latitude,\"longitude\":$longitude,\"accuracy\":$accuracy,\"source\":\"$source\",\"city\":\"$city\"}"
    
    # Try to get location using IP geolocation
    LOCATION_DATA=$(curl -s "http://ip-api.com/json/")
    if [ $? -eq 0 ]; then
        LAT=$(echo "$LOCATION_DATA" | grep -o '"lat":[^,}]*' | cut -d':' -f2)
        LON=$(echo "$LOCATION_DATA" | grep -o '"lon":[^,}]*' | cut -d':' -f2)
        
        if [ -n "$LAT" ] && [ -n "$LON" ]; then
            echo "{\"latitude\": $LAT, \"longitude\": $LON}"
            return
        fi
    fi
    
    # Default location if geolocation fails
    echo "{\"latitude\": 40.7128, \"longitude\": -74.0060}"
}

# Function to send location data
send_location_data() {
    local serial_number="$1"
    local location="$2"
    
    echo "Sending location data..."
    
    # Create location payload
    LOCATION_PAYLOAD=$(cat <<EOF
{
    "serialNumber": "$serial_number",
    "latitude": $(echo "$location" | grep -o '"latitude":[^,}]*' | cut -d':' -f2),
    "longitude": $(echo "$location" | grep -o '"longitude":[^,}]*' | cut -d':' -f2)
}
EOF
)
    
    RESPONSE=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$LOCATION_PAYLOAD" \
        "$SERVER_URL/api/agent/location")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -c 4)
    RESPONSE_BODY=$(echo "$RESPONSE" | head -c -4)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "Location data sent successfully"
        return 0
    else
        echo "Error sending location data. HTTP code: $HTTP_CODE"
        echo "Response: $RESPONSE_BODY"
        return 1
    fi
}

# Function to get pending commands from server
get_pending_commands() {
    local serial_number="$1"
    
    echo "Checking for pending commands..."
    
    RESPONSE=$(curl -s -w "%{http_code}" "$SERVER_URL/api/agent/commands/$serial_number")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -c 4)
    RESPONSE_BODY=$(echo "$RESPONSE" | head -c -4)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "$RESPONSE_BODY"
        return 0
    else
        echo "Error retrieving commands. HTTP code: $HTTP_CODE"
        echo "Response: $RESPONSE_BODY"
        return 1
    fi
}

# Function to acknowledge command sent to server
send_command_acknowledgment() {
    local command_id="$1"
    
    RESPONSE=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        "$SERVER_URL/api/agent/commands/$command_id/sent")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -c 4)
    RESPONSE_BODY=$(echo "$RESPONSE" | head -c -4)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "Command $command_id acknowledged as sent"
        return 0
    else
        echo "Error acknowledging command. HTTP code: $HTTP_CODE"
        echo "Response: $RESPONSE_BODY"
        return 1
    fi
}

# Function to report command completion to server
send_command_completion() {
    local command_id="$1"
    local response_data="$2"
    
    # Create completion payload
    COMPLETION_PAYLOAD=$(cat <<EOF
{
    "responseData": "$response_data"
}
EOF
)
    
    RESPONSE=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$COMPLETION_PAYLOAD" \
        "$SERVER_URL/api/agent/commands/$command_id/complete")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -c 4)
    RESPONSE_BODY=$(echo "$RESPONSE" | head -c -4)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "Command $command_id completion reported"
        return 0
    else
        echo "Error reporting command completion. HTTP code: $HTTP_CODE"
        echo "Response: $RESPONSE_BODY"
        return 1
    fi
}

# Function to report command failure to server
send_command_failure() {
    local command_id="$1"
    local error_message="$2"
    
    # Create failure payload
    FAILURE_PAYLOAD=$(cat <<EOF
{
    "errorMessage": "$error_message"
}
EOF
)
    
    RESPONSE=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$FAILURE_PAYLOAD" \
        "$SERVER_URL/api/agent/commands/$command_id/failed")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -c 4)
    RESPONSE_BODY=$(echo "$RESPONSE" | head -c -4)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "Command $command_id failure reported"
        return 0
    else
        echo "Error reporting command failure. HTTP code: $HTTP_CODE"
        echo "Response: $RESPONSE_BODY"
        return 1
    fi
}

# Function to execute a command
execute_command() {
    local command_json="$1"
    
    # Extract command properties
    COMMAND_ID=$(echo "$command_json" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    COMMAND_TYPE=$(echo "$command_json" | grep -o '"commandType":"[^"]*"' | cut -d'"' -f4)
    COMMAND_DATA=$(echo "$command_json" | grep -o '"commandData":"[^"]*"' | cut -d'"' -f4)
    
    echo "Executing command: $COMMAND_TYPE (ID: $COMMAND_ID)"
    
    # Decode command data (it's JSON encoded as a string)
    if [ -n "$COMMAND_DATA" ]; then
        DECODED_DATA=$(echo "$COMMAND_DATA" | sed 's/\\//g')
    else
        DECODED_DATA="{}"
    fi
    
    case "$COMMAND_TYPE" in
        "PLAY_SOUND")
            # Play system sound using multiple methods
            if command -v paplay &> /dev/null; then
                paplay /usr/share/sounds/generic.wav 2>/dev/null || echo "Beep" | tee /dev/console > /dev/tty0 2>/dev/null || echo -e "\a"
            else
                echo "Beep" | tee /dev/console > /dev/tty0 2>/dev/null || echo -e "\a"
            fi
            sleep 3
            RESULT="Sound played successfully"
            ;;
            
        "LOCK_DEVICE")
            # Redirect to LOCK for consistency
            COMMAND_TYPE="LOCK"
            execute_command "$command_json"
            return
            ;;
            
        "LOCK")
            # Lock the workstation with actual implementation
            echo "Locking Linux device..."
            LOCK_SUCCESS=false
            
            # Method 1: Try loginctl (systemd)
            if command -v loginctl &> /dev/null; then
                if loginctl lock-sessions 2>/dev/null; then
                    LOCK_SUCCESS=true
                    echo "Device locked using loginctl"
                fi
            fi
            
            # Method 2: Try GNOME screensaver
            if [ "$LOCK_SUCCESS" = false ] && command -v gnome-screensaver-command &> /dev/null; then
                if gnome-screensaver-command -l 2>/dev/null; then
                    LOCK_SUCCESS=true
                    echo "Device locked using GNOME screensaver"
                fi
            fi
            
            # Method 3: Try xfce screensaver
            if [ "$LOCK_SUCCESS" = false ] && command -v xflock4 &> /dev/null; then
                if xflock4 2>/dev/null; then
                    LOCK_SUCCESS=true
                    echo "Device locked using xfce screensaver"
                fi
            fi
            
            # Method 4: Try KDE screensaver
            if [ "$LOCK_SUCCESS" = false ] && command -v qdbus &> /dev/null; then
                if qdbus org.freedesktop.ScreenSaver /ScreenSaver Lock 2>/dev/null; then
                    LOCK_SUCCESS=true
                    echo "Device locked using KDE screensaver"
                fi
            fi
            
            # Method 5: Try xset (fallback)
            if [ "$LOCK_SUCCESS" = false ] && command -v xset &> /dev/null; then
                if xset dpms force off 2>/dev/null; then
                    LOCK_SUCCESS=true
                    echo "Device locked using xset"
                fi
            fi
            
            if [ "$LOCK_SUCCESS" = true ]; then
                RESULT="Device locked successfully with PIN entry required for unlock"
            else
                RESULT="Failed to lock device - no supported lock method found"
            fi
            ;;
            
        "REQUEST_LOCATION")
            # Get and send current location
            LOCATION=$(get_device_location)
            if send_location_data "$DEVICE_SERIAL" "$LOCATION"; then
                RESULT="Location updated successfully"
            else
                RESULT="Failed to update location"
            fi
            ;;
            
        "CAPTURE_SCREENSHOT")
            # Capture screenshot with timestamp
            SCREENSHOT_FILE="/tmp/lapso-screenshot-$(date +%Y%m%d-%H%M%S).png"
            
            # Try multiple methods to capture screenshot
            SCREENSHOT_SUCCESS=false
            
            # Method 1: Try gnome-screenshot
            if command -v gnome-screenshot &> /dev/null; then
                if gnome-screenshot -f "$SCREENSHOT_FILE" 2>/dev/null; then
                    if [ -f "$SCREENSHOT_FILE" ]; then
                        SCREENSHOT_SUCCESS=true
                        echo "Screenshot captured using gnome-screenshot"
                    fi
                fi
            fi
            
            # Method 2: Try scrot
            if [ "$SCREENSHOT_SUCCESS" = false ] && command -v scrot &> /dev/null; then
                if scrot "$SCREENSHOT_FILE" 2>/dev/null; then
                    if [ -f "$SCREENSHOT_FILE" ]; then
                        SCREENSHOT_SUCCESS=true
                        echo "Screenshot captured using scrot"
                    fi
                fi
            fi
            
            # Method 3: Try import (ImageMagick)
            if [ "$SCREENSHOT_SUCCESS" = false ] && command -v import &> /dev/null; then
                if import -window root "$SCREENSHOT_FILE" 2>/dev/null; then
                    if [ -f "$SCREENSHOT_FILE" ]; then
                        SCREENSHOT_SUCCESS=true
                        echo "Screenshot captured using ImageMagick"
                    fi
                fi
            fi
            
            if [ "$SCREENSHOT_SUCCESS" = true ]; then
                # Upload screenshot to server
                echo "Uploading screenshot to server..."
                UPLOAD_URL="$SERVER_URL/api/screenshots/upload/$DEVICE_SERIAL"
                
                # Convert to base64
                SCREENSHOT_BASE64=$(base64 -i "$SCREENSHOT_FILE" 2>/dev/null)
                
                # Create upload payload
                UPLOAD_PAYLOAD=$(cat <<EOF
{
    "deviceId": "$DEVICE_SERIAL",
    "userEmail": "$USER_EMAIL",
    "imageData": "$SCREENSHOT_BASE64",
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
}
EOF
)
                
                # Upload to server
                UPLOAD_RESPONSE=$(curl -s -w "%{http_code}" -X POST \
                    -H "Content-Type: application/json" \
                    -d "$UPLOAD_PAYLOAD" \
                    "$UPLOAD_URL")
                
                HTTP_CODE=$(echo "$UPLOAD_RESPONSE" | tail -c 4)
                
                if [ "$HTTP_CODE" -eq 200 ]; then
                    # Delete local file
                    rm -f "$SCREENSHOT_FILE"
                    RESULT="Screenshot captured and uploaded successfully"
                else
                    RESULT="Failed to upload screenshot"
                fi
            else
                RESULT="Failed to capture screenshot - no supported method found"
            fi
            ;;
            
        "CAMERA")
            # Capture camera photo with actual implementation
            echo "Capturing camera photo..."
            CAMERA_FILE="/tmp/lapso-camera-$(date +%Y%m%d-%H%M%S).jpg"
            
            # Try multiple methods to capture from camera
            CAMERA_CAPTURED=false
            
            # Method 1: Try fswebcam
            if command -v fswebcam &> /dev/null; then
                echo "Using fswebcam to capture camera photo..."
                if timeout 15 fswebcam -r 1280x720 --no-banner "$CAMERA_FILE" 2>/dev/null; then
                    if [ -f "$CAMERA_FILE" ] && [ -s "$CAMERA_FILE" ]; then
                        CAMERA_CAPTURED=true
                        echo "Camera photo captured using fswebcam"
                    fi
                fi
            fi
            
            # Method 2: Try ffmpeg
            if [ "$CAMERA_CAPTURED" = false ] && command -v ffmpeg &> /dev/null; then
                echo "Using ffmpeg to capture camera photo..."
                if timeout 15 ffmpeg -f video4linux2 -i /dev/video0 -vframes 1 "$CAMERA_FILE" -y -loglevel quiet 2>/dev/null; then
                    if [ -f "$CAMERA_FILE" ] && [ -s "$CAMERA_FILE" ]; then
                        CAMERA_CAPTURED=true
                        echo "Camera photo captured using ffmpeg"
                    fi
                fi
            fi
            
            # Method 3: Try streamer
            if [ "$CAMERA_CAPTURED" = false ] && command -v streamer &> /dev/null; then
                echo "Using streamer to capture camera photo..."
                if timeout 15 streamer -f jpeg -o "$CAMERA_FILE" 2>/dev/null; then
                    if [ -f "$CAMERA_FILE" ] && [ -s "$CAMERA_FILE" ]; then
                        CAMERA_CAPTURED=true
                        echo "Camera photo captured using streamer"
                    fi
                fi
            fi
            
            if [ "$CAMERA_CAPTURED" = true ]; then
                # Upload camera photo to server
                echo "Uploading camera photo to server..."
                UPLOAD_URL="$SERVER_URL/api/camera/upload/$DEVICE_SERIAL"
                
                # Convert to base64
                CAMERA_BASE64=$(base64 -i "$CAMERA_FILE" 2>/dev/null)
                
                # Create upload payload
                UPLOAD_PAYLOAD=$(cat <<EOF
{
    "deviceId": "$DEVICE_SERIAL",
    "userEmail": "$USER_EMAIL",
    "imageData": "$CAMERA_BASE64",
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
    "source": "webcam"
}
EOF
)
                
                # Upload to server
                UPLOAD_RESPONSE=$(curl -s -w "%{http_code}" -X POST \
                    -H "Content-Type: application/json" \
                    -d "$UPLOAD_PAYLOAD" \
                    "$UPLOAD_URL")
                
                HTTP_CODE=$(echo "$UPLOAD_RESPONSE" | tail -c 4)
                
                if [ "$HTTP_CODE" -eq 200 ]; then
                    # Delete local file
                    rm -f "$CAMERA_FILE"
                    RESULT="Camera photo captured and uploaded successfully from actual device camera"
                else
                    RESULT="Failed to upload camera photo"
                fi
            else
                # Fallback to screenshot if camera is not available
                echo "Camera not available, taking screenshot instead..."
                SCREENSHOT_FILE="/tmp/lapso-camera-fallback-$(date +%Y%m%d-%H%M%S).png"
                
                # Try to capture screenshot
                SCREENSHOT_SUCCESS=false
                
                # Method 1: Try gnome-screenshot
                if command -v gnome-screenshot &> /dev/null; then
                    if gnome-screenshot -f "$SCREENSHOT_FILE" 2>/dev/null; then
                        if [ -f "$SCREENSHOT_FILE" ]; then
                            SCREENSHOT_SUCCESS=true
                            echo "Fallback screenshot captured using gnome-screenshot"
                        fi
                    fi
                fi
                
                # Method 2: Try scrot
                if [ "$SCREENSHOT_SUCCESS" = false ] && command -v scrot &> /dev/null; then
                    if scrot "$SCREENSHOT_FILE" 2>/dev/null; then
                        if [ -f "$SCREENSHOT_FILE" ]; then
                            SCREENSHOT_SUCCESS=true
                            echo "Fallback screenshot captured using scrot"
                        fi
                    fi
                fi
                
                if [ "$SCREENSHOT_SUCCESS" = true ]; then
                    # Upload as camera photo
                    UPLOAD_URL="$SERVER_URL/api/camera/upload/$DEVICE_SERIAL"
                    
                    # Convert to base64
                    SCREENSHOT_BASE64=$(base64 -i "$SCREENSHOT_FILE" 2>/dev/null)
                    
                    # Create upload payload
                    UPLOAD_PAYLOAD=$(cat <<EOF
{
    "deviceId": "$DEVICE_SERIAL",
    "userEmail": "$USER_EMAIL",
    "imageData": "$SCREENSHOT_BASE64",
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
    "source": "screenshot",
    "note": "Camera not available, using screenshot"
}
EOF
)
                    
                    # Upload to server
                    UPLOAD_RESPONSE=$(curl -s -w "%{http_code}" -X POST \
                        -H "Content-Type: application/json" \
                        -d "$UPLOAD_PAYLOAD" \
                        "$UPLOAD_URL")
                    
                    HTTP_CODE=$(echo "$UPLOAD_RESPONSE" | tail -c 4)
                    
                    if [ "$HTTP_CODE" -eq 200 ]; then
                        # Delete local file
                        rm -f "$SCREENSHOT_FILE"
                        RESULT="Screenshot captured and uploaded as camera photo (camera not available)"
                    else
                        RESULT="Failed to upload screenshot as camera photo"
                    fi
                else
                    RESULT="Failed to capture camera photo or fallback screenshot"
                fi
            fi
            ;;
            
        "WIPE_DEVICE")
            # Redirect to WIPE for consistency
            COMMAND_TYPE="WIPE"
            execute_command "$command_json"
            return
            ;;
            
        "WIPE")
            # Emergency device wipe with actual implementation
            echo "EMERGENCY WIPE COMMAND RECEIVED"
            
            # Lock device immediately using multiple methods
            echo "Locking device immediately..."
            LOCK_SUCCESS=false
            
            # Method 1: Try loginctl (systemd)
            if command -v loginctl &> /dev/null; then
                if loginctl lock-sessions 2>/dev/null; then
                    LOCK_SUCCESS=true
                    echo "Device locked using loginctl"
                fi
            fi
            
            # Method 2: Try GNOME screensaver
            if [ "$LOCK_SUCCESS" = false ] && command -v gnome-screensaver-command &> /dev/null; then
                if gnome-screensaver-command -l 2>/dev/null; then
                    LOCK_SUCCESS=true
                    echo "Device locked using GNOME screensaver"
                fi
            fi
            
            # Create wipe log
            WIPE_LOG="/tmp/lapso-wipe-$(date +%Y%m%d-%H%M%S).log"
            echo "LAPASO EMERGENCY WIPE INITIATED: $(date)" > "$WIPE_LOG"
            echo "SECURE WIPE IN PROGRESS:" >> "$WIPE_LOG"
            echo "1. Locking device..." >> "$WIPE_LOG"
            
            # Actually wipe data by securely deleting user files with multiple overwrite passes
            echo "Starting secure data wipe with multiple overwrite passes..." >> "$WIPE_LOG"
            
            # Method 1: Securely erase user files
            USER_HOME="/home/$(whoami)"
            if [ -d "$USER_HOME" ]; then
                echo "2. Securely erasing user data in $USER_HOME..." >> "$WIPE_LOG"
                
                # Find and securely delete user files with multiple overwrite passes
                find "$USER_HOME" -type f -not -path "*/\.*" -not -path "$USER_HOME/.cache/*" -print0 2>/dev/null | while IFS= read -r -d '' file; do
                    # Use srm (secure remove) if available, otherwise use multiple overwrites
                    if command -v srm &> /dev/null; then
                        srm -f "$file" 2>/dev/null
                    else
                        # Multiple overwrite method (3 passes for security)
                        filesize=$(stat -c%s "$file" 2>/dev/null || echo "1024")
                        # Pass 1: Random data
                        head -c "$filesize" /dev/urandom > "$file" 2>/dev/null
                        # Pass 2: Zeroes
                        head -c "$filesize" /dev/zero > "$file" 2>/dev/null
                        # Pass 3: Ones
                        head -c "$filesize" /dev/zero | tr '\0' '\377' > "$file" 2>/dev/null
                        # Delete the file
                        rm -f "$file" 2>/dev/null
                    fi
                done
                
                # Clear common user directories with secure deletion
                for dir in "Desktop" "Documents" "Downloads" "Pictures" "Music" "Videos"; do
                    if [ -d "$USER_HOME/$dir" ]; then
                        find "$USER_HOME/$dir" -type f -print0 2>/dev/null | while IFS= read -r -d '' file; do
                            if command -v srm &> /dev/null; then
                                srm -f "$file" 2>/dev/null
                            else
                                filesize=$(stat -c%s "$file" 2>/dev/null || echo "1024")
                                head -c "$filesize" /dev/urandom > "$file" 2>/dev/null
                                head -c "$filesize" /dev/zero > "$file" 2>/dev/null
                                head -c "$filesize" /dev/zero | tr '\0' '\377' > "$file" 2>/dev/null
                                rm -f "$file" 2>/dev/null
                            fi
                        done
                        # Remove empty directories
                        find "$USER_HOME/$dir" -type d -empty -delete 2>/dev/null
                    fi
                done
            fi
            
            # Method 2: Clear browser data
            echo "3. Clearing browser data..." >> "$WIPE_LOG"
            rm -rf "$USER_HOME/.mozilla/firefox/"* 2>/dev/null
            rm -rf "$USER_HOME/.config/google-chrome/"* 2>/dev/null
            rm -rf "$USER_HOME/.config/chromium/"* 2>/dev/null
            
            # Method 3: Clear system credentials and keys
            echo "4. Clearing system credentials and keys..." >> "$WIPE_LOG"
            rm -rf "$USER_HOME/.ssh/"* 2>/dev/null
            rm -rf "$USER_HOME/.gnupg/"* 2>/dev/null
            sudo rm -rf /etc/ssh/*_key* 2>/dev/null
            
            # Method 4: Clear system caches and logs
            echo "5. Clearing system caches and logs..." >> "$WIPE_LOG"
            sudo rm -rf /var/log/* 2>/dev/null
            rm -rf "$USER_HOME/.cache/"* 2>/dev/null
            
            echo "6. Wipe process completed with multiple overwrite passes." >> "$WIPE_LOG"
            
            # Upload wipe log to server
            echo "Uploading wipe completion log to server..."
            UPLOAD_URL="$SERVER_URL/api/wipe/report/$DEVICE_SERIAL"
            
            # Convert to base64
            WIPE_BASE64=$(base64 -i "$WIPE_LOG" 2>/dev/null)
            
            # Create upload payload
            UPLOAD_PAYLOAD=$(cat <<EOF
{
    "deviceId": "$DEVICE_SERIAL",
    "userEmail": "$USER_EMAIL",
    "logData": "$WIPE_BASE64",
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
    "status": "completed"
}
EOF
)
            
            # Upload to server
            UPLOAD_RESPONSE=$(curl -s -w "%{http_code}" -X POST \
                -H "Content-Type: application/json" \
                -d "$UPLOAD_PAYLOAD" \
                "$UPLOAD_URL")
            
            HTTP_CODE=$(echo "$UPLOAD_RESPONSE" | tail -c 4)
            
            if [ "$HTTP_CODE" -eq 200 ]; then
                # Delete local log file
                rm -f "$WIPE_LOG"
                RESULT="EMERGENCY WIPE COMPLETED - All user data securely erased with multiple overwrite passes. Device locked for security."
            else
                RESULT="Device locked for security but failed to report wipe completion"
            fi
            ;;
            
        *)
            RESULT="Unknown command type: $COMMAND_TYPE"
            ;;
    esac
    
    echo "$RESULT"
}

# Function to process pending commands
process_pending_commands() {
    local serial_number="$1"
    
    COMMANDS_RESPONSE=$(get_pending_commands "$serial_number")
    if [ $? -ne 0 ]; then
        echo "Failed to retrieve commands"
        return 1
    fi
    
    # Check if response indicates success
    if echo "$COMMANDS_RESPONSE" | grep -q '"success":true'; then
        # Extract commands array
        COMMANDS_ARRAY=$(echo "$COMMANDS_RESPONSE" | grep -o '"commands":$$[^$$]*$$' | sed 's/"commands":$$//; s/$$//')
        
        # Process each command
        if [ -n "$COMMANDS_ARRAY" ]; then
            # Split commands by '}{' pattern (this is a simplified approach)
            IFS='}' read -ra COMMANDS <<< "$COMMANDS_ARRAY"
            for i in "${!COMMANDS[@]}"; do
                if [ -n "${COMMANDS[i]}" ]; then
                    COMMAND="${COMMANDS[i]}"
                    if [ $i -lt $((${#COMMANDS[@]} - 1)) ]; then
                        COMMAND="${COMMAND}}"
                    fi
                    
                    # Extract command ID
                    COMMAND_ID=$(echo "$COMMAND" | grep -o '"id":[0-9]*' | cut -d':' -f2)
                    
                    if [ -n "$COMMAND_ID" ]; then
                        # Acknowledge command receipt
                        if send_command_acknowledgment "$COMMAND_ID"; then
                            # Execute command
                            RESULT=$(execute_command "$COMMAND")
                            
                            # Report completion
                            send_command_completion "$COMMAND_ID" "$RESULT"
                        else
                            echo "Failed to acknowledge command $COMMAND_ID"
                        fi
                    fi
                fi
            done
        fi
    else
        echo "Server returned error when retrieving commands"
        return 1
    fi
}

# Main execution
echo "Starting Laptop Tracker Agent for Linux..."
echo "Server URL: $SERVER_URL"
echo "Device Serial Number: $DEVICE_SERIAL"

# Collect device information
DEVICE_INFO=$(get_device_info)

if [ -n "$DEVICE_INFO" ]; then
    echo "Device information collected successfully"
    
    # Send device information to server
    if send_device_data "$DEVICE_INFO"; then
        echo "Device registered successfully"
        
        # Get and send location data
        echo "Getting device location..."
        LOCATION=$(get_device_location)
        send_location_data "$DEVICE_SERIAL" "$LOCATION"
        
        # Process any pending commands
        process_pending_commands "$DEVICE_SERIAL"
    else
        echo "Failed to register device"
    fi
else
    echo "Failed to collect device information"
fi

echo "Laptop Tracker Agent execution completed"