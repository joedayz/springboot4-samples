# 09-reactive-develop-solution

Solución del laboratorio de desarrollo reactivo con Spring Boot 4.

## Implementación

- **POST /suggestion** - Crea una sugerencia
- **GET /suggestion/{id}** - Obtiene sugerencia por ID
- **GET /suggestion** - Lista todas las sugerencias
- **DELETE /suggestion** - Elimina todas las sugerencias

## Tecnologías

- Spring WebFlux (reactivo)
- Spring Data R2DBC
- PostgreSQL (R2DBC)
- Project Reactor (Mono, Flux)

## Ejecutar

**macOS / Linux / Windows:**
```bash
cd suggestions
mvn spring-boot:run
```

## Tests

Requiere Docker o Podman. Ver [CONTAINERS.md](../CONTAINERS.md) para Podman + Testcontainers.

```bash
cd suggestions
mvn test
```
