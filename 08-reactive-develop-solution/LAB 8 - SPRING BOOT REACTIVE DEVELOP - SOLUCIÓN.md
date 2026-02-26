# LAB 8: SPRING BOOT REACTIVE DEVELOP — Solución de Referencia

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 08-reactive-develop-solution

Este documento contiene el **código completo** de la solución.

---

## 1. SuggestionController.java

**Ruta:** `suggestions/src/main/java/com/bcp/training/SuggestionController.java`

```java
package com.bcp.training;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suggestion")
public class SuggestionController {

    private final SuggestionRepository repository;

    public SuggestionController(SuggestionRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Mono<Suggestion> create(@RequestBody Suggestion newSuggestion) {
        return repository.save(newSuggestion);
    }

    @GetMapping("/{id}")
    public Mono<Suggestion> get(@PathVariable Long id) {
        return repository.findById(id);
    }

    @GetMapping
    public Flux<Suggestion> list() {
        return repository.findAll();
    }

    @DeleteMapping
    public Mono<Long> deleteAll() {
        return repository.count().flatMap(count -> repository.deleteAll().thenReturn(count));
    }
}
```

---

## 2. application.yml

**Ruta:** `suggestions/src/main/resources/application.yml`

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/suggestions
    username: postgres
    password: postgres
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

---

## Resumen de endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /suggestion | Crear sugerencia |
| GET | /suggestion/{id} | Obtener por ID |
| GET | /suggestion | Listar todas |
| DELETE | /suggestion | Eliminar todas |

---

## Ejecutar

```bash
cd suggestions
mvn spring-boot:run
mvn test
```
