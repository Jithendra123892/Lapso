# 🧪 LAPSO Remote Control Testing Guide

## Pre-Flight Checklist
✅ Agent installed and running  
✅ Server running on http://localhost:8080  
✅ Device showing in dashboard with green status  
✅ Battery info working (-1 for desktops, 0-100 for laptops)

## Testing Lock Feature 🔒

### Expected Behavior
When you click the Lock button, the laptop should:
1. Immediately display Windows lock screen (Ctrl+Alt+Del screen)
2. Require password/PIN to unlock
3. All apps remain open in background

### Test Steps
1. Login to http://localhost:8080
2. Navigate to Map view (`/map`)
3. Select your device from dropdown (e.g., "SUHASINI")
4. Click **Lock** button
5. Click **Confirm** in dialog

### Success Criteria
**Browser Console (F12):**
```
🔒 Sending lock command to: /api/quick-actions/lock/0B7ABA31-AD7B-4AE3-8301-C4C6E6EFCE32
Lock response status: 200
Lock response data: {success: true, message: "Lock command queued for device"}
```

**Server Log:**
```
📋 Queued command for device 0B7ABA31-AD7B-4AE3-8301-C4C6E6EFCE32: LOCK
```

**Agent Log (C:\ProgramData\Lapso\agent.log):**
```
[2025-10-22 23:30:00] INFO: Retrieved 1 pending commands
[2025-10-22 23:30:01] INFO: Executing command: LOCK
[2025-10-22 23:30:01] INFO: Locking device with enhanced security...
[2025-10-22 23:30:01] INFO: Device locked (basic screen lock)
```

**Physical Result:**
- Windows lock screen appears immediately
- Laptop asks for password/PIN
- All programs stay running (just locked)

---

## Testing Screenshot Feature 📸

### Expected Behavior
Agent captures current screen and uploads to server.

### Test Steps
1. Open something visible on screen (e.g., Notepad with "TEST" written)
2. In LAPSO UI, select device and click **Screenshot**
3. Confirm the dialog

### Success Criteria
**Browser Console:**
```
📸 Sending screenshot command to: /api/quick-actions/screenshot/[DEVICE-ID]
Screenshot response status: 200
Screenshot response data: {success: true, message: "Screenshot command queued"}
```

**Server Log:**
```
📋 Queued command for device [DEVICE-ID]: SCREENSHOT
📤 Screenshot uploaded for device [DEVICE-ID]
```

**Agent Log:**
```
[2025-10-22 23:35:00] INFO: Taking screenshot...
[2025-10-22 23:35:01] INFO: Uploading screenshot to server...
[2025-10-22 23:35:02] INFO: Screenshot captured and uploaded successfully
```

**Result:**
- Screenshot appears in LAPSO UI under "Recent Screenshots"
- Image matches what was on screen at capture time

---

## Testing Wipe Feature 🗑️

### Expected Behavior
**EMERGENCY FEATURE** - Locks device immediately with warning.

### Test Steps (⚠️ CAREFUL)
1. **BACKUP ANY IMPORTANT DATA FIRST**
2. Select device and click **Wipe**
3. Confirm **first** dialog
4. Confirm **second** dialog (emergency confirmation)

### Success Criteria
**Browser Console:**
```
💀 Sending WIPE command to: /api/quick-actions/wipe/[DEVICE-ID]
Wipe response status: 200
Wipe response data: {success: true, message: "WIPE command queued - EMERGENCY ACTION"}
```

**Server Log:**
```
⚠️ EMERGENCY WIPE QUEUED for device [DEVICE-ID]
```

**Agent Log:**
```
[2025-10-22 23:40:00] WARN: EMERGENCY WIPE COMMAND!
[2025-10-22 23:40:01] INFO: Device locked for security
[2025-10-22 23:40:01] WARN: EMERGENCY WIPE INITIATED - Device locked for security
```

**Result:**
- Device locks immediately (same as Lock command)
- In production: Would securely erase data
- Current test mode: Just locks device as safeguard

---

## Troubleshooting

### Problem: "Authentication required" error
**Solution:** Make sure you're logged into http://localhost:8080 in your browser. Session-based auth requires active login.

### Problem: No commands executed on device
**Check:**
1. Agent logs: `C:\ProgramData\Lapso\agent.log`
2. Server logs in terminal
3. Device ID matches in both places

**Fix:**
```powershell
# Restart agent manually
C:\Program Files\LAPSO\laptop-tracker-agent.ps1
```

### Problem: Commands stay in queue
**Possible causes:**
- Agent not running (check Task Scheduler)
- Agent polling interval (30 seconds)
- Network connectivity issues

**Check:**
```powershell
# View scheduled task
Get-ScheduledTask -TaskName "LAPSO Agent"

# View agent process
Get-Process -Name "powershell" | Where-Object {$_.Path -like "*LAPSO*"}
```

### Problem: Dialog appearing behind map
**Status:** ✅ **FIXED** - Dialogs now use z-index 99999 with modal backdrop

### Problem: Battery showing -1
**Status:** ✅ **EXPECTED** - Desktop PCs without battery show -1 (not an error)

---

## Command Flow Diagram

```
┌─────────────┐
│  Browser UI │
│  (Vaadin)   │
└──────┬──────┘
       │ POST /api/quick-actions/{action}/{deviceId}
       v
┌─────────────────────┐
│ QuickActionsController│
│ (Spring Boot)        │
└──────┬──────────────┘
       │ queue command
       v
┌─────────────────────┐
│ DeviceCommandController│
│ (In-memory queue)    │
└──────┬──────────────┘
       │ wait for polling
       v
┌─────────────────────┐
│ Agent polls every   │
│ 30 seconds          │
└──────┬──────────────┘
       │ GET /api/device-commands/poll
       v
┌─────────────────────┐
│ Execute-Command     │
│ (PowerShell)        │
└──────┬──────────────┘
       │ rundll32.exe user32.dll,LockWorkStation
       v
┌─────────────────────┐
│ Windows Lock Screen │
└─────────────────────┘
```

---

## Quick Test Script

Run this in PowerShell to verify agent connectivity:

```powershell
# Check if agent is running
$agentPath = "C:\Program Files\LAPSO\laptop-tracker-agent.ps1"
if (Test-Path $agentPath) {
    Write-Host "✅ Agent installed" -ForegroundColor Green
    
    # Check scheduled task
    $task = Get-ScheduledTask -TaskName "LAPSO Agent" -ErrorAction SilentlyContinue
    if ($task) {
        Write-Host "✅ Scheduled task exists: $($task.State)" -ForegroundColor Green
    } else {
        Write-Host "❌ Scheduled task not found" -ForegroundColor Red
    }
    
    # Check logs
    $logPath = "C:\ProgramData\Lapso\agent.log"
    if (Test-Path $logPath) {
        Write-Host "✅ Agent log found" -ForegroundColor Green
        Write-Host "`nLast 5 log entries:" -ForegroundColor Cyan
        Get-Content $logPath -Tail 5
    }
} else {
    Write-Host "❌ Agent not installed" -ForegroundColor Red
}

# Test server connectivity
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 5
    Write-Host "`n✅ Server is responding (HTTP $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "`n❌ Server not responding: $_" -ForegroundColor Red
}
```

---

**Last Updated:** 2025-10-22  
**Status:** Ready for testing  
**Build:** laptop-tracker-3.2.8.jar
