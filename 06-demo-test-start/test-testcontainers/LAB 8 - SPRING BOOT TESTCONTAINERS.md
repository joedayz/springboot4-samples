# LAB 8 - SPRING BOOT 4 TESTCONTAINERS

## Objetivos

- Integrar **Testcontainers** con Spring Boot 4 usando `@ServiceConnection`
- Usar **PostgreSQL** como base de datos real en tests
- Comprender la diferencia entre Quarkus Dev Services y Spring Boot Testcontainers
- Usar `@Testcontainers` y `@Container` de JUnit 5

## Conceptos Clave

| Quarkus | Spring Boot 4 |
|---------|--------------|
| `QuarkusTestResourceConfigurableLifecycleManager` | `@Testcontainers` + `@Container` |
| `@QuarkusTestResource` (custom annotation) | `@ServiceConnection` (auto-config) |
| Configuración manual de properties | Auto-configuración con `@ServiceConnection` |
| Dev Services (automático) | Testcontainers (explícito, más control) |

> **`@ServiceConnection`** es una feature de Spring Boot 3.1+ que configura automáticamente las propiedades de conexión (URL, usuario, password) a partir del contenedor de Testcontainers. No necesitas `@DynamicPropertySource`.

## Estructura del Proyecto

```
test-testcontainers/
├── pom.xml
├── src/main/java/com/bcp/training/expenses/
│   ├── TestContainersApplication.java
│   ├── Associate.java              (JPA Entity)
│   ├── Expense.java                (JPA Entity con @ManyToOne)
│   ├── AssociateRepository.java    (Spring Data JPA)
│   ├── ExpenseRepository.java      (Spring Data JPA)
│   ├── AssociateController.java    (@RestController)
│   └── ExpenseController.java      (@RestController con paginación)
├── src/main/resources/
│   ├── application.yml
│   └── data.sql                    (Seed data)
└── src/test/java/com/bcp/training/expenses/
    └── AssociateControllerTest.java (Test con Testcontainers)
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
    <artifactId>test-testcontainers</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>test-testcontainers</name>
    <description>Testcontainers demo with Spring Boot 4</description>

    <properties>
        <java.version>21</java.version>
        <testcontainers.version>1.20.4</testcontainers.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

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
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
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

> **Nota:** Se necesita el BOM de Testcontainers (`testcontainers-bom`) para gestionar las versiones de los módulos de Testcontainers.

---

## Paso 2: `application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tc-test
    username: tc-user
    password: tc-pass
  jpa:
    hibernate:
      ddl-auto: create
    show-sql: true
    defer-datasource-initialization: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  sql:
    init:
      mode: always
```

> **`defer-datasource-initialization: true`** es CLAVE: asegura que `data.sql` se ejecute DESPUÉS de que Hibernate cree las tablas. Sin esto, obtendrás el error `relation "associate" does not exist`.

---

## Paso 3: `TestContainersApplication.java`

```java
package com.bcp.training.expenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestContainersApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestContainersApplication.class, args);
    }
}
```

---

## Paso 4: `Associate.java` - Entidad padre

```java
package com.bcp.training.expenses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "associate")
public class Associate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "associate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Expense> expenses = new ArrayList<>();

    public Associate() {
    }

    public Associate(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Expense> getExpenses() { return expenses; }
    public void setExpenses(List<Expense> expenses) { this.expenses = expenses; }
}
```

---

## Paso 5: `Expense.java` - Entidad hijo con @ManyToOne

```java
package com.bcp.training.expenses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expense")
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

    private BigDecimal amount;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associate_id", insertable = false, updatable = false)
    private Associate associate;

    @Column(name = "associate_id")
    private Long associateId;

    public Expense() {
    }

    public Expense(String name, PaymentMethod paymentMethod, BigDecimal amount, Long associateId) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.creationDate = LocalDateTime.now();
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.associateId = associateId;
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
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Associate getAssociate() { return associate; }
    public void setAssociate(Associate associate) { this.associate = associate; }
    public Long getAssociateId() { return associateId; }
    public void setAssociateId(Long associateId) { this.associateId = associateId; }
}
```

---

## Paso 6: `AssociateRepository.java`

```java
package com.bcp.training.expenses;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssociateRepository extends JpaRepository<Associate, Long> {
}
```

---

## Paso 7: `ExpenseRepository.java`

```java
package com.bcp.training.expenses;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    long deleteByUuid(UUID uuid);
}
```

---

## Paso 8: `AssociateController.java`

```java
package com.bcp.training.expenses;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/associates")
public class AssociateController {

    private final AssociateRepository repository;

    public AssociateController(AssociateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Associate> list() {
        return repository.findAll();
    }
}
```

---

## Paso 9: `ExpenseController.java` - Con paginación

```java
package com.bcp.training.expenses;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseRepository repository;

