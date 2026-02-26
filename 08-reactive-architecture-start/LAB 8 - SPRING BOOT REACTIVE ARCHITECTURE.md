# LAB 8: SPRING BOOT REACTIVE ARCHITECTURE

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

## Descripción

Este laboratorio demuestra cómo implementar una arquitectura reactiva en Spring Boot 4 para mejorar el rendimiento de aplicaciones que realizan operaciones de I/O lentas. El proyecto consta de dos servicios:

- **products**: Servicio REST que expone una API para consultar productos y su historial de precios
- **prices**: Servicio externo que proporciona datos históricos de precios (simula un proceso costoso que tarda ~2 segundos)

## Problema

El endpoint `GET /products/{id}/priceHistory` depende del servicio **prices** para obtener datos históricos. En la versión inicial, `PricesService` usa `.block()` en WebClient, lo que bloquea el hilo durante ~2 segundos por cada request. Las solicitudes se ponen en cola y el rendimiento se degrada significativamente.

## Estructura del Proyecto

```
08-reactive-architecture-start/
├── products/          # Servicio Spring Boot (Java)
│   ├── src/main/java/com/bcp/training/
│   │   ├── ProductsController.java    # Controlador REST
│   │   ├── PricesService.java         # Cliente REST (WebClient)
│   │   ├── ProductPriceHistory.java   # Modelo de datos
│   │   └── Price.java                 # Modelo de datos
│   ├── benchmark.sh                   # Script de benchmark (Linux/Mac)
│   ├── benchmark.ps1                  # Script de benchmark (Windows)
│   └── pom.xml
└── prices/            # Servicio externo (Python Flask)
    ├── app.py
    ├── Containerfile
    └── requirements.txt
```

## Prerequisitos

- Java 21+
- Maven 3.8+
- Docker o Podman
- curl (o PowerShell en Windows)

## Configuración Inicial

### 1. Ejecutar el servicio prices

**Opción A: docker-compose (recomendado)**
```bash
docker compose up -d
# o
podman compose up -d
```

**Opción B: imagen preconstruida**
```bash
docker run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
# o
podman run -d --name prices -p 5500:5000 --restart=always docker.io/joedayz/do378-reactive-architecture-prices:latest
```

