# LAB 10: SPRING BOOT REACTIVE REVIEW

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

## Objetivo

En este laboratorio aprenderás a:
- Implementar un servicio REST reactivo que publica eventos a Kafka
- Consumir eventos y reenviarlos según reglas de negocio (afiliación)
- Integrar Spring WebFlux + R2DBC + Spring Kafka

## Prerequisitos

- **Java 21**
- **Docker o Podman** (PostgreSQL + Kafka)

## 1. Estructura del proyecto

- **reactive-speaker**: REST API para speakers. Tiene GET /{id} y GET (listAll). Falta POST y Kafka.

## 2. Levantar infraestructura

```bash
cd ../10-reactive-review-solution
docker compose up -d
```

## 3. Tarea 1: Implementar POST /speakers

### 3.1. Crear el evento SpeakerWasCreated

```java
package com.bcp.training.event;

import com.bcp.training.model.Affiliation;

public class SpeakerWasCreated {
    public Long id;
    public String fullName;
    public Affiliation affiliation;
    public String email;
    // constructor + getters/setters
}
```

### 3.2. Configurar KafkaTemplate

Crea `KafkaProducerConfig` con `KafkaTemplate<String, Object>` para publicar eventos.

### 3.3. Agregar método create en SpeakerController

- Recibir `Speaker` en el body
- Guardar con `repository.save()`
- Publicar `SpeakerWasCreated` al topic `speaker-was-created`
- Retornar `ResponseEntity.created(URI.create("/speakers/" + id))`

## 4. Tarea 2: Implementar NewSpeakersProcessor

### 4.1. Crear eventos de salida

- `EmployeeSignedUp` (speakerId, fullName, email)
- `UpstreamMemberSignedUp` (speakerId, fullName, email)

### 4.2. Implementar NewSpeakersProcessor

- Consumir de `speaker-was-created` con `@KafkaListener`
- Si `affiliation == RED_HAT` → publicar `EmployeeSignedUp` a `employees-signed-up`
- Si `affiliation == GNOME_FOUNDATION` → publicar `UpstreamMemberSignedUp` a `upstream-members-signed-up`

## 5. Ejecutar

```bash
cd reactive-speaker
mvn spring-boot:run
```

## 6. Probar

```bash
curl -X POST http://localhost:8080/speakers -H "Content-Type: application/json" \
  -d '{"fullName":"Jane Doe","affiliation":"RED_HAT","email":"jane@redhat.com"}'
curl http://localhost:8080/speakers
```

## Solución completa

Ver `10-reactive-review-solution/LAB 10 - SPRING BOOT REACTIVE REVIEW - SOLUCIÓN.md`.
