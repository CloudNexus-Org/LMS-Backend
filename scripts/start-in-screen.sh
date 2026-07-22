#!/usr/bin/env bash
# Internal: runs inside a detached screen session. Do not call directly.
set -uo pipefail

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="/tmp/lms-logs"
mkdir -p "$LOG_DIR"

export POSTGRES_PORT=15432
export EUREKA_ENABLED=false
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092

PORTS=(8761 8080 8081 8082 8083 8084 8085 8086 8087 8090 8091 8092 8093 8094 8095)

start_module() {
  local mod="$1"
  echo "[$(date +%H:%M:%S)] Starting $mod..."
  cd "$BASE_DIR" || exit 1
  if [ "$mod" = "catalog-service" ] || [ "$mod" = "analytics-service" ]; then
    find "$BASE_DIR/$mod/target/classes" -name '* 2.class' -delete 2>/dev/null || true
  fi
  if [ -f "$BASE_DIR/$mod/target/$mod-1.0.0-SNAPSHOT.jar" ] && [ $(wc -c < "$BASE_DIR/$mod/target/$mod-1.0.0-SNAPSHOT.jar") -gt 1000000 ]; then
    java -Xms128m -Xmx256m -jar "$BASE_DIR/$mod/target/$mod-1.0.0-SNAPSHOT.jar" > "$LOG_DIR/${mod}.log" 2>&1 &
  else
    mvn -pl "$mod" spring-boot:run -q > "$LOG_DIR/${mod}.log" 2>&1 &
  fi
  echo $! > "$LOG_DIR/${mod}.pid"
}

wait_port() {
  local port="$1" timeout="${2:-90}"
  for _ in $(seq 1 "$timeout"); do
    nc -z 127.0.0.1 "$port" 2>/dev/null && return 0
    sleep 1
  done
  return 1
}

start_module eureka-server
wait_port 8761 60

start_module api-gateway
wait_port 8080 60

for mod in auth-service user-service catalog-service mentor-service; do start_module "$mod"; done
sleep 15
for mod in content-service media-service enrollment-service learning-service; do start_module "$mod"; done
sleep 15
for mod in certificate-service review-service notification-service analytics-service admin-service; do
  start_module "$mod"
done

echo "[$(date +%H:%M:%S)] All services launched. Keeping screen session alive..."
# Keep screen session (and child JVMs) running
wait
