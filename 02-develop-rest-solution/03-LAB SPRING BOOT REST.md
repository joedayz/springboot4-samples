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

### Linux/Mac

```bash
cd expense-service
mvn spring-boot:run
```

### Windows (CMD)

```cmd
cd expense-service
mvnw.cmd spring-boot:run
```

### Windows (PowerShell)

```powershell
cd expense-service
.\mvnw.cmd spring-boot:run
```

### 4.1. Verifica Swagger / OpenAPI

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 5. Probar los endpoints del servicio

### 5.1. Listar todos los gastos

### Linux/Mac

```bash
curl http://localhost:8080/expenses
```

### Windows (CMD)

```cmd
curl http://localhost:8080/expenses
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri http://localhost:8080/expenses -Method GET | Select-Object -ExpandProperty Content
```

### 5.2. Crear un nuevo gasto

### Linux/Mac

```bash
curl -X POST http://localhost:8080/expenses \
  -H "Content-Type: application/json" \
  -d '{"name":"New Book","paymentMethod":"CASH","amount":"25.50"}'
```

### Windows (CMD)

```cmd
curl -X POST http://localhost:8080/expenses -H "Content-Type: application/json" -d "{\"name\":\"New Book\",\"paymentMethod\":\"CASH\",\"amount\":\"25.50\"}"
```

### Windows (PowerShell)

```powershell
$body = @{
    name = "New Book"
    paymentMethod = "CASH"
    amount = "25.50"
} | ConvertTo-Json

Invoke-WebRequest -Uri http://localhost:8080/expenses -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

## 6. Implementar el cliente REST

### 6.1. Revisa la interfaz `ExpenseServiceClient`

Ubicada en: `expense-client/src/main/java/com/bcp/training/client/ExpenseServiceClient.java`

Este archivo ya define el contrato que deberás implementar. Su estado inicial es:

```java
package com.bcp.training.client;

import com.bcp.training.model.Expense;

import java.util.Set;

public interface ExpenseServiceClient {

    Set<Expense> getAll();

    Expense create(Expense expense);
}
```

> **Nota:** No modifiques esta interfaz. En los siguientes pasos crearás una clase que la implemente.

### 6.2. Revisa el `ClientController` (estado inicial)

Ubicado en: `expense-client/src/main/java/com/bcp/training/web/ClientController.java`

Antes de modificarlo, el archivo luce así:

```java
package com.bcp.training.web;

import com.bcp.training.client.ExpenseServiceClient;
import com.bcp.training.model.Expense;

import java.util.Set;

public class ClientController {

    public ExpenseServiceClient service;

    public Set<Expense> getAll() {
        return service.getAll();
    }

    public Expense create(Expense expense) {
        return service.create(expense);
    }
}
```

> **Observa:** La clase no tiene anotaciones REST, el campo `service` es público y no hay inyección de dependencias por constructor.

### 6.3. Crea la implementación `ExpenseServiceClientImpl`

Crea un nuevo archivo en: `expense-client/src/main/java/com/bcp/training/service/ExpenseServiceClientImpl.java`

```java
package com.bcp.training.web;

import com.bcp.training.client.ExpenseServiceClient;
import com.bcp.training.model.Expense;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Set;

@Service
public class ExpenseServiceClientImpl implements ExpenseServiceClient {

   private final RestClient restClient;

   public ExpenseServiceClientImpl(RestClient restClient) {
      this.restClient = restClient;
   }

   @Override
   public Set<Expense> getAll() {
      return restClient.get()
              .uri("/expenses")
              .retrieve()
              .body(new ParameterizedTypeReference<>() {
              });
   }

   @Override
   public Expense create(Expense expense) {
      return restClient.post()
              .uri("/expenses")
              .body(expense)
              .retrieve()
              .body(Expense.class);
   }
}
```

### 6.4. Configura el `RestClient`

Crea un nuevo archivo en: `expense-client/src/main/java/com/bcp/training/config/RestClientConfig.java`

```java
package com.bcp.training.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient expenseServiceRestClient(
            @Value("${expense.service.base-url}") String baseUrl
    ) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
```

## 7. Implementar el ClientController

### 7.1. Modifica `ClientController`

Abre `expense-client/src/main/java/com/bcp/training/web/ClientController.java` y realiza los siguientes cambios:

1. Agrega las anotaciones `@RestController` y `@RequestMapping("/expenses")` a la clase
2. Cambia el campo público `service` a `private final` e inyéctalo por constructor
3. Anota `getAll()` con `@GetMapping`
4. Anota `create()` con `@PostMapping` y agrega `@RequestBody` al parámetro

El resultado final debe ser:

```java
package com.bcp.training.web;

