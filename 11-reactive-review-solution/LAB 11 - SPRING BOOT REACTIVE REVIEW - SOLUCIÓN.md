# LAB 11: SPRING BOOT REACTIVE REVIEW — Solución de Referencia

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 11-reactive-review-solution

Este documento contiene el **código completo** de la solución.

---

## 1. SpeakerController.java

**Ruta:** `reactive-speaker/src/main/java/com/bcp/training/controller/SpeakerController.java`

```java
package com.bcp.training.controller;

import com.bcp.training.event.SpeakerWasCreated;
import com.bcp.training.model.Speaker;
import com.bcp.training.repository.SpeakerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/speakers")
public class SpeakerController {

    private static final String TOPIC = "speaker-was-created";

    private final SpeakerRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SpeakerController(SpeakerRepository repository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> create(@RequestBody Speaker newSpeaker) {
        return repository.save(newSpeaker)
                .doOnSuccess(saved -> kafkaTemplate.send(TOPIC,
                        new SpeakerWasCreated(saved.getId(), newSpeaker.getFullName(),
                                newSpeaker.getAffiliation(), newSpeaker.getEmail())))
                .map(saved -> ResponseEntity.created(URI.create("/speakers/" + saved.getId())).build());
    }

    @GetMapping("/{id}")
    public Mono<Speaker> get(@PathVariable Long id) {
        return repository.findById(id);
    }

    @GetMapping
    public Flux<Speaker> listAll() {
        return repository.findAll();
    }
}
```

---

## 2. NewSpeakersProcessor.java

**Ruta:** `reactive-speaker/src/main/java/com/bcp/training/reactive/NewSpeakersProcessor.java`

```java
package com.bcp.training.reactive;

import com.bcp.training.event.EmployeeSignedUp;
import com.bcp.training.event.SpeakerWasCreated;
import com.bcp.training.event.UpstreamMemberSignedUp;
import com.bcp.training.model.Affiliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewSpeakersProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewSpeakersProcessor.class);
    private static final String EMPLOYEES_TOPIC = "employees-signed-up";
    private static final String UPSTREAM_TOPIC = "upstream-members-signed-up";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NewSpeakersProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "speaker-was-created", groupId = "reactive-speaker-processor", containerFactory = "kafkaListenerContainerFactory")
    public void sendEventNotifications(SpeakerWasCreated event) {
        if (event.affiliation == Affiliation.RED_HAT) {
            kafkaTemplate.send(EMPLOYEES_TOPIC, new EmployeeSignedUp(event.id, event.fullName, event.email));
        } else if (event.affiliation == Affiliation.GNOME_FOUNDATION) {
            kafkaTemplate.send(UPSTREAM_TOPIC, new UpstreamMemberSignedUp(event.id, event.fullName, event.email));
        }
    }
}
```

---

## 3. Resumen de flujo

| Acción | Topic / Endpoint |
|--------|------------------|
| POST /speakers | Crea speaker, publica a `speaker-was-created` |
| RED_HAT | NewSpeakersProcessor → `employees-signed-up` |
| GNOME_FOUNDATION | NewSpeakersProcessor → `upstream-members-signed-up` |

---

## 4. Ejecutar

**Infraestructura (Docker o Podman):**
```bash
docker compose up -d
# o: podman compose up -d
```

**Aplicación:**
```bash
cd reactive-speaker
mvn spring-boot:run
```

Ver [CONTAINERS.md](../CONTAINERS.md) para Windows y más comandos.

## 5. Probar

```bash
curl -X POST http://localhost:8080/speakers -H "Content-Type: application/json" \
  -d '{"fullName":"Jane Doe","affiliation":"RED_HAT","email":"jane@redhat.com"}'
curl http://localhost:8080/speakers
```
