@echo off
setlocal

set APP_NAME=tutoringsys-backend
set JAR_FILE=backend-1.0.0.jar
set LOG_FILE=logs\startup.log

set JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC

if exist app.pid (
    set /p PID=<app.pid
    tasklist /FI "PID eq %PID%" 2>nul | find "%PID%" >nul
    if %errorlevel% equ 0 (
        echo Application is already running with PID: %PID%
        exit /b 1
    )
)

if not exist logs mkdir logs

echo Starting %APP_NAME%...
echo ======================================== >> %LOG_FILE%
echo Starting application at %date% %time% >> %LOG_FILE%

start /b java %JAVA_OPTS% -jar %JAR_FILE% --spring.profiles.active=prod >> %LOG_FILE% 2>&1

echo Application started
echo Logs: %LOG_FILE%
echo.
echo To check status: health-check.bat
echo To stop: stop.bat
