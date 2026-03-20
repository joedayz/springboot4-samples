# LAB 20: SPRING BOOT 4 TOLERANCE REVIEW

**Autor:** José Díaz  
**Github Repo:** https://github.com/joedayz/springboot4-samples

## Descripción del Proyecto

Este laboratorio utiliza dos servicios:

### session
Un servicio que mantiene una lista de sesiones de speaking. Contiene un caché local de speakers en cada sesión y enriquece la información de cada speaker usando el servicio **speaker**.

Los speakers del servicio tienen first name y surname. Los speakers cacheados solo tienen el first name.

### speaker
Un servicio que mantiene el registro completo de speakers. Puedes usarlo para probar opcionalmente el servicio session.

## Objetivo

Completar el laboratorio asegurando que el servicio **session** pase todas las pruebas.

## Requisitos Previos

- Java 21 o superior
- Maven 3.8+ o el wrapper incluido (`./mvnw` o `.\mvnw.cmd`)
- Acceso al proyecto `17-tolerance-review-start`

---

## Tarea 1: Agregar Liveness y Readiness Probes

### Objetivo
Exponer liveness y readiness del microservicio session usando Spring Boot Actuator.

Deben devolver:
- **Liveness:** estado UP con detalle `"message": "Service is alive"`
- **Readiness:** estado UP con detalle `"message": "Service is ready"`

### Pasos

#### 1.1 Implementar LivenessIndicator

Crea o abre `session/src/main/java/com/bcp/training/conference/session/LivenessIndicator.java` e implementa `HealthIndicator`. El indicador debe devolver `Health.up()` con un detalle `"message", "Service is alive"`.

**Código a implementar:**

```java
package com.bcp.training.conference.session;


import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("serviceIsAlive")
public class LivenessIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().withDetail("message", "Service is alive").build();
    }
}
```

#### 1.2 Implementar ReadinessIndicator

Crea o abre `session/src/main/java/com/bcp/training/conference/session/ReadinessIndicator.java` e implementa `HealthIndicator` con detalle `"message", "Service is ready"`.

**Código a implementar:**

```java
package com.bcp.training.conference.session;


import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("serviceIsReady")
public class ReadinessIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().withDetail("message", "Service is ready").build();
    }
}
```

#### 1.3 Configurar grupos de health

En `session/src/main/resources/application.properties` asegura que los probes estén habilitados y que los indicadores formen parte de los grupos:

```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.probes.enabled=true
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true
management.health.liveness.include=livenessState,serviceIsAlive
management.health.readiness.include=readinessState,serviceIsReady,db
```

#### 1.4 Verificar las pruebas

Desde el directorio **session**:

**Windows:**
```powershell
.\mvnw.cmd clean test -Dtest=SessionControllerTest#testLivenessProbe,SessionControllerTest#testReadinessProbe
```

**Linux/Mac:**
```bash
./mvnw clean test -Dtest=SessionControllerTest#testLivenessProbe,SessionControllerTest#testReadinessProbe
```

```powershell
.\mvnw clean test -Dtest=SessionControllerTest#testLivenessProbe,SessionControllerTest#testReadinessProbe
```

**Resultado esperado:**
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

---

## Tarea 2: Implementar Fallback para GET /sessions

### Objetivo
El endpoint `GET /sessions` del servicio session llama al servicio speaker para enriquecer los datos. Debes implementar un fallback que use `SessionStore#findAllWithoutEnrichment` cuando el servicio speaker no esté disponible.

### Pasos

#### 2.1 Implementar allSessionsFallback

En `session/src/main/java/com/bcp/training/conference/session/SessionController.java` añade un método fallback que devuelva las sesiones sin enriquecer:

```java
public Collection<Session> allSessionsFallback(Exception ex) {
    log.warn("Fallback for GET /sessions", ex);
    return sessionStore.findAllWithoutEnrichment();
}
```

El fallback debe tener la misma firma que `allSessions` más un último parámetro `Exception` (o `Throwable`).

#### 2.2 Configurar Fallback en allSessions

Usa la anotación de Resilience4j `@CircuitBreaker` con `fallbackMethod` para que, en caso de fallo, se llame a `allSessionsFallback`. Puedes usar un circuit breaker por nombre (por ejemplo `"sessions"`) y configurarlo en `application.properties` si lo deseas.

**Código actualizado:**

```java
@GetMapping
@CircuitBreaker(name = "sessions", fallbackMethod = "allSessionsFallback")
public Collection<Session> allSessions() {
    return sessionStore.findAll();
}
```

Añade el import: `import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;`

#### 2.3 Verificar la prueba

**Windows:**
```powershell
.\mvnw.cmd clean test -Dtest=SessionControllerTest#testAllSessionsFallback
```

