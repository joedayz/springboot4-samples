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
- Spring Boot 4.0.3 (el proyecto usa esta versión por compatibilidad con dependencias)

## ⚠️ Consideraciones Importantes para Spring Boot 4

### Cambios de API en Spring Boot 4

1. **Health Indicators**: El paquete correcto es `org.springframework.boot.health.contributor.*` (NO `org.springframework.boot.actuate.health.*`)

2. **Testing sin @MockBean**: Spring Boot 4 eliminó las anotaciones `@MockBean` y `@AutoConfigureMockMvc`. La solución recomendada es:
   - Usar `@Profile("!test")` en beans reales que no quieres en tests
   - Crear `@TestConfiguration` con mocks de Mockito
   - Usar `@ActiveProfiles("test")` en tus tests
   - Configurar MockMvc manualmente con `MockMvcBuilders.webAppContextSetup()`

3. **Configuración de Health Endpoints**: Es necesario agregar propiedades adicionales para que los detalles de los componentes de health sean visibles en los tests

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

**⚠️ IMPORTANTE:** En Spring Boot 4, el paquete correcto es `org.springframework.boot.health.contributor.*`

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

**⚠️ IMPORTANTE:** En Spring Boot 4, el paquete correcto es `org.springframework.boot.health.contributor.*`

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

**⚠️ IMPORTANTE:** Las propiedades `show-details` y `show-components` son necesarias para que los tests puedan verificar los componentes de health.

```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.probes.enabled=true
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true
management.health.liveness.include=livenessState,serviceIsAlive
management.health.readiness.include=readinessState,serviceIsReady,db

# Necesario para que los tests puedan verificar los detalles de los componentes
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always
```

#### 1.4 Configurar SpeakerServiceClient para tests

**⚠️ CRÍTICO:** Spring Boot 4 no incluye `@MockBean`. La solución es excluir el bean real de los tests usando `@Profile`.

En `session/src/main/java/com/bcp/training/conference/session/SpeakerServiceClient.java`, agrega la anotación `@Profile("!test")` para excluir este bean cuando el perfil "test" esté activo:

```java
import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")  // Excluir del contexto cuando el perfil "test" está activo
public class SpeakerServiceClient {
    // ... resto del código
}
```

#### 1.5 Configurar los tests

Los tests en `SessionControllerTest` deben seguir este patrón para Spring Boot 4:

```java
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@ActiveProfiles("test")  // Activa el perfil test para excluir SpeakerServiceClient real
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SpeakerServiceClient speakerServiceClient() {
            return Mockito.mock(SpeakerServiceClient.class);  // Mock manual con Mockito
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @Autowired
    private SpeakerServiceClient speakerServiceClient;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Mockito.reset(speakerServiceClient);
    }
    
    // ... resto de los tests
}
```

#### 1.6 Verificar las pruebas

Desde el directorio **session**:

**Windows:**
```powershell
.\mvnw.cmd clean test -Dtest=SessionControllerTest#testLivenessProbe,SessionControllerTest#testReadinessProbe
```

**Linux/Mac:**
```bash
./mvnw clean test -Dtest=SessionControllerTest#testLivenessProbe,SessionControllerTest#testReadinessProbe
```

**Resultado esperado:**
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

**Nota sobre los tests:** Los tests verifican dos cosas:
1. Que `/actuator/health/liveness` y `/actuator/health/readiness` devuelven `{"status":"UP"}`
2. Que el endpoint principal `/actuator/health` expone los componentes con sus detalles (por eso necesitamos `show-details` y `show-components`)

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

**Resultado esperado:** Todas las pruebas pasan (por ejemplo 6 tests, 0 fallos).

```
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Notas Importantes sobre los Tests

### Datos de Prueba

Los tests usan datos pre-cargados desde `data.sql`. Los speakers en la base de datos son:
- Emmanuel (uuid: s-1-1)
- Clement (uuid: s-1-2)
- Alex (uuid: s-1-3)
- Burr (uuid: s-1-4)

**⚠️ IMPORTANTE:** Los mocks en los tests deben usar nombres que coincidan con estos datos. Por ejemplo:

```java
// CORRECTO: Mock que coincide con el speaker de la BD
SpeakerFromService s = new SpeakerFromService("s-1-1", "Emmanuel", "");

