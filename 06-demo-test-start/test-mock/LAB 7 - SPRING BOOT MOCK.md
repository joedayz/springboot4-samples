# LAB 7 - SPRING BOOT 4 MOCK

## Objetivos

- Comprender las técnicas de mocking en Spring Boot 4
- Usar **@MockitoBean** para reemplazar beans en el contexto
- Usar **@MockitoSpyBean** para espiar beans reales
- Usar **@HttpExchange** para clients REST declarativos (moderno Spring Boot)
- Mockear repositorios, servicios y clientes REST

## Conceptos Clave

| Quarkus | Spring Boot 4 |
|---------|--------------|
| `@InjectMock` | `@MockitoBean` (nuevo en Spring Boot 4) |
| `@InjectSpy` | `@MockitoSpyBean` (nuevo en Spring Boot 4) |
| `PanacheMock.mock()` | `@MockitoBean` en el Repository |
| `@Mock` (CDI alternative) | `@MockitoBean` |
| `@RegisterRestClient` | `@HttpExchange` + `RestClient` |
| Quarkus REST Client | Spring `HttpServiceProxyFactory` |

> **Importante:** Spring Boot 4 introduce `@MockitoBean` y `@MockitoSpyBean` como reemplazo de los deprecados `@MockBean` y `@SpyBean`. Están en el paquete `org.springframework.test.context.bean.override.mockito`.

## Estructura del Proyecto

```
test-mock/
├── pom.xml
├── src/main/java/com/bcp/training/expenses/
│   ├── TestMockApplication.java
│   ├── Expense.java                    (JPA Entity)
│   ├── ExpenseRepository.java          (Spring Data JPA)
│   ├── ExpenseService.java             (@Service)
│   ├── ExpenseNotFoundException.java   (Exception)
│   ├── ExpenseController.java          (@RestController)
│   ├── FraudScore.java                 (Record DTO)
│   ├── FraudScoreClient.java           (@HttpExchange interface)
│   └── FraudScoreClientConfig.java     (RestClient config)
├── src/main/resources/
│   └── application.yml
└── src/test/java/com/bcp/training/expenses/
    ├── CrudTest.java                   (Integration CRUD tests)
    ├── RepositoryMockTest.java         (@MockitoBean en Repository)
    ├── RestClientMockTest.java         (@MockitoBean en FraudScoreClient)
    ├── ServiceMockTest.java            (@MockitoBean en ExpenseService)
    └── SpyTest.java                    (@MockitoSpyBean en ExpenseService)
```

---

## Paso 1: `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.2</version>
        <relativePath/>
    </parent>

    <groupId>com.bcp.training</groupId>
    <artifactId>test-mock</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>test-mock</name>
    <description>Mocking demo with Spring Boot 4</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.8.6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Paso 2: `application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

fraud-score:
  base-url: http://localhost:9080
```

---

## Paso 3: `TestMockApplication.java`

```java
package com.bcp.training.expenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestMockApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestMockApplication.class, args);
    }
}
```

---

## Paso 4: `Expense.java` - Entidad JPA

```java
package com.bcp.training.expenses;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Expense {

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uuid;
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private double amount;

    public Expense() {
    }

    public Expense(UUID uuid, String name, LocalDateTime creationDate, PaymentMethod paymentMethod, double amount) {
        this.uuid = uuid;
        this.name = name;
        this.creationDate = creationDate;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public Expense(String name, PaymentMethod paymentMethod, double amount) {
        this(UUID.randomUUID(), name, LocalDateTime.now(), paymentMethod, amount);
    }

    public static Expense of(String name, PaymentMethod paymentMethod, double amount) {
        return new Expense(name, paymentMethod, amount);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
```

---

## Paso 5: `ExpenseRepository.java`

```java
package com.bcp.training.expenses;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByUuid(UUID uuid);
    long deleteByUuid(UUID uuid);
    long countByUuid(UUID uuid);
}
```

---

## Paso 6: `FraudScore.java` - Record DTO

```java
package com.bcp.training.expenses;

public record FraudScore(int score) {
}
```

> Los **records** son la forma moderna de crear DTOs inmutables en Java 21+.

---

## Paso 7: `FraudScoreClient.java` - REST Client con @HttpExchange

En lugar del `@RegisterRestClient` de Quarkus, Spring Boot 4 usa **@HttpExchange**:

```java
package com.bcp.training.expenses;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/score")
public interface FraudScoreClient {

    @GetExchange
    FraudScore getByAmount(@RequestParam("amount") double amount);
}
```

---

## Paso 8: `FraudScoreClientConfig.java` - Configuración del RestClient

