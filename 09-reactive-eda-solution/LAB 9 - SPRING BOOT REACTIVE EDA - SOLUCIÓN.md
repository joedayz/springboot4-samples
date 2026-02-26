# LAB 9: SPRING BOOT REACTIVE EDA — Solución de Referencia

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 09-reactive-eda-solution

Este documento contiene el **código completo** de la solución EDA con Kafka.

---

## Estructura

- **joedayz-bank**: REST API, publica `BankAccountWasCreated`, consume para asignar tipo (premium/regular)
- **fraud-detector**: Consume `BankAccountWasCreated`, publica `LowRiskAccountWasDetected` o `HighRiskAccountWasDetected`

---

## 1. joedayz-bank — BankAccountsController.java

**Ruta:** `joedayz-bank/src/main/java/com/bcp/training/controller/BankAccountsController.java`

```java
package com.bcp.training.controller;

import com.bcp.training.event.BankAccountWasCreated;
import com.bcp.training.model.BankAccount;
import com.bcp.training.repository.BankAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/accounts")
public class BankAccountsController {

    private static final String TOPIC = "bank-account-was-created";

    private final BankAccountRepository repository;
    private final KafkaTemplate<String, BankAccountWasCreated> kafkaTemplate;

    public BankAccountsController(BankAccountRepository repository,
                                  KafkaTemplate<String, BankAccountWasCreated> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    public Flux<BankAccount> get() {
        return repository.findAllOrderById();
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> create(@Valid @RequestBody BankAccount bankAccount) {
        if (bankAccount.getBalance() == null || bankAccount.getBalance() <= 0) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return repository.save(bankAccount)
                .doOnSuccess(saved -> kafkaTemplate.send(TOPIC, new BankAccountWasCreated(saved.getId(), saved.getBalance())))
                .map(saved -> ResponseEntity.created(URI.create("/accounts/" + saved.getId())).build());
    }
}
```

---

## 2. joedayz-bank — AccountTypeProcessor.java

**Ruta:** `joedayz-bank/src/main/java/com/bcp/training/reactive/AccountTypeProcessor.java`

```java
package com.bcp.training.reactive;

import com.bcp.training.event.BankAccountWasCreated;
import com.bcp.training.model.BankAccount;
import com.bcp.training.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountTypeProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProcessor.class);
    private final BankAccountRepository repository;

    public AccountTypeProcessor(BankAccountRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "bank-account-was-created", groupId = "joedayz-bank-account-type", containerFactory = "kafkaListenerContainerFactory")
    public void processNewBankAccountEvents(BankAccountWasCreated event) {
        String assignedAccountType = event.balance >= 100000 ? "premium" : "regular";
        LOGGER.info("Processing BankAccountWasCreated - ID: {} Balance: {} Type: {}", event.id, event.balance, assignedAccountType);
        repository.findById(event.id)
                .flatMap(entity -> {
                    entity.setType(assignedAccountType);
                    return repository.save(entity);
                })
                .subscribe();
    }
}
```

---

## 3. fraud-detector — FraudProcessor.java

**Ruta:** `fraud-detector/src/main/java/com/bcp/training/reactive/FraudProcessor.java`

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
        Integer fraudScore = event.balance > 25000 ? 75 : (event.balance > 3000 ? 25 : -1);
        if (fraudScore > 50) {
            kafkaTemplate.send(HIGH_RISK_TOPIC, new HighRiskAccountWasDetected(event.id));
        } else if (fraudScore > 20) {
            kafkaTemplate.send(LOW_RISK_TOPIC, new LowRiskAccountWasDetected(event.id));
        }
    }
}
```

---

## 4. Ejecutar

```bash
docker compose up -d
cd joedayz-bank && mvn spring-boot:run
cd fraud-detector && mvn spring-boot:run
```

## 5. Probar

```bash
curl -X POST http://localhost:8080/accounts -H "Content-Type: application/json" -d '{"balance": 5000}'
curl http://localhost:8080/accounts
```
