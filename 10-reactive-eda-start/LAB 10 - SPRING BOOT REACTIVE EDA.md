# LAB 10: SPRING BOOT REACTIVE EDA

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

---

## Objetivo

En este laboratorio aprenderás a:
- Implementar arquitectura orientada a eventos (EDA) con Spring Boot y Kafka
- Publicar eventos desde un servicio REST (`joedayz-bank`)
- Consumir eventos y procesarlos (`fraud-detector`)
- Usar Spring Kafka para producers y consumers

---

## Prerequisitos

- **Java 21**
- **Docker o Podman** (PostgreSQL + Kafka)

---

## 1. Estructura del proyecto

```
10-reactive-eda-start/
├── docker-compose.yml
├── joedayz-bank/          ← REST API para cuentas bancarias
│   └── src/main/java/com/bcp/training/
│       ├── JoedayzBankApplication.java
│       ├── config/
│       │   ├── KafkaConsumerConfig.java   ✅ ya completo
│       │   └── KafkaProducerConfig.java   ✅ ya completo
│       ├── controller/
│       │   └── BankAccountsController.java   ⚠️ COMPLETAR (Tarea 1)
│       ├── event/
│       │   └── BankAccountWasCreated.java   ✅ ya completo
│       ├── model/
│       │   └── BankAccount.java   ✅ ya completo
│       ├── reactive/
│       │   └── AccountTypeProcessor.java   ⚠️ COMPLETAR (Tarea 3)
│       └── repository/
│           └── BankAccountRepository.java   ✅ ya completo
│
├── fraud-detector/        ← Consumidor de eventos / Detector de fraude
│   └── src/main/java/com/bcp/training/
│       ├── FraudDetectorApplication.java
│       ├── config/
│       │   ├── KafkaConsumerConfig.java   ✅ ya completo
│       │   └── KafkaProducerConfig.java   ✅ ya completo
│       ├── event/
│       │   ├── BankAccountWasCreated.java        ✅ ya completo
│       │   ├── HighRiskAccountWasDetected.java   ✅ ya completo
│       │   └── LowRiskAccountWasDetected.java    ✅ ya completo
│       └── reactive/
│           └── FraudProcessor.java   ⚠️ COMPLETAR (Tarea 2)
```

> Los archivos marcados con ⚠️ requieren que completes el código. Los marcados con ✅ ya están listos.

---

## 2. Levantar infraestructura

Primero, levanta PostgreSQL, Zookeeper y Kafka con Docker Compose:

**macOS / Linux:**
```bash
docker compose up -d
# o: podman compose up -d
```

**Windows:**
```powershell
docker compose up -d
# o: podman compose up -d
```

Verifica que los contenedores estén corriendo:
```bash
docker compose ps
```

Deberías ver 3 servicios: `joedayz-postgres`, `joedayz-zookeeper`, `joedayz-kafka`.

---

## 3. Tarea 1: Publicar evento `BankAccountWasCreated` desde joedayz-bank

### Contexto

El archivo `BankAccountsController.java` ya tiene un método `sendBankAccountEvent()` que envía un evento a Kafka, pero:
1. **Falta** inyectar el `KafkaTemplate` como dependencia del controlador.
2. **Falta** invocar `sendBankAccountEvent()` después de guardar la cuenta en la base de datos.

### 3.1. Inyectar `KafkaTemplate` en el constructor

Abre `joedayz-bank/src/main/java/com/bcp/training/controller/BankAccountsController.java`.

**Código actual** (incompleto):
```java
private final BankAccountRepository repository;

public BankAccountsController(BankAccountRepository repository) {
    this.repository = repository;
}
```

**Código a completar** — agrega el campo `kafkaTemplate` y modifica el constructor:
```java
private final BankAccountRepository repository;
private final KafkaTemplate<String, BankAccountWasCreated> kafkaTemplate;

public BankAccountsController(BankAccountRepository repository,
                              KafkaTemplate<String, BankAccountWasCreated> kafkaTemplate) {
    this.repository = repository;
    this.kafkaTemplate = kafkaTemplate;
}
```

### 3.2. Llamar a `sendBankAccountEvent` después de guardar

