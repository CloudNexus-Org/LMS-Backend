# Restart one Spring Boot service from LMS-Backend root
param(
  [Parameter(Mandatory = $true)][string]$Service
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Path = Join-Path $Root $Service
if (-not (Test-Path $Path)) { throw "Missing service folder: $Path" }

Get-CimInstance Win32_Process -Filter "Name='java.exe'" | ForEach-Object {
  if ($_.CommandLine -and $_.CommandLine -like "*$Service*") {
    Write-Host "Stopping $Service pid=$($_.ProcessId)"
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
  }
}

Start-Sleep -Seconds 2
Set-Location $Path
Write-Host "Starting $Service ..."
mvn spring-boot:run -q
