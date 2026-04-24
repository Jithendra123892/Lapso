# LAPSO Windows Agent Installer (non-interactive friendly)
# Detects device ID automatically. Prompts for user email on first run if not provided.
param(
    [string]$UserEmail,
    [string]$ServerUrl = "http://localhost:8080"
)

Write-Host "LAPSO Agent Installer" -ForegroundColor Green
Write-Host "======================" -ForegroundColor Green
Write-Host ""

# Check if running as administrator
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "This installer must be run as Administrator." -ForegroundColor Red
    Write-Host "Right-click PowerShell and select 'Run as Administrator'" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "Running with Administrator privileges" -ForegroundColor Green

# Auto-detect DeviceId (UUID) and device name
try {
    $csprod = Get-CimInstance Win32_ComputerSystemProduct -ErrorAction SilentlyContinue
    $DeviceId = $csprod.UUID
    if ([string]::IsNullOrWhiteSpace($DeviceId) -or $DeviceId -eq 'FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF') {
        $bios = Get-CimInstance Win32_BIOS -ErrorAction SilentlyContinue
        $DeviceId = $bios.SerialNumber
    }
} catch { $DeviceId = $env:COMPUTERNAME }
if ([string]::IsNullOrWhiteSpace($DeviceId)) { $DeviceId = $env:COMPUTERNAME }

Write-Host ("Detected Device ID: {0}" -f $DeviceId) -ForegroundColor Cyan

# Read user email from env or prompt once
if ([string]::IsNullOrWhiteSpace($UserEmail)) {
    $UserEmail = $env:LAPSO_USER_EMAIL
}
if ([string]::IsNullOrWhiteSpace($UserEmail)) {
    $UserEmail = Read-Host "Enter your LAPSO account email"
}
if ([string]::IsNullOrWhiteSpace($UserEmail)) {
    Write-Host "User email is required to link this device to your account." -ForegroundColor Red
    exit 1
}

# Create LAPSO directory
$lapsoDir = "C:\Program Files\LAPSO"
if (!(Test-Path $lapsoDir)) {
    New-Item -ItemType Directory -Path $lapsoDir -Force | Out-Null
    Write-Host "✅ Created LAPSO directory: $lapsoDir" -ForegroundColor Green
}

# Download and install the agent script
$agentScript = "$lapsoDir\lapso-agent.ps1"
$agentUrl = "$ServerUrl/api/agents/download/windows/laptop-tracker-agent.ps1"

try {
    Write-Host "Downloading LAPSO agent..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $agentUrl -OutFile $agentScript -UseBasicParsing
    Write-Host "Agent downloaded successfully" -ForegroundColor Green
} catch {
    Write-Host "Failed to download agent: $($_.Exception.Message)" -ForegroundColor Red
    pause
    exit 1
}

# Create configuration file
$configFile = "$lapsoDir\config.json"
$config = @{
    deviceId = $DeviceId
    userEmail = $UserEmail
    serverUrl = $ServerUrl
    updateInterval = 30
    enableLocationTracking = $true
    enableSystemMonitoring = $true
    enableNetworkMonitoring = $true
    agentVersion = "1.0.0"
} | ConvertTo-Json -Depth 3

$config | Out-File -FilePath $configFile -Encoding UTF8
Write-Host "Configuration saved: $configFile" -ForegroundColor Green

# Create Windows Service
$serviceName = "LAPSOAgent"
$serviceDisplayName = "LAPSO Laptop Security Agent"
$serviceDescription = "LAPSO free laptop tracking and security service - Better than Microsoft Find My Device"

# Remove existing service if it exists
if (Get-Service -Name $serviceName -ErrorAction SilentlyContinue) {
    Write-Host "🔄 Removing existing LAPSO service..." -ForegroundColor Yellow
    Stop-Service -Name $serviceName -Force -ErrorAction SilentlyContinue
    sc.exe delete $serviceName | Out-Null
}