**Código actual** (incompleto):
```java
@PostMapping
public Mono<ResponseEntity<Void>> create(@Valid @RequestBody BankAccount bankAccount) {
    if (bankAccount.getBalance() == null) {
        return Mono.just(ResponseEntity.badRequest().build());
    }
    if (bankAccount.getBalance() <= 0) {
        return Mono.just(ResponseEntity.badRequest().build());
    }
    return repository.save(bankAccount)
            .map(saved -> ResponseEntity.created(URI.create("/accounts/" + saved.getId())).build());
}
```

**Código a completar** — agrega `.doOnSuccess(...)` antes del `.map(...)`:
```java
@PostMapping
public Mono<ResponseEntity<Void>> create(@Valid @RequestBody BankAccount bankAccount) {
    if (bankAccount.getBalance() == null) {
        return Mono.just(ResponseEntity.badRequest().build());
    }
    if (bankAccount.getBalance() <= 0) {
        return Mono.just(ResponseEntity.badRequest().build());
    }
    return repository.save(bankAccount)
            .doOnSuccess(saved -> sendBankAccountEvent(saved.getId(), saved.getBalance()))
            .map(saved -> ResponseEntity.created(URI.create("/accounts/" + saved.getId())).build());
}
```

> **Explicación:** `.doOnSuccess()` es un operador reactivo que ejecuta una acción (side-effect) cuando el `Mono` emite un valor exitosamente. Aquí lo usamos para publicar el evento a Kafka sin alterar el flujo principal.

---

## 4. Tarea 2: Implementar `FraudProcessor` en fraud-detector

### Contexto

El archivo `FraudProcessor.java` ya contiene los métodos de utilidad (`calculateFraudScore`, logs, constantes de tópicos), pero:
1. **Falta** inyectar el `KafkaTemplate` para publicar eventos de salida.
2. **Falta** implementar el método `sendEventNotifications()` anotado con `@KafkaListener`.

### 4.1. Inyectar `KafkaTemplate`

Abre `fraud-detector/src/main/java/com/bcp/training/reactive/FraudProcessor.java`.

**Código actual** (incompleto):
```java
@Component
public class FraudProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FraudProcessor.class);

    private static final String LOW_RISK_TOPIC = "low-risk-account-was-detected";
    private static final String HIGH_RISK_TOPIC = "high-risk-account-was-detected";


    private Integer calculateFraudScore(Long amount) {
```

**Código a completar** — agrega el campo `kafkaTemplate` y el constructor justo después de las constantes:
```java
@Component
public class FraudProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FraudProcessor.class);

    private static final String LOW_RISK_TOPIC = "low-risk-account-was-detected";
    private static final String HIGH_RISK_TOPIC = "high-risk-account-was-detected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private Integer calculateFraudScore(Long amount) {
```

### 4.2. Implementar el listener `sendEventNotifications`

Agrega el siguiente método **antes** de `calculateFraudScore`:

```java
@KafkaListener(topics = "bank-account-was-created", groupId = "fraud-detector", containerFactory = "kafkaListenerContainerFactory")
public void sendEventNotifications(BankAccountWasCreated event) {
    logBankAccountWasCreatedEvent(event);

    Integer fraudScore = calculateFraudScore(event.balance);

    logFraudScore(event.id, fraudScore);

    if (fraudScore > 50) {
        logEmitEvent("HighRiskAccountWasDetected", event.id);
        kafkaTemplate.send(HIGH_RISK_TOPIC, new HighRiskAccountWasDetected(event.id));
    } else if (fraudScore > 20) {
        logEmitEvent("LowRiskAccountWasDetected", event.id);
        kafkaTemplate.send(LOW_RISK_TOPIC, new LowRiskAccountWasDetected(event.id));
    }
}
```

### Resultado final completo de `FraudProcessor.java`

