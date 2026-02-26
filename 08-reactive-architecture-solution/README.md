# 08-reactive-architecture-solution

Solución del laboratorio de arquitectura reactiva con Spring Boot 4.

## Implementación

- **PricesService**: Retorna `Mono<ProductPriceHistory>` (reactivo, sin `.block()`)
- **ProductsController**: Endpoint `GET /products/{id}/priceHistory` retorna `Mono` directamente

Con la versión reactiva, 10 requests paralelos completan en ~2 segundos (en lugar de ~20 seg).

## Estructura

```
08-reactive-architecture-solution/
├── products/     # Servicio Spring Boot (Java)
└── prices/      # Servicio externo (Python Flask)
```

## Ejecutar

**Levantar prices (docker-compose, usa el proyecto local):**
```bash
docker compose up -d
# o
podman compose up -d
```

**O con imagen preconstruida:**
```bash
docker run -d --name prices -p 5500:5000 docker.io/joedayz/do378-reactive-architecture-prices:latest
```

**Ejecutar products:**
```bash
cd products
mvn spring-boot:run
```

**Benchmark (macOS/Linux):**
```bash
time ./benchmark.sh
```

Ver [CONTAINERS.md](../CONTAINERS.md) para comandos Windows/Podman.
