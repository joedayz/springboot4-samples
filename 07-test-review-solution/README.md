# 07-test-review-solution

Solución del laboratorio de revisión de pruebas con Spring Boot 4.

## Requisitos previos

- **Java 21**
- **Maven 3.9+**
- **Contenedores** (solo para el módulo `session`): Docker o Podman

## Ejecutar las pruebas

### Módulos sin contenedores (speaker, schedule)

```bash
cd speaker
mvn test
```

```bash
cd schedule
mvn test
```

### Módulo session (requiere Docker o Podman)

El módulo `session` usa Testcontainers para levantar PostgreSQL. Necesita un runtime de contenedores en ejecución.

---

## Configuración de contenedores por sistema operativo

### Docker

#### macOS
```bash
# Instalar Docker Desktop desde https://www.docker.com/products/docker-desktop/
# O con Homebrew:
brew install --cask docker

# Verificar que Docker está corriendo
docker info
```

#### Windows (PowerShell)
```powershell
# Instalar Docker Desktop desde https://www.docker.com/products/docker-desktop/
# O con winget:
winget install Docker.DockerDesktop

# Verificar que Docker está corriendo
docker info
```

#### Windows (CMD)
```cmd
REM Instalar Docker Desktop desde https://www.docker.com/products/docker-desktop/
REM Verificar que Docker está corriendo
docker info
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt-get update && sudo apt-get install -y docker.io
sudo systemctl start docker
sudo usermod -aG docker $USER
# Cerrar sesión y volver a entrar
```

---

### Podman

#### macOS
```bash
# Instalar Podman con Homebrew
brew install podman

# Iniciar la máquina virtual de Podman (primera vez)
podman machine init
podman machine start

# Verificar
podman info
```

**Testcontainers con Podman en Mac:**
```bash
# Obtener la ruta del socket (ejecutar tras podman machine start)
podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}'

# O usar la ruta típica (puede variar según versión)
export DOCKER_HOST=unix://$HOME/.local/share/containers/podman/machine/qemu/podman.sock
```

#### Windows (PowerShell)
```powershell
# Instalar Podman con winget
winget install RedHat.Podman

# O descargar desde https://podman-desktop.io/

# Iniciar Podman
podman machine init
podman machine start

# Verificar
podman info
```

#### Windows (CMD)
```cmd
REM Instalar Podman con winget
winget install RedHat.Podman

REM Iniciar Podman
podman machine init
podman machine start

REM Verificar
podman info
```

#### Linux (Fedora/RHEL)
```bash
sudo dnf install podman
podman info
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update && sudo apt-get install -y podman
podman info
```

---

## Ejecutar pruebas del módulo session

### Con Docker (macOS, Linux, Windows)

```bash
cd session
mvn test
```

### Con Podman (Linux)

```bash
# Podman en Linux (rootless) expone el socket en /run/user/<uid>/podman/
export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock
# Si usas Podman como root: unix:///run/podman/podman.sock
cd session
mvn test
```

### Con Podman (macOS)

```bash
# Asegurarse de que podman machine está corriendo
podman machine start

# Testcontainers detecta Podman si DOCKER_HOST está configurado
export DOCKER_HOST=unix://$HOME/.local/share/containers/podman/machine/qemu/podman.sock
cd session
mvn test
```

### Con Podman (Windows PowerShell)

```powershell
# Asegurarse de que podman machine está corriendo
podman machine start

# En Windows, Podman usa un named pipe (podman o docker_engine según la versión)
$env:DOCKER_HOST = "npipe:////./pipe/docker_engine"
# Si falla, probar: $env:DOCKER_HOST = "npipe:////./pipe/podman"
cd session
mvn test
```

### Con Podman (Windows CMD)

```cmd
REM Asegurarse de que podman machine está corriendo
podman machine start

REM En Windows, Podman usa un named pipe
set DOCKER_HOST=npipe:////./pipe/docker_engine
REM Si falla, probar: set DOCKER_HOST=npipe:////./pipe/podman
cd session
mvn test
```

---

## Resumen rápido por plataforma

| Plataforma | Runtime | Comando para session |
|------------|---------|----------------------|
| macOS | Docker | `cd session && mvn test` |
| macOS | Podman | `export DOCKER_HOST=unix://$HOME/.local/share/containers/podman/machine/qemu/podman.sock` luego `cd session && mvn test` |
| Windows | Docker | `cd session; mvn test` (PowerShell) |
| Windows | Podman | `$env:DOCKER_HOST="npipe:////./pipe/docker_engine"` luego `cd session; mvn test` (PowerShell) |
| Linux | Docker | `cd session && mvn test` |
| Linux | Podman | `export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock` luego `cd session && mvn test` |

---

## Ejecutar todas las pruebas

### Bash / macOS / Linux
```bash
cd speaker && mvn test && cd ../schedule && mvn test && cd ../session && mvn test
```

### Windows PowerShell
```powershell
Push-Location speaker; mvn test; Pop-Location
Push-Location schedule; mvn test; Pop-Location
Push-Location session; mvn test; Pop-Location
```

### Windows CMD
```cmd
cd speaker
mvn test
cd ..\schedule
mvn test
cd ..\session
mvn test
```

---

## Solución de problemas

**Error: "Could not find a valid Docker environment"**
- Docker: asegúrate de que Docker Desktop (o el daemon) está en ejecución.
- Podman: ejecuta `podman machine start` y configura `DOCKER_HOST` según tu SO.

**Error: "Connection refused" al ejecutar session tests**
- Verifica que el runtime de contenedores está activo: `docker ps` o `podman ps`.
