# LAB 5.5: SPRING BOOT DEVELOP REVIEW - SOLUCIÓN

**Autor:** José Díaz  
**Github Repo:** https://github.com/joedayz/springboot4-samples.git

## Objetivo

Este documento contiene la solución completa del laboratorio de desarrollo y revisión con Spring Boot 4. Aquí encontrarás:
- La implementación completa de todas las funcionalidades
- Explicaciones detalladas de cada paso
- Comparación con la versión Quarkus (Panache)
- Mejores prácticas y recomendaciones

## Comparación Quarkus vs Spring Boot

| Concepto | Quarkus | Spring Boot 4 |
|----------|---------|---------------|
| Persistencia | Hibernate ORM Panache | Spring Data JPA |
| Entidades | `PanacheEntity` (herencia) | `@Entity` + `@Id` + getters/setters |
| Repositorio | Active Record (`Speaker.findAll()`) | Repository Pattern (`SpeakerRepository`) |
| REST | JAX-RS (`@Path`, `@GET`, `@POST`) | Spring MVC (`@RequestMapping`, `@GetMapping`, `@PostMapping`) |
| JSON | JSON-B | Jackson |
| Transacciones | `jakarta.transaction.Transactional` | `org.springframework.transaction.annotation.Transactional` |
| Paginación | `.page(pageIndex, pageSize)` | `PageRequest.of(pageIndex, pageSize)` |
| Ordenamiento | `Sort.by("field")` (Panache) | `Sort.by("field")` (Spring Data) |
| Base de datos dev | PostgreSQL + Dev Services | H2 en memoria |
| OpenAPI/Swagger | SmallRye OpenAPI | SpringDoc OpenAPI |

## Estructura de la Solución

### 1. Dependencias en pom.xml

El archivo `pom.xml` incluye todas las dependencias necesarias:

```xml
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
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
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
    <!-- Dependencias de test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Comparación con Quarkus:**
- `spring-boot-starter-data-jpa` reemplaza a `quarkus-hibernate-orm-panache` + `quarkus-hibernate-orm`
- `h2` reemplaza a `quarkus-jdbc-postgresql` (usamos H2 en memoria, no necesitamos PostgreSQL externo)
- `springdoc-openapi-starter-webmvc-ui` reemplaza a `quarkus-smallrye-openapi`
- `spring-boot-starter-test` + `rest-assured` reemplaza a `quarkus-junit5` + `rest-assured`

### 2. Configuración de application.yml

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

**Explicación:**
- `jdbc:h2:mem:reviewdb` - Base de datos H2 en memoria (no necesita PostgreSQL externo)
- `h2.console.enabled: true` - Habilita la consola web H2 en `/h2-console`
- `ddl-auto: create-drop` - Recrea el esquema de base de datos en cada inicio (útil para desarrollo)
- `show-sql: true` - Muestra las queries SQL en la consola

**Ventaja sobre Quarkus:** No necesitas instalar/ejecutar PostgreSQL. H2 corre en memoria dentro de la misma JVM.

### 3. Entidad Talk

**Archivo:** `src/main/java/com/bcp/training/speaker/Talk.java`

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

    // getters y setters...
}
```

**Comparación con Quarkus:**
- En Quarkus, `Talk extends PanacheEntity` proporciona `id` automáticamente y usa campos públicos
- En Spring Boot, usamos `@Id` + `@GeneratedValue` explícitamente y campos privados con getters/setters
- Spring Boot requiere un constructor sin argumentos para JPA

### 4. Entidad Speaker

**Archivo:** `src/main/java/com/bcp/training/speaker/Speaker.java`

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

    // getters y setters...
}
```

**Comparación con Quarkus:**
- En Quarkus: `Speaker extends PanacheEntity` con campos públicos
- En Spring Boot: `@Entity` con `@Id` + `@GeneratedValue`, campos privados con getters/setters
- `orphanRemoval = true` asegura que al remover un Talk de la lista, se elimina de la BD

### 5. SpeakerRepository

**Archivo:** `src/main/java/com/bcp/training/speaker/SpeakerRepository.java`

```java
package com.bcp.training.speaker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeakerRepository extends JpaRepository<Speaker, Long> {
}
```

**Comparación con Quarkus:**
- En Quarkus Panache, no se necesita un repositorio separado - las entidades tienen métodos estáticos (`Speaker.findAll()`, `Speaker.deleteById()`)
- En Spring Boot, el patrón Repository separa la lógica de acceso a datos de las entidades
- `JpaRepository` proporciona: `findAll()`, `save()`, `deleteById()`, `existsById()`, paginación, etc.

### 6. SpeakerController Completo

**Archivo:** `src/main/java/com/bcp/training/speaker/SpeakerController.java`

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

**Explicación de métodos:**

#### getSpeakers()
- Usa `speakerRepository.findAll(pageRequest)` con paginación y ordenamiento
- `PageRequest.of(pageIndex, pageSize, Sort.by(field))` combina paginación y ordenamiento
- `.getContent()` extrae la lista de resultados del objeto `Page`

**Comparación con Quarkus:**
```java
// Quarkus Panache:
Speaker.findAll(Sort.by(filterSortBy(sortBy))).page(pageIndex, pageSize).list();

