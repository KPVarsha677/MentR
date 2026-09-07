# ============================================================
# MentR Backend — start.ps1
#
# Usage (from the mentorhub/backend/ directory):
#   .\start.ps1
#
# What it does:
#   1. Checks if port 8081 is occupied.
#   2. If so, kills the process owning it.
#   3. Starts the Spring Boot application with Maven.
# ============================================================

$port = 8081

$conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if ($conn) {
    $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
    Write-Host "Port $port is occupied by PID $($conn.OwningProcess) ($($proc.ProcessName)). Killing..." -ForegroundColor Yellow
    Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
    Write-Host "Port $port is now free." -ForegroundColor Green
} else {
    Write-Host "Port $port is free." -ForegroundColor Green
}

Write-Host ""
Write-Host "Starting MentR backend on port $port..." -ForegroundColor Cyan
mvn spring-boot:run
