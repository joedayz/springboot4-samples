# PowerShell script para desplegar todos los componentes en Kind
$ErrorActionPreference = "Stop"

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$ROOT_DIR = Split-Path -Parent $SCRIPT_DIR
$APP_MANIFEST = Join-Path (Join-Path $ROOT_DIR "k8s") "expenses-all.yaml"

kubectl apply -f $APP_MANIFEST
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al aplicar el manifiesto" -ForegroundColor Red
    exit 1
}

kubectl rollout status deployment/expense-service -w
kubectl rollout status deployment/expense-client -w

Write-Host "Client available on http://localhost:30081 (NodePort)" -ForegroundColor Green
