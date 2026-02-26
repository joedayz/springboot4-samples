# 08-reactive-develop-start

Laboratorio de desarrollo reactivo con Spring Boot 4.

## Objetivo

Implementar endpoints REST reactivos para el recurso `Suggestion` usando Spring WebFlux y R2DBC.

## Estado inicial

- **SuggestionController**: Solo tiene `DELETE /suggestion` (deleteAll)
- **Tests**: Esperan `POST` (create) y `GET /{id}` (get) que aún no están implementados

## Tareas

1. Implementar `POST /suggestion` - crear sugerencia
2. Implementar `GET /suggestion/{id}` - obtener por ID
3. Implementar `GET /suggestion` - listar todas

## Requisitos

- Java 21
- Maven 3.9+
- Docker o Podman (para tests con Testcontainers)

## Ejecutar

```bash
cd suggestions
mvn spring-boot:run
```

## Tests

```bash
cd suggestions
mvn test
```

Requiere Docker/Podman para Testcontainers (PostgreSQL).
