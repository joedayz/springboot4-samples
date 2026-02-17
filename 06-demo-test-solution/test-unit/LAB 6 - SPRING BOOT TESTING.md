# LAB 6 - SPRING BOOT 4 TESTING

## Objetivos

- Comprender el framework de testing de Spring Boot 4
- Crear tests de integración con **WebTestClient**
- Crear tests unitarios con **@SpringBootTest** y **@Autowired**
- Usar **@ConfigurationProperties** con records de Java para configuración tipada

## Conceptos Clave

| Quarkus | Spring Boot 4 |
|---------|--------------|
| `@QuarkusTest` | `@SpringBootTest` |
| `@ConfigMapping` | `@ConfigurationProperties` (record) |
| `@Inject` | `@Autowired` / Constructor injection |
| `@ApplicationScoped` | `@Component` / `@Service` |
| Panache Entity | JPA Entity + `JpaRepository` |
| RestAssured (integrado) | **WebTestClient** (nativo Spring Boot) |

## Estructura del Proyecto

```
test-unit/
├── pom.xml
├── src/main/java/com/bcp/training/expenses/
│   ├── TestUnitApplication.java
│   ├── Expense.java              (JPA Entity)
│   ├── ExpenseRepository.java    (Spring Data JPA)
│   ├── ExpenseProperties.java    (Record @ConfigurationProperties)
│   ├── ExpenseValidator.java     (@Component)
│   └── ExpenseController.java    (@RestController)
├── src/main/resources/
│   └── application.yml
└── src/test/java/com/bcp/training/expenses/
    ├── ExpenseCreationTest.java  (Integration test con WebTestClient)
    └── ExpenseValidationTest.java (Unit test con @Autowired)
```

---

## Paso 1: Revisar el `pom.xml`

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
    <artifactId>test-unit</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>test-unit</name>
    <description>Unit testing demo with Spring Boot 4</description>

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

> **Nota:** `spring-boot-starter-webflux` se agrega como dependencia de **test** para habilitar `WebTestClient` sin cambiar la aplicación a reactiva.

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

expense:
  max-amount: 2000
```

---

## Paso 3: `TestUnitApplication.java`

```java
package com.bcp.training.expenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ExpenseProperties.class)
public class TestUnitApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestUnitApplication.class, args);
    }
}
```

> `@EnableConfigurationProperties` activa el binding del record `ExpenseProperties` con la configuración del `application.yml`.

---

## Paso 4: `ExpenseProperties.java` - Configuración con Record

Spring Boot 4 permite usar **records de Java** con `@ConfigurationProperties`. Esto reemplaza al `@ConfigMapping` de Quarkus:

```java
package com.bcp.training.expenses;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "expense")
public record ExpenseProperties(BigDecimal maxAmount) {
}
```

---

## Paso 5: `Expense.java` - Entidad JPA

En lugar de Panache (Active Record), usamos JPA estándar con getters/setters:

```java
package com.bcp.training.expenses;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
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

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private BigDecimal amount;

    public Expense() {
    }

    public Expense(String name, PaymentMethod paymentMethod, String amount) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.paymentMethod = paymentMethod;
        this.amount = new BigDecimal(amount);
    }

    public static Expense of(String name, PaymentMethod paymentMethod, String amount) {
        return new Expense(name, paymentMethod, amount);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
```

---

## Paso 6: `ExpenseRepository.java` - Spring Data JPA

```java
package com.bcp.training.expenses;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByUuid(UUID uuid);
    long deleteByUuid(UUID uuid);
}
```

---

## Paso 7: `ExpenseValidator.java`

```java
package com.bcp.training.expenses;

import org.springframework.stereotype.Component;

@Component
public class ExpenseValidator {

    private final ExpenseProperties properties;

