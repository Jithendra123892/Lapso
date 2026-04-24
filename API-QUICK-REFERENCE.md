# 🚀 LAPSO Premium API Quick Reference

## Authentication
All endpoints require authentication. Include session cookies or authentication headers.

---

## 1. Remote Commands API

### Send Command
```http
POST /api/remote-commands/send
Content-Type: application/json

{
  "deviceId": "LAPTOP-001",
  "commandType": "LOCK",
  "commandParams": "{\"message\":\"Device locked remotely\"}",
  "priority": 1
}
```

### Agent Poll for Commands
```http
GET /api/remote-commands/poll/{deviceId}
```

### Report Execution Result
```http
POST /api/remote-commands/result
Content-Type: application/json

{
  "commandId": 123,
  "status": "COMPLETED",
  "resultMessage": "Device locked successfully"
}
```

### Get Command History
```http
GET /api/remote-commands/history/{deviceId}
```

---

## 2. Location History API

### Get Device History
```http
GET /api/location-history/{deviceId}?hours=24
```

### Get Heatmap Data
```http
GET /api/location-history/{deviceId}/heatmap
```

### Get Statistics
```http
GET /api/location-history/{deviceId}/stats

Response:
{
  "totalDistanceKm": 45.2,
  "maxSpeedKmh": 80.5,
  "uniqueLocations": 12,
  "timeRange": {...}
}
```

---

## 3. Device Health API

### Get Full Health Report
```http
GET /api/device-health/{deviceId}

Response:
{
  "overallScore": 85,
  "status": "GOOD",
  "batteryHealth": {...},
  "diskHealth": {...},
  "performanceHealth": {...},
  "connectivityHealth": {...},
  "securityHealth": {...},
  "issues": [],
  "recommendations": []
}
```

### Get Health Summary (All Devices)
```http
GET /api/device-health/summary
```

---

## 4. Geofencing API

### Create Geofence
```http
POST /api/geofences
Content-Type: application/json

{
  "name": "Home",
  "centerLatitude": 28.6139,
  "centerLongitude": 77.2090,
  "radiusMeters": 500,
  "fenceType": "SAFE_ZONE",
  "alertOnEntry": false,
  "alertOnExit": true,
  "autoLockOnExit": false
}
```

### List Geofences
```http
GET /api/geofences?activeOnly=true
```

### Update Geofence
```http
PUT /api/geofences/{id}
Content-Type: application/json

{
  "radiusMeters": 1000,
  "alertOnExit": true
}
```

### Check Device Location
```http
GET /api/geofences/check/{deviceId}

Response:
{
  "insideGeofences": [...],
  "isInsideAnyGeofence": true
}
```

### Toggle Geofence
```http
POST /api/geofences/{id}/toggle
```

### Delete Geofence
```http
DELETE /api/geofences/{id}
```

---

## 5. Location Sharing API

### Create Share Link
```http
POST /api/share-links/create
Content-Type: application/json

{
  "deviceId": "LAPTOP-001",
  "title": "My Location",
  "shareType": "REAL_TIME",
  "expiresInHours": 24,
  "maxViews": 10,
  "password": "secret123"
}

Response:
{
  "shareUrl": "https://your-domain.com/share/abc123...",
  "token": "abc123..."
}
```

### List My Share Links
```http
GET /api/share-links/my-links?activeOnly=true
```

### Access Share Link (Public)
```http
POST /api/share-links/access/{token}
Content-Type: application/json

{
  "password": "secret123"
}

Response:
{
  "location": {
    "deviceName": "My Laptop",
    "latitude": 28.6139,
    "longitude": 77.2090,
    "lastUpdated": "2025-10-22T10:30:00"
  }
}
```

### Deactivate Link
```http
DELETE /api/share-links/{linkId}
```

### Get Link Statistics
```http
GET /api/share-links/stats/{linkId}

Response:
{
  "totalViews": 5,
  "maxViews": 10,
  "remainingViews": 5,
  "isExpired": false
}
```

---

## Command Types Reference

### Remote Commands
- `LOCK` - Lock the device
- `UNLOCK` - Unlock the device
- `WIPE` - Wipe device data
- `SCREENSHOT` - Capture screenshot
- `ALARM` - Play alarm sound
- `MESSAGE` - Display message
- `LOCATE` - Force location update

### Geofence Types
- `SAFE_ZONE` - Home/Office (alert on exit)
- `RESTRICTED_ZONE` - Alert on entry
- `WORK_ZONE` - Track work hours
- `SCHOOL_ZONE` - Educational tracking

### Share Types
- `REAL_TIME` - Live location updates
- `SNAPSHOT` - Location at creation time
- `LAST_KNOWN` - Last known location

### Permission Levels
- `VIEW_ONLY` - Can only view
- `TRACK_CONTROL` - Can track + send commands
- `FULL_ADMIN` - Full control

---

## WebSocket Topics

Subscribe to these topics for real-time updates:

```javascript
// Device updates
/user/queue/device-updates

// Geofence alerts
/user/queue/geofence-alerts

// Share updates
/user/queue/share-updates

// Command status
/user/queue/command-updates
```

---

## Error Codes

- `401` - Not authenticated
- `403` - Access denied
- `404` - Resource not found
- `409` - Conflict (e.g., duplicate)
- `410` - Gone (expired)
- `500` - Server error

---

## Rate Limits

- Remote Commands: 10/minute per device
- Location Updates: 1/second per device
- Share Link Creation: 100/hour per user
- Geofence Operations: 50/minute per user

---

## Best Practices

1. **Always check device ownership** before operations
2. **Use WebSocket** for real-time updates instead of polling
3. **Set reasonable expiration** times for share links
4. **Monitor command status** after sending
5. **Handle offline devices** gracefully
6. **Validate geofence coordinates** before creation
7. **Use priority levels** wisely for commands
8. **Cache health data** (updates every 5 minutes)

---

## Example: Complete Workflow

### 1. Track Device Location
```javascript
// Subscribe to real-time updates
stompClient.subscribe('/user/queue/device-updates', (update) => {
  console.log('Location updated:', update);
});
```

### 2. Create Geofence Around Home
```javascript
fetch('/api/geofences', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'Home',
    centerLatitude: 28.6139,
    centerLongitude: 77.2090,
    radiusMeters: 500,
    alertOnExit: true
  })
});
```

### 3. Get Notified on Exit
```javascript
stompClient.subscribe('/user/queue/geofence-alerts', (alert) => {
  if (alert.type === 'GEOFENCE_EXIT') {
    // Send lock command
    fetch('/api/remote-commands/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        deviceId: alert.deviceId,
        commandType: 'LOCK',
        priority: 1
      })
    });
  }
});
```

### 4. Share Location with Family
```javascript
fetch('/api/share-links/create', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    deviceId: 'LAPTOP-001',
    title: 'Tracking My Laptop',
    expiresInHours: 72,
    password: 'family123'
  })
}).then(res => res.json())
  .then(data => {
    console.log('Share link:', data.shareUrl);
    // Send link to family
  });
```

---

*LAPSO Premium API v2.0*
*Last Updated: October 22, 2025*
