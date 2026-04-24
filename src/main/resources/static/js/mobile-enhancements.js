/**
 * LAPSO Mobile Enhancements
 * Progressive Web App features and mobile optimizations
 */

class LapsoMobileEnhancements {
    constructor() {
        this.init();
    }
    
    init() {
        this.setupPWA();
        this.setupMobileNavigation();
        this.setupTouchGestures();
        this.setupNotifications();
        this.setupOfflineSupport();
        this.optimizeForMobile();
    }
    
    /**
     * Progressive Web App setup
     */
    setupPWA() {
        // Register service worker
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.register('/sw.js')
                .then(registration => {
                    console.log('🔧 PWA: Service Worker registered');
                })
                .catch(error => {
                    console.log('PWA: Service Worker registration failed');
                });
        }
        
        // PWA install prompt
        let deferredPrompt;
        window.addEventListener('beforeinstallprompt', (e) => {
            e.preventDefault();
            deferredPrompt = e;
            this.showInstallBanner();
        });
        
        // Track PWA usage
        window.addEventListener('appinstalled', () => {
            console.log('📱 PWA: LAPSO installed as app');
            this.hideInstallBanner();
        });
    }
    
    showInstallBanner() {
        const banner = document.createElement('div');
        banner.className = 'pwa-install-banner';
        banner.id = 'pwa-banner';
        banner.innerHTML = `
            <div class="pwa-install-text">
                📱 Install LAPSO app for better experience
            </div>
            <button class="pwa-install-btn" id="pwa-install">Install</button>
            <button class="pwa-close-btn" id="pwa-close">×</button>
        `;
        
        document.body.appendChild(banner);
        
        document.getElementById('pwa-install').addEventListener('click', () => {
            if (deferredPrompt) {
                deferredPrompt.prompt();
                deferredPrompt.userChoice.then((choiceResult) => {
                    if (choiceResult.outcome === 'accepted') {
                        console.log('📱 PWA: User accepted install');
                    }
                    deferredPrompt = null;
                });
            }
        });
        
        document.getElementById('pwa-close').addEventListener('click', () => {
            this.hideInstallBanner();
        });
    }
    
    hideInstallBanner() {
        const banner = document.getElementById('pwa-banner');
        if (banner) {
            banner.remove();
        }
    }
    
    /**
     * Mobile navigation setup
     */
    setupMobileNavigation() {
        if (this.isMobile()) {
            this.createMobileNav();
            this.createMobileHeader();
        }
    }
    
    createMobileNav() {
        const nav = document.createElement('nav');
        nav.className = 'mobile-nav';
        nav.innerHTML = `
            <a href="/" class="mobile-nav-item active">
                <div class="mobile-nav-icon">🏠</div>
                <div>Home</div>
            </a>
            <a href="/map" class="mobile-nav-item">
                <div class="mobile-nav-icon">🗺️</div>
                <div>Map</div>
            </a>
            <a href="/add-device" class="mobile-nav-item">
                <div class="mobile-nav-icon">➕</div>
                <div>Add</div>
            </a>
            <a href="/analytics" class="mobile-nav-item">
                <div class="mobile-nav-icon">📊</div>
                <div>Stats</div>
            </a>
            <a href="#" class="mobile-nav-item" id="mobile-menu">
                <div class="mobile-nav-icon">☰</div>
                <div>Menu</div>
            </a>
        `;
        
        document.body.appendChild(nav);
        
        // Add bottom padding to body to account for fixed nav
        document.body.style.paddingBottom = '70px';
        
        // Handle active state
        this.updateActiveNavItem();
    }
    
    createMobileHeader() {
        const header = document.createElement('header');
        header.className = 'mobile-header';
        header.innerHTML = `
            <h1>🛡️ LAPSO</h1>
        `;
        
        document.body.insertBefore(header, document.body.firstChild);
    }
    
    updateActiveNavItem() {
        const currentPath = window.location.pathname;
        const navItems = document.querySelectorAll('.mobile-nav-item');
        
        navItems.forEach(item => {
            item.classList.remove('active');
            if (item.getAttribute('href') === currentPath) {
                item.classList.add('active');
            }
        });
    }
    
    /**
     * Touch gesture support
     */
    setupTouchGestures() {
        if (!this.isMobile()) return;
        
        let startY = 0;
        let currentY = 0;
        let isScrolling = false;
        
        // Pull to refresh
        document.addEventListener('touchstart', (e) => {
            startY = e.touches[0].clientY;
        });
        
        document.addEventListener('touchmove', (e) => {
            currentY = e.touches[0].clientY;
            
            if (window.scrollY === 0 && currentY > startY + 50) {
                if (!isScrolling) {
                    this.showPullToRefresh();
                    isScrolling = true;
                }
            }
        });
        
        document.addEventListener('touchend', () => {
            if (isScrolling) {
                this.triggerRefresh();
                isScrolling = false;
            }
        });
        
        // Swipe gestures for navigation
        this.setupSwipeNavigation();
    }
    
    setupSwipeNavigation() {
        let startX = 0;
        let startY = 0;
        
        document.addEventListener('touchstart', (e) => {
            startX = e.touches[0].clientX;
            startY = e.touches[0].clientY;
        });
        
        document.addEventListener('touchend', (e) => {
            const endX = e.changedTouches[0].clientX;
            const endY = e.changedTouches[0].clientY;
            
            const deltaX = endX - startX;
            const deltaY = endY - startY;
            
            // Only trigger if horizontal swipe is dominant
            if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 50) {
                if (deltaX > 0) {
                    // Swipe right - go back
                    this.handleSwipeRight();
                } else {
                    // Swipe left - go forward
                    this.handleSwipeLeft();
                }
            }
        });
    }
    
    handleSwipeRight() {
        // Go back in navigation
        if (window.history.length > 1) {
            window.history.back();
        }
    }
    
    handleSwipeLeft() {
        // Could implement forward navigation or quick actions
        console.log('Swipe left detected');
    }
    
    showPullToRefresh() {
        const refreshIndicator = document.createElement('div');
        refreshIndicator.id = 'pull-refresh';
        refreshIndicator.innerHTML = '🔄 Pull to refresh';
        refreshIndicator.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            background: #10b981;
            color: white;
            text-align: center;
            padding: 1rem;
            z-index: 1003;
            transform: translateY(-100%);
            transition: transform 0.3s ease;
        `;
        
        document.body.appendChild(refreshIndicator);
        
        setTimeout(() => {
            refreshIndicator.style.transform = 'translateY(0)';
        }, 100);
    }
    
    triggerRefresh() {
        const refreshIndicator = document.getElementById('pull-refresh');
        if (refreshIndicator) {
            refreshIndicator.innerHTML = '✅ Refreshing...';
            
            // Trigger actual refresh
            if (window.lapsoRealTime) {
                window.lapsoRealTime.updateDeviceLocations();
            }
            
            setTimeout(() => {
                refreshIndicator.remove();
            }, 1000);
        }
    }
    
    /**
     * Push notifications setup
     */
    setupNotifications() {
        if ('Notification' in window) {
            // Request permission
            if (Notification.permission === 'default') {
                this.requestNotificationPermission();
            }
            
            // Setup push notifications
            this.setupPushNotifications();
        }
    }
    
    requestNotificationPermission() {
        const banner = document.createElement('div');
        banner.className = 'notification-permission-banner';
        banner.innerHTML = `
            <div style="background: #3b82f6; color: white; padding: 1rem; border-radius: 0.5rem; margin: 1rem; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <strong>🔔 Enable Notifications</strong><br>
                    Get instant alerts when your devices go offline or move
                </div>
                <div>
                    <button id="enable-notifications" style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white; padding: 0.5rem 1rem; border-radius: 0.25rem; cursor: pointer; margin-right: 0.5rem;">Enable</button>
                    <button id="dismiss-notifications" style="background: none; border: none; color: white; font-size: 1.5rem; cursor: pointer;">×</button>
                </div>
            </div>
        `;
        
        document.body.insertBefore(banner, document.body.firstChild);
        
        document.getElementById('enable-notifications').addEventListener('click', () => {
            Notification.requestPermission().then(permission => {
                if (permission === 'granted') {
                    this.showNotification('🔔 Notifications enabled!', 'You\'ll now receive alerts about your devices');
                }
                banner.remove();
            });
        });
        
        document.getElementById('dismiss-notifications').addEventListener('click', () => {
            banner.remove();
        });
    }
    
    setupPushNotifications() {
        // In a real implementation, you'd register with a push service
        // For now, we'll simulate with local notifications
        
        // Listen for device status changes
        if (window.lapsoRealTime) {
            const originalUpdateMethod = window.lapsoRealTime.updateDeviceLocations;
            window.lapsoRealTime.updateDeviceLocations = () => {
                originalUpdateMethod.call(window.lapsoRealTime);
                this.checkForNotificationTriggers();
            };
        }
    }
    
    checkForNotificationTriggers() {
        // This would check for conditions that should trigger notifications
        // For demo purposes, we'll show occasional notifications
        
        if (Math.random() < 0.1) { // 10% chance
            this.showNotification(
                '📍 Device Location Updated',
                'Your laptop location has been updated'
            );
        }
    }
    
    showNotification(title, body, options = {}) {
        if (Notification.permission === 'granted') {
            const notification = new Notification(title, {
                body: body,
                icon: '/favicon.ico',
                badge: '/favicon.ico',
                tag: 'lapso-notification',
                ...options
            });
            
            notification.onclick = () => {
                window.focus();
                notification.close();
            };
            
            // Auto-close after 5 seconds
            setTimeout(() => {
                notification.close();
            }, 5000);
        }
    }
    
    /**
     * Offline support
     */
    setupOfflineSupport() {
        window.addEventListener('online', () => {
            this.showToast('🌐 Back online', 'success');
            if (window.lapsoRealTime) {
                window.lapsoRealTime.updateDeviceLocations();
            }
        });
        
        window.addEventListener('offline', () => {
            this.showToast('📡 Offline mode', 'warning');
        });
        
        // Cache important data
        this.cacheEssentialData();
    }
    
    cacheEssentialData() {
        // Cache device data in localStorage
        if (window.lapsoRealTime) {
            const originalUpdateMethod = window.lapsoRealTime.updateDashboardStats;
            window.lapsoRealTime.updateDashboardStats = (stats) => {
                originalUpdateMethod.call(window.lapsoRealTime, stats);
                localStorage.setItem('lapso-cached-stats', JSON.stringify(stats));
            };
        }
    }
    
    /**
     * Mobile optimizations
     */
    optimizeForMobile() {
        if (!this.isMobile()) return;
        
        // Prevent zoom on input focus
        const viewportMeta = document.querySelector('meta[name="viewport"]');
        if (viewportMeta) {
            viewportMeta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
        }
        
        // Optimize touch targets
        this.optimizeTouchTargets();
        
        // Add loading states
        this.addLoadingStates();
        
        // Optimize images
        this.optimizeImages();
    }
    
    optimizeTouchTargets() {
        const buttons = document.querySelectorAll('button, a, .clickable');
        buttons.forEach(button => {
            const rect = button.getBoundingClientRect();
            if (rect.height < 44 || rect.width < 44) {
                button.style.minHeight = '44px';
                button.style.minWidth = '44px';
                button.style.display = 'flex';
                button.style.alignItems = 'center';
                button.style.justifyContent = 'center';
            }
        });
    }
    
    addLoadingStates() {
        // Add loading skeletons for better perceived performance
        const cards = document.querySelectorAll('.status-card, .device-card');
        cards.forEach(card => {
            card.addEventListener('click', () => {
                card.classList.add('loading-skeleton');
                setTimeout(() => {
                    card.classList.remove('loading-skeleton');
                }, 1000);
            });
        });
    }
    
    optimizeImages() {
        // Lazy load images
        const images = document.querySelectorAll('img');
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        img.src = img.dataset.src;
                        img.classList.remove('lazy');
                        imageObserver.unobserve(img);
                    }
                });
            });
            
            images.forEach(img => imageObserver.observe(img));
        }
    }
    
    /**
     * Utility methods
     */
    isMobile() {
        return window.innerWidth <= 768 || /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    }
    
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `notification-toast notification-${type}`;
        toast.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>${message}</span>
                <button onclick="this.parentElement.parentElement.remove()" style="background: none; border: none; font-size: 1.2rem; cursor: pointer;">×</button>
            </div>
        `;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            if (toast.parentElement) {
                toast.remove();
            }
        }, 5000);
    }
}

// Initialize mobile enhancements
document.addEventListener('DOMContentLoaded', () => {
    window.lapsoMobile = new LapsoMobileEnhancements();
    console.log('📱 LAPSO Mobile Enhancements initialized');
});

// Export for global access
window.LapsoMobileEnhancements = LapsoMobileEnhancements;