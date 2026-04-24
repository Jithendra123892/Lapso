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