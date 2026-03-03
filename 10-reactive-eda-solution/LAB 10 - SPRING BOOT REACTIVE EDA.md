# LAB 10: SPRING BOOT REACTIVE EDA

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

## Objetivo

En este laboratorio aprenderás a:
- Implementar arquitectura orientada a eventos (EDA) con Spring Boot y Kafka
- Publicar eventos desde un servicio REST (joedayz-bank)
- Consumir eventos y procesarlos (fraud-detector)
- Usar Spring Kafka para producers y consumers

## Prerequisitos

- **Java 21**
- **Docker o Podman** (PostgreSQL + Kafka)

## 1. Estructura del proyecto

- **joedayz-bank**: REST API para cuentas bancarias (GET, POST). Falta publicar evento a Kafka.
- **fraud-detector**: Consumidor de eventos. Falta implementar el procesador.

## 2. Levantar infraestructura

**macOS / Linux:**
```bash
cd ../10-reactive-eda-solution
docker compose up -d
# o: podman compose up -d
```

**Windows:**
```powershell
cd ..\10-reactive-eda-solution
docker compose up -d
# o: podman compose up -d
```

## 3. Tarea 1: Publicar BankAccountWasCreated desde joedayz-bank

### 3.1. Crear el evento

Crea `joedayz-bank/src/main/java/com/bcp/training/event/BankAccountWasCreated.java`:

```java
package com.bcp.training.event;

public class BankAccountWasCreated {
    public Long id;
    public Long balance;

    public BankAccountWasCreated() {}

    public BankAccountWasCreated(Long id, Long balance) {
        this.id = id;
        this.balance = balance;
    }
}
```

### 3.2. Configurar KafkaTemplate

Crea `joedayz-bank/src/main/java/com/bcp/training/config/KafkaProducerConfig.java` (ver solución).

### 3.3. Implementar sendBankAccountEvent en BankAccountsController

Inyecta `KafkaTemplate<String, BankAccountWasCreated>` y en el método `create`, después de `repository.save()`, llama a:

```java
kafkaTemplate.send("bank-account-was-created", new BankAccountWasCreated(saved.getId(), saved.getBalance()));
```

## 4. Tarea 2: Implementar FraudProcessor en fraud-detector

### 4.1. Crear eventos de salida

- `LowRiskAccountWasDetected` (bankAccountId)
- `HighRiskAccountWasDetected` (bankAccountId)

### 4.2. Implementar FraudProcessor

- Consumir de `bank-account-was-created` con `@KafkaListener`
- Calcular fraud score: balance > 25000 → 75, balance > 3000 → 25, sino -1
- Si score > 50: publicar `HighRiskAccountWasDetected`
- Si score > 20: publicar `LowRiskAccountWasDetected`

## 5. Tarea 3 (opcional): AccountTypeProcessor en joedayz-bank

Consumir `bank-account-was-created` y actualizar el campo `type` de la cuenta:
- balance >= 100000 → "premium"
- sino → "regular"

## 6. Ejecutar

**Terminal 1:**
```bash
cd joedayz-bank
mvn spring-boot:run
```

**Terminal 2:**
```bash
cd fraud-detector
mvn spring-boot:run
```

Ver [CONTAINERS.md](../CONTAINERS.md) para comandos Docker/Podman por plataforma.

## Probar

```bash
curl -X POST http://localhost:8080/accounts -H "Content-Type: application/json" -d '{"balance": 5000}'
curl http://localhost:8080/accounts
```

## Solución completa

Ver `10-reactive-eda-solution/LAB 10 - SPRING BOOT REACTIVE EDA - SOLUCIÓN.md`.
