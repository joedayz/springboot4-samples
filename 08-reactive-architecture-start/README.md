# 08-reactive-architecture-start

Laboratorio de arquitectura reactiva con Spring Boot 4.

## Objetivo

Demostrar el problema de bloqueo cuando un endpoint REST llama a un servicio externo de forma **síncrona** (blocking). El endpoint `GET /products/{id}/priceHistory` usa `.block()` en WebClient, bloqueando el hilo durante ~2 segundos por request.

## Requisitos

- Java 21
- Docker o Podman (para el servicio prices)

## Ejecutar

### 1. Levantar el servicio prices (simula proceso lento ~2 seg)

**macOS / Linux (Docker o Podman):**
```bash
docker run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
# o
podman run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
```

**Windows PowerShell:**
```powershell
docker run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
# o
podman run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
```

**Windows CMD:**
```cmd
docker run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
REM o
podman run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
```

### 2. Ejecutar products

**macOS / Linux:**
```bash
cd products
mvn spring-boot:run
```

**Windows PowerShell:**
```powershell
cd products
mvn spring-boot:run
```

**Windows CMD:**
```cmd
cd products
mvn spring-boot:run
```

## Probar

**macOS / Linux:**
```bash
time curl http://localhost:8080/products/1/priceHistory
time ./benchmark.sh
```

**Windows PowerShell:**
```powershell
Measure-Command { Invoke-WebRequest http://localhost:8080/products/1/priceHistory }
# Benchmark: .\benchmark.ps1
```

**Windows CMD:**
```cmd
curl http://localhost:8080/products/1/priceHistory
```

## Tarea

Migrar `PricesService.getProductPriceHistory` para retornar `Mono<ProductPriceHistory>` en lugar de bloquear. Ver LAB 8 - SPRING BOOT REACTIVE ARCHITECTURE.

---

Ver [CONTAINERS.md](../CONTAINERS.md) para más comandos Docker/Podman por plataforma.