// Spring Boot:
PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(filteredSortBy));
speakerRepository.findAll(pageRequest).getContent();
```

#### createSpeaker()
- `speakerRepository.save(speaker)` persiste la entidad y retorna la entidad con `id` generado
- `ServletUriComponentsBuilder` construye la URI de la ubicación del nuevo recurso
- Retorna `201 Created` con headers `id` y `location`

**Comparación con Quarkus:**
```java
// Quarkus Panache:
newSpeaker.persist();
Response.created(uri).header("id", newSpeaker.id).build();

// Spring Boot:
Speaker saved = speakerRepository.save(speaker);
ResponseEntity.created(location).header("id", String.valueOf(saved.getId())).build();
```

#### deleteSpeaker()
- `speakerRepository.existsById(id)` verifica si existe
- `speakerRepository.deleteById(id)` elimina la entidad
- `ResponseStatusException(HttpStatus.NOT_FOUND)` equivale a `NotFoundException` de JAX-RS

**Comparación con Quarkus:**
```java
// Quarkus Panache:
if (!Speaker.deleteById(id)) { throw new NotFoundException(); }

// Spring Boot:
if (!speakerRepository.existsById(id)) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
speakerRepository.deleteById(id);
```

## Ejecutar la Aplicación

### Modo Desarrollo

#### Linux/Mac

```bash
cd develop-review
mvn spring-boot:run
```

#### Windows (CMD)

```cmd
cd develop-review
mvn spring-boot:run
```

#### Windows (PowerShell)

```powershell
cd develop-review
mvn spring-boot:run
```

### Verificar que la aplicación esté corriendo

Abre tu navegador y visita:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console**: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:reviewdb`)

### Ejecutar Tests

#### Linux/Mac/Windows (CMD)

```bash
mvn test
```

#### Windows (PowerShell)

```powershell
mvn test
```

## Probar los Endpoints

### 1. Crear un Speaker

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

**Respuesta esperada:** Status `201 Created` con headers `location` e `id`

### 2. Crear un segundo Speaker

#### Linux/Mac

```bash
curl -X POST http://localhost:8080/speakers \
  -H "Content-Type: application/json" \
  -d '{"name":"Noelia","organization":"Red Hat","talks":[{"title":"Consectetur adipiscing elit","duration":20}]}'
```

#### Windows (CMD)

```cmd
curl -X POST http://localhost:8080/speakers -H "Content-Type: application/json" -d "{\"name\":\"Noelia\",\"organization\":\"Red Hat\",\"talks\":[{\"title\":\"Consectetur adipiscing elit\",\"duration\":20}]}"
```

#### Windows (PowerShell)

```powershell
$body = @{
    name = "Noelia"
    organization = "Red Hat"
    talks = @(
        @{
            title = "Consectetur adipiscing elit"
            duration = 20
        }
    )
} | ConvertTo-Json -Depth 3

Invoke-WebRequest -Uri http://localhost:8080/speakers -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Headers
```

### 3. Listar Speakers (ordenamiento por defecto - id)

#### Linux/Mac/Windows (CMD)

```bash
curl http://localhost:8080/speakers
```

#### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri http://localhost:8080/speakers -Method GET | Select-Object -ExpandProperty Content
```

**Respuesta esperada:**
```json
[
  {"id":1,"name":"Pablo","organization":"Red Hat","talks":[{"id":1,"title":"Lorem ipsum dolor sit amet","duration":15}]},
  {"id":2,"name":"Noelia","organization":"Red Hat","talks":[{"id":2,"title":"Consectetur adipiscing elit","duration":20}]}
]
```

### 4. Listar Speakers ordenados por nombre

#### Linux/Mac/Windows (CMD)

```bash
curl "http://localhost:8080/speakers?sortBy=name"
```

#### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/speakers?sortBy=name" -Method GET | Select-Object -ExpandProperty Content
```

**Respuesta esperada:** Noelia aparece primero (orden alfabético)

