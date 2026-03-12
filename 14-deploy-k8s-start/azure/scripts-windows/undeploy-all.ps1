# Script para eliminar recursos de AKS (y opcionalmente ACR/AKS de Azure)
# Uso: .\undeploy-all.ps1 [-DeleteAll]

param([switch]$DeleteAll)

$azureDir = Split-Path $PSScriptRoot -Parent
$appManifest = Join-Path $azureDir "k8s\expenses-all.yaml"
$configFilePs1 = Join-Path $azureDir "azure-config.ps1"
$configFileEnv = Join-Path $azureDir "azure-config.env"

if (Test-Path $configFilePs1) { . $configFilePs1 }
elseif (Test-Path $configFileEnv) {
    Get-Content $configFileEnv | ForEach-Object {
        if ($_ -match '^export\s+(\w+)="?(.+)"?$') { Set-Variable -Name $matches[1] -Value $matches[2].Trim('"') -Scope Script }
    }
}

if (-not $ACR_NAME) {
    Write-Host "Error: ACR_NAME no configurado." -ForegroundColor Red
    exit 1
}

Write-Host "=== Undeploying from AKS ===" -ForegroundColor Cyan
$tempManifest = [System.IO.Path]::GetTempFileName()
(Get-Content $appManifest) -replace '\$\{ACR_NAME\}', $ACR_NAME | Set-Content $tempManifest
kubectl delete -f $tempManifest --ignore-not-found=true
Remove-Item $tempManifest
Write-Host "Recursos de Kubernetes eliminados." -ForegroundColor Green

if ($DeleteAll) {
    Write-Host "=== Eliminando recursos de Azure ===" -ForegroundColor Cyan
    $resourceGroup = if ($RESOURCE_GROUP) { $RESOURCE_GROUP } else { "expense-rg" }
    $aksName = if ($AKS_NAME) { $AKS_NAME } else { "expense-aks" }
    if (az aks show --name $aksName --resource-group $resourceGroup 2>$null) {
        az aks delete --name $aksName --resource-group $resourceGroup --yes --no-wait
        Write-Host "AKS eliminado." -ForegroundColor Green
    }
    if (az acr show --name $ACR_NAME --resource-group $resourceGroup 2>$null) {
        az acr delete --name $ACR_NAME --resource-group $resourceGroup --yes
        Write-Host "ACR eliminado." -ForegroundColor Green
    }
    Write-Host "Para eliminar el Resource Group: az group delete --name $resourceGroup --yes --no-wait" -ForegroundColor Yellow
} else {
    Write-Host "Para eliminar también ACR/AKS: .\undeploy-all.ps1 -DeleteAll" -ForegroundColor Yellow
}
