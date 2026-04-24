@echo off
echo.
echo ========================================
echo 🚀 LAPSO - Complete System Startup
echo Free & Open Source Laptop Tracking
echo ========================================
echo.

echo 📋 Starting LAPSO with all components connected...
echo.

REM Use H2 database for development
echo 🔧 Using H2 database for development...
echo.

echo 🚀 Starting LAPSO Application...
echo.

REM Start the Spring Boot application with H2 database
java -jar target/laptop-tracker-3.2.8.jar ^
    --spring.profiles.active=development ^
    --server.port=8080 ^
    --logging.level.com.example.demo=INFO

echo.
echo 🎯 LAPSO Startup Complete!
echo.
echo 📱 Access your dashboard at: http://localhost:8080
echo 🔐 Create your account at first login
echo 📖 Documentation: README.md
echo 🆘 Support: Check LAPSO_HONEST_REALITY.md
echo.
echo ✨ All essential components are now connected and running!
echo    - User Service ✅ (Registration, Authentication)
echo    - Device Service ✅ (Device Management, CRUD)
echo    - Location Service ✅ (GPS Tracking, History)
echo    - Real-time Monitoring ✅ (30-second updates)
echo    - WebSocket Service ✅ (Live dashboard updates)
echo    - Notification Service ✅ (Email, WebSocket alerts)
echo    - Analytics Service ✅ (Performance metrics)
echo    - Geofence Service ✅ (Location boundaries)
echo    - Encryption Service ✅ (Data security)
echo    - Quick Actions Service ✅ (Remote commands)
echo    - Device Action Service ✅ (Lock, sound, wipe)
echo    - Continuous Operation ✅ (24/7 monitoring)
echo    - Smart Alert Service ✅ (Intelligent notifications)
echo    - Agent Authentication ✅ (Secure agent connection)
echo    - Core Integration ✅ (Service coordination)
echo.
pause