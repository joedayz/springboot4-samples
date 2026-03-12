# Script para desplegar en AKS
$ErrorActionPreference = "Stop"
$azureDir = Split-Path $PSScriptRoot -Parent
$configFilePs1 = Join-Path $azureDir "azure-config.ps1"
$configFileEnv = Join-Path $azureDir "azure-config.env"
$appManifest = Join-Path $azureDir "k8s\expenses-all.yaml"

if (Test-Path $configFilePs1) { . $configFilePs1 }
elseif (Test-Path $configFileEnv) {
    Get-Content $configFileEnv | ForEach-Object {
        if ($_ -match '^export\s+(\w+)="?(.+)"?$') { Set-Variable -Name $matches[1] -Value $matches[2].Trim('"') -Scope Script }
    }
}

if (-not $ACR_NAME) {
    Write-Host "Error: ACR_NAME no configurado. Ejecuta: .\azure-setup.ps1" -ForegroundColor Red
    exit 1
}

if (-not (kubectl cluster-info 2>$null)) {
    Write-Host "Error: No hay conexión con el cluster. Ejecuta: .\azure-setup.ps1" -ForegroundColor Red
    exit 1
}

Write-Host "=== Deploying to AKS ===" -ForegroundColor Cyan
Write-Host "ACR: $ACR_NAME.azurecr.io"
$tempManifest = [System.IO.Path]::GetTempFileName()
(Get-Content $appManifest) -replace '\$\{ACR_NAME\}', $ACR_NAME | Set-Content $tempManifest
kubectl apply -f $tempManifest
Remove-Item $tempManifest

Write-Host "Esperando deployments..." -ForegroundColor Cyan
kubectl rollout status deployment/expense-service -w --timeout=5m
kubectl rollout status deployment/expense-client -w --timeout=5m

Write-Host "=== Despliegue completado ===" -ForegroundColor Green
Start-Sleep -Seconds 10
$serviceIp = kubectl get svc expense-client -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>$null
$serviceHostname = kubectl get svc expense-client -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>$null
$servicePort = kubectl get svc expense-client -o jsonpath='{.spec.ports[0].port}'

kubectl get pods
kubectl get svc expense-service expense-client
Write-Host ""
if ($serviceIp) { Write-Host "✓ Client: http://${serviceIp}:${servicePort}" -ForegroundColor Green }
elseif ($serviceHostname) { Write-Host "✓ Client: http://${serviceHostname}:${servicePort}" -ForegroundColor Green }
else {
    Write-Host "Obtén la IP: kubectl get svc expense-client" -ForegroundColor Yellow
    Write-Host "O port-forward: kubectl port-forward svc/expense-client 8081:8080" -ForegroundColor Yellow
}
