# 🛡️ LAPSO - Free & Open Source Laptop Tracking System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-24+-purple.svg)](https://vaadin.com/)

## ✨ **Simple, Honest, Completely Free - No False Claims**

**LAPSO** is a free, open-source laptop tracking web application built with Spring Boot and Vaadin. It provides real device tracking capabilities without any costs, subscriptions, or hidden fees. Built with honesty and transparency in mind.

### 🎯 **What LAPSO Actually Is**
- **Reality**: A Spring Boot web application for basic device tracking
- **Not**: Military-grade, AI-powered, or enterprise-level security system
- **Accuracy**: Depends on device GPS and network (typically 10-100 meters)
- **Comparison**: More features than Microsoft Find My Device, always free
- **Philosophy**: Honest capabilities, no marketing hype

---

## 🚀 **Quick Start**

### **Prerequisites**
- Java 17 or higher
- Maven 3.6+
- PostgreSQL (optional, H2 included for development)

### **Run Locally**
```bash
# Clone the repository
git clone https://github.com/Jithendra123892/LAPSO.git
cd LAPSO

# Run with Maven
mvn spring-boot:run

# Or use the provided script
start-lapso-complete.bat
```

### **Access the Application**
- **URL**: http://localhost:8080
- **Admin Login**: admin@lapso.in / admin123
- **Registration**: Create your account through the web interface

### **Set Up Device Tracking**
1. **Register Devices**: Log in and navigate to the dashboard
2. **Download Agents**: Go to the 'Download Agent' page to get platform-specific installers
3. **Install Agents**: Run the installers on devices you want to track
4. **Monitor Devices**: View device locations and status on the dashboard
5. **Execute Commands**: Use remote control features to lock, locate, or secure devices

---

## How to run (Windows / PowerShell)

These are the recommended steps for a local run on Windows using PowerShell. Java 17+ is required; Maven is optional because this project ships with the Maven Wrapper.

1) From the project root, start the app in dev mode

```powershell
.# using Maven Wrapper (recommended)
./mvnw.cmd spring-boot:run

# if Maven is installed globally, this also works
mvn spring-boot:run
```

2) Open the app and sign in

- URL: http://localhost:8080
- Default admin: admin@lapso.in / admin123

3) Optional: build a runnable JAR

```powershell
./mvnw.cmd -DskipTests clean package

# then run the generated JAR from the target folder (exact version may vary)
java -jar .\target\laptop-tracker-*.jar
```

Notes

- First run may take a minute while Vaadin builds the frontend.
- Stop the app with Ctrl+C in the terminal.
- PostgreSQL is optional; by default, an in-memory H2 database is used for development.
- Download page: http://localhost:8080/download-agent (Windows/macOS/Linux installers)

---

## 🌟 **Key Features**

### ✅ **Device Management**
- **Real-time tracking**: Live location updates every 30 seconds
- **Device registration**: Easy setup with cross-platform agents
- **Remote actions**: Lock, unlock, sound alarm, get location
- **Device status**: Battery level, online/offline status, last seen
- **Multi-device support**: Track multiple laptops, phones, tablets

### ✅ **User Interface**
- **Clean dashboard**: Modern, responsive design
- **Live map view**: Interactive device location display
- **Mobile-friendly**: Works on phones, tablets, and desktops
- **Real-time updates**: WebSocket-powered live updates
- **Agent downloads**: Easy installer downloads for all platforms

### ✅ **Security & Privacy**
- **Self-hosted**: Your data stays with you
- **Open source**: MIT license, fully transparent
- **Session-based auth**: Simple, secure authentication
- **No tracking**: We don't collect your personal data
- **Encrypted communication**: Secure device-server communication

### ✅ **Cross-Platform Support**
- **Windows**: PowerShell agent with installer
- **macOS**: Shell script agent with installer  
- **Linux**: Shell script agent with installer

### ✅ **Advanced Features**
- **Geofencing**: Custom location boundaries with alerts
- **Offline Detection**: Risk scoring for disconnected devices
- **Hardware Detection**: Enhanced device information gathering
- **Device Clustering**: Group devices on maps for better visualization
- **Comprehensive Reporting**: Dashboard with analytics and device history
- **Predictive Analytics**: Anticipate device behavior and potential issues

