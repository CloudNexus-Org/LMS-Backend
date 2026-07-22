# Docker = postgres + kafka + redis + zookeeper ONLY
# Backend = mvn spring-boot:run (one new window per service)
# Frontend = npm run dev
#
# Usage:  .\scripts\start-mvn-dev.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

Write-Host "=== 1) Docker infra only ==="
docker compose up -d postgres zookeeper kafka redis
if ($LASTEXITCODE -ne 0) { throw "Docker compose failed. Is Docker Desktop running?" }

Write-Host ""
Write-Host "App DB is Docker Postgres:"
Write-Host "  Host=localhost  Port=15432  User=lms_admin  Password=lms_secret"
Write-Host "Do NOT use local PostgreSQL 18 (port 4321) in pgAdmin for this app."
Write-Host ""

function Start-MvnService {
  param([string]$Dir, [string]$Title)

  $path = Join-Path $Root $Dir
  if (-not (Test-Path $path)) {
    Write-Host "SKIP missing $Dir"
    return
  }

  $script = @(
    "`$env:POSTGRES_HOST='localhost'"
    "`$env:POSTGRES_PORT='15432'"
    "`$env:POSTGRES_USER='lms_admin'"
    "`$env:POSTGRES_PASSWORD='lms_secret'"
    "`$env:KAFKA_BOOTSTRAP_SERVERS='localhost:9092'"
    "`$env:REDIS_HOST='localhost'"
    "`$env:REDIS_PORT='6379'"
    "`$env:EUREKA_ENABLED='false'"
    "`$env:LMS_DIRECTORY_CLEANUP_ENABLED='false'"
    "`$env:AUTH_SERVICE_URL='http://localhost:8081'"
    "`$env:AUTH_SERVICE_URI='http://localhost:8081'"
    "`$env:USER_SERVICE_URI='http://localhost:8082'"
    "`$env:CATALOG_SERVICE_URI='http://localhost:8083'"
    "`$env:MENTOR_SERVICE_URI='http://localhost:8084'"
    "`$env:CONTENT_SERVICE_URI='http://localhost:8085'"
    "`$env:ENROLLMENT_SERVICE_URI='http://localhost:8086'"
    "`$env:LEARNING_SERVICE_URI='http://localhost:8087'"
    "`$env:PAYMENT_SERVICE_URI='http://localhost:8089'"
    "`$env:CERTIFICATE_SERVICE_URI='http://localhost:8090'"
    "`$env:REVIEW_SERVICE_URI='http://localhost:8091'"
    "`$env:NOTIFICATION_SERVICE_URI='http://localhost:8092'"
    "`$env:ANALYTICS_SERVICE_URI='http://localhost:8093'"
    "`$env:ADMIN_SERVICE_URI='http://localhost:8094'"
    "`$env:MEDIA_SERVICE_URI='http://localhost:8095'"
    "`$env:FRONTEND_URL='http://localhost:5173'"
    "Set-Location '$path'"
    "Write-Host '=== $Title ==='"
    "mvn spring-boot:run"
  ) -join "; "

  Start-Process powershell -ArgumentList "-NoExit", "-Command", $script
  Start-Sleep -Seconds 1
}

Write-Host "=== 2) Backend mvn spring-boot:run ==="
Start-MvnService "eureka-server" "eureka-server :8761"
Start-Sleep -Seconds 5
Start-MvnService "api-gateway" "api-gateway :8080"
Start-MvnService "auth-service" "auth-service :8081"
Start-MvnService "user-service" "user-service :8082"
Start-MvnService "catalog-service" "catalog-service :8083"
Start-MvnService "mentor-service" "mentor-service :8084"
Start-MvnService "content-service" "content-service :8085"
Start-MvnService "enrollment-service" "enrollment-service :8086"
Start-MvnService "learning-service" "learning-service :8087"
Start-MvnService "razorpay" "payment-service :8089"
Start-MvnService "certificate-service" "certificate-service :8090"
Start-MvnService "review-service" "review-service :8091"
Start-MvnService "notification-service" "notification-service :8092"
Start-MvnService "analytics-service" "analytics-service :8093"
Start-MvnService "admin-service" "admin-service :8094"
Start-MvnService "media-service" "media-service :8095"

$Frontend = Join-Path (Split-Path $Root -Parent) "LMS"
if (Test-Path (Join-Path $Frontend "package.json")) {
  Write-Host "=== 3) Frontend npm run dev ==="
  $fe = "Set-Location '$Frontend'; npm run dev"
  Start-Process powershell -ArgumentList "-NoExit", "-Command", $fe
} else {
  Write-Host "Frontend not found at $Frontend"
}

Write-Host ""
Write-Host "Wait 1-2 minutes for Spring Boot to finish starting."
Write-Host "Frontend http://localhost:5173"
Write-Host "Gateway  http://localhost:8080"
Write-Host "Login    admin@cloudnexus.com / Password123!"