# Create service wrapper script
$serviceScript = "$lapsoDir\service-wrapper.ps1"
$serviceScriptContent = @"
# LAPSO Service Wrapper
`$configPath = "C:\Program Files\LAPSO\config.json"
`$agentPath = "C:\Program Files\LAPSO\lapso-agent.ps1"

if (Test-Path `$configPath) {
    `$config = Get-Content `$configPath | ConvertFrom-Json
    & `$agentPath -DeviceId `$config.deviceId -UserEmail `$config.userEmail -ServerUrl `$config.serverUrl
} else {
    Write-EventLog -LogName Application -Source "LAPSO" -EventId 1001 -EntryType Error -Message "LAPSO configuration file not found"
}
"@

$serviceScriptContent | Out-File -FilePath $serviceScript -Encoding UTF8

# Install as Windows Service using NSSM (if available) or Task Scheduler
Write-Host "Installing LAPSO scheduled task..." -ForegroundColor Yellow

# Create scheduled task instead of service for better compatibility
$taskName = "LAPSO Agent"
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-WindowStyle Hidden -ExecutionPolicy Bypass -File `"$serviceScript`""
$trigger = New-ScheduledTaskTrigger -AtStartup
$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1)
$principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest

# Remove existing task if it exists
Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue

# Register new task
Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Settings $settings -Principal $principal -Description $serviceDescription | Out-Null

Write-Host "LAPSO scheduled task installed" -ForegroundColor Green

# Start the service
Write-Host "Starting LAPSO agent..." -ForegroundColor Yellow
Start-ScheduledTask -TaskName $taskName

# Wait a moment and check status
Start-Sleep -Seconds 3
$task = Get-ScheduledTask -TaskName $taskName
if ($task.State -eq "Running") {
    Write-Host "LAPSO agent is running." -ForegroundColor Green
} else {
    Write-Host "LAPSO agent may not be running. Check Task Scheduler." -ForegroundColor Yellow
}

# Create uninstaller
$uninstallerScript = "$lapsoDir\uninstall.ps1"
$uninstallerContent = @"
# LAPSO Uninstaller
Write-Host "Uninstalling LAPSO Agent..." -ForegroundColor Yellow

# Read config to notify server
`$configPath = "C:\Program Files\LAPSO\config.json"
if (Test-Path `$configPath) {
    try {
        `$config = Get-Content `$configPath | ConvertFrom-Json
        `$body = @{
            deviceId = `$config.deviceId
            userEmail = `$config.userEmail
            reason = "User uninstalled"
        } | ConvertTo-Json
        
        Invoke-RestMethod -Uri "`$(`$config.serverUrl)/api/agent/uninstall" ``
            -Method POST ``
            -Body `$body ``
            -ContentType "application/json" ``
            -ErrorAction SilentlyContinue
        
        Write-Host "Notified server of uninstall" -ForegroundColor Green
    } catch {
        Write-Host "Could not notify server (continuing with uninstall)" -ForegroundColor Yellow
    }
}

# Stop and remove scheduled task
Unregister-ScheduledTask -TaskName "LAPSO Agent" -Confirm:`$false -ErrorAction SilentlyContinue

# Remove files
Remove-Item -Path "C:\Program Files\LAPSO" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "LAPSO Agent uninstalled successfully" -ForegroundColor Green
pause
"@

$uninstallerContent | Out-File -FilePath $uninstallerScript -Encoding UTF8

# Final instructions
Write-Host ""
Write-Host "Installation complete." -ForegroundColor Green
Write-Host ("Device ID: {0}" -f $DeviceId) -ForegroundColor Cyan
Write-Host ("User Email: {0}" -f $UserEmail) -ForegroundColor Cyan
Write-Host ("Server URL: {0}" -f $ServerUrl) -ForegroundColor Cyan
Write-Host ""
Write-Host ("To uninstall: Run {0}" -f $uninstallerScript) -ForegroundColor Yellow
Write-Host ""

pause