```java
package com.bcp.training.reactive;

import com.bcp.training.event.BankAccountWasCreated;
import com.bcp.training.event.HighRiskAccountWasDetected;
import com.bcp.training.event.LowRiskAccountWasDetected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FraudProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FraudProcessor.class);

    private static final String LOW_RISK_TOPIC = "low-risk-account-was-detected";
    private static final String HIGH_RISK_TOPIC = "high-risk-account-was-detected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "bank-account-was-created", groupId = "fraud-detector", containerFactory = "kafkaListenerContainerFactory")
    public void sendEventNotifications(BankAccountWasCreated event) {
        logBankAccountWasCreatedEvent(event);

        Integer fraudScore = calculateFraudScore(event.balance);

        logFraudScore(event.id, fraudScore);

        if (fraudScore > 50) {
            logEmitEvent("HighRiskAccountWasDetected", event.id);
            kafkaTemplate.send(HIGH_RISK_TOPIC, new HighRiskAccountWasDetected(event.id));
        } else if (fraudScore > 20) {
            logEmitEvent("LowRiskAccountWasDetected", event.id);
            kafkaTemplate.send(LOW_RISK_TOPIC, new LowRiskAccountWasDetected(event.id));
        }
    }

    private Integer calculateFraudScore(Long amount) {
        if (amount > 25000) {
            return 75;
        } else if (amount > 3000) {
            return 25;
        }
        return -1;
    }

    private void logBankAccountWasCreatedEvent(BankAccountWasCreated event) {
        LOGGER.info("Received BankAccountWasCreated - ID: {} Balance: {}", event.id, event.balance);
    }

    private void logFraudScore(Long bankAccountId, Integer score) {
        LOGGER.info("Fraud score was calculated - ID: {} Score: {}", bankAccountId, score);
    }

    private void logEmitEvent(String eventName, Long bankAccountId) {
        LOGGER.info("Sending a {} event for bank account #{}", eventName, bankAccountId);
    }
}
```

> **Lógica de fraud score:**
> - `balance > 25000` → score = 75 → **HighRiskAccountWasDetected**
> - `balance > 3000` → score = 25 → **LowRiskAccountWasDetected**
> - `balance <= 3000` → score = -1 → no se publica ningún evento

---

## 5. Tarea 3: Implementar `AccountTypeProcessor` en joedayz-bank

### Contexto

El archivo `AccountTypeProcessor.java` ya tiene los métodos `calculateAccountType()` y `logEvent()`, pero:
1. **Falta** inyectar el `BankAccountRepository` para poder actualizar la cuenta.
2. **Falta** implementar el método `processNewBankAccountEvents()` anotado con `@KafkaListener`.

### 5.1. Inyectar `BankAccountRepository`

Abre `joedayz-bank/src/main/java/com/bcp/training/reactive/AccountTypeProcessor.java`.

**Código actual** (incompleto):
```java
@Component
public class AccountTypeProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProcessor.class);


    public String calculateAccountType(Long balance) {
```

**Código a completar** — agrega la dependencia y el constructor:
```java
@Component
public class AccountTypeProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProcessor.class);

    private final BankAccountRepository repository;

    public AccountTypeProcessor(BankAccountRepository repository) {
        this.repository = repository;
    }

    public String calculateAccountType(Long balance) {
```

### 5.2. Implementar el listener `processNewBankAccountEvents`

Agrega el siguiente método con `@KafkaListener` **antes** de `calculateAccountType`:

```java
@KafkaListener(topics = "bank-account-was-created",
        groupId = "joedayz-bank-account-type-v2",
        containerFactory = "kafkaListenerContainerFactory")
public void processNewBankAccountEvents(BankAccountWasCreated event) {
    String assignedAccountType = calculateAccountType(event.balance);

    logEvent(event, assignedAccountType);

    repository.findById(event.id)
            .flatMap(entity -> {
                entity.setType(assignedAccountType);
                return repository.save(entity);
            })
            .subscribe();
}
```

> **Nota:** Necesitarás agregar los siguientes imports:
> ```java
> import com.bcp.training.repository.BankAccountRepository;
> import org.springframework.kafka.annotation.KafkaListener;
> ```

### Resultado final completo de `AccountTypeProcessor.java`

