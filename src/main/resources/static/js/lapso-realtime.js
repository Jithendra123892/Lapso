/**
 * LAPSO Real-Time Updates - Better than Microsoft Find My Device
 * Provides live updates without page refresh
 */

class LapsoRealTime {
    constructor() {
        this.updateInterval = 30000; // 30 seconds
        this.isConnected = false;
        this.init();
    }
    
    init() {
        console.log('🚀 LAPSO Real-Time System Starting...');
        console.log('📍 Note: Updates every 30 seconds when devices are online');
        console.log('🎯 Honest tracking - no false claims about accuracy');
        this.startLocationUpdates();
        this.startStatusMonitoring();
        this.showRealTimeIndicator();
    }
    
    /**
     * Start real-time location updates (every 30 seconds)
     * Microsoft Find My Device requires manual refresh
     */
    startLocationUpdates() {
        setInterval(() => {
            this.updateDeviceLocations();
        }, this.updateInterval);
        
        // Initial update
        this.updateDeviceLocations();
    }
    
    /**
     * Update device locations via API
     */
    async updateDeviceLocations() {
        try {
            const response = await fetch('/api/location/monitoring-stats');
            const stats = await response.json();
            
            // Update UI with real-time stats
            this.updateDashboardStats(stats);
            
            // Show live update indicator
            this.showUpdateIndicator();
            
            console.log('📍 Location update completed:', stats);
            
        } catch (error) {
            console.error('Failed to update locations:', error);
        }
    }
    
    /**
     * Update dashboard statistics in real-time
     */
    updateDashboardStats(stats) {
        // Update device counts
        const totalDevicesElement = document.querySelector('[data-stat="total-devices"]');
        if (totalDevicesElement) {
            totalDevicesElement.textContent = stats.totalDevices || '0';
        }
        
        const onlineDevicesElement = document.querySelector('[data-stat="online-devices"]');
        if (onlineDevicesElement) {
            onlineDevicesElement.textContent = stats.onlineDevices || '0';
        }
        
        // Update last update time
        const lastUpdateElement = document.querySelector('[data-stat="last-update"]');
        if (lastUpdateElement) {
            lastUpdateElement.textContent = new Date().toLocaleTimeString();
        }
    }
    
    /**
     * Show real-time update indicator
     */
    showUpdateIndicator() {
        // Create or update live indicator
        let indicator = document.getElementById('lapso-live-indicator');
        if (!indicator) {
            indicator = document.createElement('div');
            indicator.id = 'lapso-live-indicator';
            indicator.innerHTML = '🔴 LIVE';
            indicator.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                background: #10b981;
                color: white;
                padding: 8px 12px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: bold;
                z-index: 1000;
                animation: pulse 2s infinite;
            `;
            document.body.appendChild(indicator);
        }
        
        // Flash the indicator
        indicator.style.background = '#ef4444';
        setTimeout(() => {
            indicator.style.background = '#10b981';
        }, 500);
    }
    
    /**
     * Start status monitoring
     */
    startStatusMonitoring() {
        // Monitor connection status
        setInterval(() => {
            this.checkConnectionStatus();
        }, 5000); // Every 5 seconds
    }
    
    /**
     * Check if LAPSO is connected and working
     */
    async checkConnectionStatus() {
        try {
            const response = await fetch('/api/location/monitoring-stats');
            this.isConnected = response.ok;
            
            // Update connection indicator
            this.updateConnectionIndicator(this.isConnected);
            
        } catch (error) {
            this.isConnected = false;
            this.updateConnectionIndicator(false);
        }
    }
    
    /**
     * Update connection status indicator
     */
    updateConnectionIndicator(isConnected) {
        let indicator = document.getElementById('lapso-connection-indicator');
        if (!indicator) {
            indicator = document.createElement('div');
            indicator.id = 'lapso-connection-indicator';
            indicator.style.cssText = `
                position: fixed;
                bottom: 20px;
                right: 20px;
                padding: 8px 12px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: bold;
                z-index: 1000;
            `;
            document.body.appendChild(indicator);
        }
        
        if (isConnected) {
            indicator.innerHTML = '✅ LAPSO Connected';
            indicator.style.background = '#10b981';
            indicator.style.color = 'white';
        } else {
            indicator.innerHTML = '❌ Connection Lost';
            indicator.style.background = '#ef4444';
            indicator.style.color = 'white';
        }
    }
    
    /**
     * Show real-time notification
     */
    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
            color: white;
            padding: 12px 16px;
            border-radius: 8px;
            font-size: 14px;
            z-index: 1001;
            animation: slideIn 0.3s ease;
        `;
        notification.textContent = message;
        document.body.appendChild(notification);
        
        // Remove after 5 seconds
        setTimeout(() => {
            notification.remove();
        }, 5000);
    }
}

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes pulse {
        0% { opacity: 1; }
        50% { opacity: 0.5; }
        100% { opacity: 1; }
    }
    
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
`;
document.head.appendChild(style);

// Initialize LAPSO Real-Time System
document.addEventListener('DOMContentLoaded', () => {
    window.lapsoRealTime = new LapsoRealTime();
    console.log('🛡️ LAPSO Real-Time System Initialized');
    console.log('⚡ Updates every 30 seconds - More frequent than Microsoft Find My Device');
    console.log('🎯 Honest reality: Requires devices to be online for tracking');
});

// Export for global access
window.LapsoRealTime = LapsoRealTime;