    public ExpenseValidator(ExpenseProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(Expense expense) {
        return amountIsValid(expense);
    }

    private boolean amountIsValid(Expense expense) {
        return expense.getAmount().compareTo(properties.maxAmount()) <= 0;
    }
}
```

---

## Paso 8: `ExpenseController.java`

```java
package com.bcp.training.expenses;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseRepository repository;
    private final ExpenseValidator validator;

    public ExpenseController(ExpenseRepository repository, ExpenseValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @GetMapping
    public List<Expense> list() {
        return repository.findAll();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Expense> create(@RequestBody Expense expense) {
        Expense newExpense = Expense.of(expense.getName(), expense.getPaymentMethod(),
                expense.getAmount().toString());

        if (!validator.isValid(newExpense)) {
            return ResponseEntity.badRequest().build();
        }

        Expense saved = repository.save(newExpense);
        return ResponseEntity.status(201).body(saved);
    }

    @DeleteMapping("/{uuid}")
    @Transactional
    public ResponseEntity<List<Expense>> delete(@PathVariable UUID uuid) {
        long deleted = repository.deleteByUuid(uuid);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repository.findAll());
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Void> update(@RequestBody Expense expense) {
        if (expense.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return repository.findById(expense.getId())
                .map(existing -> {
                    existing.setUuid(expense.getUuid());
                    existing.setName(expense.getName());
                    existing.setAmount(expense.getAmount());
                    existing.setPaymentMethod(expense.getPaymentMethod());
                    repository.save(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
```

---

## Paso 9: Completar `ExpenseValidationTest.java`

Este test verifica la lógica de validación usando el contexto de Spring:

```java
package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ExpenseValidationTest {

    @Autowired
    ExpenseProperties properties;

    @Autowired
    ExpenseValidator validator;

    @Test
    public void testExpenseWithMaxAmountIsValid() {
        var expense = givenExpenseWithAmount(properties.maxAmount());

        assertTrue(validator.isValid(expense));
    }

    @Test
    public void testExpenseOverMaxAmountIsInvalid() {
        var expense = givenExpenseWithAmount(properties.maxAmount().add(new BigDecimal("0.1")));

        assertFalse(validator.isValid(expense));
    }

    private Expense givenExpenseWithAmount(BigDecimal amount) {
        return Expense.of("Max amount expense", Expense.PaymentMethod.CREDIT_CARD, amount.toString());
    }
}
```

> **Equivalencia:** `@QuarkusTest` + `@Inject` se convierte en `@SpringBootTest` + `@Autowired`

---

## Paso 10: Completar `ExpenseCreationTest.java`

Este test usa **WebTestClient** para hacer requests HTTP reales contra el servidor:

```java
package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExpenseCreationTest {

    @LocalServerPort
    int port;

    @Autowired
    ExpenseRepository repository;

    @Test
    public void testCreateExpense() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        String json = """
                {
                    "name": "Test Expense",
                    "paymentMethod": "CASH",
                    "amount": 1234
                }
                """;

        client.post().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isCreated();

        client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Test Expense")
                .jsonPath("$[0].paymentMethod").isEqualTo("CASH")
                .jsonPath("$[0].amount").isEqualTo(1234);
    }
}
```

---

## Paso 11: Ejecutar los tests

```bash
mvn test
```

Resultado esperado:
```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 - BUILD SUCCESS
```

---

## Comparación con Quarkus

| Aspecto | Quarkus | Spring Boot 4 |
|---------|---------|--------------|
| Config tipada | `@ConfigMapping` interface | `@ConfigurationProperties` record |
| Test annotation | `@QuarkusTest` | `@SpringBootTest` |
| HTTP testing | RestAssured (integrado) | WebTestClient (nativo) |
| Persistencia | Panache (Active Record) | Spring Data JPA (Repository) |
| Inyección | CDI `@Inject` | `@Autowired` / Constructor |
| Server para tests | Puerto por defecto | `RANDOM_PORT` + `@LocalServerPort` |
| Text Blocks | Disponible | `"""..."""` (Java 21 nativo) |