---

## 🏗️ **Architecture**

### **Backend (Spring Boot)**
- **Framework**: Spring Boot 3.2+ with Java 17
- **Database**: H2 (development) / PostgreSQL (production)
- **Security**: Spring Security with session management
- **Real-time**: WebSocket support for live updates
- **API**: RESTful endpoints for device management

### **Frontend (Vaadin)**
- **Framework**: Vaadin Flow 24+ (Java-based UI)
- **Design**: Clean, modern interface with CSS3
- **Responsive**: Mobile-first design approach
- **Real-time**: Live dashboard updates without refresh
- **PWA**: Progressive Web App capabilities

### **Agent Communication Protocol**

**Command/Control Architecture:**
1. **Heartbeat**: Agents send device information and location every 30 seconds
2. **Command Polling**: Agents check for pending commands from the server
3. **Result Reporting**: Agents report command execution results back to the server

**Security Implementation:**
- All communication encrypted via HTTPS
- Device authentication with unique device IDs
- Command queuing system for reliable delivery
- Result acknowledgment for command tracking

### **Services Architecture**
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

### **Agent Implementation Details**

**Windows Agent (PowerShell):**
- Multiple lock methods (rundll32, VBS script, Win32 API, Scheduled Task)
- Webcam capture using Win32 API
- Secure data wiping with 3 overwrite passes
- Self-updating mechanism

**macOS Agent (Bash):**
- Multiple lock methods (pmset, AppleScript, screencapture)
- Camera capture using imagesnap/ffmpeg/fswebcam
- Secure data wiping with 3 overwrite passes
- Keychain and credential clearing

**Linux Agent (Bash):**
- Multiple lock methods (loginctl, GNOME/KDE screensavers, xset)
- Camera capture using fswebcam/ffmpeg/streamer
- Secure data wiping with 3 overwrite passes
- System credential clearing

---

## � Connectivity (Diagrams + Explanation)

For a single page with the full connectivity setup (architecture + end-to-end sequence) using Mermaid diagrams, see:

- docs/CONNECTIVITY.md — Email/password authentication, agent heartbeat, command flow, map view, and export-to-image tips.

You can view the diagrams in VS Code Markdown Preview or on GitHub and export them as PNG/SVG.

## �📱 **Device Agents**

### **Windows Agent** (`lapso-installer.ps1`)
```powershell
# Download and run
Invoke-WebRequest -Uri "http://localhost:8080/api/agents/download/windows/lapso-installer.ps1" -OutFile "lapso-installer.ps1"
PowerShell -ExecutionPolicy Bypass -File "lapso-installer.ps1"
```

### **macOS/Linux Agent** (`lapso-installer.sh`)
```bash
# Download and run
curl -O http://localhost:8080/api/agents/download/macos/lapso-installer.sh
chmod +x lapso-installer.sh
./lapso-installer.sh
```

### **Agent Communication Protocol**

Agents communicate with the server using a simple REST API protocol:

1. **Heartbeat**: Agents send device information and location every 30 seconds
2. **Command Polling**: Agents check for pending commands from the server
3. **Result Reporting**: Agents report command execution results back to the server

---

## 🔧 **Configuration**

### **Database Configuration**

#### **H2 (Default - Development)**
```properties
# Embedded H2 database (no setup required)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

#### **PostgreSQL (Production)**
```properties
# PostgreSQL configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/lapso
spring.datasource.username=postgres
spring.datasource.password=your-password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### **Application Properties**
```properties
# Server configuration
server.port=8080
server.servlet.context-path=/

# Logging
logging.level.com.example.demo=INFO
logging.file.name=logs/lapso.log

# Vaadin
vaadin.productionMode=false
```

---

## 🛠️ **Development**

