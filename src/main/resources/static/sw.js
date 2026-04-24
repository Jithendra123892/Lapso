// LAPSO Service Worker - PWA Support
const CACHE_NAME = 'lapso-v1.0.0';
const STATIC_CACHE = 'lapso-static-v1';
const DYNAMIC_CACHE = 'lapso-dynamic-v1';

// Files to cache for offline functionality
const STATIC_FILES = [
    '/',
    '/login',
    '/css/mobile-responsive.css',
    '/js/mobile-enhancements.js',
    '/js/lapso-realtime.js',
    '/manifest.json',
    '/favicon.ico'
];

// Install event - cache static files
self.addEventListener('install', (event) => {
    console.log('🔧 Service Worker: Installing...');
    
    event.waitUntil(
        caches.open(STATIC_CACHE)
            .then((cache) => {
                console.log('🔧 Service Worker: Caching static files');
                return cache.addAll(STATIC_FILES);
            })
            .then(() => {
                console.log('✅ Service Worker: Installation complete');
                return self.skipWaiting();
            })
            .catch((error) => {
                console.error('❌ Service Worker: Installation failed', error);
            })
    );
});

// Activate event - clean up old caches
self.addEventListener('activate', (event) => {
    console.log('🔧 Service Worker: Activating...');
    
    event.waitUntil(
        caches.keys()
            .then((cacheNames) => {
                return Promise.all(
                    cacheNames.map((cacheName) => {
                        if (cacheName !== STATIC_CACHE && cacheName !== DYNAMIC_CACHE) {
                            console.log('🗑️ Service Worker: Deleting old cache', cacheName);
                            return caches.delete(cacheName);
                        }
                    })
                );
            })
            .then(() => {
                console.log('✅ Service Worker: Activation complete');
                return self.clients.claim();
            })
    );
});

// Fetch event - serve cached files or fetch from network
self.addEventListener('fetch', (event) => {
    const { request } = event;
    const url = new URL(request.url);
    
    // Skip non-GET requests
    if (request.method !== 'GET') {
        return;
    }
    
    // Skip external requests
    if (url.origin !== location.origin) {
        return;
    }
    
    // Handle API requests differently
    if (url.pathname.startsWith('/api/')) {
        event.respondWith(handleApiRequest(request));
        return;
    }
    
    // Handle static files
    event.respondWith(handleStaticRequest(request));
});

