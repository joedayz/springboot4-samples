# 21-monitor-review-start (Spring Boot 4)

Monitor review: speakers service, sessions service y dashboard React.

## Módulos

- **speakers** (puerto 8082): API de speakers con JPA (H2). Endpoints: `GET /speaker`, `GET /speaker/sorted?sort=...`, `GET /speaker/{uuid}`, `POST /speaker`, `PUT /speaker/{uuid}`.
- **sessions** (puerto 8081): API de sesiones en memoria; enriquece datos con el servicio speakers. Endpoints: `GET /sessions`, `GET /sessions/{id}`.
- **dashboard**: frontend React/TypeScript que consume ambos servicios.

## Cómo ejecutar

1. Arrancar **speakers** (debe ir primero):
   ```bash
   cd speakers && mvn spring-boot:run
   ```

2. Arrancar **sessions**:
   ```bash
   cd sessions && mvn spring-boot:run
   ```

3. Arrancar el **dashboard**:
   ```bash
   cd dashboard && npm install && npm start
   ```

El dashboard usa por defecto:
- `REACT_APP_SESSION_SERVICE=http://localhost:8081`
- `REACT_APP_SPEAKER_SERVICE=http://localhost:8082`

Para otro host/puerto, crear `.env` en `dashboard/` con:
```
REACT_APP_SESSION_SERVICE=http://localhost:8081
REACT_APP_SPEAKER_SERVICE=http://localhost:8082
```

## CORS

Ambos servicios tienen CORS habilitado para que el dashboard (origen distinto) pueda llamar a las APIs.