### **Project Structure**
```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── controller/          # REST API controllers
│   │   ├── model/              # JPA entities
│   │   ├── repository/         # Data repositories
│   │   ├── service/            # Business logic
│   │   ├── views/              # Vaadin UI views
│   │   ├── config/             # Configuration classes
│   │   └── security/           # Security components
│   └── resources/
│       ├── static/             # Static web resources
│       │   ├── agents/         # Device agent scripts
│       │   ├── css/           # Stylesheets
│       │   └── js/            # JavaScript files
│       └── application.properties
```

### **Key Components**

#### **Views (Vaadin UI)**
- `CleanLoginView`: User authentication
- `CleanDashboard`: Main dashboard with device overview
- `CleanMapView`: Interactive map for device locations
- `AgentDownloadView`: Agent download and setup
- `CleanAnalyticsView`: Device analytics and reports

#### **Services**
- `SimpleAuthService`: User authentication and session management
- `DeviceService`: Device CRUD operations and management
- `AnalyticsService`: Device analytics and reporting
- `EnhancedLocationService`: Location processing and geofencing
- `WebSocketService`: Real-time communication

#### **Controllers (REST API)**
- `MainApiController`: Device management API
- `LocationController`: Location tracking API
- `AgentDataController`: Agent communication API
- `SystemStatusController`: System health and status

---

## 🔒 **Security Features**

### **Authentication**
- Session-based authentication (no JWT complexity)
- Simple login/logout functionality
- User registration and authentication system
- User registration through web interface

### **Data Protection**
- All data stored locally (self-hosted)
- No external data collection or tracking
- Secure device-server communication
- CSRF protection enabled

### **Device Security**
- Remote device locking capabilities
- Device status monitoring
- Secure agent-server communication
- Device authentication tokens

---

## 📊 **API Endpoints**

### **Device Management**
```
GET    /api/devices              # List user devices
POST   /api/devices              # Register new device
PUT    /api/devices/{id}         # Update device
DELETE /api/devices/{id}         # Remove device
```

### **Location Tracking**
```
POST   /api/location/update      # Update device location
GET    /api/location/{deviceId}  # Get device location
GET    /api/location/history     # Location history
```

### **System Status**
```
GET    /api/system/status        # System health check
GET    /api/system/metrics       # Performance metrics
```

### **Agent Downloads**
```
GET    /api/agents/download/windows/lapso-installer.ps1    # Windows agent
GET    /api/agents/download/macos/lapso-installer.sh       # macOS agent
GET    /api/agents/download/linux/lapso-installer.sh       # Linux agent
```

---

## 🚀 **Deployment**

### **Local Development**
```bash
# Clone and run
git clone https://github.com/Jithendra123892/LAPSO.git
cd LAPSO
mvn spring-boot:run
```

### **Production Deployment**
```bash
# Build JAR
mvn clean package

# Run with production profile
java -jar target/laptop-tracker-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
```

### **Docker Deployment**
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/laptop-tracker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 🔍 **Monitoring & Health Checks**

### **Health Check Scripts**
- `check-lapso-health.bat`: System health verification
- `system-health-check.bat`: Comprehensive system check
- `run-working-system.bat`: Start with health monitoring

### **Built-in Monitoring**
- Real-time device status dashboard
- WebSocket connection monitoring
- Database health checks
- Service availability monitoring

---

## 🆓 **Advantages Over Commercial Solutions**

### **vs Microsoft Find My Device**
- ✅ **More features**: Remote actions, history, analytics
- ✅ **Cross-platform**: Windows + macOS + Linux support
- ✅ **Always free**: No subscription costs
- ✅ **Privacy**: Self-hosted, your data stays with you
- ✅ **Open source**: Transparent, modifiable code
- ✅ **Real-time**: Live updates vs manual refresh

### **vs Prey, LoJack, etc.**
- ✅ **No monthly fees**: Completely free forever
- ✅ **Self-hosted**: Full control over your data
- ✅ **Open source**: No vendor lock-in
- ✅ **Simple setup**: Easy installation and configuration
- ✅ **Honest marketing**: No false claims or hype

---

## 🐛 **Troubleshooting**

### **Common Issues**

#### **Application Won't Start**
```bash
# Check Java version
java -version

# Check port availability
netstat -an | findstr :8080

# View logs
tail -f logs/lapso.log
```

