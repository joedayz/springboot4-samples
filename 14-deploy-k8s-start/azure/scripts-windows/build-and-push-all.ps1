# Script para construir y subir imágenes a ACR
# Requiere: azure-setup.ps1 ejecutado previamente

$ErrorActionPreference = "Stop"
$rootDir = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$azureDir = Split-Path $PSScriptRoot -Parent
$configFilePs1 = Join-Path $azureDir "azure-config.ps1"
$configFileEnv = Join-Path $azureDir "azure-config.env"

if (Test-Path $configFilePs1) { . $configFilePs1 }
elseif (Test-Path $configFileEnv) {
    Get-Content $configFileEnv | ForEach-Object {
        if ($_ -match '^export\s+(\w+)="?(.+)"?$') { Set-Variable -Name $matches[1] -Value $matches[2].Trim('"') -Scope Script }
    }
}

if (-not $ACR_NAME) {
    Write-Host "Error: ACR_NAME no está configurado. Ejecuta primero: .\azure-setup.ps1" -ForegroundColor Red
    exit 1
}

$acrLoginServer = "$ACR_NAME.azurecr.io"
Write-Host "=== Build and Push to ACR ===" -ForegroundColor Cyan
Write-Host "ACR: $acrLoginServer"

if (-not (Get-Command az -ErrorAction SilentlyContinue)) { Write-Host "Error: Azure CLI no está instalado." -ForegroundColor Red; exit 1 }
if (-not (az account show 2>$null)) { Write-Host "Error: No estás logueado. Ejecuta: az login" -ForegroundColor Red; exit 1 }

$containerCmd = if (Get-Command podman -ErrorAction SilentlyContinue) { "podman" } elseif (Get-Command docker -ErrorAction SilentlyContinue) { "docker" } else { $null }
if (-not $containerCmd) { Write-Host "Error: No se encontró podman ni docker." -ForegroundColor Red; exit 1 }
Write-Host "Detectado: $containerCmd"

Write-Host "=== Login a ACR ===" -ForegroundColor Cyan
if ($containerCmd -eq "podman") {
    $acrToken = az acr login --name $ACR_NAME --expose-token --output tsv --query accessToken 2>$null
    if (-not $acrToken) { Write-Host "Error: No se pudo obtener token de ACR." -ForegroundColor Red; exit 1 }
    $acrToken | & $containerCmd login $acrLoginServer --username "00000000-0000-0000-0000-000000000000" --password-stdin
} else {
    az acr login --name $ACR_NAME
}
Write-Host ""

function Build-AndPush {
    param([string]$Dir, [string]$Name, [string]$Dockerfile)
    Write-Host "=== Building $Name in $Dir ===" -ForegroundColor Cyan
    Push-Location (Join-Path $rootDir $Dir)
    mvn -q package
    $imageTag = "$acrLoginServer/${Name}:latest"
    if ($containerCmd -eq "podman") {
        & $containerCmd build --platform linux/amd64 -f $Dockerfile -t $imageTag .
    } else {
        if (docker buildx version 2>$null) { docker buildx build --platform linux/amd64 -f $Dockerfile -t $imageTag --load . }
        else { docker build --platform linux/amd64 -f $Dockerfile -t $imageTag . }
    }
    Write-Host "=== Pushing $imageTag ===" -ForegroundColor Cyan
    & $containerCmd push $imageTag
    Write-Host "Imagen $imageTag lista." -ForegroundColor Green
    Write-Host ""
    Pop-Location
}

Build-AndPush -Dir "expense-service" -Name "expense-service" -Dockerfile "src/main/docker/Dockerfile.jvm"
Build-AndPush -Dir "expense-client" -Name "expense-client" -Dockerfile "src/main/docker/Dockerfile.jvm"
Write-Host "=== Todas las imágenes subidas a ACR ===" -ForegroundColor Green
