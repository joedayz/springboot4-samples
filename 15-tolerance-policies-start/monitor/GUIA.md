# LAB 18: SPRING BOOT 4 - POLÍTICAS DE TOLERANCIA A FALLOS

**Autor:** José Díaz

**Github Repo:** https://github.com/joedayz/springboot4-samples.git

## Introducción

Este ejercicio requiere que agregues resiliencia a la aplicación monitor. Esta aplicación es un microservicio que brinda información de instancias cloud, como información del sistema o utilización CPU. La aplicación monitor provee datos invocando a otros microservicios, los cuales, son simulados por simplicidad.

Se utiliza **Resilience4j** con Spring Boot 4 para implementar las políticas de tolerancia a fallos (equivalente a SmallRye Fault Tolerance en Quarkus).

## Prerequisitos

- Java 21 o superior
- Maven 3.8+ instalado (o usar el wrapper `./mvnw`)
- Editor de código favorito
- `curl` instalado (Linux/Mac) o PowerShell (Windows)
- `jq` instalado (opcional, para formatear JSON)

## Paso 1: Abrir el Proyecto

Abre el proyecto `15-tolerance-policies-start` con tu editor favorito.

## Paso 2: Revisar los Endpoints

Revisa los endpoints en `src/main/java/com/bcp/training/MonitorResource.java`. Este controlador REST expone los siguientes endpoints que llaman a otros servicios:

- **`/info`** - Invoca `InfoService` para obtener información del sistema, acerca de la instancia cloud.
- **`/status`** - Invoca `StatusService` para obtener el status de la instancia cloud.
- **`/cpu/stats`** - Invoca `CpuStatsService` para obtener datos de CPU de la instancia cloud.
- **`/cpu/predict`** - Invoca `CpuPredictionService` para predecir el futuro de carga CPU de la instancia cloud.

## Paso 3: Verificar Resilience4j e Iniciar la Aplicación

El proyecto ya incluye la dependencia **Resilience4j** para Spring Boot 3/4. Verifica en `pom.xml` que existan:

- `resilience4j-spring-boot3`
- `spring-boot-starter-aop`

### Iniciar la Aplicación en Modo Desarrollo

#### Linux/Mac:

```bash
cd monitor
./mvnw spring-boot:run
```

#### Windows (PowerShell):

```powershell
cd monitor
.\mvnw.cmd spring-boot:run
```

**Salida esperada:**

```
Started MonitorApplication in X.XXX seconds
```

La aplicación estará disponible en **http://localhost:8080**.

## Paso 4: Usar la Política de Reintentos (Retry)

Hacer que la aplicación sea resiliente a fallas en **InfoService**.

### 4a. Probar el Endpoint sin Resiliencia

Abre una nueva terminal y haz un request al endpoint `/info`. Si `InfoService` aún no tiene la anotación `@Retry`, el endpoint falla.

#### Linux/Mac:

```bash
curl localhost:8080/info; echo
```

#### Windows (PowerShell):

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/info" -Method Get
```

**Salida esperada (sin retry):**

```json
{"details":"Error id ...output omitted..."}
```

### 4b. Inspeccionar InfoService

Inspecciona el archivo `src/main/java/com/bcp/training/sysinfo/InfoService.java`. Solo una de las cinco invocaciones al método `getInfo` son exitosas.

### 4c. Agregar la Anotación @Retry

Agrega la anotación `@Retry` de Resilience4j al método `getInfo`. Usa el nombre de instancia `info` (la configuración estará en `application.properties`).

```java
import io.github.resilience4j.retry.annotation.Retry;

@Retry(name = "info")
public Info getInfo() {
    ...
}
```

### 4d. Configurar Retry en application.properties

En `src/main/resources/application.properties` agrega (si no existe):

```properties
resilience4j.retry.instances.info.maxAttempts=5
```

### 4e. Verificar que Funciona

Re-ejecuta el request y verifica que funciona.

#### Linux/Mac:

```bash
curl localhost:8080/info; echo
```

#### Windows (PowerShell):

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/info" -Method Get
```

