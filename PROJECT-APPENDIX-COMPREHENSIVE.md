# Appendix: Comprehensive Technical Implementation Details

## A. System Architecture

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

### Core Components

1. **Device Agents** (Platform-specific implementations)
   - Windows: PowerShell script with Win32 API integration
   - macOS: Bash script with system utilities
   - Linux: Bash script with distribution-agnostic commands

2. **LAPSO Server** (Spring Boot Application)
   - REST API for device communication
   - WebSocket for real-time updates
   - Business logic services
   - Data access repositories

3. **Database**
   - Development: H2 in-memory database
   - Production: PostgreSQL with geospatial extensions

4. **Web Dashboard** (Vaadin-based UI)
   - Interactive map view
   - Device management interface
   - Remote control panel

## B. Agent Implementation Details

### 1. Screen Lock Functionality

**Windows Agent:**
- Multiple lock methods for reliability:
  - `rundll32.exe user32.dll,LockWorkStation`
  - VBS script execution
  - Win32 API direct call
  - Scheduled task execution
- PIN/Password required for unlock

**macOS Agent:**
- Multiple lock methods:
  - `pmset displaysleepnow`
  - AppleScript screen saver activation
  - `caffeinate` command
- PIN/Password required for unlock

**Linux Agent:**
- Multiple lock methods:
  - `loginctl lock-sessions` (systemd)
  - Desktop environment specific commands (GNOME, KDE, XFCE)
  - `xset dpms force off` (fallback)
- PIN/Password required for unlock

### 2. Camera Control Functionality

**Windows Agent:**
- Win32 API webcam capture
- Screenshot fallback when camera unavailable
- Multiple initialization attempts

**macOS Agent:**
- `imagesnap` (primary)
- `ffmpeg` (secondary)
- `fswebcam` (tertiary)
- Screenshot fallback

**Linux Agent:**
- `fswebcam` (primary)
- `ffmpeg` (secondary)
- `streamer` (tertiary)
- Screenshot fallback

### 3. Remote Wipe Functionality

**All Platforms:**
- Secure 3-pass data overwrite:
  1. Random data
  2. Zeros
  3. Ones
- User data deletion
- System cache clearing
- Credential removal

## C. API Endpoints

### Remote Commands
- `POST /api/remote-commands/send` - Send command to device
- `GET /api/remote-commands/poll/{deviceId}` - Agent polls for commands
- `POST /api/remote-commands/result` - Report command execution result

### Device Management
- `GET /api/devices` - List user devices
- `POST /api/devices/register` - Register new device
- `PUT /api/devices/{id}` - Update device information

### Location Services
- `GET /api/location/{deviceId}` - Get current location
- `GET /api/location-history/{deviceId}` - Get location history

## D. Security Features

### Authentication
- Session-based authentication
- Role-based access control
- Device ownership verification

### Data Protection
- HTTPS encryption for all communications
- Secure password storage
- Data encryption at rest (production)

### Command Security
- Command expiration (1 hour default)
- Priority-based execution
- Execution confirmation
- Audit logging

## E. Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.8
- **Language**: Java 17
- **Build Tool**: Apache Maven
- **Database**: H2 (dev), PostgreSQL (prod)

### Frontend
- **Framework**: Vaadin Flow 24.2.0
- **Build Tool**: Vite
- **Languages**: TypeScript, HTML, CSS

### Agents
- **Windows**: PowerShell 5.1+
- **macOS**: Bash 3.2+
- **Linux**: Bash 4.0+

## F. Deployment Requirements

### Server
- Java 17 or higher
- 2GB RAM minimum
- PostgreSQL 12+ (production)
- 10GB disk space

### Client Devices
- **Windows**: Windows 10/11
- **macOS**: macOS 10.15+
- **Linux**: Ubuntu 18.04+, CentOS 7+, or compatible

### Network
- Outbound HTTPS access to server
- Port 8080 (default) or configurable
- Static IP or domain name for server

