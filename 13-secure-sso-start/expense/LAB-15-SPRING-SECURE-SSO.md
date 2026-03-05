# LAB 15: Spring Boot 4 – Secure SSO (OIDC con Keycloak)

**Autor:** José Díaz  
**Repositorio:** springboot4-samples

Guía para implementar Single Sign-On (SSO) en **Spring Boot 4** usando OpenID Connect (OIDC) y Keycloak como proveedor de identidad.

## Objetivos

- Configurar Keycloak como proveedor OIDC
- Integrar autenticación OIDC en una aplicación Spring Boot 4
- Implementar autorización por roles (`read`, `modify`, `delete`)
- Probar los endpoints REST con tokens de acceso

## Prerrequisitos

- Java 21+
- Maven 3.8+
- Docker o Podman
- Conocimientos básicos de REST y Spring Boot

## Arquitectura

- **Keycloak**: servidor de identidad (OIDC)
- **Spring Security OAuth2 Resource Server**: valida JWT emitidos por Keycloak
- **Roles**: `read`, `modify`, `delete` para control de acceso

---

## Paso 1: Configurar Keycloak

### Opción A: Docker

**Windows (PowerShell o CMD):**

```cmd
cd expense
docker compose up -d
```

**Mac/Linux:**

```bash
cd expense
docker compose up -d
```

### Opción B: Podman

**Windows (WSL2 o PowerShell):**

```powershell
cd expense
podman compose up -d
```

**Mac/Linux:**

```bash
cd expense
podman compose up -d
```

### Opción C: Podman sin compose

**Mac/Linux:**

```bash
cd expense
podman run -d \
  --name keycloak \
  -p 8888:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v $(pwd)/realm.json:/opt/keycloak/data/import/realm.json:Z \
  quay.io/keycloak/keycloak:24.0 \
  start-dev --import-realm
```

**Windows (PowerShell):**

```powershell
cd expense
podman run -d `
  --name keycloak `
  -p 8888:8080 `
  -e KEYCLOAK_ADMIN=admin `
  -e KEYCLOAK_ADMIN_PASSWORD=admin `
  -v "${PWD}/realm.json:/opt/keycloak/data/import/realm.json:Z" `
  quay.io/keycloak/keycloak:24.0 `
  start-dev --import-realm
```

### Verificar que Keycloak está en marcha

**Windows:**

```powershell
docker ps
# o con Podman:
podman ps
```

**Mac/Linux:**

```bash
docker ps
# o:
podman ps
```

Debe aparecer un contenedor `keycloak` con el puerto 8888.

- Consola de administración: **http://localhost:8888**
- Usuario: `admin`
- Contraseña: `admin`

El `realm.json` del proyecto ya define el realm `quarkus`, el cliente `backend-service` (secret `secret`) y los usuarios `user`/`superuser` (contraseña `redhat`).

---

## Paso 2: Configurar la aplicación Spring Boot

### 2.1 Dependencias

El proyecto ya incluye en `pom.xml`:

- `spring-boot-starter-oauth2-resource-server`
- `spring-boot-starter-security`

No hace falta añadir más dependencias para OIDC.

### 2.2 application.properties

En `src/main/resources/application.properties` debe estar:

```properties
# OIDC / Keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8888/realms/quarkus
```

Si Keycloak usa certificado autofirmado y da problemas, puedes usar (solo desarrollo):

```properties
# Opcional en dev si hay problemas TLS
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8888/realms/quarkus/protocol/openid-connect/certs
```

### 2.3 Base de datos

La aplicación usa PostgreSQL. Asegúrate de tener un servidor en `localhost:5432` (por ejemplo con Docker/Podman) o ajusta la URL en `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/expensedb
spring.datasource.username=developer
spring.datasource.password=developer
```

**PostgreSQL con Docker:**

```bash
docker run -d --name postgres-expense -p 5432:5432 \
  -e POSTGRES_USER=developer -e POSTGRES_PASSWORD=developer \
  -e POSTGRES_DB=expensedb postgres:14
```

**PostgreSQL con Podman:**

```bash
podman run -d --name postgres-expense -p 5432:5432 \
  -e POSTGRES_USER=developer -e POSTGRES_PASSWORD=developer \
  -e POSTGRES_DB=expensedb postgres:14