**Salida esperada:**

```json
{"NAME":"Linux","ARCH":"amd64","VERSION":"4.18.0-372.32.1.el8_6.x86_64"}
```

### 4f. Revisar los Logs

Inspecciona los logs de la aplicación y verifica que Spring Boot/Resilience4j reintentó los requests varias veces.

**Logs esperados:**

```
ERROR [com.bcp.training.sysinfo.InfoService] (...) Request #1 has failed
ERROR [com.bcp.training.sysinfo.InfoService] (...) Request #2 has failed
ERROR [com.bcp.training.sysinfo.InfoService] (...) Request #3 has failed
ERROR [com.bcp.training.sysinfo.InfoService] (...) Request #4 has failed
INFO [com.bcp.training.sysinfo.InfoService] (...) Request #5 has succeeded
```

## Paso 5: Usar la Política de Timeout

Hacer que la aplicación sea resiliente a delays en **StatusService** usando **TimeLimiter** de Resilience4j. El TimeLimiter requiere que el método retorne `CompletableFuture` para poder interrumpir la ejecución tras el tiempo límite.

### 5a. Probar el Endpoint sin Timeout

Haz un request al endpoint `/status`. El request puede tomar cerca de 5 segundos en completar si no hay timeout configurado.

#### Linux/Mac:

```bash
curl localhost:8080/status; echo
```

#### Windows (PowerShell):

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/status" -Method Get
```

**Salida esperada:**

```
Running
```

### 5b. Revisar los Logs

Inspecciona los logs de la aplicación y verifica que los requests están tomando cerca de 5 segundos en completarse cuando hay delay.

**Logs esperados:**

```
WARN [com.bcp.training.status.StatusService] (...) Request #1 is taking too long...
INFO [com.bcp.training.status.StatusService] (...) Request #1 completed in 5001 milliseconds
```

### 5c. Inspeccionar StatusService

Inspecciona el archivo `src/main/java/com/bcp/training/status/StatusService.java`. Observa dos aspectos:

- El método `getStatus` experimenta delays en 4 de 5 invocaciones.
- El método debe poder ser interrumpido tras un timeout y reintentar (Retry) cuando ocurra la excepción de timeout.

### 5d. Agregar TimeLimiter y Retry

1. **Configuración en `application.properties`:**

```properties
resilience4j.timelimiter.instances.status.timeoutDuration=200ms
resilience4j.retry.instances.status.maxAttempts=5
```

2. **Cambiar el método para que retorne `CompletableFuture<String>`** y anotar con `@TimeLimiter` y `@Retry`:

```java
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@TimeLimiter(name = "status")
@Retry(name = "status")
public CompletableFuture<String> getStatus() {
    return CompletableFuture.supplyAsync(() -> {
        callCount++;
        delayPossibly();
        return "Running";
    });
}
```

3. **Actualizar el controlador** en `MonitorResource.java` para obtener el resultado del futuro:

```java
@GetMapping("/status")
public String getWeatherConditions() {
    return statusService.getStatus().join();
}
```

**Nota:** Si `join()` lanza `CompletionException` por timeout, Resilience4j Retry reintentará según la configuración.

### 5e. Verificar que la Respuesta es Rápida

Re-ejecuta el request y verifica que la respuesta es rápida (tras los reintentos, uno completará en menos de 200 ms).

#### Linux/Mac:

```bash
curl http://localhost:8080/status; echo
```

#### Windows (PowerShell):

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/status" -Method Get
```

**Salida esperada:**

```
Running
```

### 5f. Revisar los Logs de Timeout

Revisa los logs de la aplicación y verifica que Resilience4j ha interrumpido las invocaciones lentas y reintenta.

**Logs esperados (conceptualmente):**

