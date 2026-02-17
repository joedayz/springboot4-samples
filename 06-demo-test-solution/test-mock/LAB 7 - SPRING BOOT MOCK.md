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

## Paso 1: REST Client con @HttpExchange (moderno)

En lugar del `@RegisterRestClient` de Quarkus, Spring Boot 4 usa **@HttpExchange**:

```java
@HttpExchange("/score")
public interface FraudScoreClient {
    @GetExchange
    FraudScore getByAmount(@RequestParam("amount") double amount);
}
```

Se registra como bean con **RestClient** (la forma moderna):

```java
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

## Paso 2: FraudScore como Record

```java
public record FraudScore(int score) {
}
```

> Los **records** son la forma moderna de crear DTOs inmutables en Java 21+.

## Paso 3: ExpenseService

```java
@Service
public class ExpenseService {
    public static final double MINIMUM_AMOUNT = 500;
    private final ExpenseRepository repository;

    // list(), create(), delete(), update(), exists(), meetsMinimumAmount()
}
```

## Paso 4: CrudTest (completo)

Este test ya viene completo y usa **WebTestClient** con tests ordenados:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CrudTest {
    // 6 tests CRUD ordenados: empty list → create → update (not found / found) → delete (not found / found)
}
```

## Paso 5: Completar RepositoryMockTest

Reemplaza `PanacheMock` de Quarkus con `@MockitoBean`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RepositoryMockTest {

    @LocalServerPort
    int port;

    @MockitoBean                                    // Reemplaza el bean real
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

## Paso 6: Completar RestClientMockTest

Mockea el `FraudScoreClient` (equivale a `@InjectMock @RestClient` de Quarkus):

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RestClientMockTest {

    @LocalServerPort
    int port;

    @MockitoBean
    FraudScoreClient fraudScoreClient;

    @Test
    public void highFraudScoreReturns400() {
        when(fraudScoreClient.getByAmount(anyDouble()))
                .thenReturn(new FraudScore(500));

        // POST /expenses/score → expect 400
    }
}
```

## Paso 7: Completar ServiceMockTest

Mockea el servicio completo:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ServiceMockTest {

    @MockitoBean
    ExpenseService mockExpenseService;

    @Test
    public void creatingAnExpenseReturns400OnInvalidAmount() {
        when(mockExpenseService.meetsMinimumAmount(anyDouble()))
                .thenReturn(false);

        // POST /expenses → expect 400
    }
}
```

## Paso 8: Completar SpyTest

Usa `@MockitoSpyBean` para espiar el bean real sin reemplazarlo:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SpyTest {

    @MockitoSpyBean                                 // Spy: bean real + verificación
    ExpenseService expenseService;

    @Test
    public void listOfExpensesCallsExpenseList() {
        // GET /expenses → verify expenseService.list() was called once
        verify(expenseService, times(1)).list();
    }
}
```

> **Diferencia Mock vs Spy:**
> - `@MockitoBean`: Reemplaza completamente el bean (todos los métodos retornan null/0/false)
> - `@MockitoSpyBean`: Envuelve el bean real (los métodos se ejecutan normalmente, pero puedes verificar invocaciones)

## Paso 9: Ejecutar los tests

```bash
mvn test
```

Resultado esperado:
```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0 - BUILD SUCCESS
```

## Comparación con Quarkus

| Aspecto | Quarkus | Spring Boot 4 |
|---------|---------|--------------|
| Mock bean | `@InjectMock` | `@MockitoBean` |
| Spy bean | `@InjectSpy` | `@MockitoSpyBean` |
| Mock Panache | `PanacheMock.mock()` | `@MockitoBean` en Repository |
| CDI Alternative | `@Mock` (Quarkus) | `@MockitoBean` |
| REST Client | `@RegisterRestClient` | `@HttpExchange` |
| REST Client mock | `@InjectMock @RestClient` | `@MockitoBean` |
| Contexto limpio | Automático por test | `@DirtiesContext` |