import com.bcp.training.client.ExpenseServiceClient;
import com.bcp.training.model.Expense;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/expenses")
public class ClientController {

    private final ExpenseServiceClient service;

    public ClientController(ExpenseServiceClient service) {
        this.service = service;
    }

    @GetMapping
    public Set<Expense> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Expense create(@RequestBody Expense expense) {
        return service.create(expense);
    }
}
```

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

### Linux/Mac

```bash
cd expense-client
mvn spring-boot:run
```

### Windows (CMD)

```cmd
cd expense-client
mvnw.cmd spring-boot:run
```

### Windows (PowerShell)

```powershell
cd expense-client
.\mvnw.cmd spring-boot:run
```

### 9.1. Verifica Swagger / OpenAPI

- Swagger UI: http://localhost:8090/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8090/v3/api-docs

## 10. Probar el cliente REST

### 10.1. Listar gastos

### Linux/Mac

```bash
curl http://localhost:8090/expenses
```

### Windows (CMD)

```cmd
curl http://localhost:8090/expenses
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri http://localhost:8090/expenses -Method GET | Select-Object -ExpandProperty Content
```

### 10.2. Crear un gasto

### Linux/Mac

```bash
curl -X POST http://localhost:8090/expenses \
  -H "Content-Type: application/json" \
  -d '{"name":"Training Course","paymentMethod":"CREDIT_CARD","amount":"99.99"}'
```

### Windows (CMD)

```cmd
curl -X POST http://localhost:8090/expenses -H "Content-Type: application/json" -d "{\"name\":\"Training Course\",\"paymentMethod\":\"CREDIT_CARD\",\"amount\":\"99.99\"}"
```

### Windows (PowerShell)

```powershell
$body = @{
    name = "Training Course"
    paymentMethod = "CREDIT_CARD"
    amount = "99.99"
} | ConvertTo-Json

Invoke-WebRequest -Uri http://localhost:8090/expenses -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

## 11. Probar desde la interfaz web

El proyecto `expense-client` incluye una página HTML ubicada en:

`expense-client/src/main/resources/static/index.html`

Spring Boot sirve automáticamente los archivos estáticos de la carpeta `static/`, por lo que no se necesita configuración adicional.

### 11.1. Abre la interfaz web

Con ambos servicios corriendo (`expense-service` en el puerto 8080 y `expense-client` en el puerto 8090), abre tu navegador en:

> **http://localhost:8090**

Deberías ver la página **Expenses Client** con:
- Un formulario para agregar gastos (Name, Amount, Payment Method)
- Una tabla que muestra los gastos existentes

### 11.2. Prueba el flujo completo

1. **Verifica la lista inicial:** La tabla debería mostrar los dos gastos precargados del servicio ("Spring Boot REST" y "Cloud Native Workshop")
2. **Crea un nuevo gasto:** Completa el formulario con los datos:
    - Name: `Training Course`
    - Amount: `99.99`
    - Payment Method: `Credit Card`
3. **Haz clic en "Save":** Aparecerá un mensaje de confirmación y el nuevo gasto se mostrará en la tabla

> **Nota:** Esta página consume los mismos endpoints `/expenses` que probaste con `curl` en la sección anterior, pero a través del `ClientController` del proyecto `expense-client`.

## 12. Construir imágenes de contenedor (opcional)

### Linux/Mac

```bash
cd expense-service
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=expense-service:1.0.0-SNAPSHOT

cd ../expense-client
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=expense-client:1.0.0-SNAPSHOT
```

### Windows (CMD)

```cmd
cd expense-service
mvnw.cmd spring-boot:build-image -Dspring-boot.build-image.imageName=expense-service:1.0.0-SNAPSHOT

cd ..\expense-client
mvnw.cmd spring-boot:build-image -Dspring-boot.build-image.imageName=expense-client:1.0.0-SNAPSHOT
```

### Windows (PowerShell)

```powershell
cd expense-service
.\mvnw.cmd spring-boot:build-image -Dspring-boot.build-image.imageName=expense-service:1.0.0-SNAPSHOT

cd ..\expense-client
.\mvnw.cmd spring-boot:build-image -Dspring-boot.build-image.imageName=expense-client:1.0.0-SNAPSHOT
```

---

**Enjoy!**

**Joe**
