# LAB 8: SPRING BOOT REACTIVE DEVELOP

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

## Objetivo

En este laboratorio aprenderás a:
- Implementar endpoints REST reactivos con Spring WebFlux
- Usar Spring Data R2DBC para acceso reactivo a PostgreSQL
- Trabajar con Project Reactor (`Mono`, `Flux`)
- Configurar R2DBC con PostgreSQL

## Prerequisitos

- **Java 21**
- **Maven 3.9+**
- **Docker o Podman** (para tests con Testcontainers)

## 1. Cargar el proyecto 08-reactive-develop-start

El proyecto contiene:
- `suggestions`: Servicio REST reactivo con solo `DELETE /suggestion` implementado

### Estado inicial

- `SuggestionController`: Solo tiene `deleteAll()` (DELETE)
- Los tests esperan `POST` (crear) y `GET /{id}` (obtener por ID) que aún no están implementados

## 2. Implementar POST /suggestion (crear sugerencia)

### 2.1. Abre `SuggestionController.java`

Ubicado en: `suggestions/src/main/java/com/bcp/training/SuggestionController.java`

### 2.2. Agrega el método create

```java
@PostMapping
public Mono<Suggestion> create(@RequestBody Suggestion newSuggestion) {
    return repository.save(newSuggestion);
}
```

**Explicación:**
- `@PostMapping`: Mapea peticiones POST a `/suggestion`
- `Mono<Suggestion>`: Retorna un único resultado de forma reactiva
- `repository.save()`: Persiste la entidad en PostgreSQL vía R2DBC

## 3. Implementar GET /suggestion/{id} (obtener por ID)

### 3.1. Agrega el método get

```java
@GetMapping("/{id}")
public Mono<Suggestion> get(@PathVariable Long id) {
    return repository.findById(id);
}
```

**Explicación:**
- `@GetMapping("/{id}")`: Mapea GET a `/suggestion/{id}`
- `@PathVariable Long id`: Extrae el ID de la URL
- `repository.findById(id)`: Busca por ID (retorna `Mono.empty()` si no existe)

## 4. Implementar GET /suggestion (listar todas)

### 4.1. Agrega el método list

```java
@GetMapping
public Flux<Suggestion> list() {
    return repository.findAll();
}
```

**Explicación:**
- `Flux<Suggestion>`: Retorna un flujo de múltiples resultados
- `repository.findAll()`: Lista todas las sugerencias

## 5. Ejecutar la aplicación

```bash
cd suggestions
mvn spring-boot:run
```

Requiere PostgreSQL en `localhost:5432` con base de datos `suggestions` (o configurar en `application.yml`).

## 6. Ejecutar los tests

```bash
cd suggestions
mvn test
```

Los tests usan Testcontainers para levantar PostgreSQL automáticamente. Necesitas Docker o Podman en ejecución.

## Comparación Quarkus vs Spring Boot 4

| Concepto | Quarkus | Spring Boot 4 |
|----------|---------|----------------|
| REST reactivo | JAX-RS + Mutiny | Spring WebFlux |
| Tipos reactivos | `Uni`, `Multi` | `Mono`, `Flux` |
| Persistencia reactiva | Hibernate Reactive Panache | Spring Data R2DBC |
| Base de datos | Dev Services (PostgreSQL) | Testcontainers (tests) |

## Solución completa

Ver `08-reactive-develop-solution/LAB 8 - SPRING BOOT REACTIVE DEVELOP - SOLUCIÓN.md` para el código completo de referencia.
