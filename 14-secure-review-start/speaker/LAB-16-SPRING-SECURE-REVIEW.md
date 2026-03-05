# LAB 16: Spring Boot 4 – Secure Review (Speaker + OIDC + CORS)

**Autor:** José Díaz  
**Repositorio:** springboot4-samples

Abre el proyecto **14-secure-review-start** y sigue los pasos para integrar el servicio **speaker** con Keycloak (SSO) y configurar CORS y autorización por roles.

## Objetivo

- Integrar la aplicación speaker con Keycloak (OIDC).
- Configurar CORS para un frontend en localhost (puertos 9000 y 8080).
- Proteger los endpoints con roles `read` y `modify`.

## Prerrequisitos

- Java 21+
- Maven 3.8+
- Docker o Podman (para Keycloak)
- (Opcional) Node.js para el frontend speaker-dashboard

---

## 1. Abrir el proyecto

### 1.1 Navegar al directorio

**Windows:**

```cmd
cd 14-secure-review-start\speaker
```

**Mac/Linux:**

```bash
cd 14-secure-review-start/speaker
```

### 1.2 Abrir el proyecto en tu editor

Abre la carpeta `speaker` (o el repo `springboot4-samples`) en tu IDE.

---

## 2. Integrar con el servidor SSO (Keycloak)

Configuración objetivo:

- **URL de Keycloak:** http://localhost:8888  
- **Realm:** quarkus  
- **Client Id:** backend-service  
- **Client secret:** secret  

### 2.1 Dependencias

El proyecto ya incluye en `pom.xml`:

- `spring-boot-starter-oauth2-resource-server`
- `spring-boot-starter-security`

No es necesario añadir más dependencias para OIDC.

### 2.2 Configurar OIDC en application.properties

En `src/main/resources/application.properties` asegúrate de tener:

```properties
# OIDC / Keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8888/realms/quarkus
```

### 2.3 Levantar Keycloak (si no está en marcha)

Desde una carpeta que tenga un `docker-compose.yml` con Keycloak y un `realm.json` (por ejemplo el del lab 15), o ejecutando directamente:

**Docker:**

```bash
docker run -d --name keycloak -p 8888:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0 start-dev
```

**Podman:**

```bash
podman run -d --name keycloak -p 8888:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0 start-dev
```

Luego configura el realm `quarkus`, el cliente `backend-service` (secret `secret`) y los usuarios `user` / `superuser` (por ejemplo importando un `realm.json` como en el LAB 15).

Comprueba en el navegador: **http://localhost:8888**

---

## 3. Configurar CORS para el frontend

La aplicación debe aceptar peticiones desde:

- http://localhost:9000 (típico en desarrollo del frontend)
- http://localhost:8080 (producción u otro backend)

### 3.1 Configuración en application.properties

Añade o verifica en `src/main/resources/application.properties`:

```properties
# CORS
cors.allowed-origins=http://localhost:9000,http://localhost:8080,http://172.17.0.1:8080
```

### 3.2 Clase de configuración CORS (WebMvcConfig)

Debe existir una clase que implemente `WebMvcConfigurer` y registre CORS. Por ejemplo en `com.bcp.training.speaker.WebMvcConfig`:

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:8080}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("accept", "authorization", "content-type", "x-requested-with")
                .exposedHeaders("Content-Disposition")
                .maxAge(86400);
    }
}
```

Si en el proyecto ya existe `WebMvcConfig` con esta lógica, solo verifica que `cors.allowed-origins` incluya los orígenes que uses (9000, 8080).

---

## 4. Configurar autorización de los endpoints

Reglas objetivo:

- **GET /speakers** → rol `read`
- **GET /speakers/{uuid}** → rol `read`
- **POST /speakers** → rol `modify`
- **PUT /speakers/{uuid}** → rol `modify`

### 4.1 Asegurar SpeakerController

En `SpeakerController` (o el controlador que expone `/speakers`), usa `@PreAuthorize` en cada método:

```java
@GetMapping
@PreAuthorize("hasRole('read')")
public List<Speaker> getSpeakers() { ... }

@GetMapping("/{uuid}")
@PreAuthorize("hasRole('read')")
public Speaker findByUuid(@PathVariable String uuid) { ... }

@PostMapping
@PreAuthorize("hasRole('modify')")
@ResponseStatus(HttpStatus.CREATED)
public Speaker insert(@RequestBody Speaker speaker) { ... }

