# LAB 5.5: SPRING BOOT DEVELOP REVIEW

**Autor:** José Díaz  
**Github Repo:** https://github.com/joedayz/springboot4-samples.git

## Objetivo

En este laboratorio aprenderás a:
- Convertir una aplicación REST simple a una aplicación con persistencia usando Spring Data JPA
- Implementar paginación y ordenamiento en endpoints REST
- Configurar y usar H2 como base de datos en memoria
- Implementar operaciones CRUD completas con Spring Data JPA
- Crear relaciones entre entidades JPA (@OneToMany)
- Usar tests con RestAssured para validar el comportamiento

## Comparación con Quarkus

| Concepto | Quarkus | Spring Boot 4 |
|----------|---------|---------------|
| Persistencia | Hibernate ORM Panache | Spring Data JPA |
| Entidades | `PanacheEntity` (herencia) | `@Entity` + `@Id` + getters/setters |
| Repositorio | Active Record (`Speaker.findAll()`) | Repository Pattern (`SpeakerRepository`) |
| REST | JAX-RS (`@Path`, `@GET`, `@POST`) | Spring MVC (`@RequestMapping`, `@GetMapping`, `@PostMapping`) |
| JSON | JSON-B | Jackson |
| Transacciones | `jakarta.transaction.Transactional` | `org.springframework.transaction.annotation.Transactional` |
| Paginación | `.page(pageIndex, pageSize)` | `PageRequest.of(pageIndex, pageSize)` |
| Base de datos dev | PostgreSQL + Dev Services | H2 en memoria |
| OpenAPI/Swagger | SmallRye OpenAPI | SpringDoc OpenAPI |

## 1. Cargar en su IDE el proyecto 05-develop-review-start

Abre el proyecto en tu IDE preferido. El proyecto contiene:
- `Speaker`: Modelo de datos simple (sin persistencia, con UUID como id)
- `SpeakerController`: Controlador REST básico (sin paginación ni persistencia, lista en memoria)
- `SpeakerControllerTest`: Tests que definen el comportamiento esperado (¡estos tests deben pasar al final!)

## 2. Examinar la estructura del proyecto

### 2.1. Clase Speaker

La clase `Speaker` actualmente es un POJO simple sin persistencia:

```java
public class Speaker {
    private String id = UUID.randomUUID().toString();
    private String name;
    private String organization;
    // getters y setters
}
```

### 2.2. Clase SpeakerController

El `SpeakerController` tiene métodos básicos pero NO implementa:
- Paginación
- Ordenamiento
- Persistencia en base de datos
- Endpoint DELETE

## 3. Agregar dependencias de Spring Data JPA

### 3.1. Abre el archivo `pom.xml`

Ubicado en: `develop-review/pom.xml`

### 3.2. Agrega las dependencias necesarias

Agrega las siguientes dependencias dentro de la sección `<dependencies>` (donde dice el TODO):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.1</version>
</dependency>
```

**NOTA:** 
- `spring-boot-starter-data-jpa` trae Hibernate ORM + Spring Data JPA
- `h2` es la base de datos en memoria (no necesitas instalar nada externo)
- `springdoc-openapi` genera documentación Swagger UI automática

## 4. Configurar application.yml

### 4.1. Abre el archivo `application.yml`

Ubicado en: `develop-review/src/main/resources/application.yml`

### 4.2. Agrega la configuración de H2 y JPA

Reemplaza el contenido con:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:reviewdb
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
```

**NOTA:** 
- `jdbc:h2:mem:reviewdb` - Base de datos H2 en memoria (se pierde al reiniciar)
- `h2.console.enabled: true` - Consola web H2 disponible en `/h2-console`
- `ddl-auto: create-drop` - Hibernate crea y elimina las tablas automáticamente
- **Ventaja sobre Quarkus**: No necesitas instalar PostgreSQL ni Docker para desarrollo

## 5. Crear la entidad Talk

### 5.1. Crea la clase `Talk.java`

Crea un nuevo archivo en: `develop-review/src/main/java/com/bcp/training/speaker/Talk.java`

### 5.2. Implementa la entidad Talk

```java
package com.bcp.training.speaker;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Talk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int duration;

    public Talk() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
```

**Comparación con Quarkus:**
- En Quarkus: `Talk extends PanacheEntity` (proporciona `id` automáticamente, campos públicos)
- En Spring Boot: `@Entity` con `@Id` + `@GeneratedValue`, campos privados con getters/setters
- Spring Boot requiere un constructor sin argumentos para JPA

## 6. Convertir Speaker a entidad JPA

### 6.1. Abre la clase `Speaker.java`

Ubicada en: `develop-review/src/main/java/com/bcp/training/speaker/Speaker.java`

