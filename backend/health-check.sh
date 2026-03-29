#!/bin/bash

APP_NAME="tutoringsys-backend"
PID_FILE="app.pid"
HEALTH_URL="http://localhost:8080/actuator/health"

check_process() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            return 0
        fi
    fi
    return 1
}

check_health() {
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" 2>/dev/null)
    if [ "$RESPONSE" = "200" ]; then
        return 0
    fi
    return 1
}

echo "========================================"
echo "Health Check for $APP_NAME"
echo "========================================"
echo ""

if check_process; then
    PID=$(cat "$PID_FILE")
    echo "[OK] Process is running (PID: $PID)"
else
    echo "[FAIL] Process is not running"
    exit 1
fi

if check_health; then
    echo "[OK] Health endpoint is responding"
    
    HEALTH_STATUS=$(curl -s "$HEALTH_URL" 2>/dev/null | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
    echo "[INFO] Health status: $HEALTH_STATUS"
else
    echo "[FAIL] Health endpoint is not responding"
    exit 1
fi

echo ""
echo "Application is healthy!"
exit 0
