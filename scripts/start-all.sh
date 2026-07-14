#!/bin/bash
# Start all LMS microservices (run after eureka-server and api-gateway)

SERVICES=(
  "auth-service:8081"
  "user-service:8082"
  "catalog-service:8083"
  "mentor-service:8084"
  "content-service:8085"
  "enrollment-service:8086"
  "learning-service:8087"
  "assessment-service:8088"
  "payment-service:8089"
  "certificate-service:8090"
  "review-service:8091"
  "notification-service:8092"
  "analytics-service:8093"
  "admin-service:8094"
  "media-service:8095"
)

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "Starting LMS Microservices..."
for entry in "${SERVICES[@]}"; do
  IFS=':' read -r service port <<< "$entry"
  echo "  → Starting $service on port $port"
  cd "$BASE_DIR/$service" && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$port" &
  sleep 3
done

echo ""
echo "All services starting. Check Eureka dashboard: http://localhost:8761"
echo "API Gateway: http://localhost:8080"
