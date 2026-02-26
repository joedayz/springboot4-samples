# Docker y Podman — Comandos por plataforma

Guía de comandos para **Docker** y **Podman** en Windows, macOS y Linux.

---

## Instalación

### Docker

| Plataforma | Comando |
|------------|---------|
| **macOS** | `brew install --cask docker` o [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| **Windows (PowerShell)** | `winget install Docker.DockerDesktop` |
| **Windows (CMD)** | Descargar desde [docker.com](https://www.docker.com/products/docker-desktop/) |
| **Linux (Ubuntu/Debian)** | `sudo apt-get update && sudo apt-get install -y docker.io` |
| **Linux (Fedora)** | `sudo dnf install docker` |

### Podman

| Plataforma | Comando |
|------------|---------|
| **macOS** | `brew install podman` → `podman machine init` → `podman machine start` |
| **Windows** | `winget install RedHat.Podman` o [Podman Desktop](https://podman-desktop.io/) |
| **Linux (Fedora/RHEL)** | `sudo dnf install podman` |
| **Linux (Ubuntu/Debian)** | `sudo apt-get install podman` |

### Verificar

```bash
# Docker
docker info

# Podman
podman info
```

---

## Levantar un contenedor (run)

**Ejemplo: servicio prices (08-reactive-architecture)**

### Docker

```bash
docker run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
```

### Podman

```bash
podman run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
```

### Windows PowerShell (Docker o Podman)

```powershell
docker run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
# o
podman run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
```

### Windows CMD (Docker o Podman)

```cmd
docker run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
REM o
podman run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
```

---

## Docker Compose / Podman Compose

**Ejemplo: 10-reactive-eda, 11-reactive-review (PostgreSQL + Kafka)**

### macOS / Linux

```bash
# Docker
docker compose up -d

# Podman
podman compose up -d
```

### Windows PowerShell

```powershell
# Docker
docker compose up -d

# Podman
podman compose up -d
```

### Windows CMD

```cmd
REM Docker
docker compose up -d

REM Podman
podman compose up -d
```

---

## Testcontainers con Podman

Testcontainers necesita `DOCKER_HOST` cuando usas Podman.

### macOS

```bash
podman machine start
export DOCKER_HOST=unix://$HOME/.local/share/containers/podman/machine/qemu/podman.sock
```

### Linux

```bash
export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock
# Si usas Podman como root: unix:///run/podman/podman.sock
```

### Windows PowerShell

```powershell
podman machine start
$env:DOCKER_HOST = "npipe:////./pipe/docker_engine"
# Si falla: $env:DOCKER_HOST = "npipe:////./pipe/podman"
```

### Windows CMD

```cmd
podman machine start
set DOCKER_HOST=npipe:////./pipe/docker_engine
REM Si falla: set DOCKER_HOST=npipe:////./pipe/podman
```

---

## Comandos útiles

| Acción | Docker | Podman |
|--------|--------|--------|
| Listar contenedores | `docker ps` | `podman ps` |
| Detener contenedor | `docker stop prices` | `podman stop prices` |
| Eliminar contenedor | `docker rm prices` | `podman rm prices` |
| Ver logs | `docker logs prices` | `podman logs prices` |
| Bajar compose | `docker compose down` | `podman compose down` |

---

## Resumen rápido

| Plataforma | Runtime | Nota |
|------------|---------|------|
| macOS | Docker | Sin configuración extra |
| macOS | Podman | `export DOCKER_HOST=unix://$HOME/.local/share/containers/podman/machine/qemu/podman.sock` para Testcontainers |
| Windows | Docker | Sin configuración extra |
| Windows | Podman | `$env:DOCKER_HOST="npipe:////./pipe/docker_engine"` para Testcontainers |
| Linux | Docker | `sudo systemctl start docker` |
| Linux | Podman | `export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock` para Testcontainers |
