# LAB 3: SPRING BOOT REST

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

## Objetivo

En este laboratorio aprenderás a:
- Crear endpoints REST usando Spring MVC
- Implementar un servicio de aplicación con Spring
- Configurar un cliente REST usando `RestClient`
- Probar los endpoints usando Swagger UI (springdoc-openapi)

## 1. Cargar en su IDE el proyecto 02-develop-rest-start

El proyecto contiene dos módulos:
- `expense-service`: Servicio REST que gestiona gastos
- `expense-client`: Cliente que consume el servicio REST

## 2. Implementar el ExpenseController

### 2.1. Abre la clase `ExpenseController`

Ubicada en: `expense-service/src/main/java/com/bcp/training/ExpenseController.java`

### 2.2. Agrega las anotaciones necesarias

```java
@RestController
@RequestMapping("/expenses")
public class ExpenseController {
```

### 2.3. Inyecta el ExpenseService

```java
private final ExpenseService expenseService;

public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
}
```

### 2.4. Anota los métodos HTTP

- `list()` → `@GetMapping`
- `create(Expense expense)` → `@PostMapping`
- `delete(UUID uuid)` → `@DeleteMapping("/{uuid}")`
- `update(Expense expense)` → `@PutMapping`

En el método `delete`, retorna `404` si no existe.

## 3. Configurar el ExpenseService como bean

### 3.1. Abre la clase `ExpenseService`

Ubicada en: `expense-service/src/main/java/com/bcp/training/ExpenseService.java`

### 3.2. Agrega la anotación `@Service`

```java
@Service
public class ExpenseService {
```

### 3.3. Agrega datos iniciales

```java
@PostConstruct
void init() {
    expenses.add(new Expense("Spring Boot REST", Expense.PaymentMethod.DEBIT_CARD, "10.00"));
    expenses.add(new Expense("Cloud Native Workshop", Expense.PaymentMethod.CREDIT_CARD, "15.00"));
}
```

## 4. Iniciar el servicio expense-service

```bash
cd expense-service
mvn spring-boot:run
```

### 4.1. Verifica Swagger / OpenAPI

- Swagger UI: http://localhost:8080/swagger-ui/index.html  
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 5. Probar los endpoints del servicio

### 5.1. Listar todos los gastos

```bash
curl http://localhost:8080/expenses
```

### 5.2. Crear un nuevo gasto

```bash
curl -X POST http://localhost:8080/expenses \
  -H "Content-Type: application/json" \
  -d '{"name":"New Book","paymentMethod":"CASH","amount":"25.50"}'
```

## 6. Implementar el cliente REST

### 6.1. Abre `ExpenseServiceClient`

Ubicada en: `expense-client/src/main/java/com/bcp/training/client/ExpenseServiceClient.java`

### 6.2. Crea una implementación con `RestClient`

```java
@Service
public class ExpenseServiceClientImpl implements ExpenseServiceClient {
    private final RestClient restClient;

    public ExpenseServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }
}
```

### 6.3. Configura el `RestClient`

Crea `RestClientConfig`:

```java
@Configuration
public class RestClientConfig {
    @Bean
    RestClient expenseServiceRestClient(
        @Value("${expense.service.base-url}") String baseUrl,
        RestClient.Builder builder
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
```

### 6.4. Implementa los métodos `getAll()` y `create()`

Usa `RestClient` para consumir `/expenses` del servicio.

## 7. Implementar el ClientController

Abre `expense-client/src/main/java/com/bcp/training/service/ClientController.java` y agrega:

```java
@RestController
@RequestMapping("/expenses")
public class ClientController {
    private final ExpenseServiceClient service;
    public ClientController(ExpenseServiceClient service) {
        this.service = service;
    }
}
```

Anota los métodos con `@GetMapping` y `@PostMapping`.

## 8. Configurar el cliente REST

En `expense-client/src/main/resources/application.yml`:

```yaml
server:
  port: 8090

expense:
  service:
    base-url: http://localhost:8080
```

## 9. Iniciar el cliente expense-client

```bash
cd expense-client
mvn spring-boot:run
```

### 9.1. Verifica Swagger / OpenAPI

- Swagger UI: http://localhost:8090/swagger-ui/index.html  
- OpenAPI JSON: http://localhost:8090/v3/api-docs

## 10. Probar el cliente REST

### 10.1. Listar gastos

```bash
curl http://localhost:8090/expenses
```

### 10.2. Crear un gasto

```bash
curl -X POST http://localhost:8090/expenses \
  -H "Content-Type: application/json" \
  -d '{"name":"Training Course","paymentMethod":"CREDIT_CARD","amount":"99.99"}'
```

## 11. Construir imágenes de contenedor (opcional)

```bash
cd expense-service
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=expense-service:1.0.0-SNAPSHOT

cd ../expense-client
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=expense-client:1.0.0-SNAPSHOT
```

---

**Enjoy!**

**Joe**