**Opción C: construir desde prices/**
```bash
cd prices
docker build -f Containerfile -t prices:latest .
docker run -d --name prices -p 5500:5000 prices:latest
```

### 2. Configuración del servicio products

El archivo `application.yml` configura WebClient para conectar a prices en `http://localhost:5500`. El servicio products usa WebClient con `.block()`, lo que hace la llamada bloqueante.

## Ejecución del Laboratorio

### Paso 1: Verificar el problema de bloqueo

#### 1.1. Navegar al directorio del proyecto

```bash
cd products
```

#### 1.2. Iniciar el servicio products

```bash
mvn spring-boot:run
```

Deberías ver:
```
Started ProductsApplication in X.XXX seconds
```

#### 1.3. Probar el endpoint (bloqueante)

**Linux/macOS/Git Bash:**
```bash
time curl http://localhost:8080/products/1/priceHistory
```

**Windows PowerShell:**
```powershell
Measure-Command { Invoke-WebRequest http://localhost:8080/products/1/priceHistory }
```

Verifica que el request toma alrededor de 2 segundos en finalizar.

#### 1.4. Ejecutar el benchmark

El script `benchmark.sh` envía 10 requests en un segundo, pero toma 3 segundos al ser implementado reactivamente. De no ser así demoraría 20 segundos aprox. (10 x 2seg cada llamada).

**Linux/macOS/Git Bash:**
```bash
time ./benchmark.sh
```

**Windows PowerShell:**
```powershell
.\benchmark.ps1
```

**Resultado esperado:** ~3 segundos (implementación reactiva permite concurrencia)

#### 1.5. Inspeccionar los logs

Verifica en los logs que cada request muestra ~2000 ms de duración. Con 1 worker thread, todos los requests deberían usar el mismo thread (ej. `event-loop-1`).

#### 1.6. Demo de bloqueo con /blocking-bad (opcional)

Para ver el efecto de bloquear el event loop:

1. **Terminal 1:** `curl http://localhost:8080/products/blocking-bad` (bloquea 30 seg)
2. **Terminal 2:** Esperar 2 seg, luego `time ./benchmark.sh`
3. **Resultado esperado:** El benchmark tarda ~32 seg (30 de bloqueo + 2 de priceHistory)

**Nota:** Netty usa 1 event loop thread (`app.netty.worker-count: 1`) mediante `ReactorResourceFactory`. Si ves `LinkError: failed to load the required native library`, usa `-Dreactor.netty.native=false` para forzar NIO. El warning `MacOSDnsServerAddressStreamProvider` en macOS es inofensivo.

### Paso 2: Dependencias (ya incluidas)

Spring WebFlux y WebClient ya están en el `pom.xml` mediante `spring-boot-starter-webflux`. No es necesario agregar dependencias.

### Paso 3: Implementar operaciones no bloqueantes

#### 3.1. Modificar PricesService para retornar Mono

Actualiza `products/src/main/java/com/bcp/training/PricesService.java`:

**Antes (bloqueante):**
```java
public ProductPriceHistory getProductPriceHistory(Long productId) {
    return webClient.get()
            .uri("/history/{productId}", productId)
            .retrieve()
            .bodyToMono(ProductPriceHistory.class)
            .block();  // BLOQUEANTE
}
```

**Después (reactivo):**
```java
public Mono<ProductPriceHistory> getProductPriceHistory(Long productId) {
    return webClient.get()
            .uri("/history/{productId}", productId)
            .retrieve()
            .bodyToMono(ProductPriceHistory.class);
}
```

#### 3.2. Modificar ProductsController para usar Mono

Actualiza `products/src/main/java/com/bcp/training/ProductsController.java`:

**Antes:**
```java
@GetMapping("/{productId}/priceHistory")
public ProductPriceHistory getProductPriceHistory(@PathVariable Long productId) {
    return pricesService.getProductPriceHistory(productId);
}
```

**Después:**
```java
@GetMapping("/{productId}/priceHistory")
public Mono<ProductPriceHistory> getProductPriceHistory(@PathVariable Long productId) {
    return pricesService.getProductPriceHistory(productId);
}
```

#### 3.3. Reiniciar la aplicación

Detén la aplicación (Ctrl+C) y reinicia:

```bash
mvn spring-boot:run
```

#### 3.4. Probar el endpoint asíncrono

**Linux/macOS/Git Bash:**
```bash
curl http://localhost:8080/products/1/priceHistory | jq
```

**Windows PowerShell:**
```powershell
Invoke-RestMethod http://localhost:8080/products/1/priceHistory | ConvertTo-Json -Depth 10
```

Deberías recibir una respuesta válida sin errores.

#### 3.5. Ejecutar el benchmark nuevamente

**Linux/macOS/Git Bash:**
```bash
time ./benchmark.sh
```

**Windows PowerShell:**
```powershell
.\benchmark.ps1
```

**Resultado esperado:** ~3 segundos (6 veces más rápido que la versión bloqueante)

El tiempo de respuesta no-bloqueante procesa los 10 requests en paralelo, completando en ~2-3 segundos en lugar de ~20.

### Paso 4: Manejar operaciones bloqueantes con Schedulers

#### 4.1. Probar el endpoint bloqueante

El endpoint `/products/blocking` simula una operación que bloquea el event loop por 30 segundos:

**Linux/macOS/Git Bash:**
```bash
curl http://localhost:8080/products/blocking; echo
```

**Windows PowerShell:**
```powershell
Invoke-WebRequest http://localhost:8080/products/blocking
```

**Advertencia:** En Spring WebFlux, `Thread.sleep()` en el controller bloquea el hilo de Netty. Puedes ver logs de bloqueo.

#### 4.2. Ejecutar benchmark mientras el endpoint bloqueante está activo

Mientras esperas que el endpoint bloqueante responda, abre una nueva terminal y ejecuta el benchmark:

```bash
time ./benchmark.sh
```

**Resultado:** El benchmark será muy lento porque el event loop está bloqueado.

#### 4.3. Mover la operación bloqueante a un Scheduler

Modifica `ProductsController.java` para ejecutar `Thread.sleep` en un thread separado (equivalente a `@Blocking` en Quarkus):

```java
import reactor.core.scheduler.Schedulers;

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
```

`Schedulers.boundedElastic()` ejecuta el código en un pool de worker threads, evitando bloquear el event loop de Netty.

#### 4.4. Reiniciar y probar

1. Reinicia la aplicación
2. Envía un request al endpoint `/products/blocking`
3. Mientras esperas, ejecuta el benchmark en otra terminal

**Resultado esperado:** El benchmark debería completarse en ~3 segundos, demostrando que el event loop no está bloqueado.

## Conceptos Clave

### Operaciones Bloqueantes vs No Bloqueantes

- **Bloqueante:** Una operación que espera por I/O (red, base de datos, archivos) bloquea el hilo hasta que completa
- **No Bloqueante:** Una operación que no bloquea el hilo, permitiendo que otros requests sean procesados mientras espera

### Spring WebFlux vs Quarkus

| Concepto | Quarkus | Spring Boot 4 |
|----------|---------|---------------|
| REST Client | MicroProfile Rest Client | WebClient |
| Reactivo | Uni | Mono |
| No bloqueante | Retornar Uni | Retornar Mono (sin .block()) |
| Operación bloqueante | @Blocking | subscribeOn(Schedulers.boundedElastic()) |

### Threads en Spring WebFlux

- **Event Loop (Netty):** Maneja operaciones no bloqueantes (I/O asíncrono)
- **boundedElastic():** Pool de worker threads para operaciones bloqueantes

## Resultados Esperados

| Escenario | Tiempo (10 requests) | Comportamiento |
|-----------|----------------------|----------------|
| Bloqueante (inicial, .block()) | ~20 segundos | Requests en serie |
| No Bloqueante (Mono) | ~3 segundos | Requests en paralelo |
| Bloqueante con Schedulers.boundedElastic() | No bloquea event loop | Benchmark ~3 seg |

## Finalización

Para detener la aplicación products, presiona `Ctrl+C` en la terminal.

Para detener el contenedor prices:

**Docker:**
```bash
docker stop prices
docker rm prices
```

**Podman:**
```bash
podman stop prices
podman rm prices
```

**Con docker-compose:**
```bash
docker compose down
# o
podman compose down
```

## Referencias

- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor](https://projectreactor.io/)
- [WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)

---

**Felicitaciones. Has terminado el laboratorio.**

José Díaz
