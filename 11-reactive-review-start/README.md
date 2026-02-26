# 11-reactive-review-start

Laboratorio reactivo con Kafka y Spring Boot 4.

## Estado inicial

- **SpeakerController**: Solo GET /{id} y GET (listAll)
- Falta: POST para crear speakers y emitir SpeakerWasCreated a Kafka
- Falta: NewSpeakersProcessor para consumir y reenviar según Affiliation

## Tareas

1. Implementar POST /speakers (crear + publicar a Kafka)
2. Implementar NewSpeakersProcessor (RED_HAT → employees-signed-up, GNOME_FOUNDATION → upstream-members-signed-up)

## Requisitos

- Java 21
- Docker o Podman (PostgreSQL + Kafka)

## Ejecutar

**Infraestructura (macOS / Linux):**
```bash
cd ../11-reactive-review-solution
docker compose up -d
# o
podman compose up -d
```

**Infraestructura (Windows):**
```powershell
cd ..\11-reactive-review-solution
docker compose up -d
# o
podman compose up -d
```

**Aplicación:**
```bash
cd reactive-speaker
mvn spring-boot:run
```

Ver [CONTAINERS.md](../CONTAINERS.md) para más comandos.
