# 09-reactive-develop-start

Laboratorio de desarrollo reactivo con Spring Boot 4.

## Objetivo

Implementar endpoints REST reactivos para el recurso `Suggestion` usando Spring WebFlux y R2DBC.

## Estado inicial

- **SuggestionController**: Solo tiene `DELETE /suggestion` (deleteAll)
- **Tests**: Esperan `POST` (create) y `GET /{id}` (get) que aún no están implementados

## Tareas

1. Implementar `POST /suggestion` - crear sugerencia
2. Implementar `GET /suggestion/{id}` - obtener por ID
3. Implementar `GET /suggestion` - listar todas

## Guía completa

Ver **[LAB 9 - SPRING BOOT REACTIVE DEVELOP.md](LAB%209%20-%20SPRING%20BOOT%20REACTIVE%20DEVELOP.md)** para la guía paso a paso (estructura similar a la guía Quarkus LAB-11-GUIA.md).

## Requisitos

- Java 21
- Maven 3.9+
- Docker o Podman (para PostgreSQL en desarrollo y tests)

## Ejecutar

Requiere PostgreSQL en `localhost:5432` con base de datos `suggestions`.

**Levantar PostgreSQL:**
```bash
cd suggestions
docker compose up -d
# o con Podman:
podman compose up -d
```

**Ejecutar la aplicación:**
```bash
mvn spring-boot:run
```

## Tests

Requiere Docker o Podman para Testcontainers (PostgreSQL).

**Con Docker:**
```bash
cd suggestions
mvn test
```

**Con Podman (macOS):**
```bash
podman machine start
export DOCKER_HOST=unix://$HOME/.local/share/containers/podman/machine/qemu/podman.sock
cd suggestions
mvn test
```

**Con Podman (Windows PowerShell):**
```powershell
podman machine start
$env:DOCKER_HOST = "npipe:////./pipe/docker_engine"
cd suggestions
mvn test
```

**Con Podman (Linux):**
```bash
export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock
cd suggestions
mvn test
```

Ver [CONTAINERS.md](../CONTAINERS.md) para más detalles.