## G. Database Schema

### Core Tables

#### Devices Table (`laptops`)
- `id` - Primary key
- `user_id` - Foreign key to users table
- `device_id` - Unique device identifier
- `device_name` - Human-readable device name
- `manufacturer`, `model`, `serial_number` - Hardware information
- `os_name`, `os_version` - Operating system details
- `latitude`, `longitude`, `altitude` - Current location
- `address` - Reverse geocoded address
- `is_online` - Online status
- `last_seen` - Last communication timestamp
- `battery_level` - Battery percentage
- `is_charging` - Charging status
- `is_locked` - Lock status
- `is_stolen` - Theft status
- `agent_version` - Installed agent version
- `created_at`, `updated_at` - Record timestamps

#### Remote Commands Table (`remote_commands`)
- `id` - Primary key
- `device_id` - Foreign key to devices table
- `user_id` - Foreign key to users table
- `command_type` - Type of command (LOCK, WIPE, etc.)
- `command_params` - JSON parameters for the command
- `status` - Command status (PENDING, SENT, COMPLETED, etc.)
- `priority` - Execution priority (1-10)
- `created_at`, `sent_at`, `executed_at`, `completed_at` - Timestamps
- `expires_at` - Expiration time
- `result` - Execution result or error message
- `screenshot_url` - URL for captured screenshots
- `retry_count`, `max_retries` - Retry mechanism

## H. Configuration Properties

### Application Properties
```properties
# Server configuration
server.port=8080
spring.application.name=LAPSO

# Database configuration
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/postgres}
spring.datasource.username=${DATABASE_USERNAME:postgres}
spring.datasource.password=${DATABASE_PASSWORD:}

# JPA/Hibernate settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Connection pooling (HikariCP)
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# Flyway migrations
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Session configuration
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false
```

## I. Maven Dependencies

### Core Frameworks
- Spring Boot 3.2.8 (Web, Data JPA, Security, WebSocket)
- Vaadin 24.2.0 (Core UI framework)
- PostgreSQL Driver 42.7.3

### Key Libraries
- Flyway 10.19.0 (Database migrations)
- Caffeine 3.1.8 (Caching)
- Lombok 1.18.32 (Code generation)
- JWT 0.12.5 (Authentication)
- Ehcache 3.10.8 (Second-level caching)

### Testing
- Spring Boot Starter Test
- TestContainers (PostgreSQL testing)

## J. REST API Reference

### Device Management Endpoints
- `GET /api/devices` - Retrieve all devices for current user
- `POST /api/devices/register` - Register a new device
- `PUT /api/devices/{id}` - Update device information
- `DELETE /api/devices/{id}` - Remove device

### Remote Command Endpoints
- `POST /api/remote-commands/send` - Send a command to a device
- `GET /api/remote-commands/poll/{deviceId}` - Agent polls for pending commands
- `POST /api/remote-commands/result` - Report command execution results
- `GET /api/remote-commands/history/{deviceId}` - Get command history

### Location Endpoints
- `GET /api/location/{deviceId}` - Get current device location
- `GET /api/location-history/{deviceId}` - Get historical location data
- `POST /api/location/{deviceId}/update` - Update device location

### Geofencing Endpoints
- `POST /api/geofences` - Create a new geofence
- `GET /api/geofences` - List all geofences
- `PUT /api/geofences/{id}` - Update a geofence
- `DELETE /api/geofences/{id}` - Delete a geofence

## K. WebSocket Topics

- `/user/queue/device-updates` - Real-time device updates
- `/user/queue/geofence-alerts` - Geofence breach notifications
- `/user/queue/command-updates` - Command status updates
- `/user/queue/share-updates` - Location sharing updates

## L. Agent Communication Protocol

### Heartbeat Process
1. Agent sends heartbeat with device information and location
2. Server validates device registration
3. Server updates device status and location
4. Server checks for pending commands
5. Response includes any pending commands

