#!/usr/bin/env bash

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$PROJECT_DIR/.run"
PID_FILE="$RUN_DIR/bootrun.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "Приложение не запущено."
    rm -rf "$RUN_DIR"
    exit 0
fi

PID=$(cat "$PID_FILE")

if kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
    sleep 2
fi

if kill -0 "$PID" 2>/dev/null; then
    kill -9 "$PID"
    sleep 1
fi

rm -rf "$RUN_DIR"
echo "Приложение остановлено."
