# 10-reactive-eda-start

Laboratorio EDA (Event-Driven Architecture) con Spring Boot 4.

## Estructura

- **joedayz-bank**: REST API (GET, POST /accounts), R2DBC, PostgreSQL
- **fraud-detector**: Aplicación mínima (solo Kafka config)

## Tareas

1. En joedayz-bank: implementar `sendBankAccountEvent` para publicar `BankAccountWasCreated` a Kafka
2. En fraud-detector: implementar consumer de `bank-account-was-created` y producer de alertas

## Requisitos

- Java 21
- Docker o Podman (PostgreSQL + Kafka)

## Ejecutar infraestructura

**macOS / Linux (Docker o Podman):**
```bash
cd ../10-reactive-eda-solution
docker compose up -d
# o
podman compose up -d
```

**Windows PowerShell:**
```powershell
cd ..\10-reactive-eda-solution
docker compose up -d
# o
podman compose up -d
```

**Windows CMD:**
```cmd
cd ..\10-reactive-eda-solution
docker compose up -d
REM o
podman compose up -d
```

## Ejecutar aplicaciones

**macOS / Linux / Windows:**
```bash
cd joedayz-bank
mvn spring-boot:run
```

En otra terminal:
```bash
cd fraud-detector
mvn spring-boot:run
```

Ver [CONTAINERS.md](../CONTAINERS.md) para más comandos.
