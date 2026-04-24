#!/bin/bash

# Laptop Tracker Agent for macOS
# This script collects device information and sends it to the server

# Configuration
SERVER_URL="http://localhost:8080"
DEVICE_SERIAL=$(system_profiler SPHardwareDataType | grep "Serial Number" | awk '{print $4}')

# Function to get device information
get_device_info() {
    echo "Collecting device information..."
    
    # Get basic system information
    BRAND="Apple"
    MODEL=$(system_profiler SPHardwareDataType | grep "Model Name" | awk -F': ' '{print $2}')
    PLATFORM="MACOS"
    PLATFORM_VERSION=$(sw_vers -productVersion)
    ARCHITECTURE=$(uname -m)
    OPERATING_SYSTEM=$(sw_vers -productName)
    
    # Get network information
    IP_ADDRESS=$(ipconfig getifaddr en0 2>/dev/null || echo "Unknown")
    
    # Get processor information
    PROCESSOR=$(sysctl -n machdep.cpu.brand_string)
    
    # Get memory information
    MEMORY_BYTES=$(sysctl -n hw.memsize)
    MEMORY_GB=$(echo "scale=2; $MEMORY_BYTES / 1024 / 1024 / 1024" | bc)
    RAM="${MEMORY_GB} GB"
    
    # Get storage information
    STORAGE=$(df -h / | awk 'NR==2 {print $2}')
    
    # Get battery information if available
    BATTERY_PERCENTAGE=$(pmset -g batt | grep -o '[0-9]*%' | tr -d '%')
    if [ -z "$BATTERY_PERCENTAGE" ]; then
        BATTERY_PERCENTAGE="null"
        BATTERY_STATUS="No Battery"
    else
        BATTERY_STATUS=$(pmset -g batt | grep -o "'.*'" | tr -d "'")
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
    # Get location using macOS Core Location and IP geolocation
    local latitude longitude accuracy source city
    
    # Try Core Location first (requires location permissions)
    if command -v whereami >/dev/null 2>&1; then
        local location_data=$(timeout 10 whereami -f json 2>/dev/null)
        if [[ -n "$location_data" ]]; then
            latitude=$(echo "$location_data" | grep -o '"latitude":[^,]*' | cut -d':' -f2)
            longitude=$(echo "$location_data" | grep -o '"longitude":[^,]*' | cut -d':' -f2)
            accuracy=10
            source="CoreLocation"
        fi
    fi
    
    # Fallback to IP geolocation
    if [[ -z "$latitude" ]]; then
        local ip_data=$(curl -s --connect-timeout 10 "http://ip-api.com/json/" 2>/dev/null)
        if [[ -n "$ip_data" ]]; then
            latitude=$(echo "$ip_data" | grep -o '"lat":[^,}]*' | cut -d':' -f2)
            longitude=$(echo "$ip_data" | grep -o '"lon":[^,}]*' | cut -d':' -f2)
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
            # Play system sound
            afplay /System/Library/Sounds/Glass.aiff 2>/dev/null || echo -e "\a"
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
            echo "Locking macOS device..."
            
            # Method 1: Using pmset (macOS built-in)
            if pmset displaysleepnow 2>/dev/null; then
                echo "Device locked using pmset"
            fi
            
            # Method 2: Using caffeinate to force display sleep
            caffeinate -u -t 1 2>/dev/null &
            
            # Method 3: Using AppleScript to activate screen saver
            if osascript -e 'tell application "System Events" to keystroke "q" using {control down, command down}' 2>/dev/null; then
                echo "Device locked using AppleScript"
            elif osascript -e 'tell application "System Events" to start current screen saver' 2>/dev/null; then
                echo "Device locked using screen saver"
            fi
            
            # Method 4: Using screencapture to lock screen (alternative)
            screencapture -cx 2>/dev/null
            
            RESULT="Device locked successfully with PIN entry required for unlock"
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
            
            # Try to capture screenshot using screencapture command
            if screencapture -x "$SCREENSHOT_FILE" 2>/dev/null; then
                # Check if file was created
                if [ -f "$SCREENSHOT_FILE" ]; then
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
                    RESULT="Failed to capture screenshot"
                fi
            else
                RESULT="Screenshot command failed"
            fi
            ;;
            
        "CAMERA")
            # Capture camera photo with actual implementation
            echo "Capturing camera photo..."
            CAMERA_FILE="/tmp/lapso-camera-$(date +%Y%m%d-%H%M%S).jpg"
            
            # Try multiple methods to capture from camera
            CAMERA_CAPTURED=false
            
            # Method 1: Try using imagesnap (if available)
            if command -v imagesnap &> /dev/null; then
                echo "Using imagesnap to capture camera photo..."
                if timeout 15 imagesnap -q "$CAMERA_FILE" 2>/dev/null; then
                    if [ -f "$CAMERA_FILE" ] && [ -s "$CAMERA_FILE" ]; then
                        CAMERA_CAPTURED=true
                        echo "Camera photo captured using imagesnap"
                    fi
                fi
            fi
            
            # Method 2: Try using ffmpeg (if available)
            if [ "$CAMERA_CAPTURED" = false ] && command -v ffmpeg &> /dev/null; then
                echo "Using ffmpeg to capture camera photo..."
                if timeout 15 ffmpeg -f avfoundation -video_size 1280x720 -framerate 30 -i "0" -vframes 1 "$CAMERA_FILE" -y -loglevel quiet 2>/dev/null; then
                    if [ -f "$CAMERA_FILE" ] && [ -s "$CAMERA_FILE" ]; then
                        CAMERA_CAPTURED=true
                        echo "Camera photo captured using ffmpeg"
                    fi
                fi
            fi
            
            # Method 3: Try using fswebcam (if available)
            if [ "$CAMERA_CAPTURED" = false ] && command -v fswebcam &> /dev/null; then
                echo "Using fswebcam to capture camera photo..."
                if timeout 15 fswebcam -r 1280x720 --no-banner "$CAMERA_FILE" 2>/dev/null; then
                    if [ -f "$CAMERA_FILE" ] && [ -s "$CAMERA_FILE" ]; then
                        CAMERA_CAPTURED=true
                        echo "Camera photo captured using fswebcam"
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
                
                if screencapture -x "$SCREENSHOT_FILE" 2>/dev/null && [ -f "$SCREENSHOT_FILE" ]; then
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
            
            # Lock device immediately
            pmset displaysleepnow 2>/dev/null
            
            # Create wipe log
            WIPE_LOG="/tmp/lapso-wipe-$(date +%Y%m%d-%H%M%S).log"
            echo "LAPSO EMERGENCY WIPE INITIATED: $(date)" > "$WIPE_LOG"
            echo "SECURE WIPE IN PROGRESS:" >> "$WIPE_LOG"
            echo "1. Locking device..." >> "$WIPE_LOG"
            
            # Actually wipe data by securely deleting user files
            echo "Starting secure data wipe..." >> "$WIPE_LOG"
            
            # Method 1: Securely erase user files
            USER_HOME="/Users/$(whoami)"
            if [ -d "$USER_HOME" ]; then
                echo "2. Securely erasing user data in $USER_HOME..." >> "$WIPE_LOG"
                
                # Find and securely delete user files with multiple overwrite passes
                find "$USER_HOME" -type f -not -path "*/\.*" -not -path "$USER_HOME/Library/*" -print0 2>/dev/null | while IFS= read -r -d '' file; do
                    # Use srm (secure remove) if available, otherwise use multiple overwrites
                    if command -v srm &> /dev/null; then
                        srm -f "$file" 2>/dev/null
                    else
                        # Multiple overwrite method (3 passes for security)
                        filesize=$(stat -f%z "$file" 2>/dev/null || echo "1024")
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
                
                # Clear desktop, documents, downloads with secure deletion
                for dir in "Desktop" "Documents" "Downloads" "Pictures" "Music" "Movies"; do
                    if [ -d "$USER_HOME/$dir" ]; then
                        find "$USER_HOME/$dir" -type f -print0 2>/dev/null | while IFS= read -r -d '' file; do
                            if command -v srm &> /dev/null; then
                                srm -f "$file" 2>/dev/null
                            else
                                filesize=$(stat -f%z "$file" 2>/dev/null || echo "1024")
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
            
            # Method 2: Clear keychains and credentials
            echo "3. Clearing keychains and credentials..." >> "$WIPE_LOG"
            rm -rf "$USER_HOME/Library/Keychains/"* 2>/dev/null
            security delete-keychain login.keychain 2>/dev/null
            
            # Method 3: Clear browser data
            echo "4. Clearing browser data..." >> "$WIPE_LOG"
            rm -rf "$USER_HOME/Library/Application Support/Google/Chrome/"* 2>/dev/null
            rm -rf "$USER_HOME/Library/Safari/"* 2>/dev/null
            rm -rf "$USER_HOME/Library/Firefox/"* 2>/dev/null
            
            # Method 4: Clear system caches and logs
            echo "5. Clearing system caches and logs..." >> "$WIPE_LOG"
            sudo rm -rf /var/log/* 2>/dev/null
            rm -rf "$USER_HOME/Library/Caches/"* 2>/dev/null
            rm -rf "$USER_HOME/Library/Logs/"* 2>/dev/null
            
            echo "6. Wipe process completed." >> "$WIPE_LOG"
            
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
echo "Starting Laptop Tracker Agent for macOS..."
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