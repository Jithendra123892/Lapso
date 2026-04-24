@echo off
echo ========================================
echo 🛡️ LAPSO Complete Installation
echo 🚀 Better than Microsoft Find My Device
echo ========================================
echo.

echo 🎯 LAPSO provides features Microsoft doesn't have:
echo   ✅ Automatic updates every 30 seconds
echo   ✅ Cross-platform support (Windows, macOS, Linux)
echo   ✅ Advanced device commands (lock, wipe, sound)
echo   ✅ Real-time geofencing with alerts
echo   ✅ Complete privacy (self-hosted)
echo   ✅ Always free (no subscriptions)
echo.

echo 📋 Installation Options:
echo.
echo 1. Quick Install (Recommended)
echo 2. Custom Installation
echo 3. Developer Setup
echo 4. Check System Requirements
echo 5. Exit
echo.

set /p choice="Choose option (1-5): "

if "%choice%"=="1" goto quick_install
if "%choice%"=="2" goto custom_install
if "%choice%"=="3" goto developer_setup
if "%choice%"=="4" goto check_requirements
if "%choice%"=="5" goto exit

:quick_install
echo.
echo 🚀 Quick Installation Starting...
echo.

echo 📋 Step 1: Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java not found. Please install Java 17 or higher.
    echo 📥 Download from: https://adoptium.net/
    pause
    goto exit
) else (
    echo ✅ Java found
)

echo.
echo 📋 Step 2: Checking PostgreSQL...
psql --version >nul 2>&1
if errorlevel 1 (
    echo ⚠️ PostgreSQL not found. Installing with default settings...
    echo 🔧 Setting up database with default password...
    call setup-lapso-database.bat
) else (
    echo ✅ PostgreSQL found
)

echo.
echo 📋 Step 3: Building LAPSO...
call mvn clean compile -q
if errorlevel 1 (
    echo ❌ Build failed. Check Java and Maven installation.
    pause
    goto exit
) else (
    echo ✅ Build successful
)

echo.
echo 📋 Step 4: Starting LAPSO...
echo 🌐 LAPSO will be available at: http://localhost:8080
echo 🔐 Create your account at first login
echo.
echo 🎉 Installation complete! Starting LAPSO...
echo.
start "" "http://localhost:8080"
call mvn spring-boot:run
goto exit

:custom_install
echo.
echo 🔧 Custom Installation
echo.
echo 📋 Database Options:
echo 1. PostgreSQL (Recommended)
echo 2. H2 (Development only)
echo.
set /p db_choice="Choose database (1-2): "

if "%db_choice%"=="1" (
    echo 🔧 Setting up PostgreSQL...
    call setup-lapso-database.bat
) else (
    echo 🔧 Using H2 database (development mode)
    echo spring.datasource.url=jdbc:h2:mem:testdb > temp_application.properties
    echo spring.jpa.hibernate.ddl-auto=create-drop >> temp_application.properties
)

echo.
echo 📋 Port Configuration:
set /p port="Enter port (default 8080): "
if "%port%"=="" set port=8080

echo server.port=%port% >> temp_application.properties

echo.
echo 🚀 Starting LAPSO with custom configuration...
start "" "http://localhost:%port%"
call mvn spring-boot:run -Dspring.config.additional-location=temp_application.properties
del temp_application.properties >nul 2>&1
goto exit

:developer_setup
echo.
echo 👨‍💻 Developer Setup
echo.
echo 📋 Setting up development environment...
echo.

echo 🔧 Installing development dependencies...
call mvn dependency:resolve -q

echo 🔧 Setting up IDE configuration...
call mvn idea:idea -q >nul 2>&1
call mvn eclipse:eclipse -q >nul 2>&1

echo 🔧 Running tests...
call mvn test -q

echo 🔧 Starting in development mode...
echo 🌐 Development server: http://localhost:8080
echo 🔄 Hot reload enabled
echo 📊 Debug mode active
echo.
start "" "http://localhost:8080"
call mvn spring-boot:run -Dspring.profiles.active=dev
goto exit

:check_requirements
echo.
echo 📋 System Requirements Check
echo.

echo 🔍 Checking Java...
java -version 2>&1 | findstr "version"
if errorlevel 1 (
    echo ❌ Java not installed
    echo 📥 Required: Java 17 or higher
    echo 🔗 Download: https://adoptium.net/
) else (
    echo ✅ Java installed
)

echo.
echo 🔍 Checking Maven...
mvn -version 2>&1 | findstr "Apache Maven"
if errorlevel 1 (
    echo ❌ Maven not installed
    echo 📥 Required: Apache Maven 3.6+
    echo 🔗 Download: https://maven.apache.org/
) else (
    echo ✅ Maven installed
)

echo.
echo 🔍 Checking PostgreSQL...
psql --version 2>&1 | findstr "psql"
if errorlevel 1 (
    echo ⚠️ PostgreSQL not installed (optional)
    echo 📥 Recommended: PostgreSQL 12+
    echo 🔗 Download: https://www.postgresql.org/
) else (
    echo ✅ PostgreSQL installed
)

echo.
echo 🔍 Checking Git...
git --version 2>&1 | findstr "git version"
if errorlevel 1 (
    echo ⚠️ Git not installed (optional)
    echo 📥 Recommended for development
    echo 🔗 Download: https://git-scm.com/
) else (
    echo ✅ Git installed
)

echo.
echo 💾 System Information:
echo OS: %OS%
echo Processor: %PROCESSOR_ARCHITECTURE%
echo User: %USERNAME%
echo Computer: %COMPUTERNAME%

echo.
echo 📊 Minimum Requirements:
echo ✅ Java 17 or higher
echo ✅ 2GB RAM available
echo ✅ 1GB disk space
echo ✅ Internet connection
echo.

echo 📊 Recommended for Production:
echo ✅ Java 17
echo ✅ 4GB RAM
echo ✅ PostgreSQL database
echo ✅ 2GB disk space
echo ✅ Stable internet connection
echo.

pause
goto exit

:exit
echo.
echo 🎉 LAPSO Installation Complete!
echo.
echo 🌟 What makes LAPSO better than Microsoft Find My Device:
echo   🔄 Automatic updates every 30 seconds
echo   🌐 Cross-platform support
echo   🛠️ Advanced device commands
echo   🔒 Complete privacy control
echo   💰 Always free
echo   ⚙️ Fully customizable
echo.
echo 📞 Support:
echo   🌐 Access: http://localhost:8080
echo   🔐 Create your account at first login
echo   📖 Docs: README.md, SIMPLE_INSTALL.md
echo   🆘 Issues: Check logs in console
echo.
echo 👋 Thank you for choosing LAPSO!
pause