# Script para configurar Azure: login, crear ACR y AKS si no existen
# Uso: .\azure-setup.ps1 [RESOURCE_GROUP] [LOCATION] [ACR_NAME] [AKS_NAME]

param(
    [string]$ResourceGroup = "expense-rg",
    [string]$Location = "eastus",
    [string]$AcrName = "expenseacr$(Get-Random -Maximum 99999)",
    [string]$AksName = "expense-aks"
)

Write-Host "=== Azure Setup ===" -ForegroundColor Cyan
Write-Host "Resource Group: $ResourceGroup"
Write-Host "Location: $Location"
Write-Host "ACR Name: $AcrName"
Write-Host "AKS Name: $AksName"
Write-Host ""

if (-not (Get-Command az -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Azure CLI no está instalado." -ForegroundColor Red
    exit 1
}

Write-Host "=== Verificando login en Azure ===" -ForegroundColor Cyan
if (-not (az account show 2>$null)) {
    Write-Host "No estás logueado. Iniciando login..."
    az login
}

$accountName = az account show --query name -o tsv
Write-Host "Logueado como: $accountName"
Write-Host ""

Write-Host "=== Creando Resource Group ===" -ForegroundColor Cyan
if (az group show --name $ResourceGroup 2>$null) {
    Write-Host "Resource Group '$ResourceGroup' ya existe."
} else {
    az group create --name $ResourceGroup --location $Location
}
Write-Host ""

Write-Host "=== Creando Azure Container Registry (ACR) ===" -ForegroundColor Cyan
if (az acr show --name $AcrName --resource-group $ResourceGroup 2>$null) {
    Write-Host "ACR '$AcrName' ya existe."
} else {
    az acr create --resource-group $ResourceGroup --name $AcrName --sku Basic --admin-enabled true
}

$acrLoginServer = az acr show --name $AcrName --resource-group $ResourceGroup --query loginServer -o tsv
Write-Host "ACR Login Server: $acrLoginServer"
Write-Host ""

Write-Host "=== Creando Azure Kubernetes Service (AKS) ===" -ForegroundColor Cyan
if (az aks show --name $AksName --resource-group $ResourceGroup 2>$null) {
    Write-Host "AKS '$AksName' ya existe."
} else {
    Write-Host "Creando AKS '$AksName'... (puede tardar varios minutos)"
    az aks create `
        --resource-group $ResourceGroup `
        --name $AksName `
        --node-count 2 `
        --enable-addons monitoring `
        --generate-ssh-keys `
        --attach-acr $AcrName
}
Write-Host ""

Write-Host "=== Conectando AKS con ACR ===" -ForegroundColor Cyan
az aks update --name $AksName --resource-group $ResourceGroup --attach-acr $AcrName 2>$null
Write-Host ""

Write-Host "=== Obteniendo credenciales de AKS ===" -ForegroundColor Cyan
az aks get-credentials --resource-group $ResourceGroup --name $AksName --overwrite-existing
Write-Host ""

Write-Host "=== Verificando conexión con AKS ===" -ForegroundColor Cyan
kubectl cluster-info
kubectl get nodes
Write-Host ""

$azureDir = Split-Path $PSScriptRoot -Parent
$configFilePs1 = Join-Path $azureDir "azure-config.ps1"
$configFileEnv = Join-Path $azureDir "azure-config.env"

@"
`$RESOURCE_GROUP = "$ResourceGroup"
`$LOCATION = "$Location"
`$ACR_NAME = "$AcrName"
`$AKS_NAME = "$AksName"
`$ACR_LOGIN_SERVER = "$acrLoginServer"
"@ | Out-File -FilePath $configFilePs1 -Encoding utf8

# También escribir .env para bash
@"
export RESOURCE_GROUP="$ResourceGroup"
export LOCATION="$Location"
export ACR_NAME="$AcrName"
export AKS_NAME="$AksName"
export ACR_LOGIN_SERVER="$acrLoginServer"
"@ | Out-File -FilePath $configFileEnv -Encoding utf8

Write-Host "=== Configuración completada ===" -ForegroundColor Green
Write-Host "Configuración guardada en: $configFilePs1 y $configFileEnv"
Write-Host "Para PowerShell: . $configFilePs1"
Write-Host "Para bash: source $configFileEnv"
