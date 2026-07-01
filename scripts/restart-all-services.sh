#!/usr/bin/env bash
# Restart all 15 LMS services in a detached screen session (macOS-safe).
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SESSION="lms-services"
LOG_DIR="/tmp/lms-logs"
mkdir -p "$LOG_DIR"

PORTS=(8761 8080 8081 8082 8083 8084 8085 8086 8087 8090 8091 8092 8093 8094 8095)

echo "Stopping existing LMS screen session (if any)..."
screen -S "$SESSION" -X quit 2>/dev/null || true
sleep 2

echo "Stopping processes on LMS ports..."
for p in "${PORTS[@]}"; do
  pid=$(lsof -ti tcp:"$p" 2>/dev/null || true)
  if [ -n "$pid" ]; then kill -9 $pid 2>/dev/null || true; fi
done
sleep 2

echo "Starting all services in screen session '$SESSION'..."
screen -dmS "$SESSION" bash "$BASE_DIR/scripts/start-in-screen.sh"

echo "Waiting for services to boot (up to 2 min)..."
for i in $(seq 1 120); do
  UP=0
  for p in "${PORTS[@]}"; do
    nc -z 127.0.0.1 "$p" 2>/dev/null && UP=$((UP + 1))
  done
  if [ "$UP" -eq "${#PORTS[@]}" ]; then
    echo ""
    echo "✅ All $UP / ${#PORTS[@]} services UP"
    echo "Gateway: http://localhost:8080"
    echo "Logs:    $LOG_DIR"
    echo "Screen:  screen -r $SESSION  (attach) · screen -S $SESSION -X quit  (stop)"
    exit 0
  fi
  printf "\r  %s/%s ports up..." "$UP" "${#PORTS[@]}"
  sleep 1
done

echo ""
echo "⚠️  Timeout — some services may still be starting."
echo "Check logs in $LOG_DIR and run: screen -r $SESSION"
exit 1
