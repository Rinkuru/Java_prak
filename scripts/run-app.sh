#!/usr/bin/env bash

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$PROJECT_DIR/.run"
PID_FILE="$RUN_DIR/bootrun.pid"
LOG_FILE="$RUN_DIR/bootrun.log"
GRADLE_USER_HOME_DIR="$PROJECT_DIR/.gradle-ant"

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
APP_JAR="$(find "$PROJECT_DIR/build/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | sort | tail -n 1)"

if [ -z "$APP_JAR" ]; then
    echo "Готовый jar не найден. Собираю приложение через Gradle bootJar..."
    GRADLE_USER_HOME="$GRADLE_USER_HOME_DIR" ./gradlew --no-daemon bootJar || exit 1
    APP_JAR="$(find "$PROJECT_DIR/build/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | sort | tail -n 1)"
fi

if [ -z "$APP_JAR" ]; then
    echo "Не удалось найти runnable jar в build/libs."
    exit 1
fi

nohup setsid java -jar "$APP_JAR" < /dev/null > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

echo "Приложение запущено."
echo "PID: $(cat "$PID_FILE")"
echo "Артефакт: $APP_JAR"
echo "Лог: $LOG_FILE"
