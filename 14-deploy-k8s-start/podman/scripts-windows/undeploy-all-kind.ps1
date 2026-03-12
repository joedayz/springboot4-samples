# PowerShell script para eliminar todos los componentes de Kind
$ErrorActionPreference = "Stop"

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$ROOT_DIR = Split-Path -Parent $SCRIPT_DIR
$APP_MANIFEST = Join-Path (Join-Path $ROOT_DIR "k8s") "expenses-all.yaml"

kubectl delete -f $APP_MANIFEST --ignore-not-found
kubectl delete configmap expense-client-config --ignore-not-found

Write-Host "Remaining resources with label app=expense-* (should be none):" -ForegroundColor Yellow
kubectl get all -l app=expense-client 2>$null
kubectl get all -l app=expense-service 2>$null
