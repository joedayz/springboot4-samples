# 09-reactive-eda-start

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

```bash
# Desde 09-reactive-eda-solution (mismo docker-compose)
docker compose -f ../09-reactive-eda-solution/docker-compose.yml up -d
```

## Ejecutar aplicaciones

```bash
cd joedayz-bank
mvn spring-boot:run
```

```bash
cd fraud-detector
mvn spring-boot:run
```
