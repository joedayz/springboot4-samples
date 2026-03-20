# LAB 8: SPRING BOOT REACTIVE ARCHITECTURE — Solución de Referencia

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 08-reactive-architecture-solution

Este documento contiene el **código completo** de la solución, siguiendo la estructura del laboratorio original.

---

## 1. PricesService.java

**Ruta:** `products/src/main/java/com/bcp/training/PricesService.java`

```java
package com.bcp.training;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PricesService {

    private final WebClient webClient;

    public PricesService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:5500")
                .build();
    }

    public Mono<ProductPriceHistory> getProductPriceHistory(Long productId) {
        return webClient.get()
                .uri("/history/{productId}", productId)
                .retrieve()
                .bodyToMono(ProductPriceHistory.class);
    }
}
```

---

## 2. ProductsController.java

**Ruta:** `products/src/main/java/com/bcp/training/ProductsController.java`

```java
package com.bcp.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final PricesService pricesService;

    public ProductsController(PricesService pricesService) {
        this.pricesService = pricesService;
    }

    @GetMapping("/{productId}/priceHistory")
    public Mono<ProductPriceHistory> getProductPriceHistory(@PathVariable Long productId) {
        return pricesService.getProductPriceHistory(productId);
    }

    @GetMapping("/blocking")
    public Mono<String> blocking() {
        return Mono.fromCallable(() -> {
            try {
                Thread.sleep(30000);
                return "I am a blocking operation";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
```

---

## 3. Ejecutar

### Levantar prices

**Docker o Podman:**
```bash
docker compose up -d
# o
podman compose up -d
```

```powershell
docker compose up -d
# o
podman compose up -d
```

### Ejecutar products

```bash
cd products
mvn spring-boot:run
```

```powershell
cd products
mvn spring-boot:run
```

### Benchmark

**Linux/macOS:**
```bash
time ./benchmark.sh
```

```powershell
time ./benchmark.sh
```

**Windows PowerShell:**
```powershell
.\benchmark.ps1
```

**Resultado esperado:** ~3 segundos para 10 requests (vs ~20 seg en versión bloqueante)

---

## 4. Resultados Esperados

| Escenario | Tiempo (10 requests) |
|-----------|----------------------|
| Bloqueante (start) | ~20 segundos |
| No Bloqueante (solution) | ~3 segundos |

---

## 5. Detener

```bash
# Aplicación: Ctrl+C

# Contenedor prices
docker compose down
# o
podman compose down
```

```powershell
# Aplicación: Ctrl+C

# Contenedor prices
docker compose down
# o
podman compose down
```

---

Ver [CONTAINERS.md](../CONTAINERS.md) para comandos por plataforma (Windows, macOS, Linux).
