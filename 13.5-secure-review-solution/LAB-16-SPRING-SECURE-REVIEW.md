# LAB 16: SPRING BOOT 4 SECURE REVIEW

**Autor:** José Díaz  
**Repositorio:** https://github.com/JoeDayz/springboot4-samples (o tu fork)

Este documento es la **guía del lab** adaptada a Spring Boot 4. El proyecto **13-secure-review-solution** contiene el código de referencia ya implementado.

## Instrucciones (para trabajar con 13-secure-review-start)

Este ejercicio usa la aplicación **speaker** como backend. El backend se integra con un servidor Keycloak para autenticación y autorización (OAuth2 Resource Server con JWT). Además, el backend se integra con una aplicación front-end SPA (speaker-dashboard).

### 1. Abre la aplicación

#### 1.1. Navega al directorio 13-secure-review-start

#### 1.2. Abre el proyecto con tu editor favorito.

### 2. Integra la aplicación speaker con el servidor SSO (Keycloak)

Usa la siguiente configuración:
- **SSO Server URL:** http://localhost:8888
- **Keycloak realm:** quarkus
- **Client Id:** backend-service
- **Client secret:** secret

#### 2.1. Dependencias Spring Boot 4

En el módulo `speaker` el `pom.xml` debe incluir:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

#### 2.2. Configura la integración con OIDC/JWT

En `speaker/src/main/resources/application.properties`:

```properties
# OIDC / Keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8888/realms/quarkus
```

El backend valida los JWT emitidos por Keycloak (realm **quarkus**). No necesitas client-id/secret en el backend para solo validar tokens; Keycloak los firma y el backend verifica la firma con el issuer-uri.

### 3. Configura CORS para la aplicación speaker

La aplicación debe permitir solo peticiones desde localhost en el puerto 9000 (dev) u 8080 (prod). Denegar peticiones de otros orígenes.

#### 3.1. Opción A: Propiedades en `application.properties`

```properties
# CORS (lista separada por comas)
cors.allowed-origins=http://localhost:9000,http://localhost:8080
```

Y una clase de configuración que use este valor, por ejemplo `WebMvcConfig` implementando `WebMvcConfigurer` y en `addCorsMappings` usar `allowedOrigins(allowedOrigins.split(","))`, además de `allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")` y `allowedHeaders("accept", "authorization", "content-type", "x-requested-with")`.

#### 3.2. Opción B: Solo en `application.properties` (Spring Boot 2.4+)

Si prefieres no usar una clase Java, puedes definir un bean `CorsConfigurationSource` o usar las propiedades de Spring Boot para CORS según la versión.

En el proyecto de referencia se usa **Opción A** con `WebMvcConfig` y la propiedad `cors.allowed-origins`.

### 4. Configurar la autorización de los endpoints

- **GET /speakers:** requiere el rol `read`.
- **GET /speakers/{uuid}:** requiere el rol `read`.
- **POST /speakers:** requiere el rol `modify`.
- **PUT /speakers/{uuid}:** requiere el rol `modify`.

#### 4.1. Abre `com.bcp.training.speaker.SpeakerController`

En Spring Security con JWT usamos `@PreAuthorize` (no `@RolesAllowed` de Jakarta). Asegura los endpoints así:

- Para lectura: `@PreAuthorize("hasRole('read')")` en los métodos que correspondan a GET.
- Para escritura: `@PreAuthorize("hasRole('modify')")` en los métodos que correspondan a POST y PUT.

Habilita la seguridad por método en la configuración de seguridad con `@EnableMethodSecurity` (en la clase que configura `SecurityFilterChain`).

#### 4.2. Conversión de roles del JWT a autoridades Spring

El JWT de Keycloak trae los roles en `realm_access.roles` y/o `resource_access.backend-service.roles`. Spring Security espera autoridades del tipo `ROLE_read`, `ROLE_modify`. Necesitas un `JwtAuthenticationConverter` que use un `Converter<Jwt, Collection<GrantedAuthority>>` (por ejemplo una clase interna `KeycloakRealmRoleConverter`) que lea esos claims y devuelva `SimpleGrantedAuthority("ROLE_read")` etc. Así `hasRole('read')` coincidirá con `ROLE_read`.

