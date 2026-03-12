# 13-secure-review-start (Spring Boot 4)

Revisión de seguridad: servicio **speaker** (OAuth2 Resource Server / JWT con Keycloak) y **speaker-dashboard** (React + Keycloak).

## Módulos

- **speaker** (puerto 8082): API REST de speakers con JPA (H2), protegida con JWT. Roles: `read` (GET) y `modify` (POST, PUT). Endpoints: `GET /speakers`, `GET /speakers/{uuid}`, `POST /speakers`, `PUT /speakers/{uuid}`.
- **speaker-dashboard**: Frontend React/TypeScript con PatternFly y Keycloak; consume el API enviando el token Bearer.

## Requisitos

- Keycloak en `http://localhost:8888` con realm **quarkus** (puedes levantarlo con el mismo `docker-compose` del proyecto):
  - En la carpeta `speaker`: `docker-compose -f docker-compose.yaml up -d`
  - El archivo `realm.json` en esa carpeta se importa al arrancar (realm quarkus, clients backend-service y frontend-service, usuarios user/superuser).
- Usuarios de prueba: **user** (rol `read`, password `redhat`) y **superuser** (roles `read`, `modify`, `delete`, password `redhat`).

## Cómo ejecutar

1. Levantar Keycloak con el mismo `docker-compose` del lab (en la carpeta `speaker`):
   ```bash
   cd speaker && docker-compose -f docker-compose.yaml up -d
   ```
   Keycloak quedará en http://localhost:8888 (admin/admin). El realm **quarkus** se importa desde `realm.json`.

2. Arrancar **speaker**:
   ```bash
   cd speaker && mvn spring-boot:run
   ```

3. Configurar el dashboard: copiar `.env.example` a `.env` y ajustar si hace falta:
   ```bash
   cd speaker-dashboard && cp .env.example .env
   ```

4. Arrancar **speaker-dashboard**:
   ```bash
   cd speaker-dashboard && npm install && npm run dev
   ```
   Abre http://localhost:8080 (o el puerto que indique webpack-dev-server). Inicia sesión con Keycloak y se listarán los speakers.

## Variables de entorno

- **speaker** (application.properties): `spring.security.oauth2.resourceserver.jwt.issuer-uri`, `cors.allowed-origins`.
- **speaker-dashboard** (.env): `BACKEND` (URL del API), `REACT_APP_KEYCLOAK_URL`, `REACT_APP_KEYCLOAK_REALM`, `REACT_APP_CLIENT_ID`.

## Equivalencias Quarkus → Spring Boot 4

- `quarkus-oidc` + `quarkus-smallrye-jwt` → `spring-boot-starter-oauth2-resource-server` (JWT)
- `@RolesAllowed("read")` → `@PreAuthorize("hasRole('read')")`
- Roles en JWT (realm_access / resource_access) → `KeycloakRealmRoleConverter` que expone `ROLE_read` y `ROLE_modify`