Se registra como bean con **RestClient** (la forma moderna de Spring Boot 4):

```java
package com.bcp.training.expenses;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class FraudScoreClientConfig {

    @Value("${fraud-score.base-url:http://localhost:9080}")
    private String baseUrl;

    @Bean
    FraudScoreClient fraudScoreClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(FraudScoreClient.class);
    }
}
```

---

## Paso 9: `ExpenseNotFoundException.java`

```java
package com.bcp.training.expenses;

public class ExpenseNotFoundException extends RuntimeException {
    public ExpenseNotFoundException(String message) {
        super(message);
    }
}
```

---

## Paso 10: `ExpenseService.java`

```java
package com.bcp.training.expenses;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    public static final double MINIMUM_AMOUNT = 500;

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public List<Expense> list() {
        return repository.findAll();
    }

    @Transactional
    public Expense create(Expense expense) {
        Expense newExpense = Expense.of(expense.getName(), expense.getPaymentMethod(), expense.getAmount());
        return repository.save(newExpense);
    }

    @Transactional
    public void delete(UUID uuid) {
        long deleted = repository.deleteByUuid(uuid);
        if (deleted == 0) {
            throw new ExpenseNotFoundException("Expense not found with uuid: " + uuid);
        }
    }

    @Transactional
    public void update(Expense newExpense) {
        Expense original = repository.findByUuid(newExpense.getUuid())
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));
        original.setName(newExpense.getName());
        original.setAmount(newExpense.getAmount());
        original.setPaymentMethod(newExpense.getPaymentMethod());
        repository.save(original);
    }

    public boolean exists(UUID uuid) {
        return repository.countByUuid(uuid) == 1;
    }

    public boolean meetsMinimumAmount(double amount) {
        return amount >= MINIMUM_AMOUNT;
    }
}
```

---

## Paso 11: `ExpenseController.java`

```java
package com.bcp.training.expenses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final FraudScoreClient fraudScoreClient;

    public ExpenseController(ExpenseService expenseService, FraudScoreClient fraudScoreClient) {
        this.expenseService = expenseService;
        this.fraudScoreClient = fraudScoreClient;
    }

    @GetMapping
    public List<Expense> list() {
        return expenseService.list();
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Expense expense) {
        if (!expenseService.meetsMinimumAmount(expense.getAmount())) {
            return ResponseEntity.badRequest().build();
        }

        Expense created = expenseService.create(expense);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(created.getUuid())
                .toUri();

        return ResponseEntity.created(location)
                .header("uuid", created.getUuid().toString())
                .build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody Expense expense) {
        if (!expenseService.exists(expense.getUuid())) {
            return ResponseEntity.notFound().build();
        }
        expenseService.update(expense);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        try {
            expenseService.delete(uuid);
            return ResponseEntity.noContent().build();
        } catch (ExpenseNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/score")
    public ResponseEntity<Void> fraudScore(@RequestBody Expense expense) {
        FraudScore fraud = fraudScoreClient.getByAmount(expense.getAmount());

        if (fraud.score() > 200) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }
}
```

---

## Paso 12: `CrudTest.java` (completo)

Este test ya viene completo y usa **WebTestClient** con tests ordenados:

```java
package com.bcp.training.expenses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CrudTest {

    public static final String NON_EXISTING_UUID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

    @LocalServerPort
    int port;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @Order(1)
    public void initialListOfExpensesIsEmpty() {
        client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @Order(2)
    public void creatingAnExpenseReturns201WithHeaders() {
        client.post().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(generateExpenseJson("", "Expense 1", "CASH", 1000))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location")
                .expectHeader().exists("uuid");
    }

    @Test
    @Order(3)
    public void updateNonExistingExpenseReturns404() {
        client.put().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(generateExpenseJson(NON_EXISTING_UUID, "Expense 1", "CASH", 1000))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(3)
    public void updateExistingExpenseReturns204() {
        var response = client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Expense[].class)
                .returnResult()
                .getResponseBody();

        assertThat(response.length, is(1));

        String expenseUuid = response[0].getUuid().toString();
        double originalAmount = response[0].getAmount();
        double newAmount = originalAmount * 10;

        client.put().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(generateExpenseJson(expenseUuid, "Expense 1", "CASH", newAmount))
                .exchange()
                .expectStatus().isNoContent();

        var updatedResponse = client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Expense[].class)
                .returnResult()
                .getResponseBody();

        assertThat(updatedResponse.length, is(1));
        assertThat(updatedResponse[0].getUuid().toString(), is(expenseUuid));
        assertThat(updatedResponse[0].getAmount(), is(newAmount));
    }

    @Test
    @Order(4)
    public void deleteNonExistingExpenseReturns404() {
        client.delete().uri("/expenses/{uuid}", NON_EXISTING_UUID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    public void deleteExistingExpenseReturns204() {
        var response = client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Expense[].class)
                .returnResult()
                .getResponseBody();

        assertThat(response.length, is(1));
        String expenseUuid = response[0].getUuid().toString();

        client.delete().uri("/expenses/{uuid}", expenseUuid)
                .exchange()
                .expectStatus().isNoContent();
    }

    public static String generateExpenseJson(String uuid, String name, String paymentMethod, double amount) {
        return "{"
                + (uuid.isEmpty() ? "" : "\"uuid\":\"" + uuid + "\",")
                + "\"name\":\"" + name + "\","
                + "\"paymentMethod\":\"" + paymentMethod + "\","
                + "\"amount\":" + amount + "}";
    }
}
```

