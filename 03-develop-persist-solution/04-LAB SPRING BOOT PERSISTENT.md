# LAB 4: SPRING BOOT PERSISTENT

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

## Objetivo

En este laboratorio aprenderás a:
- Configurar Spring Data JPA con H2 en Spring Boot 4
- Crear entidades JPA con anotaciones estándar
- Establecer relaciones entre entidades (OneToMany, ManyToOne)
- Configurar H2 como base de datos en memoria
- Implementar repositorios con Spring Data JPA (`JpaRepository`)
- Crear un servicio con operaciones CRUD
- Agregar paginación y ordenamiento con `Pageable`
- Usar transacciones con `@Transactional`

## 1. Cargar en su IDE el proyecto 03-develop-persist-start

Abre el proyecto en tu IDE preferido. El proyecto contiene:
- `expense-service`: Servicio REST que gestiona gastos (actualmente sin persistencia)

### 1.1. Estructura del proyecto

El módulo `expense-service` contiene:
- `Expense`: Modelo de datos que representa un gasto (sin anotaciones JPA)
- `Associate`: Modelo de datos que representa un asociado (sin anotaciones JPA)
- `ExpenseController`: Controlador REST con operaciones CRUD (incompleto)

## 2. Agregar dependencias de Spring Data JPA y H2

### 2.1. Abre el archivo `pom.xml`

Ubicado en: `expense-service/pom.xml`

### 2.2. Agrega las dependencias necesarias

Agrega las siguientes dependencias dentro del elemento `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Explicación:**
- `spring-boot-starter-data-jpa`: Incluye Spring Data JPA, Hibernate y todo lo necesario para persistencia
- `spring-boot-starter-validation`: Incluye Bean Validation (Jakarta Validation)
- `h2`: Base de datos en memoria para desarrollo (equivalente a Quarkus Dev Services con PostgreSQL)

**Resultado esperado:** El archivo `pom.xml` debe incluir estas dependencias junto con las existentes (`spring-boot-starter-web`, `spring-boot-devtools`, `springdoc-openapi`).

## 3. Configurar la base de datos

### 3.1. Abre el archivo `application.yml`

Ubicado en: `expense-service/src/main/resources/application.yml`

### 3.2. Agrega la configuración de la base de datos

Reemplaza los comentarios TODO con:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:expensedb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    defer-datasource-initialization: true
  sql:
    init:
      mode: always
```

**Explicación:**
- `spring.datasource`: Configura la conexión a H2 en memoria
- `spring.h2.console`: Habilita la consola web de H2 para inspeccionar la base de datos
- `spring.jpa.hibernate.ddl-auto=create-drop`: Crea y recrea el esquema al iniciar (equivalente a `quarkus.hibernate-orm.database.generation=drop-and-create`)
- `spring.jpa.show-sql=true`: Muestra las consultas SQL en consola
- `spring.jpa.defer-datasource-initialization=true`: Permite ejecutar `data.sql` después de crear el esquema
- `spring.sql.init.mode=always`: Ejecuta `data.sql` al iniciar (equivalente a `import.sql` en Quarkus)

## 4. Convertir Associate en una entidad JPA

### 4.1. Abre la clase `Associate`

Ubicada en: `expense-service/src/main/java/com/bcp/training/model/Associate.java`

### 4.2. Agrega la anotación `@Entity` y las anotaciones JPA

```java
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
```

### 4.3. Anota la clase con `@Entity`

```java
@Entity
public class Associate {
```

### 4.4. Agrega las anotaciones al campo `id`

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**Nota:** En Spring Boot usamos `@Id` + `@GeneratedValue` en lugar de extender `PanacheEntity` como en Quarkus.

### 4.5. Agrega la relación OneToMany con Expense

Agrega las anotaciones necesarias al campo `expenses`:

```java
@JsonIgnore
@OneToMany(mappedBy = "associate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Expense> expenses = new ArrayList<>();
```

**Nota:** Usamos `@JsonIgnore` de Jackson (en lugar de `@JsonbTransient` de JSON-B que usa Quarkus).

### 4.6. Agrega un constructor sin argumentos

Agrega el constructor requerido por JPA:

```java
public Associate() {
}
```

**Resultado esperado:**

```java
@Entity
public class Associate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "associate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Expense> expenses = new ArrayList<>();

    public Associate() {
    }

    public Associate(String name) {
        this.name = name;
    }

    // getters y setters...
}
```

## 5. Convertir Expense en una entidad JPA

### 5.1. Abre la clase `Expense`

Ubicada en: `expense-service/src/main/java/com/bcp/training/model/Expense.java`

### 5.2. Agrega los imports necesarios

```java
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
import jakarta.validation.constraints.NotNull;
```

