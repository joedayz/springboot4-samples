# LAB 11: SPRING BOOT REACTIVE REVIEW — Solución de Referencia

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 11-reactive-review-solution

Este documento contiene el **código completo** de la solución.  
Partiendo del proyecto `11-reactive-review-start`, se deben crear/modificar los archivos que se indican a continuación.

---

## 1. docker-compose.yml

> Ya incluido en `11-reactive-review-start`. No requiere cambios.

---

## 2. pom.xml (dependencia adicional)

**Ruta:** `reactive-speaker/pom.xml`

Agregar `jackson-databind` a las dependencias existentes:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

---

## 3. application.yml (configuración adicional)

**Ruta:** `reactive-speaker/src/main/resources/application.yml`

Agregar la sección `consumer` bajo `kafka`:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/testing
    username: developer
    password: developer
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: reactive-speaker-processor
      auto-offset-reset: earliest
```

---

## 4. Evento — SpeakerWasCreated.java (CREAR)

**Ruta:** `reactive-speaker/src/main/java/com/bcp/training/event/SpeakerWasCreated.java`

```java
package com.bcp.training.event;

import com.bcp.training.model.Affiliation;

public class SpeakerWasCreated {
    public Long id;
    public String fullName;
    public Affiliation affiliation;
    public String email;

    public SpeakerWasCreated() {}

    public SpeakerWasCreated(Long id, String fullName, Affiliation affiliation, String email) {
        this.id = id;
        this.fullName = fullName;
        this.affiliation = affiliation;
        this.email = email;
    }
}
```

---

## 5. Evento — EmployeeSignedUp.java (CREAR)

**Ruta:** `reactive-speaker/src/main/java/com/bcp/training/event/EmployeeSignedUp.java`

```java
package com.bcp.training.event;

public class EmployeeSignedUp {
    public Long speakerId;
    public String fullName;
    public String email;

    public EmployeeSignedUp() {}

    public EmployeeSignedUp(Long speakerId, String fullName, String email) {
        this.speakerId = speakerId;
        this.fullName = fullName;
        this.email = email;
    }
}
```

---

## 6. Evento — UpstreamMemberSignedUp.java (CREAR)

**Ruta:** `reactive-speaker/src/main/java/com/bcp/training/event/UpstreamMemberSignedUp.java`

```java
package com.bcp.training.event;

public class UpstreamMemberSignedUp {
    public Long speakerId;
    public String fullName;
    public String email;

    public UpstreamMemberSignedUp() {}

    public UpstreamMemberSignedUp(Long speakerId, String fullName, String email) {
        this.speakerId = speakerId;
        this.fullName = fullName;
        this.email = email;
    }
}
```

---

## 7. KafkaProducerConfig.java (CREAR)

**Ruta:** `reactive-speaker/src/main/java/com/bcp/training/config/KafkaProducerConfig.java`

```java
package com.bcp.training.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

---

## 8. KafkaConsumerConfig.java (CREAR)

**Ruta:** `reactive-speaker/src/main/java/com/bcp/training/config/KafkaConsumerConfig.java`

```java
package com.bcp.training.config;

import com.bcp.training.event.SpeakerWasCreated;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, SpeakerWasCreated> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "reactive-speaker-processor");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.bcp.training.event,com.bcp.training.model");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SpeakerWasCreated.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SpeakerWasCreated> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SpeakerWasCreated> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

---

## 9. SpeakerController.java (MODIFICAR)

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

## 10. NewSpeakersProcessor.java (CREAR)

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
        logProcessEvent(event.id);

        if (event.affiliation == Affiliation.RED_HAT) {
            logEmitEvent("EmployeeSignedUp", event.affiliation);
            kafkaTemplate.send(EMPLOYEES_TOPIC, new EmployeeSignedUp(event.id, event.fullName, event.email));
        } else if (event.affiliation == Affiliation.GNOME_FOUNDATION) {
            logEmitEvent("UpstreamMemberSignedUp", event.affiliation);
            kafkaTemplate.send(UPSTREAM_TOPIC, new UpstreamMemberSignedUp(event.id, event.fullName, event.email));
        }
    }

    private void logEmitEvent(String eventName, Affiliation affiliation) {
        LOGGER.info("Sending event {} for affiliation {}", eventName, affiliation);
    }

    private void logProcessEvent(Long eventId) {
        LOGGER.info("Processing SpeakerWasCreated event: ID {}", eventId);
    }
}
```

---

## 11. Resumen de flujo

| Acción | Topic / Endpoint |
|--------|------------------|
| POST /speakers | Crea speaker, publica a `speaker-was-created` |
| RED_HAT | NewSpeakersProcessor → `employees-signed-up` |
| GNOME_FOUNDATION | NewSpeakersProcessor → `upstream-members-signed-up` |

---

## 12. Ejecutar

**Infraestructura (Docker o Podman):**
```bash
docker compose up -d
# o: podman compose up -d
```

```powershell
docker compose up -d
# o: podman compose up -d
```

**Aplicación:**
```bash
cd reactive-speaker
mvn spring-boot:run
```

```powershell
cd reactive-speaker
mvn spring-boot:run
```

## 13. Probar

```bash
curl -X POST http://localhost:8080/speakers -H "Content-Type: application/json" \
  -d '{"fullName":"Jane Doe","affiliation":"RED_HAT","email":"jane@redhat.com"}'
curl http://localhost:8080/speakers
```

```powershell
curl -X POST http://localhost:8080/speakers -H "Content-Type: application/json" \
  -d '{"fullName":"Jane Doe","affiliation":"RED_HAT","email":"jane@redhat.com"}'
curl http://localhost:8080/speakers
```
