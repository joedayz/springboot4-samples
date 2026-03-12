# PowerShell script para verificar Docker Desktop Kubernetes
$ErrorActionPreference = "Stop"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "docker not found. Install Docker Desktop first." -ForegroundColor Red
    exit 1
}

if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "kubectl not found. Install kubectl first." -ForegroundColor Red
    exit 1
}

try {
    docker info | Out-Null
} catch {
    Write-Host "Docker Desktop no está ejecutándose. Por favor, inicia Docker Desktop." -ForegroundColor Red
    exit 1
}

try {
    kubectl cluster-info | Out-Null
} catch {
    Write-Host "Kubernetes no está disponible en Docker Desktop." -ForegroundColor Red
    Write-Host "Por favor, habilita Kubernetes en Docker Desktop:" -ForegroundColor Yellow
    Write-Host "  Settings > Kubernetes > Enable Kubernetes" -ForegroundColor Cyan
    exit 1
}

Write-Host "Docker Desktop Kubernetes está disponible." -ForegroundColor Green
kubectl cluster-info
Write-Host ""
Write-Host "Contexto actual: $(kubectl config current-context)" -ForegroundColor Cyan