### 5.3. Anota la clase con `@Entity`

```java
@Entity
public class Expense {
```

### 5.4. Agrega las anotaciones al campo `id`

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

### 5.5. Agrega `@NotNull` al campo `uuid`

```java
@NotNull
private UUID uuid;
```

### 5.6. Agrega `@JsonFormat` al campo `creationDate`

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime creationDate;
```

### 5.7. Agrega `@Enumerated` al campo `paymentMethod`

```java
@Enumerated(EnumType.STRING)
private PaymentMethod paymentMethod;
```

### 5.8. Agrega la relación ManyToOne con Associate

```java
@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "associate_id", insertable = false, updatable = false)
private Associate associate;
```

### 5.9. Anota el campo `associateId` con `@Column`

```java
@Column(name = "associate_id")
private Long associateId;
```

### 5.10. Agrega un constructor sin argumentos

```java
public Expense() {
    this.uuid = UUID.randomUUID();
    this.creationDate = LocalDateTime.now();
}
```

**Resultado esperado:** La clase `Expense` debe tener la anotación `@Entity` y todas las anotaciones JPA necesarias, incluyendo la relación `@ManyToOne` con `Associate`.

## 6. Crear los repositorios Spring Data JPA

A diferencia de Quarkus Panache (patrón Active Record), Spring Boot usa el patrón Repository con interfaces que extienden `JpaRepository`.

### 6.1. Crea la interfaz `AssociateRepository`

Crea el archivo: `expense-service/src/main/java/com/bcp/training/repository/AssociateRepository.java`

```java
package com.bcp.training.repository;

import com.bcp.training.model.Associate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssociateRepository extends JpaRepository<Associate, Long> {
}
```

**Nota:** Spring Data JPA genera automáticamente la implementación. No necesitas escribir código SQL ni implementar la interfaz.

### 6.2. Crea la interfaz `ExpenseRepository`

Crea el archivo: `expense-service/src/main/java/com/bcp/training/repository/ExpenseRepository.java`

```java
package com.bcp.training.repository;

import com.bcp.training.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByUuid(UUID uuid);

    long deleteByUuid(UUID uuid);
}
```

**Explicación:**
- `JpaRepository<Expense, Long>` proporciona métodos CRUD: `save()`, `findById()`, `findAll()`, `deleteById()`, etc.
- `findByUuid()`: Spring Data genera automáticamente la consulta a partir del nombre del método
- `deleteByUuid()`: Elimina por UUID y retorna el número de registros eliminados

**Equivalencia con Quarkus Panache:**

| Quarkus Panache | Spring Data JPA |
|---|---|
| `Expense.findAll()` | `expenseRepository.findAll()` |
| `Expense.findByIdOptional(id)` | `expenseRepository.findById(id)` |
| `expense.persist()` | `expenseRepository.save(expense)` |
| `Expense.delete("uuid", uuid)` | `expenseRepository.deleteByUuid(uuid)` |

## 7. Crear el servicio ExpenseService

### 7.1. Crea la clase `ExpenseService`

Crea el archivo: `expense-service/src/main/java/com/bcp/training/service/ExpenseService.java`

```java
package com.bcp.training.service;

