# Laboratorio: Revisión de Pruebas

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 07-test-review-start

## Instrucciones

Está probando una aplicación basada en microservicios que implementa un sistema de gestión de conferencias. La aplicación consta de tres microservicios:

1. **Microservicio `schedule`:**
   - **Función:** Gestiona los horarios de las conferencias.
   - **Base de datos:** Almacena datos en una base de datos H2 en memoria.
   - **Razón del fallo inicial de las pruebas:** Las pruebas fallan inicialmente porque el endpoint HTTP de prueba no está configurado y el H2 no está funcionando correctamente en tests.

2. **Microservicio `speaker`:**
   - **Función:** Gestiona los oradores de las conferencias.
   - **Base de datos:** Almacena datos en una base de datos H2 en memoria.
   - **Inicialización:** Cuando el servicio se inicia, Spring Boot pobla la base de datos con datos de prueba.
   - **Razón del fallo inicial de las pruebas:** Las pruebas fallan inicialmente debido a una dependencia faltante y un escenario de prueba que requiere que la base de datos devuelva una lista vacía de oradores.

3. **Microservicio `session`:**
   - **Función:** Gestiona las sesiones de las conferencias.
   - **Base de datos:** Almacena datos en una base de datos PostgreSQL.
   - **Dependencias:** Este servicio depende del servicio `speaker` para obtener información de los oradores.
   - **Razón del fallo inicial de las pruebas:** Las pruebas fallan inicialmente porque no puede encontrar la imagen del contenedor PostgreSQL y el servicio `speaker` no es accesible.

**Objetivo Final:** Debe hacer que las pruebas pasen en cada uno de los tres servicios.

---

## Paso 1: Abrir el proyecto schedule y corregir la clase ScheduleResourceTest

### Instrucciones:
- Convierta las pruebas de esta clase en pruebas Spring Boot.
- Haga que las pruebas utilicen la URL base del ScheduleController.

### 1.1. Navegue al directorio del servicio schedule.

```bash
cd schedule
```

```powershell
cd schedule
```

### 1.2. Verifique que cuatro pruebas estén fallando.

```bash
mvn test
```

```powershell
mvn test
```

### 1.3. Reemplace el contenido completo de `src/test/java/com/bcp/training/conference/ScheduleResourceTest.java`:

```java
package com.bcp.training.conference;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScheduleResourceTest {

    private static final int GIVEN_ID = 101;
    private static final int GIVEN_VENUE_ID = 101;

    @LocalServerPort
    int port;

    @Autowired
    ScheduleRepository scheduleRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/schedule";
    }

    @Test
    public void testRetrieve() {
        given()
                .when()
                .get("/" + GIVEN_ID)
                .then()
                .statusCode(200)
                .body("id", equalTo(GIVEN_ID))
                .body("venueId", equalTo(GIVEN_VENUE_ID));
    }

    @Test
    public void testAdd() {
        given()
                .when()
                .body("{\"venueId\":1010,\"date\":\"2020-03-20\"}")
                .contentType(ContentType.JSON)
                .post()
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("venueId", equalTo(1010));
    }

    @Test
    public void testAllSchedules() {
        long count = scheduleRepository.count();
        List<Schedule> schedules = given()
                .when()
                .get("/all")
                .thenReturn().as(List.class);
        assertThat(schedules, hasSize((int) count));
    }

    @Test
    public void testRetrieveByVenue() {
        long count = scheduleRepository.findByVenueId(101).size();
        List<Schedule> scheds = given().when()
                .get("/venue/101")
                .thenReturn().as(List.class);
        assertThat(scheds, hasSize((int) count));
    }
}
```

### 1.4. Ejecute las pruebas. Deberían fallar por error de conexión a la base de datos (URL H2 externa).

---

## Paso 2: Corregir la configuración de la base de datos para tests

### Instrucción:
Modifique `src/main/resources/application.yml` para que la URL H2 externa solo se use en producción.

### 2.1. Reemplace el contenido completo de `schedule/src/main/resources/application.yml`:

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:h2:mem:schedules
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    defer-datasource-initialization: true

---
# Prod profile: use external H2
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:h2:tcp://localhost/~/schedules
```

### 2.2. Ejecute las pruebas. Deberían pasar.

```bash
mvn test
```

```powershell
mvn test
```

---

## Paso 3: Cambie al servicio speaker e inyecte la dependencia DeterministicIdGenerator

### 3.1. Navegue al directorio speaker.

```bash
cd ../speaker
```

```powershell
cd ../speaker
```

### 3.2. Cree el archivo `src/test/java/com/bcp/training/speaker/SpeakerTestConfig.java`:

```java
package com.bcp.training.speaker;