**Linux/Mac:**
```bash
./mvnw clean test -Dtest=SessionControllerTest#testAllSessionsFallback
```

```powershell
.\mvnw clean test -Dtest=SessionControllerTest#testAllSessionsFallback
```

---

## Tarea 3: Implementar política de reintento

### Objetivo
El endpoint `PUT /sessions/{sessionId}/speakers/{speakerName}` debe completarse aunque el almacén falle varias veces. Implementa una política de reintento: hasta 60 intentos con 1 segundo de espera entre intentos cuando se lance una excepción de tipo 500 (por ejemplo `ResponseStatusException` con `INTERNAL_SERVER_ERROR`).

### Pasos

#### 3.1 Añadir anotación @Retry

En `SessionController`, anota el método del endpoint con `@Retry` de Resilience4j. Usa `maxAttempts` y `delay` (o la configuración equivalente en `application.properties`) para reintentar una vez por segundo hasta 60 veces ante errores 500.

**Ejemplo con anotación:**

```java
@PutMapping("/{sessionId}/speakers/{speakerName}")
@Retry(name = "addSpeaker", fallbackMethod = "addSessionSpeakerFallback")
@Transactional
public Session addSessionSpeaker(@PathVariable String sessionId, @PathVariable String speakerName) {
    Session session = sessionStore.findByIdWithoutEnrichmentMaybeFail(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    sessionStore.addSpeakerToSession(speakerName, session);
    return sessionStore.findByIdWithoutEnrichment(sessionId).orElseThrow();
}
```

O configura el retry en `application.properties`:

```properties
resilience4j.retry.instances.addSpeaker.maxAttempts=60
resilience4j.retry.instances.addSpeaker.waitDuration=1s
resilience4j.retry.instances.addSpeaker.retryExceptions=org.springframework.web.server.ResponseStatusException
```

Si usas fallback, el método fallback solo debe usarse cuando se agoten los reintentos; si prefieres que no haya fallback y solo reintentar hasta 60 veces, no declares `fallbackMethod`.

#### 3.2 Verificar la prueba

**Windows:**
```powershell
.\mvnw.cmd clean test -Dtest=SessionControllerTest#testAddSpeakerToSession
```

**Linux/Mac:**
```bash
./mvnw clean test -Dtest=SessionControllerTest#testAddSpeakerToSession
```

```powershell
.\mvnw clean test -Dtest=SessionControllerTest#testAddSpeakerToSession
```

---

## Tarea 4: Circuit Breaker para GET /sessions/{sessionId}

### Objetivo
El endpoint `GET /sessions/{sessionId}` usa el servicio speaker para enriquecer los datos. Implementa un fallback que use `SessionStore#findByIdWithoutEnrichment` y un circuit breaker que, tras 2 fallos consecutivos, use el fallback durante 30 segundos.

### Pasos

#### 4.1 Implementar retrieveSessionFallback

Añade en `SessionController`:

```java
public Session retrieveSessionFallback(String sessionId, Exception ex) {
    log.warn("Fallback for GET /sessions/{}", sessionId, ex);
    return sessionStore.findByIdWithoutEnrichment(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
}
```

(La firma debe coincidir con `retrieveSession` más el parámetro `Exception`.)

#### 4.2 Configurar Circuit Breaker y Fallback

Anota el método `retrieveSession` con `@CircuitBreaker` y `fallbackMethod`. Configura el circuit breaker para que se abra tras 2 fallos y permanezca abierto 30 segundos (por ejemplo en `application.properties`):

```properties
resilience4j.circuitbreaker.instances.sessionDetail.slidingWindowSize=2
resilience4j.circuitbreaker.instances.sessionDetail.failureRateThreshold=100
resilience4j.circuitbreaker.instances.sessionDetail.waitDurationInOpenState=30s
```

**Código actualizado:**

```java
@GetMapping("/{sessionId}")
@CircuitBreaker(name = "sessionDetail", fallbackMethod = "retrieveSessionFallback")
public Session retrieveSession(@PathVariable String sessionId) {
    return sessionStore.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
}
```

#### 4.3 Verificar la prueba

**Windows:**
```powershell
.\mvnw.cmd clean test -Dtest=SessionControllerTest#testSessionCircuitBreaker
```

**Linux/Mac:**
```bash
./mvnw clean test -Dtest=SessionControllerTest#testSessionCircuitBreaker
```

```powershell
.\mvnw clean test -Dtest=SessionControllerTest#testSessionCircuitBreaker
```

---

## Tarea 5: Timeout para GET /sessions/{sessionId}/speakers

### Objetivo
El endpoint `GET /sessions/{sessionId}/speakers` debe responder en como máximo 1 segundo. Si la llamada al servicio speaker tarda más, debe usarse un fallback (sesión sin enriquecer).