// INCORRECTO: Mock con nombre diferente causará fallos en assertions
SpeakerFromService s = new SpeakerFromService("s-1-1", "First", "Last");
```

### Método enrichFromService

El método `Speaker.enrichFromService()` combina `nameFirst + " " + nameLast`. **Asegúrate de usar `.trim()`** para eliminar espacios extra cuando `nameLast` está vacío:

```java
public static void enrichFromService(SpeakerFromService dto, Speaker speaker) {
    speaker.setName((dto.getNameFirst() + " " + dto.getNameLast()).trim());
    speaker.setUuid(dto.getUuid());
}
```

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
- Indicadores personalizados que implementan `HealthIndicator` del paquete `org.springframework.boot.health.contributor.*`
- Grupos `liveness` y `readiness` para Kubernetes
- En Spring Boot 4, necesitas configurar `show-details` y `show-components` explícitamente para exponer detalles de los componentes

### @CircuitBreaker (Resilience4j)
- Abre el circuito tras N fallos y usa `fallbackMethod` mientras está abierto
- Parámetros típicos: `slidingWindowSize`, `failureRateThreshold`, `waitDurationInOpenState`
- El método fallback debe tener la misma firma que el método original más un parámetro `Exception`

### @Retry (Resilience4j)
- Reintentos con `maxAttempts`, `waitDuration` y `retryExceptions`
- Puedes configurarlo con anotaciones o en `application.properties`

### @TimeLimiter (Resilience4j)
- Timeout máximo de ejecución; opcionalmente `fallbackMethod` si se supera
- **Importante:** Solo funciona con métodos que devuelven `CompletableFuture` o `CompletionStage`

### Testing en Spring Boot 4
- **No existe `@MockBean`**: Usa `@Profile("!test")` + `@TestConfiguration` con mocks de Mockito
- **No existe `@AutoConfigureMockMvc`**: Configura `MockMvc` manualmente con `MockMvcBuilders.webAppContextSetup()`
- Usa `@ActiveProfiles("test")` para activar el perfil de test

---

## Troubleshooting

### Error: "cannot find symbol class Health" o "package org.springframework.boot.actuate.health does not exist"

**Solución:** Estás usando el import incorrecto. En Spring Boot 4, usa:
```java
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
```

### Error: "Failed to load ApplicationContext" en tests

**Solución:** 
1. Verifica que `SpeakerServiceClient` tenga `@Profile("!test")`
2. Verifica que tu test tenga `@ActiveProfiles("test")`
3. Verifica que tengas un `@TestConfiguration` con el mock

### Tests de health fallan con "JSON path no encontrado"

**Solución:** Agrega estas propiedades en `application.properties`:
```properties
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always
```

### Assertion falla: expected "Emmanuel" but was "First Last"

**Solución:** Los mocks deben usar nombres que coincidan con los datos pre-cargados en `data.sql`. Usa:
```java
new SpeakerFromService("s-1-1", "Emmanuel", "")
```

## Referencias

- [Spring Boot 4 Actuator – Health](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)
- [Resilience4j with Spring Boot](https://resilience4j.readthedocs.io/en/latest/springboot3.html)
- [Spring Boot 4 Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

## Cambios en Spring Boot 4

Este laboratorio documenta cambios importantes en Spring Boot 4:

1. **Paquete Health**: En Spring Boot 4.0.x el paquete correcto es `boot.health.contributor.*` (diferente a versiones anteriores)
2. **Testing**: Eliminadas `@MockBean` y `@AutoConfigureMockMvc`, requiere configuración manual
3. **Health Details**: Requiere configuración explícita de `show-details` y `show-components`
4. **Versión**: Se recomienda usar Spring Boot 4.0.3 o superior por mejoras en resolución de dependencias

---

¡Disfruta del laboratorio!

José Díaz  
GitHub: [@joedayz](https://github.com/joedayz)