import com.bcp.training.speaker.idgenerator.IdGenerator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SpeakerTestConfig {

    @Bean
    @Primary
    public IdGenerator idGenerator() {
        return new DeterministicIdGenerator();
    }
}
```

### 3.3. Reemplace el contenido completo de `src/test/java/com/bcp/training/speaker/SpeakerResourceTest.java`:

```java
package com.bcp.training.speaker;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import com.bcp.training.speaker.idgenerator.IdGenerator;

import java.util.Collections;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SpeakerTestConfig.class)
public class SpeakerResourceTest {

    @LocalServerPort
    int port;

    @MockitoBean
    SpeakerRepository speakerRepository;

    @Autowired
    IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testNewSpeaker() {
        when(speakerRepository.save(any(Speaker.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID uuid = new UUID(1, 1);
        ((DeterministicIdGenerator) idGenerator).setNextUUID(uuid);

        given()
                .body("{\"nameFirst\": \"Jordi\",\"nameLast\": \"Sola\"}")
                .contentType(ContentType.JSON)
                .when()
                .post("/speaker")
                .then()
                .statusCode(200)
                .body("nameFirst", is("Jordi"))
                .body("nameLast", is("Sola"))
                .body("uuid", is(uuid.toString()));
    }

    @Test
    public void testListEmptySpeakers() {
        when(speakerRepository.findAll()).thenReturn(Collections.emptyList());

        given()
                .when()
                .get("/speaker")
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }
}
```

### 3.4. Ejecute las pruebas. Ambas deberían pasar.

```bash
mvn test
```

```powershell
mvn test
```

---

## Paso 4: Abra el microservicio session y corrija la imagen de PostgreSQL

### 4.1. Navegue al directorio session.

```bash
cd ../session
```

```powershell
cd ../session
```

### 4.2. Modifique `src/test/java/com/bcp/training/conference/session/PostgresTestConfig.java`:

Cambie la línea de la imagen de `invalid/postgresql/image` a `postgres:14.1`:

```java
package com.bcp.training.conference.session;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class PostgresTestConfig {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:14.1"))
            .withDatabaseName("sessions")
            .withUsername("postgres")
            .withPassword("postgres");

    static {
        postgres.start();
    }
}
```

### 4.3. Ejecute las pruebas. `testCreateSession` debería pasar; `testGetSessionWithSpeaker` fallará porque el SpeakerService no es accesible.

---

## Paso 5: Mockee el SpeakerService en testGetSessionWithSpeaker

### 5.1. Reemplace el contenido completo de `src/test/java/com/bcp/training/conference/session/SessionResourceTest.java`:

```java
package com.bcp.training.conference.session;

import com.bcp.training.conference.speaker.Speaker;
import com.bcp.training.conference.speaker.SpeakerService;
import io.restassured.RestAssured;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfig.class)
public class SessionResourceTest {

    @LocalServerPort
    int port;

    @MockitoBean
    SpeakerService speakerService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresTestConfig.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresTestConfig.postgres::getUsername);
        registry.add("spring.datasource.password", PostgresTestConfig.postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testCreateSession() {
        given()
                .contentType("application/json")
                .and()
                .body(sessionWithSpeakerId(12))
                .when()
                .post("/sessions")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("speakerId", equalTo(12));
    }

    @Test
    public void testGetSessionWithSpeaker() {
        int speakerId = 12;

        Mockito.when(speakerService.getById(Mockito.anyInt()))
                .thenReturn(new Speaker(speakerId, "Pablo", "Solar"));

        given()
                .contentType("application/json")
                .and()
                .body(sessionWithSpeakerId(speakerId))
                .post("/sessions");

        when()
                .get("/sessions/1")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("speaker.firstName", equalTo("Pablo"));
    }

    private Session sessionWithSpeakerId(int speakerId) {
        Session session = new Session();
        session.speakerId = speakerId;
        return session;
    }
}
```

### 5.2. Ejecute las pruebas. Todas deberían pasar.

```bash
mvn test
```

```powershell
mvn test
```

**Nota:** El módulo session requiere Docker o Podman en ejecución. Ver `README.md` para configuración por sistema operativo.

---

## Conclusión

¡Enjoy!

**José**
