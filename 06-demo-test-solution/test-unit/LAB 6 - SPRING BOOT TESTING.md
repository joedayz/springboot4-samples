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

## Paso 1: Revisar el `pom.xml`

Dependencias principales:

```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
</dependency>

<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- WebTestClient (requiere WebFlux en classpath) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <scope>test</scope>
</dependency>
```

> **Nota:** `spring-boot-starter-webflux` se agrega como dependencia de test para habilitar `WebTestClient` sin cambiar la aplicación a reactiva.

## Paso 2: Configuración con Record

Spring Boot 4 permite usar **records de Java** con `@ConfigurationProperties`:

```java
@ConfigurationProperties(prefix = "expense")
public record ExpenseProperties(BigDecimal maxAmount) {
}
```

Esto reemplaza al `@ConfigMapping` de Quarkus. El record se activa con:

```java
@SpringBootApplication
@EnableConfigurationProperties(ExpenseProperties.class)
public class TestUnitApplication { ... }
```

Y se configura en `application.yml`:

```yaml
expense:
  max-amount: 2000
```

## Paso 3: Entidad JPA y Repository

En lugar de Panache, usamos **JPA estándar + Spring Data**:

```java
@Entity
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uuid;
    private String name;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private BigDecimal amount;
    // getters, setters, factory methods
}
```

```java
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByUuid(UUID uuid);
    long deleteByUuid(UUID uuid);
}
```

## Paso 4: ExpenseValidator

```java
@Component
public class ExpenseValidator {
    private final ExpenseProperties properties;

    public ExpenseValidator(ExpenseProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(Expense expense) {
        return expense.getAmount().compareTo(properties.maxAmount()) <= 0;
    }
}
```

## Paso 5: REST Controller

```java
@RestController
@RequestMapping("/expenses")
public class ExpenseController {
    private final ExpenseRepository repository;
    private final ExpenseValidator validator;

    // Constructor injection
    // GET /expenses - list all
    // POST /expenses - create (validates with ExpenseValidator)
    // DELETE /expenses/{uuid} - delete by UUID
    // PUT /expenses - update by ID
}
```

## Paso 6: Completar `ExpenseValidationTest`

Este test verifica la lógica de validación usando el contexto de Spring:

```java
@SpringBootTest                    // Levanta el contexto completo
public class ExpenseValidationTest {

    @Autowired                     // Inyección de dependencias
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
}
```

> **Equivalencia:** `@QuarkusTest` + `@Inject` → `@SpringBootTest` + `@Autowired`

## Paso 7: Completar `ExpenseCreationTest`

Este test usa **WebTestClient** para hacer requests HTTP reales:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExpenseCreationTest {

    @LocalServerPort
    int port;

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

        // Crear expense
        client.post().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isCreated();

        // Verificar que existe
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

## Paso 8: Ejecutar los tests

```bash
mvn test
```

Resultado esperado:
```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 - BUILD SUCCESS
```

## Comparación con Quarkus

| Aspecto | Quarkus | Spring Boot 4 |
|---------|---------|--------------|
| Config tipada | `@ConfigMapping` interface | `@ConfigurationProperties` record |
| Test annotation | `@QuarkusTest` | `@SpringBootTest` |
| HTTP testing | RestAssured (integrado) | WebTestClient (nativo) |
| Persistencia | Panache (Active Record) | Spring Data JPA (Repository) |
| Inyección | CDI `@Inject` | `@Autowired` / Constructor |
| Server para tests | Puerto por defecto | `RANDOM_PORT` + `@LocalServerPort` |
