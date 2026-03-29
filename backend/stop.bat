@echo off
setlocal

set APP_NAME=tutoringsys-backend

if not exist app.pid (
    echo Application is not running (no PID file found)
    exit /b 0
)

set /p PID=<app.pid

tasklist /FI "PID eq %PID%" 2>nul | find "%PID%" >nul
if %errorlevel% neq 0 (
    echo Application is not running (PID: %PID%)
    del app.pid
    exit /b 0
)

echo Stopping %APP_NAME% (PID: %PID%)...

taskkill /PID %PID% /F >nul 2>&1

del app.pid
echo Application stopped
