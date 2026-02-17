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

## Paso 1: Dependencias en `pom.xml`

```xml
<!-- Spring Boot Testcontainers Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers PostgreSQL -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers JUnit 5 -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Se necesita el BOM de Testcontainers para gestionar versiones:

```xml
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
```

## Paso 2: Entidades JPA

**Associate** (padre):
```java
@Entity
public class Associate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "associate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();
}
```

**Expense** (hijo):
```java
@Entity
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID uuid;
    private String name;
    private BigDecimal amount;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associate_id", insertable = false, updatable = false)
    private Associate associate;

    @Column(name = "associate_id")
    private Long associateId;
}
```

## Paso 3: Configuración de application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tc-test
    username: tc-user
    password: tc-pass
  jpa:
    hibernate:
      ddl-auto: create
    defer-datasource-initialization: true
  sql:
    init:
      mode: always
```

> **`defer-datasource-initialization: true`** asegura que `data.sql` se ejecute DESPUÉS de que Hibernate cree las tablas.

## Paso 4: Seed Data (data.sql)

```sql
INSERT INTO associate (id, name) VALUES (1, 'Jaime') ON CONFLICT DO NOTHING;
INSERT INTO associate (id, name) VALUES (2, 'Pablo') ON CONFLICT DO NOTHING;

INSERT INTO expense (name, payment_method, amount, associate_id, uuid)
SELECT 'Desk', 'CASH', 150.50, 1, gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM expense WHERE name = 'Desk' AND associate_id = 1);
-- ... más datos seed
```

## Paso 5: Completar el Test

### Comparación Quarkus vs Spring Boot 4

**Quarkus** (complejo, custom annotation + lifecycle manager):
```java
@QuarkusTest
@TestHTTPEndpoint(AssociateResource.class)
@WithPostgresDB(name = "tc-test", username = "tc-user", password = "tc-pass")
public class AssociateResourceTest {
    // ... requiere PostgresDBTestResource.java y WithPostgresDB.java
}
```

**Spring Boot 4** (simple, declarativo):
```java
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
| `@Container` | Marca el contenedor para lifecycle management |
| `@ServiceConnection` | **Spring Boot auto-configura** la conexión (URL, user, pass) |
| `static` | Contenedor compartido entre todos los tests de la clase |

> **Ventaja de `@ServiceConnection`:** No necesitas escribir `@DynamicPropertySource` ni `Map<String, String> start()` como en Quarkus. Spring Boot detecta el tipo de contenedor y configura automáticamente las propiedades de conexión.

## Paso 6: Ejecutar los tests

```bash
mvn test
```

> **Prerequisito:** Docker debe estar corriendo.

Resultado esperado:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 - BUILD SUCCESS
```

## Troubleshooting

### Error: "relation associate does not exist"
Asegúrate de tener `defer-datasource-initialization: true` en `application.yml`.
Sin esto, Spring intenta ejecutar `data.sql` antes de que Hibernate cree las tablas.

### Error: "Could not connect to Ryuk"
Verifica que Docker Desktop esté corriendo.

### Error: "Testcontainers is not available"
Agrega las dependencias `spring-boot-testcontainers`, `testcontainers:postgresql` y `testcontainers:junit-jupiter`.

## Comparación Final: Quarkus Dev Services vs Spring Boot Testcontainers

| Aspecto | Quarkus Dev Services | Spring Boot 4 Testcontainers |
|---------|---------------------|------------------------------|
| Configuración | Custom `QuarkusTestResourceLifecycleManager` | `@ServiceConnection` (1 anotación) |
| Archivos necesarios | 3 (TestResource + Annotation + Test) | 1 (solo el Test) |
| Propiedades | Retornar `Map<String, String>` manual | Auto-configuración |
| Annotations | Custom (`@WithPostgresDB`) | Estándar (`@Testcontainers` + `@Container`) |
| Complejidad | Media-Alta | **Baja** |