import com.bcp.training.model.Expense;
import com.bcp.training.repository.AssociateRepository;
import com.bcp.training.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AssociateRepository associateRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          AssociateRepository associateRepository) {
        this.expenseRepository = expenseRepository;
        this.associateRepository = associateRepository;
    }

    @Transactional(readOnly = true)
    public List<Expense> list(int pageSize, int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize,
                Sort.by("amount").and(Sort.by("associateId")));
        Page<Expense> page = expenseRepository.findAll(pageRequest);
        return page.getContent();
    }

    public Expense create(Expense expense) {
        associateRepository.findById(expense.getAssociateId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Associate not found"));

        expense.setUuid(UUID.randomUUID());
        expense.setCreationDate(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    public void delete(UUID uuid) {
        long numDeleted = expenseRepository.deleteByUuid(uuid);
        if (numDeleted == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Expense not found");
        }
    }

    public void update(Expense expense) {
        Expense existing = expenseRepository.findById(expense.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Expense not found"));

        existing.setUuid(expense.getUuid());
        existing.setName(expense.getName());
        existing.setAmount(expense.getAmount());
        existing.setPaymentMethod(expense.getPaymentMethod());
        expenseRepository.save(existing);
    }
}
```

**Explicación:**
- `@Service`: Registra la clase como bean de Spring
- `@Transactional`: Todas las operaciones se ejecutan dentro de una transacción (equivalente a `@Transactional` de Jakarta en Quarkus)
- `@Transactional(readOnly = true)`: Optimiza las consultas de solo lectura
- `PageRequest.of()`: Crea una solicitud de paginación (equivalente a `PanacheQuery` con `Page.of()` en Quarkus)
- `Sort.by()`: Define el ordenamiento (equivalente a `Sort.by().and()` de Panache)
- `expenseRepository.save()`: Persiste o actualiza la entidad (equivalente a `entity.persist()` en Panache)

## 8. Actualizar el ExpenseController

### 8.1. Abre la clase `ExpenseController`

Ubicada en: `expense-service/src/main/java/com/bcp/training/controller/ExpenseController.java`

### 8.2. Inyecta el ExpenseService

```java
private final ExpenseService expenseService;

public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
}
```

### 8.3. Implementa el método `list()` con paginación

```java
@GetMapping
public List<Expense> list(
        @RequestParam(defaultValue = "5") int pageSize,
        @RequestParam(defaultValue = "1") int pageNum) {
    return expenseService.list(pageSize, pageNum);
}
```

### 8.4. Implementa el método `create()`

```java
@PostMapping
public Expense create(@RequestBody Expense expense) {
    return expenseService.create(expense);
}
```

### 8.5. Implementa el método `delete()`

```java
@DeleteMapping("/{uuid}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable UUID uuid) {
    expenseService.delete(uuid);
}
```

### 8.6. Implementa el método `update()`

```java
@PutMapping
@ResponseStatus(HttpStatus.NO_CONTENT)
public void update(@RequestBody Expense expense) {
    expenseService.update(expense);
}
```

**Resultado esperado:**

```java
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<Expense> list(
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNum) {
        return expenseService.list(pageSize, pageNum);
    }

    @PostMapping
    public Expense create(@RequestBody Expense expense) {
        return expenseService.create(expense);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID uuid) {
        expenseService.delete(uuid);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody Expense expense) {
        expenseService.update(expense);
    }
}
```

## 9. Iniciar la aplicación

### 9.1. Navega al directorio expense-service

### Linux/Mac

```bash
cd expense-service
```

### Windows (CMD)

```cmd
cd expense-service
```

### Windows (PowerShell)

```powershell
cd expense-service
```

### 9.2. Inicia la aplicación

### Linux/Mac

```bash
mvn spring-boot:run
```

### Windows (CMD)

```cmd
mvnw.cmd spring-boot:run
```

### Windows (PowerShell)

```powershell
.\mvnw.cmd spring-boot:run
```

**Nota:** A diferencia de Quarkus Dev Services (que inicia un contenedor PostgreSQL automáticamente), Spring Boot usa H2 en memoria, por lo que no necesitas Docker.

### 9.3. Verifica que la aplicación esté corriendo

Abre tu navegador y visita:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console**: http://localhost:8080/h2-console

Para la consola H2, usa:
- **JDBC URL**: `jdbc:h2:mem:expensedb`
- **User**: `sa`
- **Password**: (vacío)

## 10. Probar los endpoints

### 10.1. Listar todos los gastos con paginación

### Linux/Mac

```bash
curl "http://localhost:8080/expenses?pageSize=5&pageNum=1"
```

### Windows (CMD)

```cmd
curl "http://localhost:8080/expenses?pageSize=5&pageNum=1"
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/expenses?pageSize=5&pageNum=1" -Method GET | Select-Object -ExpandProperty Content
```

**Resultado esperado:** Deberías ver un array JSON con los gastos inicializados desde `data.sql`, ordenados por `amount` y `associateId`.

### 10.2. Listar la segunda página

### Linux/Mac

```bash
curl "http://localhost:8080/expenses?pageSize=5&pageNum=2"
```

### Windows (CMD)

```cmd
curl "http://localhost:8080/expenses?pageSize=5&pageNum=2"
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/expenses?pageSize=5&pageNum=2" -Method GET | Select-Object -ExpandProperty Content
```

### 10.3. Crear un nuevo gasto

### Linux/Mac

```bash
curl -X POST http://localhost:8080/expenses \
  -H "Content-Type: application/json" \
  -d '{"name":"New Book","paymentMethod":"CASH","amount":25.50,"associateId":1}'
```

### Windows (CMD)

```cmd
curl -X POST http://localhost:8080/expenses -H "Content-Type: application/json" -d "{\"name\":\"New Book\",\"paymentMethod\":\"CASH\",\"amount\":25.50,\"associateId\":1}"
```

### Windows (PowerShell)

```powershell
$body = @{
    name = "New Book"
    paymentMethod = "CASH"
    amount = 25.50
    associateId = 1
} | ConvertTo-Json

Invoke-WebRequest -Uri http://localhost:8080/expenses -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

**Resultado esperado:** Deberías recibir el objeto JSON del gasto creado con un `id` y `uuid` generados.

### 10.4. Actualizar un gasto

Primero, obtén el UUID de un gasto existente listando los gastos. Luego:

### Linux/Mac

```bash
curl -X PUT http://localhost:8080/expenses \
  -H "Content-Type: application/json" \
  -d '{"id":1,"uuid":"<UUID_DEL_GASTO>","name":"Updated Desk","paymentMethod":"CREDIT_CARD","amount":200.00,"associateId":1}'
```

### Windows (CMD)

```cmd
curl -X PUT http://localhost:8080/expenses -H "Content-Type: application/json" -d "{\"id\":1,\"uuid\":\"<UUID_DEL_GASTO>\",\"name\":\"Updated Desk\",\"paymentMethod\":\"CREDIT_CARD\",\"amount\":200.00,\"associateId\":1}"
```

### Windows (PowerShell)

```powershell
$body = @{
    id = 1
    uuid = "<UUID_DEL_GASTO>"
    name = "Updated Desk"
    paymentMethod = "CREDIT_CARD"
    amount = 200.00
    associateId = 1
} | ConvertTo-Json

Invoke-WebRequest -Uri http://localhost:8080/expenses -Method PUT -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

### 10.5. Eliminar un gasto

### Linux/Mac

```bash
curl -X DELETE http://localhost:8080/expenses/<UUID_DEL_GASTO>
```

### Windows (CMD)

```cmd
curl -X DELETE http://localhost:8080/expenses/<UUID_DEL_GASTO>
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/expenses/<UUID_DEL_GASTO>" -Method DELETE
```

## 11. Verificar la base de datos

### 11.1. Acceder a la consola H2

Abre tu navegador y visita: http://localhost:8080/h2-console

Usa estos datos de conexión:
- **JDBC URL**: `jdbc:h2:mem:expensedb`
- **User**: `sa`
- **Password**: (vacío)

### 11.2. Consultar los gastos

```sql
SELECT * FROM EXPENSE;
```

### 11.3. Consultar los asociados

```sql
SELECT * FROM ASSOCIATE;
```

### 11.4. Consultar gastos con su asociado

```sql
SELECT e.*, a.name AS associate_name
FROM EXPENSE e
JOIN ASSOCIATE a ON e.associate_id = a.id
ORDER BY e.amount, e.associate_id;
```

## 12. Diferencias clave: Spring Boot vs Quarkus

| Concepto | Quarkus | Spring Boot 4 |
|---|---|---|
| **ORM** | Hibernate ORM + Panache | Spring Data JPA + Hibernate |
| **Patrón** | Active Record (`PanacheEntity`) | Repository (`JpaRepository`) |
| **Entidad base** | `extends PanacheEntity` | `@Id` + `@GeneratedValue` |
| **Persistir** | `entity.persist()` | `repository.save(entity)` |
| **Buscar todos** | `Entity.findAll()` | `repository.findAll()` |
| **Buscar por ID** | `Entity.findByIdOptional(id)` | `repository.findById(id)` |
| **Eliminar** | `Entity.delete("field", value)` | `repository.deleteByField(value)` |
| **Paginación** | `PanacheQuery` + `Page.of()` | `PageRequest.of()` + `Page<T>` |
| **Transacciones** | `@Transactional` (Jakarta) | `@Transactional` (Spring) |
| **JSON** | JSON-B (`@JsonbTransient`) | Jackson (`@JsonIgnore`) |
| **Base de datos dev** | Dev Services (PostgreSQL auto) | H2 en memoria |
| **Datos iniciales** | `import.sql` | `data.sql` |
| **REST** | JAX-RS (`@Path`, `@GET`) | Spring MVC (`@RequestMapping`, `@GetMapping`) |

## Resumen

En este laboratorio has aprendido a:
- Configurar Spring Data JPA con H2 en Spring Boot 4
- Crear entidades JPA con `@Entity`, `@Id`, `@GeneratedValue`
- Establecer relaciones entre entidades (`@OneToMany`, `@ManyToOne`)
- Configurar H2 como base de datos en memoria con consola web
- Implementar repositorios con `JpaRepository` (patrón Repository)
- Crear un servicio con `@Service` y `@Transactional`
- Agregar paginación con `PageRequest` y ordenamiento con `Sort`
- Inicializar datos con `data.sql`

## Próximos pasos

- Explora más métodos de Spring Data JPA como consultas derivadas (`findByNameContaining`, `findByAmountGreaterThan`)
- Implementa consultas personalizadas con `@Query`
- Agrega validación usando `@Valid` en el controlador
- Configura PostgreSQL en lugar de H2 para producción
- Implementa especificaciones con `JpaSpecificationExecutor`
- Explora Spring Boot Docker Compose Support como alternativa a H2

---

**Enjoy!**

**Joe**