```
WARN [com.bcp.training.status.StatusService] (...) Request #1 is taking too long...
WARN [com.bcp.training.status.StatusService] (...) Request #1 has been interrupted after 200 milliseconds
...
INFO [com.bcp.training.status.StatusService] (...) Request #5 completed in 0 milliseconds
```

## Paso 6: Usar una Política Fallback

Hacer la aplicación resiliente cuando hay data faltante en **CpuStatsService**. En Resilience4j el fallback se implementa usando **@CircuitBreaker** con el atributo `fallbackMethod`.

### 6a. Probar el Endpoint

Haz un request al endpoint `/cpu/stats`. La respuesta contiene uso de CPU en formato time series, media y desviación estándar.

#### Linux/Mac:

```bash
curl -s localhost:8080/cpu/stats | jq
```

#### Windows (PowerShell):

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/cpu/stats" -Method Get | ConvertTo-Json -Depth 10
```

**Salida esperada (éxito):**

```json
{
    "usageTimeSeries": [ ... ],
    "mean": 0.4992622886387308,
    "standardDeviation": 0.319795785721884
}
```

### 6b. Repetir hasta que Ocurra un Error

Repite el request hasta que un error ocurra (cuando la serie contiene null y el cálculo lanza NullPointerException).

**Salida esperada (error):**

```json
{
  "details": "Error id ... NullPointerException",
  "stack": "...output omitted..."
}
```

### 6c. Revisar los Logs

Inspecciona los logs. El error ocurre cuando el método `getCpuStats` llama a `calculateMean` con valores null en la serie.

### 6d. Inspeccionar CpuStatsService

En `src/main/java/com/bcp/training/cpu/CpuStatsService.java`, una de cada tres invocaciones de `getCpuStats` puede fallar porque la data contiene valores null, lo que provoca NullPointerException al calcular media y desviación estándar.

### 6e. Agregar @CircuitBreaker con fallbackMethod

Agrega la anotación `@CircuitBreaker` al método `getCpuStats` con un método fallback. El método fallback debe tener la misma firma que el método original más un parámetro `Throwable` al final.

Para poder devolver la misma serie que causó el error pero con media y desviación 0, guarda la serie en un campo de instancia antes de calcular (así el fallback puede usarla):

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

private List<Double> lastSeries;  // campo de instancia para el fallback

@CircuitBreaker(name = "cpuStats", fallbackMethod = "getCpuStatsWithMissingValues")
public CpuStats getCpuStats() {
    List<Double> series = getCpuUsageTimeSeries();
    lastSeries = series;
    double mean = calculateMean(series);
    double standardDeviation = calculateStandardDeviation(series);
    return new CpuStats(series, mean, standardDeviation);
}

public CpuStats getCpuStatsWithMissingValues(Throwable t) {
    return new CpuStats(lastSeries != null ? lastSeries : List.of(), 0.0, 0.0);
}
```

### 6f. Configuración opcional en application.properties

```properties
resilience4j.circuitbreaker.instances.cpuStats.slidingWindowSize=10
resilience4j.circuitbreaker.instances.cpuStats.minimumNumberOfCalls=5
resilience4j.circuitbreaker.instances.cpuStats.failureRateThreshold=50
```

### 6g. Verificar el Fallback

Repite el request al endpoint `/cpu/stats` hasta recibir una respuesta con valores null. La aplicación debe usar el método fallback y devolver media y desviación estándar en 0.0.

**Salida esperada:**

```json
{
  "usageTimeSeries": [ 0.09..., null, 0.22..., null, ... ],
  "mean": 0.0,
  "standardDeviation": 0.0
}
```

## Paso 7: Usar el Patrón Circuit Breaker

Detener el tráfico que se envía a **CpuPredictionService** cuando este servicio no está disponible.

### 7a. Probar el Endpoint

Haz un request al endpoint `/cpu/predict`. La respuesta es la carga prevista de CPU.

