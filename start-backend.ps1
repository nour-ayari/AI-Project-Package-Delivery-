# Start Backend Server
Write-Host "Starting Delivery Planner Backend..." -ForegroundColor Green

# Check if Maven is installed
if (!(Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Maven is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Navigate to project root
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# Start Spring Boot application
Write-Host "Starting Spring Boot application on port 8080..." -ForegroundColor Cyan
mvn spring-boot:run

# If Maven command fails
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Failed to start backend server" -ForegroundColor Red
    exit 1
}