    public ExpenseController(ExpenseRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Expense> list(
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNum) {
        PageRequest pageRequest = PageRequest.of(
                pageNum - 1, pageSize,
                Sort.by("amount").and(Sort.by("associateId")));
        return repository.findAll(pageRequest).getContent();
    }

    @PostMapping
    @Transactional
    public Expense create(@RequestBody Expense expense) {
        Expense newExpense = new Expense(
                expense.getName(), expense.getPaymentMethod(),
                expense.getAmount(), expense.getAssociateId());
        return repository.save(newExpense);
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

## Paso 10: `data.sql` - Seed Data

```sql
INSERT INTO associate (id, name) VALUES (1, 'Jaime') ON CONFLICT DO NOTHING;
INSERT INTO associate (id, name) VALUES (2, 'Pablo') ON CONFLICT DO NOTHING;

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Desk', 'CASH', 150.50, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Desk' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Online Learning', 'CREDIT_CARD', 75.00, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Online Learning' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Books', 'CASH', 50.00, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Books' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Internet', 'CREDIT_CARD', 20.00, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Internet' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Phone', 'CASH', 15.00, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Phone' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Bookshelf', 'CASH', 150.50, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Bookshelf' AND associate_id = 1);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Printer Cartridges', 'CREDIT_CARD', 15.00, 2, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Printer Cartridges' AND associate_id = 2);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Online Learning', 'CASH', 50.00, 2, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Online Learning' AND associate_id = 2);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Internet', 'CREDIT_CARD', 20.00, 2, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Internet' AND associate_id = 2);

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Phone', 'CASH', 15.00, 2, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Phone' AND associate_id = 2);
```

---

## Paso 11: Completar `AssociateControllerTest.java`

### Comparación Quarkus vs Spring Boot 4

**Quarkus** (complejo - requiere 3 archivos: `PostgresDBTestResource.java`, `WithPostgresDB.java`, `AssociateResourceTest.java`):

```java
// Archivo 1: PostgresDBTestResource.java
public class PostgresDBTestResource implements QuarkusTestResourceConfigurableLifecycleManager<WithPostgresDB> {
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>(imageName);
    // init(), start() con Map<String,String>, stop()
}

// Archivo 2: WithPostgresDB.java
@QuarkusTestResource(value = PostgresDBTestResource.class, restrictToAnnotatedClass = true)
public @interface WithPostgresDB { ... }

// Archivo 3: AssociateResourceTest.java
@QuarkusTest
@WithPostgresDB(name = "tc-test", username = "tc-user", password = "tc-pass")
public class AssociateResourceTest { ... }
```

**Spring Boot 4** (simple - solo 1 archivo):

```java
package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AssociateControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.1")
            .withDatabaseName("tc-test")
            .withUsername("tc-user")
            .withPassword("tc-pass");

    @LocalServerPort
    int port;

    @Test
    public void testListAllEndpoint() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        Associate[] associates = client.get().uri("/associates")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Associate[].class)
                .returnResult()
                .getResponseBody();

        assertThat(associates).hasSize(2);
    }

    @Test
    public void testExpensesPagination() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        Expense[] expenses = client.get().uri("/expenses?pageSize=5&pageNum=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Expense[].class)
                .returnResult()
                .getResponseBody();

        assertThat(expenses).hasSizeLessThanOrEqualTo(5);
    }
}
```

### Explicación de las anotaciones:

| Anotación | Función |
|-----------|---------|
| `@Testcontainers` | Habilita la integración JUnit 5 con Testcontainers |
| `@Container` | Marca el contenedor para lifecycle management automático |
| `@ServiceConnection` | Spring Boot auto-configura la conexión (URL, user, pass) |
| `static` | Contenedor compartido entre todos los tests de la clase |

> **Ventaja de `@ServiceConnection`:** No necesitas escribir `@DynamicPropertySource` ni `Map<String, String> start()` como en Quarkus. Spring Boot detecta el tipo de contenedor y configura automáticamente las propiedades de conexión.

---

## Paso 12: Ejecutar los tests

```bash
mvn test
```

> **Prerequisito:** Docker debe estar corriendo.

Resultado esperado:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 - BUILD SUCCESS
```

---

## Troubleshooting

### Error: `relation "associate" does not exist`
Asegúrate de tener `defer-datasource-initialization: true` en `application.yml`.
Sin esto, Spring intenta ejecutar `data.sql` antes de que Hibernate cree las tablas.

### Error: `Could not connect to Ryuk`
Verifica que Docker Desktop esté corriendo.

### Error: `Testcontainers is not available`
Agrega las dependencias `spring-boot-testcontainers`, `testcontainers:postgresql` y `testcontainers:junit-jupiter`.

---

## Comparación Final: Quarkus Dev Services vs Spring Boot Testcontainers

| Aspecto | Quarkus Dev Services | Spring Boot 4 Testcontainers |
|---------|---------------------|------------------------------|
| Configuración | Custom `QuarkusTestResourceLifecycleManager` | `@ServiceConnection` (1 anotación) |
| Archivos necesarios | 3 (TestResource + Annotation + Test) | 1 (solo el Test) |
| Propiedades | Retornar `Map<String, String>` manual | Auto-configuración |
| Annotations | Custom (`@WithPostgresDB`) | Estándar (`@Testcontainers` + `@Container`) |
| Complejidad | Media-Alta | **Baja** |