#### **Database Connection Issues**
```bash
# H2 Console (development)
http://localhost:8080/h2-console

# PostgreSQL connection test
psql -h localhost -U postgres -d lapso
```

#### **Agent Connection Issues**
- Verify server is running on port 8080
- Check firewall settings
- Ensure agent has network connectivity
- Verify agent authentication token

---

## 📚 **Documentation**

### **User Guides**
- **Getting Started**: This README
- **Agent Setup**: Available in `/agents/` directory
- **API Documentation**: Built-in Swagger UI (development mode)

### **Developer Guides**
- **Code Structure**: See Project Structure section
- **Contributing**: Fork, create feature branch, submit PR
- **Building**: Standard Maven build process

---

## 🤝 **Contributing**

We welcome contributions! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### **Development Guidelines**
- Follow existing code style and conventions
- Add tests for new functionality
- Update documentation as needed
- Keep commits focused and descriptive

---

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### **MIT License Summary**
- ✅ **Commercial use**: Use in commercial projects
- ✅ **Modification**: Modify the code as needed
- ✅ **Distribution**: Distribute copies freely
- ✅ **Private use**: Use privately without restrictions
- ❗ **Liability**: No warranty or liability
- ❗ **Attribution**: Include original license

---

## 🌟 **Project Status**

### **Current Version**: 1.0.0
### **Status**: ✅ **Production Ready**

#### **✅ Completed Features**
- User authentication and registration
- Device management and tracking
- Real-time dashboard with live updates
- Cross-platform agent support (Windows, macOS, Linux)
- Interactive map view with location accuracy
- Agent download system
- Mobile-responsive design
- WebSocket real-time communication
- Database integration (H2 + PostgreSQL)
- Security implementation
- API endpoints
- Health monitoring
- Remote device control (lock, screenshot, camera, wipe)
- Geofencing with predictive analytics
- Offline device detection with risk scoring
- Enhanced hardware detection
- Device clustering on maps
- Comprehensive reporting dashboard
- Device history tracking

#### **🔄 In Progress**
- Enhanced analytics and reporting
- Advanced geofencing features
- Mobile app development
- Performance optimizations

#### **📋 Planned Features**
- Email notifications
- Advanced device actions
- Multi-user support
- Cloud deployment guides
- Enhanced security features

---

## 📞 **Support & Contact**

### **Getting Help**
1. **Check Documentation**: Start with this README
2. **Search Issues**: Look through existing GitHub issues
3. **Create Issue**: Report bugs or request features
4. **Community**: Join discussions in GitHub Discussions

### **Project Information**
- **Repository**: https://github.com/Jithendra123892/LAPSO
- **Issues**: https://github.com/Jithendra123892/LAPSO/issues
- **License**: MIT License
- **Language**: Java (Spring Boot + Vaadin)

---

## 🎉 **Success Indicators**

Your LAPSO installation is successful when you see:

✅ **Application Status**: Running on http://localhost:8080  
✅ **Login**: Admin login works (admin@lapso.in / admin123)  
✅ **Dashboard**: Clean interface loads properly  
✅ **Agent Downloads**: All platform agents available  
✅ **Device Registration**: Can add and manage devices  
✅ **Real-time Updates**: Live dashboard updates working  
✅ **Map View**: Interactive map displays correctly  
✅ **Database**: H2 or PostgreSQL connected successfully  

---

## 🚀 **Get Started Now!**

```bash
# Quick start in 3 commands
git clone https://github.com/Jithendra123892/LAPSO.git
cd LAPSO
mvn spring-boot:run

# Then visit: http://localhost:8080
# Login with: admin@lapso.in / admin123
```

**Your free, open-source laptop tracking system is ready!** 🛡️

---

*Built with ❤️ using Spring Boot, Vaadin, and open-source technologies*

# LAPSO - Laptop Tracking System

LAPSO is a free, open-source laptop tracking system that helps you locate and secure your devices in case of theft or loss.

## 🌟 Key Features