#### Linux/Mac:

```bash
curl localhost:8080/cpu/predict; echo
```

#### Windows (PowerShell):

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/cpu/predict" -Method Get
```

**Salida esperada:**

```
0.9822281195867076
```

### 7b. Ejecutar el Script de Prueba

Ejecuta el script `predict_many.sh` (Linux/macOS) o `predict_many.ps1` (Windows). El script invoca el endpoint `/cpu/predict` cada segundo. El servicio solo acepta un request cada 2 segundos; el resto fallan.

#### Linux/Mac:

```bash
cd monitor
chmod +x predict_many.sh
./predict_many.sh
```

#### Windows (PowerShell):

```powershell
cd monitor
.\predict_many.ps1
```

**Salida esperada:**

```
0.4997873140920043
{"details":"Error id ..."}
{"details":"Error id ..."}
```

### 7c. Detener el Script

Presiona `Ctrl+C` para detener el bucle o script.

### 7d. Revisar CpuPredictionService

Revisa `src/main/java/com/bcp/training/cpu/CpuPredictionService.java`. El servicio solo puede manejar un request cada dos segundos; si no, lanza una excepción.

### 7e. Agregar la Anotación @CircuitBreaker

Agrega la anotación `@CircuitBreaker` al método `predictSystemLoad` con un nombre de instancia (por ejemplo `cpuPredict`) y configura en `application.properties`:

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "cpuPredict")
public Double predictSystemLoad() {
    callCount++;
    crashPossibly();
    return Math.random();
}
```

En `application.properties`:

```properties
resilience4j.circuitbreaker.instances.cpuPredict.slidingWindowSize=10
resilience4j.circuitbreaker.instances.cpuPredict.minimumNumberOfCalls=6
resilience4j.circuitbreaker.instances.cpuPredict.waitDurationInOpenState=3s
resilience4j.circuitbreaker.instances.cpuPredict.failureRateThreshold=50
```

Así, tras 6 llamadas con al menos 50% de fallos, el circuito se abre y permanece abierto 3 segundos. El controlador ya captura `CallNotPermittedException` y devuelve el mensaje "Prediction service is not available at the moment".

### 7f. Ejecutar el Script Nuevamente

Ejecuta de nuevo el bucle o script. Después de varios fallos, el circuit breaker abrirá el circuito y la aplicación responderá: "Prediction service is not available at the moment". Tras unos segundos el circuito se cerrará y volverás a obtener valores válidos.

### 7g. Detener el Script

Presiona `Ctrl+C` para detener el script.

## Paso 8: Detener la Aplicación

En la terminal donde la aplicación está corriendo, presiona `Ctrl+C` para detener Spring Boot.

## Construcción y Ejecución con Docker

Si necesitas construir y ejecutar la aplicación con contenedores:

### Construir la Aplicación

```bash
cd monitor
./mvnw clean package -DskipTests
docker build -t monitor:latest .
```

(Asegúrate de tener un `Dockerfile` en el directorio `monitor` que use la imagen base de Eclipse Temurin o similar y ejecute el JAR generado en `target/*.jar`.)

### Ejecutar el Contenedor

```bash
docker run -i --rm -p 8080:8080 monitor:latest
```

## Resumen

En este laboratorio has implementado las siguientes políticas de tolerancia a fallos con **Spring Boot 4** y **Resilience4j**:

1. **@Retry** - Reintentos automáticos cuando un servicio falla
2. **@TimeLimiter** - Timeout para evitar esperas prolongadas (con `CompletableFuture`)
3. **@CircuitBreaker con fallbackMethod** - Método alternativo cuando el método principal falla
4. **@CircuitBreaker** - Protección contra sobrecarga de servicios no disponibles

Estas políticas hacen que tu aplicación sea más resiliente y capaz de manejar fallos de manera elegante.

---

**¡Esto concluye el laboratorio!**

**Enjoy!**

**José**