```

---

## Paso 3: Obtener un token de acceso

### Windows (PowerShell)

```powershell
cd expense
. .\get_token.ps1 superuser redhat
echo $env:TOKEN
```

Para usuario solo lectura:

```powershell
. .\get_token.ps1 user redhat
```

### Mac/Linux (bash)

```bash
cd expense
chmod +x get_token.sh
source ./get_token.sh superuser redhat
echo $TOKEN
```

Solo lectura:

```bash
source ./get_token.sh user redhat
```

### Sin scripts (curl)

**Mac/Linux:**

```bash
export TOKEN=$(curl -s -X POST "http://localhost:8888/realms/quarkus/protocol/openid-connect/token" \
  --user "backend-service:secret" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=superuser" \
  -d "password=redhat" \
  -d "grant_type=password" | jq -r '.access_token')
echo $TOKEN
```

**Windows (PowerShell) con curl (si está instalado):**

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8888/realms/quarkus/protocol/openid-connect/token" `
  -Method Post -Body @{
    grant_type="password"
    client_id="backend-service"
    client_secret="secret"
    username="superuser"
    password="redhat"
  } -ContentType "application/x-www-form-urlencoded"
$env:TOKEN = $response.access_token
```

---

## Paso 4: Ejecutar la aplicación

### Modo desarrollo

**Windows:**

```cmd
cd expense
mvnw.cmd spring-boot:run
```

**Mac/Linux:**

```bash
cd expense
./mvnw spring-boot:run
```

La API queda en **http://localhost:8080**.

### Modo producción (JAR)

**Windows:**

```cmd
mvnw.cmd clean package -DskipTests
java -jar target/expense-restful-service-1.0.0-SNAPSHOT.jar
```

**Mac/Linux:**

```bash
./mvnw clean package -DskipTests
java -jar target/expense-restful-service-1.0.0-SNAPSHOT.jar
```

---

## Paso 5: Probar los endpoints

### 5.1 Endpoint OIDC (roles del usuario)

**Mac/Linux:**

```bash
curl -s http://localhost:8080/oidc -H "Authorization: Bearer $TOKEN" | jq
```

**Windows (PowerShell):**

```powershell
Invoke-RestMethod -Uri http://localhost:8080/oidc -Headers @{ Authorization = "Bearer $env:TOKEN" }
```

### 5.2 Listar gastos (GET) – rol `read`

**Mac/Linux:**

```bash
curl -s http://localhost:8080/expense -H "Authorization: Bearer $TOKEN" | jq
```

**Windows:**

```powershell
Invoke-RestMethod -Uri http://localhost:8080/expense -Headers @{ Authorization = "Bearer $env:TOKEN" }
```

### 5.3 Crear gasto (POST) – rol `modify`

**Mac/Linux:**

```bash
curl -X POST http://localhost:8080/expense \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Concurrency modern in Java",
    "paymentMethod": "CREDIT_CARD",
    "amount": 65.00
  }'
```

**Windows (PowerShell):**

```powershell
$headers = @{
  "Content-Type" = "application/json"
  "Authorization" = "Bearer $env:TOKEN"
}
$body = '{"name":"Almuerzo","paymentMethod":"CREDIT_CARD","amount":45.75}'
Invoke-RestMethod -Uri http://localhost:8080/expense -Method Post -Headers $headers -Body $body
```

### 5.4 Eliminar gasto (DELETE) – rol `delete`

**Mac/Linux:**

```bash
UUID=3f1817f2-3dcf-472f-a8b2-77bfe25e79d1
curl -X DELETE -H "Authorization: Bearer $TOKEN" "http://localhost:8080/expense/$UUID"
```

**Windows:**

```powershell
$UUID = "3f1817f2-3dcf-472f-a8b2-77bfe25e79d1"
Invoke-RestMethod -Uri "http://localhost:8080/expense/$UUID" -Method Delete -Headers @{ Authorization = "Bearer $env:TOKEN" }
```

---

## Paso 6: Probar autorización por roles

### Con usuario `user` (solo lectura)

**Mac/Linux:**

```bash
source ./get_token.sh user redhat
curl -s http://localhost:8080/expense -H "Authorization: Bearer $TOKEN" | jq   # debe funcionar
curl -X POST http://localhost:8080/expense -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Test","paymentMethod":"CREDIT_CARD","amount":10.00}'   # debe devolver 403
```

**Windows:**

```powershell
. .\get_token.ps1 user redhat
Invoke-RestMethod -Uri http://localhost:8080/expense -Headers @{ Authorization = "Bearer $env:TOKEN" }
# POST con mismo token debe fallar con 403
```

### Con usuario `superuser` (todos los permisos)

**Mac/Linux:**

```bash
source ./get_token.sh superuser redhat
curl -s http://localhost:8080/expense -H "Authorization: Bearer $TOKEN" | jq
curl -X POST http://localhost:8080/expense -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Test","paymentMethod":"CREDIT_CARD","amount":10.00}'
```

**Windows:**

```powershell
. .\get_token.ps1 superuser redhat
Invoke-RestMethod -Uri http://localhost:8080/expense -Headers @{ Authorization = "Bearer $env:TOKEN" }
```

---

## Resumen de endpoints

| Método   | Endpoint        | Rol      | Descripción                    |
|----------|-----------------|----------|--------------------------------|
| GET      | `/oidc`         | Ninguno  | Info y roles del usuario       |
| GET      | `/expense`      | `read`  | Listar gastos                  |
| POST     | `/expense`      | `modify`| Crear gasto                    |
| PUT      | `/expense`      | `modify`| Actualizar gasto               |
| DELETE   | `/expense/{uuid}` | `delete` | Eliminar gasto               |

## Usuarios del realm

| Usuario    | Contraseña | Roles              |
|-----------|------------|--------------------|
| `user`    | `redhat`   | `read`             |
| `superuser` | `redhat` | `read`, `modify`, `delete` |

---

## Docker / Podman: detener servicios

**Docker:**

```bash
docker compose down
```

**Podman:**

```bash
podman compose down
# o:
podman stop keycloak
podman rm keycloak
```

**Ver logs de Keycloak:**

```bash
docker logs -f keycloak
# o:
podman logs -f keycloak
```

---

## Solución de problemas

- **Keycloak no arranca:** revisa logs con `docker logs keycloak` o `podman logs keycloak`. Comprueba que el puerto 8888 esté libre.
- **Error de conexión a Keycloak:** verifica que Keycloak responda:  
  `curl http://localhost:8888/realms/quarkus/.well-known/openid-configuration`
- **401 Unauthorized:** token expirado o inválido. Obtén uno nuevo con `get_token.sh` / `get_token.ps1`.
- **403 Forbidden:** el usuario no tiene el rol necesario. Usa `superuser` para todas las operaciones.
- **jq no encontrado (Mac/Linux):** puedes quitar `| jq` de los `curl` y ver el JSON sin formatear, o instalar `jq`.

---

## Referencias

- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

---

¡Felicitaciones al completar el laboratorio!