1. **Real-time Device Tracking** - Updates every 30 seconds with 3-5 meter GPS accuracy
2. **Cross-platform Support** - Windows, macOS, and Linux agents
3. **Remote Control** - Lock, sound alarm, screenshot, camera capture, and emergency wipe
4. **Geofencing** - Get alerts when devices leave designated areas
5. **Offline Detection** - Risk scoring for offline devices
6. **Self-hosted** - Run on your own server with H2 (dev) or PostgreSQL (prod)
7. **Advanced Analytics** - Predictive analytics and device clustering
8. **Enhanced Security** - Enterprise-grade security features
9. **Comprehensive Reporting** - Dashboard with analytics and device history

## 🚀 Installation

### Server Setup

1. Install Java 17 or higher
2. Clone this repository
3. Run with: `./mvnw spring-boot:run`
4. Access at: http://localhost:8080

### Device Agent Installation

#### Windows
```powershell
# Download and run the installer
$base = 'http://localhost:8080'
$ProgressPreference='SilentlyContinue'
Invoke-WebRequest -UseBasicParsing -Uri "$base/api/agents/download/windows/lapso-installer.ps1" -OutFile 'lapso-installer.ps1'
PowerShell -ExecutionPolicy Bypass -File .\lapso-installer.ps1
```

#### macOS
```bash
curl -fsSL 'http://localhost:8080/api/agents/download/macos/lapso-installer.sh' -o lapso-installer.sh
chmod +x lapso-installer.sh
sudo ./lapso-installer.sh
```

#### Linux
```bash
curl -fsSL 'http://localhost:8080/api/agents/download/linux/lapso-installer.sh' -o lapso-installer.sh
chmod +x lapso-installer.sh
sudo ./lapso-installer.sh
```

## 🎮 Remote Control Features

All agents support these remote commands with enterprise-grade security:

| Feature | Description | Security Level |
|---------|-------------|----------------|
| 🔒 **Remote Lock** | Instantly lock device screen with PIN/Password required to unlock | High - Multiple lock methods ensure reliability |
| 🚨 **Sound Alarm** | Play loud beeping sound (10 seconds) to help locate device | Low - Non-destructive alert |
| 📸 **Screenshot** | Capture current screen and upload to dashboard | Medium - Visual evidence collection |
| 📷 **Camera Photo** | Capture photo from device camera (falls back to screenshot if no camera) | Medium - Visual evidence collection |
| 🗑️ **Emergency Wipe** | Securely erase all user data with 3 overwrite passes (DANGEROUS) | Critical - Irreversible data destruction |
| 📍 **Locate Device** | Get current GPS/IP-based location with 3-5 meter accuracy | Low - Information gathering |

### **Command Execution Process**

1. **Command Queuing**: Commands are queued in the server and associated with specific device IDs
2. **Agent Polling**: Agents check for pending commands every 30 seconds
3. **Execution**: Agents execute commands using platform-specific implementations
4. **Result Reporting**: Agents report execution results back to the server
5. **User Notification**: Results are displayed in the web dashboard in real-time

### **Security Implementation**

- **Authentication**: All commands require device authentication with unique tokens
- **Encryption**: All communication encrypted via HTTPS/TLS
- **Authorization**: Users can only control devices registered to their account
- **Audit Trail**: All commands are logged for security review
- **Redundancy**: Multiple implementation methods ensure command reliability

## 🔄 Agent Updates

Agents automatically check for updates every 30 minutes. To manually update:

1. Download the latest agent from the web interface
2. Stop your current agent (Ctrl+C in terminal)
3. Run the new agent with your device ID and email
4. Verify you see "Agent is running - sending heartbeats and polling for commands"

## 🗑️ Uninstall Agents

### Windows
Run PowerShell as Administrator:
```powershell
& "C:\Program Files\LAPSO\uninstall.ps1"
```

### macOS
Run in Terminal:
```bash
sudo /usr/local/lapso/uninstall.sh
```

### Linux
Run in Terminal:
```bash
sudo /opt/lapso/uninstall.sh
```

## 🛠️ Technical Details

### System Architecture
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

