# 09-reactive-eda-solution

Solución del laboratorio EDA con Spring Boot 4.

## Estructura

- **joedayz-bank**: REST API, R2DBC, publica `BankAccountWasCreated` a Kafka, consume para asignar tipo (premium/regular)
- **fraud-detector**: Consume `BankAccountWasCreated`, calcula fraud score, publica `LowRiskAccountWasDetected` o `HighRiskAccountWasDetected`

## Requisitos

- Java 21
- Docker o Podman

## Ejecutar

```bash
# 1. Levantar PostgreSQL y Kafka
docker compose up -d

# 2. joedayz-bank (puerto 8080)
cd joedayz-bank && mvn spring-boot:run

# 3. fraud-detector (puerto 8081)
cd fraud-detector && mvn spring-boot:run
```

## Probar

```bash
# Crear cuenta
curl -X POST http://localhost:8080/accounts -H "Content-Type: application/json" -d '{"balance": 5000}'

# Listar cuentas
curl http://localhost:8080/accounts
```
