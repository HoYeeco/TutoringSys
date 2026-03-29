@echo off
setlocal

set APP_NAME=tutoringsys-backend
set HEALTH_URL=http://localhost:8080/actuator/health

echo ========================================
echo Health Check for %APP_NAME%
echo ========================================
echo.

if exist app.pid (
    set /p PID=<app.pid
    tasklist /FI "PID eq %PID%" 2>nul | find "%PID%" >nul
    if %errorlevel% equ 0 (
        echo [OK] Process is running (PID: %PID%)
    ) else (
        echo [FAIL] Process is not running
        exit /b 1
    )
) else (
    echo [FAIL] Process is not running (no PID file)
    exit /b 1
)

curl -s -o nul -w "%%{http_code}" %HEALTH_URL% > temp_status.txt 2>nul
set /p STATUS=<temp_status.txt
del temp_status.txt

if "%STATUS%"=="200" (
    echo [OK] Health endpoint is responding
    echo.
    echo Application is healthy!
    exit /b 0
) else (
    echo [FAIL] Health endpoint is not responding (HTTP: %STATUS%)
    exit /b 1
)
