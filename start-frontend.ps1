# Start Frontend (Delivery Planner UI)
Write-Host "Starting Delivery Planner UI..." -ForegroundColor Green

# Check if Node.js is installed
if (!(Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Node.js is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Navigate to delivery-planner-ui directory
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$uiPath = Join-Path $projectRoot "delivery-planner-ui"

if (!(Test-Path $uiPath)) {
    Write-Host "Error: delivery-planner-ui directory not found" -ForegroundColor Red
    exit 1
}

Set-Location $uiPath

# Check if node_modules exists
if (!(Test-Path "node_modules")) {
    Write-Host "Installing dependencies..." -ForegroundColor Cyan
    npm install
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Failed to install dependencies" -ForegroundColor Red
        exit 1
    }
}

# Start Angular development server
Write-Host "Starting Angular development server on port 4200..." -ForegroundColor Cyan
Write-Host "Backend API proxy configured for http://localhost:8080" -ForegroundColor Yellow
Write-Host "Open http://localhost:4200 in your browser" -ForegroundColor Yellow
npm start

# If npm command fails
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Failed to start frontend server" -ForegroundColor Red
    exit 1
}
