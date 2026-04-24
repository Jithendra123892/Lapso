@echo off
echo ========================================
echo 🔍 LAPSO Health Check
echo ========================================
echo.

echo 📊 Checking LAPSO system health...
echo.

echo 1. Java Version Check:
java -version
echo.

echo 2. PostgreSQL Connection Test:
echo Testing database connection...
psql -U postgres -h localhost -d postgres -c "SELECT 'Database connection successful!' as status;" 2>nul
if %errorlevel% neq 0 (
    echo ❌ Database connection failed
    echo 💡 Run: setup-lapso-database.bat to fix
) else (
    echo ✅ Database connection successful
)
echo.

echo 3. Port Availability Check:
netstat -an | findstr :8080 >nul
if %errorlevel% equ 0 (
    echo ⚠️ Port 8080 is already in use
    echo 💡 Either stop the existing service or change port in application.properties
) else (
    echo ✅ Port 8080 is available
)
echo.

echo 4. Maven Check:
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven not found
    echo 💡 Install Maven or use: mvnw.cmd instead of mvn
) else (
    echo ✅ Maven is available
)
echo.

echo 🎯 LAPSO Health Check Complete
echo.
echo 📋 Next Steps:
echo   • If all checks pass: Run quick-start-lapso.bat
echo   • If database fails: Run setup-lapso-database.bat
echo   • If port busy: Change server.port in application.properties
echo.

pause