### Pasos

#### 5.1 Añadir TimeLimiter y fallback

Resilience4j ofrece `@TimeLimiter`. **Importante:** `@TimeLimiter` solo funciona con métodos que devuelven `CompletableFuture` o `CompletionStage`. Crea un método que devuelva `CompletableFuture.supplyAsync(() -> sessionStore.findById(sessionId))` y anótalo con `@TimeLimiter` de 1 segundo y un `fallbackMethod` que devuelva `CompletableFuture.completedFuture(sessionStore.findByIdWithoutEnrichment(sessionId))`.

Ejemplo en el controller:

```java
@GetMapping("/{sessionId}/speakers")
public Set<Speaker> sessionSpeakers(@PathVariable String sessionId) {
    Optional<Session> session = findSessionSpeakers(sessionId).join();
    return session.map(Session::getSpeakers)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
}

@TimeLimiter(name = "sessionSpeakers", fallbackMethod = "findSessionSpeakersFallback")
public CompletableFuture<Optional<Session>> findSessionSpeakers(String sessionId) {
    return CompletableFuture.supplyAsync(() -> sessionStore.findById(sessionId));
}

public CompletableFuture<Optional<Session>> findSessionSpeakersFallback(String sessionId, Exception ex) {
    log.warn("Fallback for GET /sessions/{}/speakers", sessionId, ex);
    return CompletableFuture.completedFuture(sessionStore.findByIdWithoutEnrichment(sessionId));
}
```

Configura el time limiter en `application.properties`:

```properties
resilience4j.timelimiter.instances.sessionSpeakers.timeoutDuration=1s
```

Imports: `import io.github.resilience4j.timelimiter.annotation.TimeLimiter;` y `import java.util.concurrent.CompletableFuture;`

#### 5.2 Verificar la prueba

**Windows:**
```powershell
.\mvnw.cmd clean test -Dtest=SessionControllerTest#testSessionSpeakerFallback
```

**Linux/Mac:**
```bash
./mvnw clean test -Dtest=SessionControllerTest#testSessionSpeakerFallback
```

```powershell
.\mvnw clean test -Dtest=SessionControllerTest#testSessionSpeakerFallback
```

---

## Verificación final

Desde el directorio **session**:

**Windows:**
```powershell
.\mvnw.cmd clean test
```

**Linux/Mac:**
```bash
./mvnw clean test
```

```powershell
.\mvnw clean test
```

**Resultado esperado:** Todas las pruebas pasan (por ejemplo 6 tests, 0 fallos).

---

## Ejecutar la aplicación

### Modo desarrollo

1. Arrancar **speaker** (primero):
   ```bash
   cd speaker && ./mvnw spring-boot:run
   ```

2. Arrancar **session** (en otra terminal):
   ```bash
   cd session && ./mvnw spring-boot:run
   ```

Session estará en `http://localhost:8081`, speaker en `http://localhost:8082`.

### Health checks

- **Liveness:** http://localhost:8081/actuator/health/liveness  
- **Readiness:** http://localhost:8081/actuator/health/readiness  

### Endpoints session

- `GET /sessions` - Lista todas las sesiones  
- `GET /sessions/{sessionId}` - Obtiene una sesión  
- `GET /sessions/{sessionId}/speakers` - Speakers de la sesión  
- `POST /sessions` - Crea sesión  
- `PUT /sessions/{sessionId}` - Actualiza sesión  
- `PUT /sessions/{sessionId}/speakers/{speakerName}` - Añade speaker  
- `DELETE /sessions/{sessionId}` - Elimina sesión  
- `DELETE /sessions/{sessionId}/speakers/{speakerName}` - Quita speaker  

---

## Resumen de conceptos (Spring Boot / Resilience4j)

### Health (Actuator)
- Indicadores personalizados que implementan `HealthIndicator`.
- Grupos `liveness` y `readiness` para Kubernetes.

### @CircuitBreaker (Resilience4j)
- Abre el circuito tras N fallos y usa `fallbackMethod` mientras está abierto.
- Parámetros típicos: `slidingWindowSize`, `failureRateThreshold`, `waitDurationInOpenState`.

### @Retry (Resilience4j)
- Reintentos con `maxAttempts`, `waitDuration` y `retryExceptions`.

### @TimeLimiter (Resilience4j)
- Timeout máximo de ejecución; opcionalmente `fallbackMethod` si se supera.

---

## Referencias

- [Spring Boot Actuator – Health](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)
- [Resilience4j with Spring Boot](https://resilience4j.readthedocs.io/en/latest/springboot3.html)
- [Spring Boot 4 & Resilience4j](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.resilience4j)

---

¡Disfruta del laboratorio!

José Díaz
