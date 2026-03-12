## Demo Kubernetes: expense-service + expense-client (Azure AKS)

Este directorio contiene una demo para desplegar los microservicios en **Azure Kubernetes Service (AKS)**:
- expense-service: servicio REST de gastos (Spring Boot)
- expense-client: cliente que consume expense-service

Los scripts construyen las imágenes Docker, las suben a **Azure Container Registry (ACR)** y las despliegan en AKS. La comunicación interna usa DNS con la variable EXPENSE_SVC.

### Prerrequisitos

- **Azure CLI** instalado y configurado (`az --version`)
- **Docker o Podman** para construir imágenes (los scripts detectan automáticamente)
- **kubectl** instalado
- **Maven** instalado
- Cuenta de Azure con suscripción activa

### Costos

⚠️ AKS y ACR son servicios de pago. Elimina los recursos cuando termines.

### Pasos para desplegar en AKS

#### 1. Configurar Azure (crear ACR y AKS)

**Linux/macOS:**
```bash
cd azure
./scripts/azure-setup.sh
```

**Windows (PowerShell):**
```powershell
cd azure
.\scripts-windows\azure-setup.ps1
```

Opcional: `./scripts/azure-setup.sh [RESOURCE_GROUP] [LOCATION] [ACR_NAME] [AKS_NAME]`

La configuración se guarda en `azure-config.env` (bash) o `azure-config.ps1` (PowerShell).

#### 2. Construir y subir imágenes a ACR

**Linux/macOS:**
```bash
./scripts/build-and-push-all.sh
```

**Windows:**
```powershell
.\scripts-windows\build-and-push-all.ps1
```

Las imágenes se construyen para **linux/amd64** (compatible con AKS).

#### 3. Desplegar en AKS

**Linux/macOS:**
```bash
./scripts/deploy-all.sh
```

**Windows:**
```powershell
.\scripts-windows\deploy-all.ps1
```

#### 4. Verificar

```bash
kubectl get pods
kubectl get svc expense-client
# Probar: curl http://<EXTERNAL-IP>:8080/expenses
# O: kubectl port-forward svc/expense-client 8081:8080
```

### Health checks (probes)

Los deployments usan **Liveness y Readiness** de Spring Boot Actuator:
- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

Las apps ya incluyen `spring-boot-starter-actuator`; los endpoints se exponen por defecto en Spring Boot 2.3+.

### Limpieza

Solo recursos de Kubernetes:
```bash
./scripts/undeploy-all.sh          # Linux/macOS
.\scripts-windows\undeploy-all.ps1 # Windows
```

Eliminar también ACR y AKS:
```bash
./scripts/undeploy-all.sh --delete-all   # Linux/macOS
.\scripts-windows\undeploy-all.ps1 -DeleteAll  # Windows
```

El Resource Group se mantiene; para borrarlo: `az group delete --name <RESOURCE_GROUP> --yes --no-wait`

### Diferencias con Docker Desktop / Podman

- Imágenes en ACR (remoto), no locales
- LoadBalancer obtiene IP pública en Azure
- AKS y ACR tienen costos asociados
- Los scripts detectan Podman o Docker automáticamente