```java
package com.bcp.training.reactive;

import com.bcp.training.event.BankAccountWasCreated;
import com.bcp.training.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AccountTypeProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProcessor.class);

    private final BankAccountRepository repository;

    public AccountTypeProcessor(BankAccountRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "bank-account-was-created",
            groupId = "joedayz-bank-account-type-v2",
            containerFactory = "kafkaListenerContainerFactory")
    public void processNewBankAccountEvents(BankAccountWasCreated event) {
        String assignedAccountType = calculateAccountType(event.balance);

        logEvent(event, assignedAccountType);

        repository.findById(event.id)
                .flatMap(entity -> {
                    entity.setType(assignedAccountType);
                    return repository.save(entity);
                })
                .subscribe();
    }

    public String calculateAccountType(Long balance) {
        return balance >= 100000 ? "premium" : "regular";
    }

    private void logEvent(BankAccountWasCreated event, String assignedType) {
        LOGGER.info("Processing BankAccountWasCreated - ID: {} Balance: {} Type: {}",
                event.id, event.balance, assignedType);
    }
}
```

> **Lógica de tipo de cuenta:**
> - `balance >= 100000` → tipo `"premium"`
> - `balance < 100000` → tipo `"regular"`

---

## 6. Ejecutar los microservicios

### Terminal 1 — joedayz-bank (puerto 8080)
```bash
cd joedayz-bank
mvn spring-boot:run
```

### Terminal 2 — fraud-detector (puerto 8081)
```bash
cd fraud-detector
mvn spring-boot:run
```

---

## 7. Probar

### Crear una cuenta con balance bajo (sin evento de riesgo)
```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"balance": 1000}'
```

**Esperado en los logs:**
- `joedayz-bank`: El evento se publica a Kafka y `AccountTypeProcessor` asigna tipo `"regular"`.
- `fraud-detector`: Recibe el evento pero el fraud score es `-1`, por lo que **no** se publica ningún evento de riesgo.

### Crear una cuenta con balance medio (LowRisk)
```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"balance": 5000}'
```

**Esperado en los logs del fraud-detector:**
```
Received BankAccountWasCreated - ID: 2 Balance: 5000
Fraud score was calculated - ID: 2 Score: 25
Sending a LowRiskAccountWasDetected event for bank account #2
```

### Crear una cuenta con balance alto (HighRisk)
```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"balance": 50000}'
```

**Esperado en los logs del fraud-detector:**
```
Received BankAccountWasCreated - ID: 3 Balance: 50000
Fraud score was calculated - ID: 3 Score: 75
Sending a HighRiskAccountWasDetected event for bank account #3
```

### Crear una cuenta premium
```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"balance": 150000}'
```

**Esperado en los logs de joedayz-bank:**
```
Processing BankAccountWasCreated - ID: 4 Balance: 150000 Type: premium
```

### Consultar todas las cuentas
```bash
curl http://localhost:8080/accounts
```

### Interfaz web

También puedes abrir http://localhost:8080 en tu navegador para ver la interfaz web de creación y listado de cuentas.

---

## 8. Resumen de la arquitectura

```
┌──────────────┐     POST /accounts      ┌──────────────────────┐
│   Cliente     │ ──────────────────────→ │   joedayz-bank       │
│  (curl/web)   │                         │   (puerto 8080)      │
└──────────────┘                          │                      │
                                          │  1. Guarda en BD     │
                                          │  2. Publica evento   │
                                          │     a Kafka          │
                                          └──────────┬───────────┘
                                                     │
                                          topic: bank-account-was-created
                                                     │
                              ┌───────────────────────┼──────────────────────┐
                              │                       │                      │
                              ▼                       ▼                      │
                   ┌─────────────────────┐  ┌──────────────────────┐        │
                   │  fraud-detector     │  │  AccountTypeProcessor │        │
                   │  (puerto 8081)      │  │  (dentro de           │        │
                   │                     │  │   joedayz-bank)       │        │
                   │  Calcula fraud      │  │                      │        │
                   │  score y publica:   │  │  Asigna tipo:        │        │
                   │  - HighRisk (>50)   │  │  - premium (>=100k)  │        │
                   │  - LowRisk  (>20)   │  │  - regular (<100k)   │        │
                   └─────────────────────┘  └──────────────────────┘        │
```

---

## Solución completa

Ver carpeta `10-reactive-eda-solution/`.
