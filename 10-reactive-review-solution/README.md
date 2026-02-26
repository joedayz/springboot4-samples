# 10-reactive-review-solution

Solución del laboratorio reactivo con Kafka y Spring Boot 4.

## Implementación

- **SpeakerController**: POST, GET /{id}, GET (listAll). POST emite SpeakerWasCreated a Kafka
- **NewSpeakersProcessor**: Consume speaker-was-created, RED_HAT → employees-signed-up, GNOME_FOUNDATION → upstream-members-signed-up

## Requisitos

- Java 21
- Docker o Podman

## Ejecutar

```bash
docker compose up -d
cd reactive-speaker && mvn spring-boot:run
```

## Probar

```bash
curl -X POST http://localhost:8080/speakers -H "Content-Type: application/json" \
  -d '{"fullName":"Jane Doe","affiliation":"RED_HAT","email":"jane@redhat.com"}'
curl http://localhost:8080/speakers
```
