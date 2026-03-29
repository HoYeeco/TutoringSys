#!/bin/bash

APP_NAME="tutoringsys-backend"
JAR_FILE="backend-1.0.0.jar"
PID_FILE="app.pid"
LOG_FILE="logs/startup.log"

JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "Application is already running with PID: $PID"
        exit 1
    fi
fi

mkdir -p logs

echo "Starting $APP_NAME..."
echo "========================================" >> "$LOG_FILE"
echo "Starting application at $(date)" >> "$LOG_FILE"

nohup java $JAVA_OPTS -jar "$JAR_FILE" --spring.profiles.active=prod >> "$LOG_FILE" 2>&1 &

echo $! > "$PID_FILE"
echo "Application started with PID: $(cat $PID_FILE)"
echo "Logs: $LOG_FILE"
echo ""
echo "To check status: ./health-check.sh"
echo "To stop: ./stop.sh"
