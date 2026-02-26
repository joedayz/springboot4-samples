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

**Docker o Podman (macOS / Linux):**
```bash
docker run -d --name prices -p 5500:5000 docker.io/joedayz/do378-reactive-architecture-prices:latest
# o: podman run -d --name prices -p 5500:5000 docker.io/joedayz/do378-reactive-architecture-prices:latest
cd products
mvn spring-boot:run
time ./benchmark.sh
```

**Windows:**
```powershell
docker run -d --name prices -p 5500:5000 docker.io/joedayz/do378-reactive-architecture-prices:latest
cd products
mvn spring-boot:run
.\benchmark.ps1
```

Ver [CONTAINERS.md](../CONTAINERS.md) para más comandos.