@PutMapping("/{uuid}")
@PreAuthorize("hasRole('modify')")
public Speaker update(@PathVariable String uuid, @RequestBody Speaker speaker) { ... }
```

Asegúrate de que en la clase de configuración de seguridad esté habilitado el uso de anotaciones:

```java
@EnableMethodSecurity
public class SecurityConfig { ... }
```

### 4.2 Mapeo de roles de Keycloak

En `SecurityConfig`, el conversor de JWT debe mapear los roles del realm (y si aplica del cliente) a autoridades `ROLE_read` y `ROLE_modify`. El proyecto ya incluye un `KeycloakRealmRoleConverter` que lee `realm_access.roles` y `resource_access.backend-service.roles`. Verifica que el realm en Keycloak tenga los roles `read` y `modify` asignados a los usuarios de prueba.

---

## 5. Ejecutar y probar la aplicación

### 5.1 Iniciar el servicio speaker

**Windows:**

```cmd
cd speaker
mvnw.cmd spring-boot:run
```

**Mac/Linux:**

```bash
cd speaker
./mvnw spring-boot:run
```

Por defecto el servicio corre en **http://localhost:8082** (según `server.port` en `application.properties`).

### 5.2 Comprobar que Keycloak está en marcha

Abre en el navegador **http://localhost:8888** y verifica que Keycloak responde.

Si Keycloak no está corriendo:

**Docker:**

```bash
docker compose up -d
```

**Podman:**

```bash
podman compose up -d
```

(Asegúrate de tener un `docker-compose.yml` o `compose.yml` que levante Keycloak con el realm correcto.)

### 5.3 Probar con curl (con token)

Obtén un token (por ejemplo con el script del LAB 15 o con curl al endpoint de token de Keycloak) y luego:

**Mac/Linux:**

```bash
# Obtener token (ajusta usuario/contraseña y URL si usas otro realm)
export TOKEN=$(curl -s -X POST "http://localhost:8888/realms/quarkus/protocol/openid-connect/token" \
  --user "backend-service:secret" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=superuser" -d "password=redhat" -d "grant_type=password" | jq -r '.access_token')

# GET /speakers (requiere rol read)
curl -s http://localhost:8082/speakers -H "Authorization: Bearer $TOKEN" | jq
```

**Windows (PowerShell):**

```powershell
# Después de obtener $env:TOKEN con get_token.ps1 u otro método:
Invoke-RestMethod -Uri http://localhost:8082/speakers -Headers @{ Authorization = "Bearer $env:TOKEN" }
```

---

## 6. (Opcional) Probar con el frontend speaker-dashboard

Si tienes un frontend SPA (por ejemplo speaker-dashboard) que apunta al backend en el puerto 8082:

### 6.1 Iniciar el backend

Como en 5.1:

**Windows:**

```cmd
mvnw.cmd spring-boot:run
```

**Mac/Linux:**

```bash
./mvnw spring-boot:run
```

### 6.2 Iniciar el frontend

**Windows:**

```cmd
npm install
npm run dev
```

**Mac/Linux:**

```bash
npm install
npm run dev
```

Abre **http://localhost:9000** (o el puerto que use el frontend).

### 6.3 Probar con usuario solo lectura (`user` / `redhat`)

- Inicia sesión con `user` / `redhat`.
- Debes ver el listado de speakers (GET con rol `read`).
- Intenta **Add a speaker**. Debe fallar con 403 porque `user` no tiene rol `modify`.

### 6.4 Probar con usuario con permisos (`superuser` / `redhat`)

- En otra ventana o tras cerrar sesión, entra con `superuser` / `redhat`.
- **Add a speaker** con nombre y apellido debe funcionar (rol `modify`).

### 6.5 Detener la aplicación

En la terminal donde corre Spring Boot, pulsa `Ctrl+C` (o en Windows `Ctrl+C` en la ventana de la consola).

---

## Resumen de endpoints y roles

| Método | Endpoint           | Rol      |
|--------|--------------------|----------|
| GET    | `/speakers`        | `read`   |
| GET    | `/speakers/{uuid}` | `read`   |
| POST   | `/speakers`        | `modify` |
| PUT    | `/speakers/{uuid}` | `modify` |

## Comandos rápidos por SO

### Windows

```cmd
cd 14-secure-review-start\speaker
mvnw.cmd spring-boot:run
```

### Mac/Linux

```bash
cd 14-secure-review-start/speaker
./mvnw spring-boot:run
```

### Docker/Podman – Keycloak

```bash
docker compose up -d
# o
podman compose up -d
```

---

## Solución de problemas

- **401 en /speakers:** el token no se envía o es inválido. Comprueba `Authorization: Bearer <token>` y que el issuer en la app coincida con Keycloak.
- **403 en POST/PUT:** el usuario no tiene rol `modify`. Usa `superuser` o asigna el rol en Keycloak.
- **CORS:** verifica que el origen del frontend (ej. http://localhost:9000) esté en `cors.allowed-origins` y que `WebMvcConfig` esté cargado.
- **Keycloak no arranca:** revisa `docker logs keycloak` o `podman logs keycloak` y que el puerto 8888 esté libre.

---

Si llegaste hasta aquí, has completado el **Secure Review** con Spring Boot 4.

**José**