---

## Paso 13: Completar `RepositoryMockTest.java`

Reemplaza `PanacheMock` de Quarkus con `@MockitoBean`:

```java
package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RepositoryMockTest {

    @LocalServerPort
    int port;

    @MockitoBean
    ExpenseRepository mockRepository;

    @Test
    public void listOfExpensesReturnsAnEmptyList() {
        when(mockRepository.findAll()).thenReturn(Collections.emptyList());

        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }
}
```

> **`@MockitoBean`** reemplaza completamente el bean en el contexto de Spring con un mock de Mockito.
> **`@DirtiesContext`** asegura un contexto limpio antes de ejecutar esta clase.

---

## Paso 14: Completar `RestClientMockTest.java`

Mockea el `FraudScoreClient` (equivale a `@InjectMock @RestClient` de Quarkus):

```java
package com.bcp.training.expenses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RestClientMockTest {

    @LocalServerPort
    int port;

    @MockitoBean
    FraudScoreClient fraudScoreClient;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    public void highFraudScoreReturns400() {
        when(fraudScoreClient.getByAmount(anyDouble()))
                .thenReturn(new FraudScore(500));

        client.post().uri("/expenses/score")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CrudTest.generateExpenseJson("", "Expense 1", "CASH", 50000))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void lowFraudScoreReturns200() {
        when(fraudScoreClient.getByAmount(anyDouble()))
                .thenReturn(new FraudScore(50));

        client.post().uri("/expenses/score")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CrudTest.generateExpenseJson("", "Expense 1", "CASH", 1000))
                .exchange()
                .expectStatus().isOk();
    }
}
```

---

## Paso 15: Completar `ServiceMockTest.java`

Mockea el servicio completo:

```java
package com.bcp.training.expenses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ServiceMockTest {

    @LocalServerPort
    int port;

    @MockitoBean
    ExpenseService mockExpenseService;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    public void creatingAnExpenseReturns400OnInvalidAmount() {
        when(mockExpenseService.meetsMinimumAmount(anyDouble()))
                .thenReturn(false);

        client.post().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CrudTest.generateExpenseJson("", "Expense 1", "CASH", 99999))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
```

---

## Paso 16: Completar `SpyTest.java`

Usa `@MockitoSpyBean` para espiar el bean real sin reemplazarlo:

```java
package com.bcp.training.expenses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SpyTest {

    @LocalServerPort
    int port;

    @MockitoSpyBean
    ExpenseService expenseService;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    public void listOfExpensesCallsExpenseList() {
        client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);

        verify(expenseService, times(1)).list();
    }
}
```

> **Diferencia Mock vs Spy:**
> - `@MockitoBean`: Reemplaza completamente el bean (todos los métodos retornan null/0/false por defecto)
> - `@MockitoSpyBean`: Envuelve el bean real (los métodos se ejecutan normalmente, pero puedes verificar invocaciones y opcionalmente sobreescribir comportamiento)

---

## Paso 17: Ejecutar los tests

```bash
mvn test
```

Resultado esperado:
```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0 - BUILD SUCCESS
```

---

## Comparación con Quarkus

| Aspecto | Quarkus | Spring Boot 4 |
|---------|---------|--------------|
| Mock bean | `@InjectMock` | `@MockitoBean` |
| Spy bean | `@InjectSpy` | `@MockitoSpyBean` |
| Mock Panache | `PanacheMock.mock()` | `@MockitoBean` en Repository |
| CDI Alternative | `@Mock` (Quarkus) | `@MockitoBean` |
| REST Client | `@RegisterRestClient` | `@HttpExchange` + `RestClient` |
| REST Client mock | `@InjectMock @RestClient` | `@MockitoBean` |
| Contexto limpio | Automático por test | `@DirtiesContext` |
| DTO inmutable | Clase con campos públicos | `record` de Java |