### 6.2. Convierte Speaker a entidad JPA

Reemplaza el contenido completo con:

```java
package com.bcp.training.speaker;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Speaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String organization;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Talk> talks = new ArrayList<>();

    public Speaker() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public List<Talk> getTalks() {
        return talks;
    }

    public void setTalks(List<Talk> talks) {
        this.talks = talks;
    }
}
```

**NOTA:** 
- `@Id` + `@GeneratedValue` reemplaza el UUID que teníamos antes
- El `id` ahora es `Long` en lugar de `String`
- `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` permite que un Speaker tenga múltiples Talks
- `CascadeType.ALL` propaga todas las operaciones (persist, remove, merge, etc.)
- `orphanRemoval = true` elimina Talks huérfanos de la BD

## 7. Crear el SpeakerRepository

### 7.1. Crea la interfaz `SpeakerRepository.java`

Crea un nuevo archivo en: `develop-review/src/main/java/com/bcp/training/speaker/SpeakerRepository.java`

### 7.2. Implementa el repositorio

```java
package com.bcp.training.speaker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeakerRepository extends JpaRepository<Speaker, Long> {
}
```

**NOTA:** 
- Solo necesitas declarar la interfaz, Spring Data genera la implementación automáticamente
- `JpaRepository<Speaker, Long>` indica la entidad y el tipo de ID
- Proporciona automáticamente: `findAll()`, `save()`, `deleteById()`, `existsById()`, `findById()`, y más
- También soporta paginación y ordenamiento con `findAll(Pageable)`

**Comparación con Quarkus:**
- En Quarkus Panache no necesitas repositorio: `Speaker.findAll()`, `Speaker.deleteById()`
- En Spring Boot, el Repository Pattern separa la lógica de acceso a datos

## 8. Actualizar SpeakerController para usar Spring Data JPA

### 8.1. Abre la clase `SpeakerController.java`

Ubicada en: `develop-review/src/main/java/com/bcp/training/speaker/SpeakerController.java`

### 8.2. Reemplaza todo el contenido

Reemplaza el contenido completo con:

```java
package com.bcp.training.speaker;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/speakers")
public class SpeakerController {

    private final SpeakerRepository speakerRepository;

    public SpeakerController(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }

    @GetMapping
    public List<Speaker> getSpeakers(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "25") int pageSize) {
        String filteredSortBy = filterSortBy(sortBy);
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(filteredSortBy));
        return speakerRepository.findAll(pageRequest).getContent();
    }

    @PostMapping
    public ResponseEntity<Void> createSpeaker(@RequestBody Speaker speaker) {
        Speaker saved = speakerRepository.save(speaker);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location)
                .header("id", String.valueOf(saved.getId()))
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSpeaker(@PathVariable Long id) {
        if (!speakerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        speakerRepository.deleteById(id);
    }

    private String filterSortBy(String sortBy) {
        if (!sortBy.equals("id") && !sortBy.equals("name")) {
            return "id";
        }
        return sortBy;
    }
}
```

**Cambios principales:**

1. **Eliminamos la lista en memoria** (`List<Speaker> speakers = new ArrayList<>()`)
2. **Inyectamos el repository** por constructor: `SpeakerRepository speakerRepository`
3. **GET con paginación y ordenamiento**:
   - `@RequestParam(defaultValue = "id") String sortBy` - Parámetro de ordenamiento
   - `@RequestParam(defaultValue = "0") int pageIndex` - Índice de página
   - `@RequestParam(defaultValue = "25") int pageSize` - Tamaño de página
   - `PageRequest.of()` crea el request con paginación y ordenamiento
4. **POST con persistencia**: `speakerRepository.save(speaker)` persiste y retorna la entidad con ID
5. **DELETE con validación**: Verifica existencia antes de eliminar

## 9. Verificar la implementación

Tu proyecto debería tener esta estructura:

```
develop-review/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/bcp/training/
│   │   │   ├── DevelopReviewApplication.java
│   │   │   └── speaker/
│   │   │       ├── Speaker.java          (entidad JPA)
│   │   │       ├── Talk.java             (entidad JPA)
│   │   │       ├── SpeakerRepository.java (repositorio)
│   │   │       └── SpeakerController.java (controlador REST)
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/bcp/training/speaker/
│           └── SpeakerControllerTest.java
```

## 10. Ejecutar los tests

### 10.1. Navega al directorio del proyecto

#### Linux/Mac

```bash
cd develop-review
```

#### Windows (CMD)

```cmd
cd develop-review
```

#### Windows (PowerShell)

```powershell
cd develop-review
```

### 10.2. Ejecuta los tests

