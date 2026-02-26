# 11-reactive-review-solution

Solución del laboratorio reactivo con Kafka y Spring Boot 4.

## Implementación

- **SpeakerController**: POST, GET /{id}, GET (listAll). POST emite SpeakerWasCreated a Kafka
- **NewSpeakersProcessor**: Consume speaker-was-created, RED_HAT → employees-signed-up, GNOME_FOUNDATION → upstream-members-signed-up

## Requisitos

- Java 21
- Docker o Podman

## Ejecutar

### 1. Levantar PostgreSQL y Kafka

**macOS / Linux:**
```bash
docker compose up -d
# o
podman compose up -d
```

**Windows PowerShell / CMD:**
```powershell
docker compose up -d
# o
podman compose up -d
```

### 2. Ejecutar reactive-speaker

```bash
cd reactive-speaker
mvn spring-boot:run
```

## Probar

**macOS / Linux:**
```bash
curl -X POST http://localhost:8080/speakers -H "Content-Type: application/json" \
  -d '{"fullName":"Jane Doe","affiliation":"RED_HAT","email":"jane@redhat.com"}'
curl http://localhost:8080/speakers
```

**Windows PowerShell:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/speakers -Method POST -ContentType "application/json" -Body '{"fullName":"Jane Doe","affiliation":"RED_HAT","email":"jane@redhat.com"}'
Invoke-RestMethod -Uri http://localhost:8080/speakers
```

Ver [CONTAINERS.md](../CONTAINERS.md) para más comandos.