En el proyecto de referencia esto está en `SecurityConfig.keycloakJwtConverter()`.

### 5. Opcional: usar el front-end speaker-dashboard para probar el speaker

#### 5.1. Inicia Keycloak

En el directorio `speaker` levanta Keycloak con el mismo `docker-compose` del proyecto:

**Docker:**
```bash
cd speaker && docker-compose -f docker-compose.yaml up -d
```

**Podman:**
```bash
cd speaker && podman-compose -f docker-compose.yaml up -d
```

El archivo `realm.json` en esa carpeta se importa al arrancar (realm **quarkus**, clients **backend-service** y **frontend-service**, usuarios **user** y **superuser**).

#### 5.2. Inicia el servicio speaker

```bash
cd speaker && mvn spring-boot:run
```

El backend quedará en http://localhost:8082.

#### 5.3. Comprueba que Keycloak está corriendo

En el navegador abre http://localhost:8888 y verifica que Keycloak responde (admin/admin para la consola de administración). La aplicación frontend redirigirá el login a Keycloak.

#### 5.4. Inicia el frontend

En la carpeta `speaker-dashboard`:

```bash
cp .env.example .env
npm install
npm run dev
```

Se abrirá la aplicación en el puerto configurado (por ejemplo 8080 o 9000). Usa el usuario **user** y contraseña **redhat**. Debes ver un dashboard con 4 speakers.

#### 5.5. Prueba crear un speaker (debe fallar)

Haz clic en **Add a speaker**. Ingresa nombre y apellido en first name y last name. Luego **Confirm**. Debe mostrarse un error, porque el usuario **user** solo tiene rol `read` y no está autorizado a crear speakers. Cierra las pestañas para desloguearte.

#### 5.6. Prueba con usuario autorizado

En una nueva ventana abre la URL del dashboard. Usa el usuario **superuser** y contraseña **redhat**.

#### 5.7. Prueba crear un speaker (debe funcionar)

Haz clic en **Add a speaker**, ingresa nombre y apellido y **Confirm**. La llamada debe completarse correctamente, porque **superuser** tiene el rol `modify`. Cierra la ventana del navegador.

#### 5.8. Detén las aplicaciones

En la terminal del speaker presiona `Ctrl+C` para detener Spring Boot. Si levantaste Keycloak con Docker/Podman, puedes detenerlo con `docker-compose -f docker-compose.yaml down` (o el equivalente en podman) dentro de `speaker`.

---

## Solución (este proyecto: 13-secure-review-solution)

Este proyecto **13-secure-review-solution** contiene la implementación de referencia del lab:

- **speaker**: Spring Boot 4 con `spring-boot-starter-oauth2-resource-server`, `SecurityConfig` (JWT + `KeycloakRealmRoleConverter`), `SpeakerController` con `@PreAuthorize("hasRole('read')")` y `@PreAuthorize("hasRole('modify')")`, y `WebMvcConfig` para CORS.
- **speaker-dashboard**: React + Keycloak, configurado para consumir el API con Bearer token.
- **speaker/docker-compose.yaml** y **speaker/realm.json**: mismo contenido que en el start para levantar Keycloak e importar el realm **quarkus**.

Puedes usar este proyecto como referencia o ejecutarlo directamente para validar el flujo completo.

---

## Equivalencias Quarkus → Spring Boot 4

| Quarkus | Spring Boot 4 |
|--------|-----------------|
| `quarkus-oidc` | `spring-boot-starter-oauth2-resource-server` |
| `quarkus.oidc.auth-server-url`, `client-id`, `credentials.secret` | `spring.security.oauth2.resourceserver.jwt.issuer-uri` (validación de JWT; el cliente confidencial se usa en otros flujos si aplica) |
| `quarkus.http.cors.*` | `WebMvcConfigurer.addCorsMappings` + propiedad `cors.allowed-origins` |
| `@RolesAllowed("read")` | `@PreAuthorize("hasRole('read')")` |
| `mvn quarkus:dev` | `mvn spring-boot:run` |
| `docker compose up -d` | `docker-compose -f docker-compose.yaml up -d` (en la carpeta `speaker`) |

---

Si llegaste hasta aquí, **felicitaciones**: has completado el security review con Spring Boot 4.

**José**
