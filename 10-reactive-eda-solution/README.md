# 10-reactive-eda-solution

Solución del laboratorio EDA con Spring Boot 4.

## Estructura

- **joedayz-bank**: REST API, R2DBC, publica `BankAccountWasCreated` a Kafka, consume para asignar tipo (premium/regular)
- **fraud-detector**: Consume `BankAccountWasCreated`, calcula fraud score, publica `LowRiskAccountWasDetected` o `HighRiskAccountWasDetected`

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

### 2. joedayz-bank (puerto 8080)

```bash
cd joedayz-bank
mvn spring-boot:run
```

### 3. fraud-detector (puerto 8081) — en otra terminal

```bash
cd fraud-detector
mvn spring-boot:run
```

## Probar

**macOS / Linux:**
```bash
curl -X POST http://localhost:8080/accounts -H "Content-Type: application/json" -d '{"balance": 5000}'
curl http://localhost:8080/accounts
```

**Windows PowerShell:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/accounts -Method POST -ContentType "application/json" -Body '{"balance": 5000}'
Invoke-RestMethod -Uri http://localhost:8080/accounts
```

Ver [CONTAINERS.md](../CONTAINERS.md) para más comandos.