### Command Execution Flow
1. User sends command via web dashboard
2. Server stores command in database with PENDING status
3. Agent polls for commands during heartbeat
4. Server sends command to agent with SENT status
5. Agent executes command and reports result
6. Server updates command status to COMPLETED/FAILED

## M. Database Migration Scripts

### V1__Initial_LAPSO_Schema.sql
```sql
-- LAPSO Initial Database Schema
-- PostgreSQL Migration V1
-- Creates all necessary tables for LAPSO laptop tracking system

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    name VARCHAR(255),
    picture VARCHAR(500),
    google_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    login_count INTEGER DEFAULT 0
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id);

-- Devices/Laptops table
CREATE TABLE IF NOT EXISTS laptops (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id VARCHAR(255) NOT NULL UNIQUE,
    device_name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255),
    model VARCHAR(255),
    serial_number VARCHAR(255),
    os_name VARCHAR(255),
    os_version VARCHAR(255),
    
    -- Location tracking
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    altitude DOUBLE PRECISION,
    location_accuracy DOUBLE PRECISION,
    address TEXT,
    
    -- Device status
    is_online BOOLEAN DEFAULT FALSE,
    last_seen TIMESTAMP,
    battery_level INTEGER,
    is_charging BOOLEAN DEFAULT FALSE,
    
    -- System information
    cpu_usage DOUBLE PRECISION,
    memory_total BIGINT,
    memory_used BIGINT,
    disk_total BIGINT,
    disk_used BIGINT,
    
    -- Network information
    ip_address VARCHAR(45), -- IPv6 support
    wifi_ssid VARCHAR(255),
    wifi_signal_strength INTEGER,
    
    -- Security features
    is_locked BOOLEAN DEFAULT FALSE,
    theft_detected BOOLEAN DEFAULT FALSE,
    is_stolen BOOLEAN DEFAULT FALSE,
    is_wiped BOOLEAN DEFAULT FALSE,
    
    -- Additional fields
    last_action VARCHAR(255),
    last_action_time TIMESTAMP,
    device_type VARCHAR(50) DEFAULT 'LAPTOP',
    network_name VARCHAR(255),
    public_ip VARCHAR(45),
    disk_usage INTEGER,
    memory_usage INTEGER,
    uptime_hours BIGINT,
    agent_version VARCHAR(50),
    operating_system VARCHAR(255),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_laptops_device_id ON laptops(device_id);
CREATE INDEX IF NOT EXISTS idx_laptops_user_id ON laptops(user_id);
CREATE INDEX IF NOT EXISTS idx_laptops_is_online ON laptops(is_online);
CREATE INDEX IF NOT EXISTS idx_laptops_last_seen ON laptops(last_seen);
CREATE INDEX IF NOT EXISTS idx_laptops_location ON laptops(latitude, longitude);

-- Location history table
CREATE TABLE IF NOT EXISTS location_history (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES laptops(id) ON DELETE CASCADE,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    altitude DOUBLE PRECISION,
    accuracy DOUBLE PRECISION,
    address TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(50) DEFAULT 'UNKNOWN' -- GPS, WIFI, IP, etc.
);

-- Create indexes for location history
CREATE INDEX IF NOT EXISTS idx_location_history_device_id ON location_history(device_id);
CREATE INDEX IF NOT EXISTS idx_location_history_timestamp ON location_history(timestamp);
CREATE INDEX IF NOT EXISTS idx_location_history_location ON location_history(latitude, longitude);

-- Device events table for audit trail
CREATE TABLE IF NOT EXISTS device_events (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES laptops(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,
    event_description TEXT,
    event_data JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- Create indexes for device events
CREATE INDEX IF NOT EXISTS idx_device_events_device_id ON device_events(device_id);
CREATE INDEX IF NOT EXISTS idx_device_events_timestamp ON device_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_device_events_type ON device_events(event_type);

-- Geofences table
CREATE TABLE IF NOT EXISTS geofences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    center_latitude DOUBLE PRECISION NOT NULL,
    center_longitude DOUBLE PRECISION NOT NULL,
    radius_meters INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for geofences
CREATE INDEX IF NOT EXISTS idx_geofences_user_id ON geofences(user_id);
CREATE INDEX IF NOT EXISTS idx_geofences_location ON geofences(center_latitude, center_longitude);

-- Device shares table (for family sharing)
CREATE TABLE IF NOT EXISTS device_shares (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES laptops(id) ON DELETE CASCADE,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shared_with_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permission_level VARCHAR(50) DEFAULT 'VIEW', -- VIEW, CONTROL, ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    UNIQUE(device_id, shared_with_id)
);

-- Create indexes for device shares
CREATE INDEX IF NOT EXISTS idx_device_shares_device_id ON device_shares(device_id);
CREATE INDEX IF NOT EXISTS idx_device_shares_shared_with ON device_shares(shared_with_id);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id BIGINT REFERENCES laptops(id) ON DELETE CASCADE,
    notification_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Create indexes for notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_device_id ON notifications(device_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);

-- System settings table
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default system settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('app.name', 'LAPSO', 'Application name'),
('app.version', '1.0.0', 'Application version'),
('app.mode', 'free-open-source', 'Application mode'),
('location.update_interval', '30', 'Location update interval in seconds'),
('notifications.enabled', 'true', 'Enable notifications'),
('geofence.default_radius', '100', 'Default geofence radius in meters')
ON CONFLICT (setting_key) DO NOTHING;

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers to relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_laptops_updated_at BEFORE UPDATE ON laptops FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_geofences_updated_at BEFORE UPDATE ON geofences FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_system_settings_updated_at BEFORE UPDATE ON system_settings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Database schema initialized - ready for production use

-- Create views for better data access
CREATE OR REPLACE VIEW device_summary AS
SELECT 
    l.id,
    l.device_id,
    l.device_name,
    l.manufacturer,
    l.model,
    l.is_online,
    l.last_seen,
    l.battery_level,
    l.latitude,
    l.longitude,
    l.address,
    u.email as owner_email,
    u.name as owner_name
FROM laptops l
JOIN users u ON l.user_id = u.id;

-- Create view for recent location history
CREATE OR REPLACE VIEW recent_locations AS
SELECT 
    lh.*,
    l.device_name,
    u.email as owner_email
FROM location_history lh
JOIN laptops l ON lh.device_id = l.id
JOIN users u ON l.user_id = u.id
WHERE lh.timestamp > CURRENT_TIMESTAMP - INTERVAL '24 hours'
ORDER BY lh.timestamp DESC;

-- Grant permissions to lapso_user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO lapso_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO lapso_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO lapso_user;

-- Success message
DO $$
BEGIN
    RAISE NOTICE 'LAPSO PostgreSQL schema created successfully!';
    RAISE NOTICE 'Database: lapso_db';
    RAISE NOTICE 'Tables created: %, %, %, %, %, %, %, %', 
        'users', 'laptops', 'location_history', 'device_events', 
        'geofences', 'device_shares', 'notifications', 'system_settings';
END $$;
```

### V2__Fix_User_Schema.sql
```sql
-- Fix User Schema - Make google_id nullable for regular users
-- This fixes the registration issue where non-Google users can't register

-- Drop the NOT NULL constraint on google_id if it exists
ALTER TABLE users ALTER COLUMN google_id DROP NOT NULL;

-- Make sure the column allows NULL values
ALTER TABLE users ALTER COLUMN google_id SET DEFAULT NULL;

-- Add comment for clarity
COMMENT ON COLUMN users.google_id IS 'Google OAuth ID - nullable for regular users';

-- Ensure email is unique and not null (should already be set)
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Ensure name is not null (should already be set)  
ALTER TABLE users ALTER COLUMN name SET NOT NULL;

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Create index on google_id for OAuth users
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id) WHERE google_id IS NOT NULL;
```