// Handle API requests with network-first strategy
async function handleApiRequest(request) {
    try {
        // Try network first
        const networkResponse = await fetch(request);
        
        // Cache successful responses
        if (networkResponse.ok) {
            const cache = await caches.open(DYNAMIC_CACHE);
            cache.put(request, networkResponse.clone());
        }
        
        return networkResponse;
    } catch (error) {
        console.log('🌐 Service Worker: Network failed, trying cache for API request');
        
        // Fallback to cache
        const cachedResponse = await caches.match(request);
        if (cachedResponse) {
            return cachedResponse;
        }
        
        // Return offline response for API requests
        return new Response(
            JSON.stringify({
                error: 'Offline',
                message: 'This request requires an internet connection',
                cached: false
            }),
            {
                status: 503,
                statusText: 'Service Unavailable',
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        );
    }
}

// Handle static requests with cache-first strategy
async function handleStaticRequest(request) {
    try {
        // Try cache first
        const cachedResponse = await caches.match(request);
        if (cachedResponse) {
            return cachedResponse;
        }
        
        // Fallback to network
        const networkResponse = await fetch(request);
        
        // Cache the response
        if (networkResponse.ok) {
            const cache = await caches.open(DYNAMIC_CACHE);
            cache.put(request, networkResponse.clone());
        }
        
        return networkResponse;
    } catch (error) {
        console.log('🌐 Service Worker: Both cache and network failed');
        
        // Return offline page for navigation requests
        if (request.mode === 'navigate') {
            return caches.match('/') || new Response(
                `
                <!DOCTYPE html>
                <html>
                <head>
                    <title>LAPSO - Offline</title>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            min-height: 100vh;
                            margin: 0;
                            background: #f8fafc;
                            color: #1f2937;
                            text-align: center;
                            padding: 2rem;
                        }
                        .offline-icon {
                            font-size: 4rem;
                            margin-bottom: 1rem;
                        }
                        .offline-title {
                            font-size: 2rem;
                            font-weight: 700;
                            margin-bottom: 0.5rem;
                        }
                        .offline-message {
                            font-size: 1.125rem;
                            color: #6b7280;
                            margin-bottom: 2rem;
                        }
                        .retry-button {
                            background: #10b981;
                            color: white;
                            border: none;
                            padding: 0.75rem 1.5rem;
                            border-radius: 0.5rem;
                            font-size: 1rem;
                            font-weight: 600;
                            cursor: pointer;
                            transition: background 0.2s;
                        }
                        .retry-button:hover {
                            background: #059669;
                        }
                    </style>
                </head>
                <body>
                    <div class="offline-icon">📡</div>
                    <h1 class="offline-title">You're Offline</h1>
                    <p class="offline-message">
                        LAPSO needs an internet connection to track your devices.<br>
                        Check your connection and try again.
                    </p>
                    <button class="retry-button" onclick="window.location.reload()">
                        🔄 Try Again
                    </button>
                    
                    <script>
                        // Auto-retry when back online
                        window.addEventListener('online', () => {
                            window.location.reload();
                        });
                    </script>
                </body>
                </html>
                `,
                {
                    headers: {
                        'Content-Type': 'text/html'
                    }
                }
            );
        }
        
        // Return generic offline response
        return new Response('Offline', { status: 503 });
    }
}

// Background sync for offline actions
self.addEventListener('sync', (event) => {
    console.log('🔄 Service Worker: Background sync triggered', event.tag);
    
    if (event.tag === 'device-update') {
        event.waitUntil(syncDeviceUpdates());
    }
});

async function syncDeviceUpdates() {
    try {
        // Get pending updates from IndexedDB or localStorage
        const pendingUpdates = JSON.parse(localStorage.getItem('lapso-pending-updates') || '[]');
        
        for (const update of pendingUpdates) {
            try {
                await fetch('/api/agent/heartbeat', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(update)
                });
                
                console.log('✅ Service Worker: Synced pending update');
            } catch (error) {
                console.log('❌ Service Worker: Failed to sync update', error);
            }
        }
        
        // Clear pending updates
        localStorage.removeItem('lapso-pending-updates');
        
    } catch (error) {
        console.error('❌ Service Worker: Background sync failed', error);
    }
}

// Push notification handling
self.addEventListener('push', (event) => {
    console.log('🔔 Service Worker: Push notification received');
    
    const options = {
        body: 'Your device status has been updated',
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        vibrate: [200, 100, 200],
        data: {
            dateOfArrival: Date.now(),
            primaryKey: 1
        },
        actions: [
            {
                action: 'view',
                title: 'View Dashboard',
                icon: '/icons/action-view.png'
            },
            {
                action: 'close',
                title: 'Close',
                icon: '/icons/action-close.png'
            }
        ]
    };
    
    if (event.data) {
        const data = event.data.json();
        options.body = data.message || options.body;
        options.title = data.title || 'LAPSO Alert';
    }
    
    event.waitUntil(
        self.registration.showNotification('🛡️ LAPSO', options)
    );
});

// Notification click handling
self.addEventListener('notificationclick', (event) => {
    console.log('🔔 Service Worker: Notification clicked');
    
    event.notification.close();
    
    if (event.action === 'view') {
        event.waitUntil(
            clients.openWindow('/')
        );
    }
});

// Message handling from main thread
self.addEventListener('message', (event) => {
    console.log('💬 Service Worker: Message received', event.data);
    
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
    
    if (event.data && event.data.type === 'CACHE_DEVICE_DATA') {
        cacheDeviceData(event.data.data);
    }
});

async function cacheDeviceData(data) {
    try {
        const cache = await caches.open(DYNAMIC_CACHE);
        const response = new Response(JSON.stringify(data), {
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        await cache.put('/api/cached-devices', response);
        console.log('✅ Service Worker: Device data cached');
    } catch (error) {
        console.error('❌ Service Worker: Failed to cache device data', error);
    }
}

console.log('🔧 Service Worker: Script loaded');