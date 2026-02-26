# 08-reactive-architecture-start

Laboratorio de arquitectura reactiva con Spring Boot 4.

## Objetivo

Demostrar el problema de bloqueo cuando un endpoint REST llama a un servicio externo de forma **síncrona** (blocking). El endpoint `GET /products/{id}/priceHistory` usa `.block()` en WebClient, bloqueando el hilo durante ~2 segundos por request.

## Requisitos

- Java 21
- Docker o Podman (para el servicio prices)

## Estructura

```
08-reactive-architecture-start/
├── products/     # Servicio Spring Boot (Java)
└── prices/       # Servicio externo (Python Flask, simula proceso lento ~2 seg)
```

## Ejecutar

### 1. Levantar el servicio prices (simula proceso lento ~2 seg)

**Opción A: docker-compose (recomendado, usa el proyecto local):**

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

**Opción B: imagen preconstruida:**
```bash
docker run -d --name prices -p 5500:5000 docker.io/joedayz/do378-reactive-architecture-prices:latest
# o
podman run -d --name prices -p 5500:5000 docker.io/joedayz/do378-reactive-architecture-prices:latest
```

**Opción C: construir desde `prices/`:**
```bash
cd prices
docker build -f Containerfile -t prices:latest .
docker run -d --name prices -p 5500:5000 prices:latest
```

### 2. Ejecutar products

**macOS / Linux:**
```bash
cd products
mvn spring-boot:run
```

Si ves `LinkError: failed to load the required native library`, usa:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dreactor.netty.native=false"
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
