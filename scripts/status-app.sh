#!/usr/bin/env bash

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$PROJECT_DIR/.run"
PID_FILE="$RUN_DIR/bootrun.pid"
LOG_FILE="$RUN_DIR/bootrun.log"

if [ ! -f "$PID_FILE" ]; then
    echo "Приложение не запущено."
    exit 1
fi

PID=$(cat "$PID_FILE")

if kill -0 "$PID" 2>/dev/null; then
    echo "Приложение работает. PID: $PID"
    echo
    tail "$LOG_FILE"
else
    echo "Файл PID есть, но процесса уже нет."
fi
