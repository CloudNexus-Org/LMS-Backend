# Start LMS core stack: Docker infra + host JARs
# Prerequisites: Docker Desktop running, JARs built (`mvn package -DskipTests`)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

Write-Host "Starting Postgres/Kafka/Redis..."
docker compose up -d postgres zookeeper kafka redis

$env:POSTGRES_HOST = "localhost"
$env:POSTGRES_PORT = "15432"
$env:POSTGRES_USER = "lms_admin"
$env:POSTGRES_PASSWORD = "lms_secret"
$env:KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
$env:AUTH_SERVICE_URL = "http://localhost:8081"
$env:USER_SERVICE_URI = "http://localhost:8082"
$env:CATALOG_SERVICE_URI = "http://localhost:8083"
$env:ADMIN_SERVICE_URI = "http://localhost:8094"
$env:LMS_DIRECTORY_CLEANUP_ENABLED = "false"

New-Item -ItemType Directory -Force -Path "logs" | Out-Null

$services = @(
  "eureka-server\target\eureka-server-1.0.0-SNAPSHOT.jar",
  "auth-service\target\auth-service-1.0.0-SNAPSHOT.jar",
  "user-service\target\user-service-1.0.0-SNAPSHOT.jar",
  "catalog-service\target\catalog-service-1.0.0-SNAPSHOT.jar",
  "mentor-service\target\mentor-service-1.0.0-SNAPSHOT.jar",
  "content-service\target\content-service-1.0.0-SNAPSHOT.jar",
  "admin-service\target\admin-service-1.0.0-SNAPSHOT.jar",
  "api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar"
)

foreach ($jar in $services) {
  if (-not (Test-Path $jar)) {
    throw "Missing $jar — run: mvn -pl ... package -DskipTests"
  }
  $name = Split-Path (Split-Path $jar -Parent) -Parent | Split-Path -Leaf
  if (-not $name) { $name = (Split-Path $jar -Leaf) }
  $svc = ($jar -split '\\')[0]
  Write-Host "Starting $svc..."
  Start-Process -FilePath "java" -ArgumentList "-jar", $jar `
    -WorkingDirectory $Root `
    -RedirectStandardOutput "logs\$svc.log" `
    -RedirectStandardError "logs\$svc.log.err" `
    -WindowStyle Hidden
  Start-Sleep -Seconds 3
}

Write-Host ""
Write-Host "Gateway: http://localhost:8080"
Write-Host "pgAdmin: Host=localhost Port=15432 User=lms_admin Password=lms_secret"
Write-Host "  lms_auth.auth_credentials | lms_users.users | lms_content.courses_content"
Write-Host "Admin login: admin@cloudnexus.com / Password123!"
