#!/usr/bin/env bash

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$PROJECT_DIR/.run"
PID_FILE="$RUN_DIR/bootrun.pid"
LOG_FILE="$RUN_DIR/bootrun.log"

mkdir -p "$RUN_DIR"

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        echo "Приложение уже запущено. PID: $PID"
        exit 0
    fi
    rm -f "$PID_FILE"
fi

cd "$PROJECT_DIR" || exit 1
GRADLE_USER_HOME="$PROJECT_DIR/.gradle-ant" nohup ./gradlew --no-daemon bootRun > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

echo "Приложение запущено."
echo "PID: $(cat "$PID_FILE")"
echo "Лог: $LOG_FILE"