### Agent Communication Protocol

**Heartbeat Mechanism:**
- Agents send device metadata and location every 30 seconds
- Location accuracy: 3-5 meters with GPS, 10-100 meters with IP geolocation
- Automatic fallback from GPS to IP-based location when GPS is unavailable

**Command/Control Flow:**
1. Server queues commands in memory (production: database)
2. Agents poll `/api/device-commands/poll/{deviceId}` endpoint
3. Agents execute commands and report results to `/api/device-commands/result/{deviceId}`
4. Results are stored and displayed in the web dashboard

### Agent Capabilities

**Windows Agent (PowerShell):**
- Multiple lock methods (rundll32, VBS script, Win32 API, Scheduled Task)
- Webcam capture using Win32 API
- Secure data wiping with 3 overwrite passes
- Self-updating mechanism

**macOS Agent (Bash):**
- Multiple lock methods (pmset, AppleScript, screencapture)
- Camera capture using imagesnap/ffmpeg/fswebcam
- Secure data wiping with 3 overwrite passes
- Keychain and credential clearing

**Linux Agent (Bash):**
- Multiple lock methods (loginctl, GNOME/KDE screensavers, xset)
- Camera capture using fswebcam/ffmpeg/streamer
- Secure data wiping with 3 overwrite passes
- System credential clearing

### Database Schema

**Core Tables:**
- `devices`: Device metadata, location, and status
- `users`: User accounts and authentication
- `device_events`: Audit trail of device activities
- `location_history`: Historical location tracking
- `remote_commands`: Command execution tracking

**Key Features:**
- H2 database for development (zero setup)
- PostgreSQL support for production deployments
- Flyway database migrations for schema evolution
- ACID compliance for data integrity

## 🔐 Security Features

### **Data Protection**
- **End-to-End Encryption**: All data transmission encrypted via HTTPS/TLS
- **Secure Storage**: Database encryption for sensitive information
- **Data Isolation**: User data isolated with proper access controls

### **Device Authentication**
- **Unique Device IDs**: Cryptographically secure device identifiers
- **Token-Based Auth**: Secure authentication tokens for agent communication
- **Session Management**: Proper session handling with timeouts

### **Command Security**
- **Multiple Overwrite Passes**: Secure data wiping with 3 overwrite passes
- **PIN/Password Protection**: Device unlock requires user authentication
- **Command Verification**: Results verified and logged for audit purposes

### **System Security**
- **Automatic Updates**: Agents automatically check for security patches
- **Input Validation**: All API endpoints validate and sanitize inputs
- **Rate Limiting**: Prevent abuse with request throttling
- **Audit Trail**: Comprehensive logging of all system activities

## 📊 Dashboard Features

### **Real-Time Monitoring**
- **Live Location Tracking**: Real-time device location updates every 30 seconds
- **Battery Monitoring**: Current battery level and power status
- **Performance Metrics**: CPU, memory, and disk usage statistics
- **Network Status**: Connection quality and IP address information

### **Command Management**
- **Command History**: Complete audit trail of all executed commands
- **Execution Status**: Real-time command execution feedback
- **Batch Operations**: Execute commands on multiple devices simultaneously

### **Collaboration Features**
- **Device Sharing**: Share device access with other authorized users
- **Team Management**: Organize devices into groups and teams
- **Permission Controls**: Granular access controls for shared devices

### **Advanced Analytics**
- **Geofencing**: Custom location boundaries with entry/exit alerts
- **Offline Detection**: Risk scoring for disconnected devices
- **Device Clustering**: Group devices on maps for better visualization
- **Historical Reports**: Comprehensive reporting dashboard
- **Predictive Analytics**: Anticipate device behavior and potential issues

## 🚨 Emergency Procedures

If your device is stolen:

1. **Immediately lock** the device remotely
2. **Play sound alarm** to help locate it nearby
3. **Take screenshot** to see what the thief is doing
4. **Capture camera photo** to potentially identify the thief
5. **Set geofence** alerts for location monitoring
6. **As last resort, wipe** all data securely

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For issues and feature requests, please create an issue on GitHub.
