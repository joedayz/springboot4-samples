# LAB 8: SPRING BOOT REACTIVE ARCHITECTURE — Solución de Referencia

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 08-reactive-architecture-solution

## Código de la solución

### PricesService.java

```java
public Mono<ProductPriceHistory> getProductPriceHistory(Long productId) {
    return webClient.get()
            .uri("/history/{productId}", productId)
            .retrieve()
            .bodyToMono(ProductPriceHistory.class);
}
```

### ProductsController.java

```java
@GetMapping("/{productId}/priceHistory")
public Mono<ProductPriceHistory> getProductPriceHistory(@PathVariable Long productId) {
    return pricesService.getProductPriceHistory(productId);
}
```

## Ejecutar

**Levantar prices (docker-compose):**
```bash
docker compose up -d
# o: podman compose up -d
```

**Ejecutar products:**
```bash
cd products
mvn spring-boot:run
```

**Benchmark:** `./benchmark.sh` (macOS/Linux) o `.\benchmark.ps1` (Windows)

El servicio `prices` está en `prices/` (Python Flask). Ver [CONTAINERS.md](../CONTAINERS.md) para más comandos.
