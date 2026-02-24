# Laboratorio: Revisión de Pruebas — Solución de Referencia

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 07-test-review-solution

Este documento contiene el **código completo** de la solución para cada archivo modificado.

---

## 1. Schedule — ScheduleResourceTest.java

**Ruta:** `schedule/src/test/java/com/bcp/training/conference/ScheduleResourceTest.java`

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

---

## 2. Schedule — application.yml

**Ruta:** `schedule/src/main/resources/application.yml`

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

---

## 3. Speaker — SpeakerTestConfig.java (nuevo archivo)

**Ruta:** `speaker/src/test/java/com/bcp/training/speaker/SpeakerTestConfig.java`

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

---

## 4. Speaker — SpeakerResourceTest.java

**Ruta:** `speaker/src/test/java/com/bcp/training/speaker/SpeakerResourceTest.java`

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

---

## 5. Session — PostgresTestConfig.java

**Ruta:** `session/src/test/java/com/bcp/training/conference/session/PostgresTestConfig.java`

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

---

## 6. Session — SessionResourceTest.java

**Ruta:** `session/src/test/java/com/bcp/training/conference/session/SessionResourceTest.java`

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

---

## Resumen de cambios

| Módulo   | Archivo              | Cambio principal                                                                 |
|----------|----------------------|-----------------------------------------------------------------------------------|
| schedule | ScheduleResourceTest | `@SpringBootTest`, `@LocalServerPort`, `RestAssured.basePath`, `ScheduleRepository` |
| schedule | application.yml      | H2 en memoria por defecto, URL externa solo en perfil `prod`                      |
| speaker  | SpeakerTestConfig    | Nuevo: bean `IdGenerator` para tests                                             |
| speaker  | SpeakerResourceTest  | `@Import(SpeakerTestConfig)`, `@MockitoBean` repository, mock de `findAll()`       |
| session  | PostgresTestConfig   | Imagen `postgres:14.1` en lugar de `invalid/postgresql/image`                     |
| session  | SessionResourceTest  | `@MockitoBean SpeakerService`, mock de `getById()` en `testGetSessionWithSpeaker` |

---

## Ejecutar todas las pruebas

```bash
cd speaker && mvn test
cd ../schedule && mvn test
cd ../session && mvn test
```

El módulo `session` requiere Docker o Podman. Ver `README.md` para la configuración por sistema operativo.

---

**José**
