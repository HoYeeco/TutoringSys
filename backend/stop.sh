#!/bin/bash

APP_NAME="tutoringsys-backend"
PID_FILE="app.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "Application is not running (no PID file found)"
    exit 0
fi

PID=$(cat "$PID_FILE")

if ! ps -p $PID > /dev/null 2>&1; then
    echo "Application is not running (PID: $PID)"
    rm -f "$PID_FILE"
    exit 0
fi

echo "Stopping $APP_NAME (PID: $PID)..."

kill $PID

for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "Application stopped successfully"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

echo "Application did not stop gracefully, forcing..."
kill -9 $PID
rm -f "$PID_FILE"
echo "Application force stopped"
