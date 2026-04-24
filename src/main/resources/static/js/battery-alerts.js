/**
 * LAPSO Battery Alert System - Real-time WebSocket notifications
 * Alerts users when devices have low battery or are unplugged
 */

class BatteryAlertSystem {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.userEmail = this.getUserEmail();
        
        if (this.userEmail) {
            this.connect();
        } else {
            console.log('⚠️ No user email found - battery alerts disabled');
        }
    }
    
    /**
     * Get current user email from page or session
     */
    getUserEmail() {
        // Try to get from meta tag
        const emailMeta = document.querySelector('meta[name="user-email"]');
        if (emailMeta) {
            return emailMeta.getAttribute('content');
        }
        
        // Try to get from global variable
        if (window.currentUserEmail) {
            return window.currentUserEmail;
        }
        
        // Try to parse from page content (Vaadin apps)
        const userEmailElement = document.querySelector('[data-user-email]');
        if (userEmailElement) {
            return userEmailElement.getAttribute('data-user-email');
        }
        
        return null;
    }
    
    /**
     * Connect to WebSocket server
     */
    connect() {
        console.log('🔌 Connecting to LAPSO WebSocket for battery alerts...');
        
        // Use SockJS for WebSocket connection
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        // Disable debug output
        this.stompClient.debug = null;
        
        // Connect to WebSocket
        this.stompClient.connect({}, 
            (frame) => this.onConnected(frame),
            (error) => this.onError(error)
        );
    }
    
    /**
     * Handle successful connection
     */
    onConnected(frame) {
        console.log('✅ Battery Alert System Connected');
        this.connected = true;
        this.reconnectAttempts = 0;
        
        // Subscribe to user-specific alert topic
        const alertTopic = `/topic/alerts/${this.userEmail}`;
        console.log(`📡 Subscribing to: ${alertTopic}`);
        
        this.stompClient.subscribe(alertTopic, (message) => {
            this.handleAlert(JSON.parse(message.body));
        });
        
        // Subscribe to device updates as well
        this.stompClient.subscribe('/topic/device-updates', (message) => {
            this.handleDeviceUpdate(JSON.parse(message.body));
        });
        
        this.showConnectionStatus(true);
    }
    
    /**
     * Handle connection error
     */
    onError(error) {
        console.error('❌ Battery Alert System Error:', error);
        this.connected = false;
        this.showConnectionStatus(false);
        
        // Attempt reconnection
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
            console.log(`🔄 Reconnecting in ${delay / 1000} seconds... (Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            setTimeout(() => {
                this.connect();
            }, delay);
        } else {
            console.error('❌ Max reconnection attempts reached. Please refresh the page.');
            this.showNotification('Battery alerts disconnected. Please refresh the page.', 'error', 10000);
        }
    }
    
    /**
     * Handle battery alert message
     */
    handleAlert(alert) {
        console.log('🔋 Battery Alert Received:', alert);
        
        const message = alert.message || 'Battery alert';
        const type = alert.type || 'warning';
        const deviceId = alert.deviceId || 'Unknown';
        const batteryLevel = alert.batteryLevel;
        
        // Show notification
        let notificationText = message;
        if (batteryLevel !== undefined) {
            notificationText += ` (${batteryLevel}%)`;
        }
        
        this.showNotification(notificationText, type === 'low_battery' ? 'warning' : 'info');
        
        // Play sound alert for critical battery
        if (type === 'low_battery' && batteryLevel < 10) {
            this.playAlertSound();
        }
        
        // Update UI if device card exists
        this.updateDeviceUI(deviceId, alert);
    }
    
    /**
     * Handle device update message
     */
    handleDeviceUpdate(update) {
        console.log('📱 Device Update:', update);
        
        // Update device card if exists
        if (update.deviceId) {
            this.updateDeviceUI(update.deviceId, update);
        }
    }
    
    /**
     * Update device UI with alert info
     */
    updateDeviceUI(deviceId, data) {
        // Find device card by ID
        const deviceCard = document.querySelector(`[data-device-id="${deviceId}"]`);
        if (!deviceCard) {
            return;
        }
        
        // Update battery indicator if exists
        const batteryElement = deviceCard.querySelector('[data-battery-level]');
        if (batteryElement && data.batteryLevel !== undefined) {
            batteryElement.textContent = `${data.batteryLevel}%`;
            
            // Change color based on battery level
            if (data.batteryLevel < 20) {
                batteryElement.style.color = '#ef4444'; // Red
            } else if (data.batteryLevel < 50) {
                batteryElement.style.color = '#f59e0b'; // Orange
            } else {
                batteryElement.style.color = '#10b981'; // Green
            }
        }
        
        // Add visual alert indicator
        if (data.type === 'low_battery') {
            deviceCard.style.border = '2px solid #ef4444';
            deviceCard.style.boxShadow = '0 0 10px rgba(239, 68, 68, 0.5)';
            
            // Remove after 5 seconds
            setTimeout(() => {
                deviceCard.style.border = '';
                deviceCard.style.boxShadow = '';
            }, 5000);
        }
    }
    
    /**
     * Show notification banner
     */
    showNotification(message, type = 'info', duration = 8000) {
        const notification = document.createElement('div');
        notification.className = 'lapso-battery-notification';
        
        // Icon based on type
        let icon = '🔋';
        let bgColor = '#3b82f6';
        
        if (type === 'warning' || type === 'low_battery') {
            icon = '⚠️';
            bgColor = '#f59e0b';
        } else if (type === 'error' || type === 'unplugged') {
            icon = '⚡';
            bgColor = '#ef4444';
        } else if (type === 'success') {
            icon = '✅';
            bgColor = '#10b981';
        }
        
        notification.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            background: ${bgColor};
            color: white;
            padding: 16px 20px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            z-index: 10000;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
            animation: slideInRight 0.4s ease-out;
            max-width: 350px;
            display: flex;
            align-items: center;
            gap: 10px;
        `;
        
        notification.innerHTML = `
            <span style="font-size: 20px;">${icon}</span>
            <span>${message}</span>
        `;
        
        document.body.appendChild(notification);
        
        // Auto-remove
        setTimeout(() => {
            notification.style.animation = 'slideOutRight 0.4s ease-out';
            setTimeout(() => {
                notification.remove();
            }, 400);
        }, duration);
    }
    
    /**
     * Show connection status indicator
     */
    showConnectionStatus(connected) {
        let indicator = document.getElementById('battery-alert-status');
        
        if (!indicator) {
            indicator = document.createElement('div');
            indicator.id = 'battery-alert-status';
            indicator.style.cssText = `
                position: fixed;
                bottom: 80px;
                right: 20px;
                background: ${connected ? '#10b981' : '#6b7280'};
                color: white;
                padding: 6px 12px;
                border-radius: 15px;
                font-size: 11px;
                font-weight: bold;
                z-index: 9999;
                opacity: 0.8;
            `;
            document.body.appendChild(indicator);
        }
        
        if (connected) {
            indicator.innerHTML = '🔋 Battery Alerts ON';
            indicator.style.background = '#10b981';
        } else {
            indicator.innerHTML = '🔌 Battery Alerts OFF';
            indicator.style.background = '#6b7280';
        }
    }
    
    /**
     * Play alert sound for critical battery
     */
    playAlertSound() {
        // Create simple beep using Web Audio API
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);
            
            oscillator.frequency.value = 800; // Frequency in Hz
            oscillator.type = 'sine';
            
            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
            
            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.5);
        } catch (e) {
            console.log('Could not play alert sound:', e);
        }
    }
    
    /**
     * Disconnect WebSocket
     */
    disconnect() {
        if (this.stompClient && this.connected) {
            this.stompClient.disconnect(() => {
                console.log('🔌 Battery Alert System Disconnected');
                this.connected = false;
                this.showConnectionStatus(false);
            });
        }
    }
}

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Small delay to ensure SockJS and Stomp libraries are loaded
    setTimeout(() => {
        if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
            window.batteryAlertSystem = new BatteryAlertSystem();
            console.log('🔋 LAPSO Battery Alert System Initialized');
        } else {
            console.warn('⚠️ SockJS or Stomp library not found - loading from CDN...');
            
            // Load SockJS from CDN
            const sockjsScript = document.createElement('script');
            sockjsScript.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
            sockjsScript.onload = () => {
                console.log('✅ SockJS loaded');
                
                // Load STOMP from CDN
                const stompScript = document.createElement('script');
                stompScript.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';
                stompScript.onload = () => {
                    console.log('✅ Stomp loaded');
                    window.batteryAlertSystem = new BatteryAlertSystem();
                    console.log('🔋 LAPSO Battery Alert System Initialized (with CDN libraries)');
                };
                document.head.appendChild(stompScript);
            };
            document.head.appendChild(sockjsScript);
        }
    }, 1000);
});

// Export for global access
window.BatteryAlertSystem = BatteryAlertSystem;

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (window.batteryAlertSystem) {
        window.batteryAlertSystem.disconnect();
    }
});
