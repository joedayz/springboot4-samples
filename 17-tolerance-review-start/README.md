# 17-tolerance-review-start (Spring Boot 4)

Revisión de tolerancia a fallos: servicio **speaker** (JPA) y servicio **session** (JPA, RestClient al servicio speaker).

**Guía del lab:** Ver [17-17-17-17-17-17-LAB-20-GUIDE.md](17-17-17-17-17-17-LAB-20-GUIDE.md) para las tareas (health, fallback, retry, circuit breaker, timeout) con Spring Boot 4 y Resilience4j.

## Módulos

- **speaker** (puerto 8082): API de speakers con JPA (H2). Endpoints: `GET /speaker`, `GET /speaker/sorted?sort=...`, `GET /speaker/{uuid}`, `GET /speaker/search?query=...&sort=...`, `POST /speaker`, `PUT /speaker/{uuid}`, `DELETE /speaker/{uuid}`.
- **session** (puerto 8081): API de sesiones con JPA (H2), enriquece speakers con datos del servicio speaker. Endpoints: `GET /sessions`, `GET /sessions/{id}`, `POST /sessions`, `PUT /sessions/{id}`, `DELETE /sessions/{id}`, `GET /sessions/{id}/speakers`, `PUT /sessions/{id}/speakers/{name}`, `DELETE /sessions/{id}/speakers/{name}`.

## Cómo ejecutar

1. Arrancar **speaker** (debe ir primero):
   ```bash
   cd speaker && mvn spring-boot:run
   ```

2. Arrancar **session**:
   ```bash
   cd session && mvn spring-boot:run
   ```

La variable `speaker.service.url` (por defecto `http://localhost:8082`) puede sobreescribirse con `SPEAKER_SERVICE_URL` para entornos de producción.