### 5. Listar Speakers con paginación

#### Linux/Mac/Windows (CMD)

```bash
curl "http://localhost:8080/speakers?pageSize=1&pageIndex=0"
```

#### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/speakers?pageSize=1&pageIndex=0" -Method GET | Select-Object -ExpandProperty Content
```

**Respuesta esperada:** Solo el primer Speaker (Pablo)

#### Obtener la segunda página:

#### Linux/Mac/Windows (CMD)

```bash
curl "http://localhost:8080/speakers?pageSize=1&pageIndex=1"
```

#### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/speakers?pageSize=1&pageIndex=1" -Method GET | Select-Object -ExpandProperty Content
```

**Respuesta esperada:** Solo el segundo Speaker (Noelia)

### 6. Eliminar un Speaker

#### Linux/Mac/Windows (CMD)

```bash
curl -v -X DELETE http://localhost:8080/speakers/1
```

#### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri http://localhost:8080/speakers/1 -Method DELETE
```

**Respuesta esperada:** Status `204 No Content`

### 7. Intentar eliminar un Speaker inexistente

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

## Construir y Ejecutar

### Construir la aplicación

#### Linux/Mac/Windows (CMD)

```bash
mvn clean package
```

#### Windows (PowerShell)

```powershell
mvn clean package
```

### Ejecutar la aplicación empaquetada

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

## Mejores Prácticas Implementadas

1. **Repository Pattern**: Separación clara entre entidades y acceso a datos
2. **Constructor Injection**: Inyección de dependencias por constructor (inmutable, testeable)
3. **Validación de entrada**: El método `filterSortBy()` valida y sanitiza los parámetros de ordenamiento
4. **Códigos HTTP apropiados**: 
   - `201 Created` para creación exitosa
   - `204 No Content` para eliminación exitosa
   - `404 Not Found` para recursos no encontrados
5. **Documentación OpenAPI**: SpringDoc genera documentación automática disponible en Swagger UI
6. **Paginación**: Implementada usando Spring Data para evitar cargar grandes cantidades de datos
7. **Relaciones JPA**: Uso correcto de `@OneToMany` con cascadas y `orphanRemoval`
8. **H2 en memoria**: Base de datos embebida para desarrollo sin dependencias externas

## Puntos Clave de Aprendizaje

1. **Spring Data JPA vs Panache**: 
   - Panache usa el patrón Active Record (`entity.persist()`, `Entity.findAll()`)
   - Spring Data JPA usa el patrón Repository (`repository.save()`, `repository.findAll()`)
   - Ambos simplifican el acceso a datos, pero con enfoques diferentes

2. **Paginación en Spring Data**:
   - `PageRequest.of(pageIndex, pageSize, sort)` crea el request de paginación
   - `.getContent()` extrae la lista de resultados
   - El índice de página comienza en 0

3. **Ordenamiento en Spring Data**:
   - `Sort.by("fieldName")` ordena ascendente
   - `Sort.by(Sort.Direction.DESC, "fieldName")` ordena descendente

4. **ResponseEntity vs JAX-RS Response**:
   - Spring Boot usa `ResponseEntity` para respuestas HTTP personalizadas
   - `ResponseEntity.created(uri)` crea una respuesta `201 Created`
   - `ResponseStatusException` es equivalente a `NotFoundException` de JAX-RS

5. **Relaciones JPA**:
   - `CascadeType.ALL` propaga todas las operaciones
   - `orphanRemoval = true` elimina hijos huérfanos automáticamente
   - Útil cuando los objetos hijos no tienen sentido sin el padre

## Troubleshooting

### Error: "Failed to configure a DataSource"

**Solución:** Verifica que la dependencia `spring-boot-starter-data-jpa` y `h2` estén en el `pom.xml`

### Error: "Table not found"

**Solución:** 
- Verifica que `ddl-auto: create-drop` esté configurado en `application.yml`
- Reinicia la aplicación para que Hibernate cree las tablas

### Tests fallan con "Connection refused"

**Solución:**
- Los tests usan `@SpringBootTest(webEnvironment = RANDOM_PORT)` que levanta un servidor en un puerto aleatorio
- `@LocalServerPort` inyecta el puerto correcto
- Verifica que `RestAssured.port = port` esté en `@BeforeEach`

### Error: "No qualifying bean of type 'SpeakerRepository'"

**Solución:**
- Verifica que `SpeakerRepository` extienda `JpaRepository<Speaker, Long>`
- Verifica que esté en un paquete escaneado por `@SpringBootApplication`

---

**Enjoy!**

**Joe**
