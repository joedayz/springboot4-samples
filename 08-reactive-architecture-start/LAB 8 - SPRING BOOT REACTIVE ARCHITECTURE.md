# LAB 8: SPRING BOOT REACTIVE ARCHITECTURE

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

## Objetivo

Demostrar cómo una arquitectura reactiva mejora el rendimiento cuando hay operaciones de I/O lentas. El proyecto consta de:

- **products**: Servicio REST que consulta historial de precios
- **prices**: Servicio externo (contenedor) que simula un proceso costoso (~2 segundos)

## Problema (versión start)

El endpoint `GET /products/{id}/priceHistory` usa `PricesService.getProductPriceHistory()` que hace `.block()` en WebClient. Cada request bloquea el hilo ~2 segundos. Con 10 requests paralelos: ~20 segundos.

## Solución: Migrar a reactivo

### 1. Modificar PricesService

**Ruta:** `products/src/main/java/com/bcp/training/PricesService.java`

Cambiar de:
```java
public ProductPriceHistory getProductPriceHistory(Long productId) {
    return webClient.get()
            .uri("/history/{productId}", productId)
            .retrieve()
            .bodyToMono(ProductPriceHistory.class)
            .block();  // BLOQUEANTE
}
```

A:
```java
public Mono<ProductPriceHistory> getProductPriceHistory(Long productId) {
    return webClient.get()
            .uri("/history/{productId}", productId)
            .retrieve()
            .bodyToMono(ProductPriceHistory.class);
}
```

### 2. Modificar ProductsController

**Ruta:** `products/src/main/java/com/bcp/training/ProductsController.java`

Cambiar el endpoint para retornar `Mono`:
```java
@GetMapping("/{productId}/priceHistory")
public Mono<ProductPriceHistory> getProductPriceHistory(@PathVariable Long productId) {
    return pricesService.getProductPriceHistory(productId);
}
```

## Resultado

Con la versión reactiva, 10 requests paralelos completan en ~2 segundos (todos en paralelo, no en serie).

## Comparación Quarkus vs Spring Boot 4

| Concepto | Quarkus | Spring Boot 4 |
|----------|---------|----------------|
| REST Client | MicroProfile Rest Client | WebClient |
| Reactivo | Uni | Mono |
| Bloqueante | @Blocking | .block() |

## Ejecutar (Docker o Podman)

**Levantar prices (desde el directorio del proyecto):**
```bash
docker compose up -d
# o: podman compose up -d
```

**Ejecutar products y benchmark:**
```bash
cd products
mvn spring-boot:run
# En otra terminal (macOS/Linux): time ./benchmark.sh
# Windows PowerShell: .\benchmark.ps1
```

El servicio `prices` está en `prices/` (Python Flask). Ver [CONTAINERS.md](../CONTAINERS.md) para más comandos.

## Solución completa

Ver `08-reactive-architecture-solution/`.