#### Linux/Mac/Windows (CMD)

```bash
mvn test
```

#### Windows (PowerShell)

```powershell
mvn test
```

**Resultado esperado:** Todos los tests deberían pasar (10 tests).

## 11. Ejecutar la aplicación en modo desarrollo

### 11.1. Inicia la aplicación

#### Linux/Mac

```bash
mvn spring-boot:run
```

#### Windows (CMD)

```cmd
mvn spring-boot:run
```

#### Windows (PowerShell)

```powershell
mvn spring-boot:run
```

### 11.2. Verifica que la aplicación esté corriendo

Abre tu navegador y visita:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console**: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:reviewdb`)

## 12. Probar los endpoints

### 12.1. Crear un speaker

#### Linux/Mac

```bash
curl -X POST http://localhost:8080/speakers \
  -H "Content-Type: application/json" \
  -d '{"name":"Pablo","organization":"Red Hat","talks":[{"title":"Lorem ipsum dolor sit amet","duration":15}]}'
```

#### Windows (CMD)

```cmd
curl -X POST http://localhost:8080/speakers -H "Content-Type: application/json" -d "{\"name\":\"Pablo\",\"organization\":\"Red Hat\",\"talks\":[{\"title\":\"Lorem ipsum dolor sit amet\",\"duration\":15}]}"
```

#### Windows (PowerShell)

```powershell
$body = @{
    name = "Pablo"
    organization = "Red Hat"
    talks = @(
        @{
            title = "Lorem ipsum dolor sit amet"
            duration = 15
        }
    )
} | ConvertTo-Json -Depth 3

Invoke-WebRequest -Uri http://localhost:8080/speakers -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Headers
```

### 12.2. Listar speakers con ordenamiento

#### Linux/Mac/Windows (CMD)

```bash
curl "http://localhost:8080/speakers?sortBy=name"
```

#### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/speakers?sortBy=name" -Method GET | Select-Object -ExpandProperty Content
```

### 12.3. Listar speakers con paginación

#### Linux/Mac/Windows (CMD)

```bash
curl "http://localhost:8080/speakers?pageSize=1&pageIndex=0"
```

#### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/speakers?pageSize=1&pageIndex=0" -Method GET | Select-Object -ExpandProperty Content
```

### 12.4. Eliminar un speaker

#### Linux/Mac/Windows (CMD)

```bash
curl -v -X DELETE http://localhost:8080/speakers/1
```

#### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri http://localhost:8080/speakers/1 -Method DELETE
```

**Respuesta esperada:** Status `204 No Content`

### 12.5. Intentar eliminar un speaker inexistente

#### Linux/Mac/Windows (CMD)

```bash
curl -v -X DELETE http://localhost:8080/speakers/123456
```

#### Windows (PowerShell)

```powershell
try {
    Invoke-WebRequest -Uri http://localhost:8080/speakers/123456 -Method DELETE
} catch {
    $_.Exception.Response.StatusCode
}
```

**Respuesta esperada:** Status `404 Not Found`

## 13. Construir y ejecutar la aplicación empaquetada (Opcional)

### 13.1. Construir la aplicación

#### Linux/Mac/Windows (CMD)

```bash
mvn clean package
```

#### Windows (PowerShell)

```powershell
mvn clean package
```

### 13.2. Ejecutar la aplicación empaquetada

#### Linux/Mac

```bash
java -jar target/develop-review-1.0.0-SNAPSHOT.jar
```

#### Windows (CMD)

```cmd
java -jar target\develop-review-1.0.0-SNAPSHOT.jar
```

#### Windows (PowerShell)

```powershell
java -jar target\develop-review-1.0.0-SNAPSHOT.jar
```

## Resumen

En este laboratorio has aprendido a:
- Convertir un POJO simple a una entidad JPA con `@Entity`, `@Id`, `@GeneratedValue`
- Crear un Repository con `JpaRepository` para acceso a datos
- Implementar paginación con `PageRequest.of()` y `Sort.by()`
- Usar `@RequestParam` para recibir parámetros de consulta
- Implementar operaciones CRUD con Spring Data JPA
- Crear relaciones `@OneToMany` entre entidades JPA
- Usar `ResponseEntity` para respuestas HTTP personalizadas
- Manejar errores con `ResponseStatusException`

## Próximos pasos

- Explora más características de Spring Data JPA como queries personalizadas (`@Query`)
- Implementa validación usando Bean Validation (`@NotNull`, `@Size`, etc.)
- Agrega manejo de excepciones personalizado con `@ControllerAdvice`
- Implementa filtros y búsqueda avanzada con `Specification`
- Explora las características de Spring Data REST

---

**Enjoy!**

**Joe**
