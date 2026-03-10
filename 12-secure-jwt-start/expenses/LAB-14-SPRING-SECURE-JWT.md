# LAB 14: Spring Boot 4 – Secure JWT

**Autor:** José Díaz  
**Repositorio:** springboot4-samples

Este proyecto demuestra cómo implementar autenticación y autorización con JWT (JSON Web Tokens) en **Spring Boot 4** usando Spring Security y OAuth2 Resource Server.

## Descripción del proyecto

- **JwtController**: endpoint `GET /jwt/{username}` que genera un JWT para un usuario dado.
- **UserController**: endpoint `GET /user/expenses` con los gastos del usuario autenticado (rol **USER**).
- **AdminController**: endpoint `GET /admin/expenses` que lista todos los gastos (rol **ADMIN**).

## Prerrequisitos

- Java 21 o superior
- Maven 3.8+ (o el wrapper del proyecto: `mvnw`)
- Docker o Podman (opcional, para ejecutar la app en contenedor)

## Configuración inicial

### 1. Generar las claves RSA

Es necesario generar las claves RSA para firmar y verificar los JWTs antes de ejecutar la aplicación.

#### Windows (CMD):

```cmd
cd expenses
mvn exec:java -Dexec.mainClass="com.bcp.training.jwt.GenerateKeys"
```

#### Windows (PowerShell):

```powershell
cd expenses
.\mvn exec:java "-Dexec.mainClass=com.bcp.training.jwt.GenerateKeys"
```

#### Mac/Linux:

```bash
cd expenses
./mvn exec:java -Dexec.mainClass="com.bcp.training.jwt.GenerateKeys"
```

Se generan los archivos `privateKey.pem` y `publicKey.pem` en:

- **Windows:** `C:\Users\<USERNAME>\DO378\secure-jwt\`
- **Mac/Linux:** `${HOME}/DO378/secure-jwt/`

Comprueba que existan ambos `.pem` antes de seguir.

### 2. Configurar application.properties

En `src/main/resources/application.properties` las rutas ya usan `${user.home}`. En Windows equivale a `C:\Users\<USERNAME>\`. Si generaste las claves en otra ruta, ajusta:

#### Windows (ruta fija de ejemplo):

```properties
jwt.issuer=https://example.com/redhattraining
jwt.public-key-location=file:C:/Users/<USERNAME>/DO378/secure-jwt/publicKey.pem
jwt.private-key-location=file:C:/Users/<USERNAME>/DO378/secure-jwt/privateKey.pem
```

#### Mac/Linux (por defecto en el proyecto):

```properties
jwt.issuer=https://example.com/redhattraining
jwt.public-key-location=file:${user.home}/DO378/secure-jwt/publicKey.pem
jwt.private-key-location=file:${user.home}/DO378/secure-jwt/privateKey.pem
```

## Ejecutar la aplicación

### Modo desarrollo

#### Windows:

```cmd
cd expenses
mvnw.cmd spring-boot:run
```

#### Mac/Linux:

```bash
cd expenses
./mvnw spring-boot:run
```

La aplicación queda en: **http://localhost:8080**

### Modo producción (JAR)

#### Windows:

```cmd
mvnw.cmd clean package -DskipTests
java -jar target/expenses-service-1.0.0-SNAPSHOT.jar
```

#### Mac/Linux:

```bash
./mvnw clean package -DskipTests
java -jar target/expenses-service-1.0.0-SNAPSHOT.jar
```

## Docker / Podman

### Construir y ejecutar imagen JVM

#### Docker

```bash
cd expenses
./mvnw clean package -DskipTests
docker build -t spring/expenses-jwt:latest .
docker run -i --rm -p 8080:8080 spring/expenses-jwt:latest
```

**Windows (PowerShell):**

```powershell
cd expenses
.\mvnw.cmd clean package -DskipTests
docker build -t spring/expenses-jwt:latest .
docker run -i --rm -p 8080:8080 spring/expenses-jwt:latest
```

#### Podman

```bash
cd expenses
./mvnw clean package -DskipTests
podman build -t spring/expenses-jwt:latest .
podman run -i --rm -p 8080:8080 spring/expenses-jwt:latest
```

**Windows (PowerShell):**

```powershell
cd expenses
.\mvnw.cmd clean package -DskipTests
podman build -t spring/expenses-jwt:latest .
podman run -i --rm -p 8080:8080 spring/expenses-jwt:latest
```

> **Nota:** Para que el contenedor use las claves, monta el directorio donde están (por ejemplo `-v ${HOME}/DO378/secure-jwt:/app/keys:ro`) y configura en la app las rutas a `/app/keys/privateKey.pem` y `publicKey.pem`, o usa variables de entorno para las rutas.

## Tests

### Todos los tests

#### Windows:

```cmd
mvnw.cmd test
```

#### Mac/Linux:

```bash
./mvnw test
```

### Un test concreto

#### Windows:

```cmd
mvnw.cmd test -Dtest=JwtGeneratorTest
mvnw.cmd test -Dtest=UserResourceTest
mvnw.cmd test -Dtest=AdminResourceTest
```

#### Mac/Linux:

```bash
./mvnw test -Dtest=JwtGeneratorTest
./mvnw test -Dtest=UserResourceTest
./mvnw test -Dtest=AdminResourceTest
```

## Endpoints

### Generar JWT

**Mac/Linux:**

```bash
# JWT usuario normal
curl http://localhost:8080/jwt/john

# JWT administrador
curl http://localhost:8080/jwt/admin
```

**Windows (PowerShell):**

```powershell
Invoke-RestMethod -Uri http://localhost:8080/jwt/john
Invoke-RestMethod -Uri http://localhost:8080/jwt/admin
```

### Acceder a expenses (requiere JWT)

**Mac/Linux:**

```bash
# Obtener JWT
TOKEN=$(curl -s http://localhost:8080/jwt/john)

# Gastos del usuario
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/user/expenses

# Todos los gastos (solo admin)
ADMIN_TOKEN=$(curl -s http://localhost:8080/jwt/admin)
curl -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/admin/expenses
```

**Windows (PowerShell):**

```powershell
$TOKEN = (Invoke-RestMethod -Uri http://localhost:8080/jwt/john)
Invoke-RestMethod -Uri http://localhost:8080/user/expenses -Headers @{ Authorization = "Bearer $TOKEN" }

$ADMIN_TOKEN = (Invoke-RestMethod -Uri http://localhost:8080/jwt/admin)
Invoke-RestMethod -Uri http://localhost:8080/admin/expenses -Headers @{ Authorization = "Bearer $ADMIN_TOKEN" }
```

## Instrucciones del laboratorio (paso a paso)

### Paso 1: Revisar el código

1. **JwtController.java** – endpoint `GET /jwt/{username}` que genera el JWT.
2. **UserController.java** – endpoint `GET /user/expenses` (rol USER).
3. **AdminController.java** – endpoint `GET /admin/expenses` (rol ADMIN).
4. **application.properties** – configuración de issuer y rutas de claves.

### Paso 2: Ejecutar los tests iniciales

Ejecuta todos los tests; varios fallarán porque los JWTs del *start* no llevan `groups` (roles):

#### Windows:

```cmd
mvnw.cmd test
```

#### Mac/Linux:

```bash
./mvnw test
```

Es esperable que fallen tests que asumen 401/403 o claims/roles correctos.

### Paso 3: Completar JwtGenerator

#### 3a. Modificar `generateJwtForRegularUser`

Añade en el token: `subject`, audiencia `expenses.example.com`, claim `locale` y el grupo **USER** (para que Spring lo mapee a `ROLE_USER`):

```java
public String generateJwtForRegularUser(String username) {
    return Jwts.builder()
            .issuer(issuer)
            .subject(username)
            .claim("upn", username + "@example.com")
            .claim("aud", "expenses.example.com")
            .claim("locale", "en_US")
            .claim("groups", List.of("USER"))
            .signWith(privateKey)
            .compact();
}
```

#### 3b. Modificar `generateJwtForAdmin`

Añade los grupos **USER** y **ADMIN**:

```java
public String generateJwtForAdmin(String username) {
    return Jwts.builder()
            .issuer(issuer)
            .subject(username)
            .claim("upn", username + "@example.com")
            .claim("locale", "en_US")
            .claim("groups", List.of("USER", "ADMIN"))
            .signWith(privateKey)
            .compact();
}
```

#### 3c. Comprobar tests de JWT

#### Windows:

```cmd
mvnw.cmd test -Dtest=JwtGeneratorTest
```

#### Mac/Linux:

```bash
./mvnw test -Dtest=JwtGeneratorTest
```

### Paso 4: Asegurar UserController

El controlador ya usa `@PreAuthorize("hasRole('USER')")` en el método. Si en tu versión no está, añade:

```java
@GetMapping("/expenses")
@PreAuthorize("hasRole('USER')")
public List<Expense> listUserExpenses(@AuthenticationPrincipal Jwt jwt) {
    // ...
}
```

Comprueba:

#### Windows:

```cmd
mvnw.cmd test -Dtest=UserResourceTest
```

#### Mac/Linux:

```bash
./mvnw test -Dtest=UserResourceTest
```

### Paso 5: Asegurar AdminController

De forma análoga, el endpoint de admin debe tener `@PreAuthorize("hasRole('ADMIN')")`. Verifica:

#### Windows:

```cmd
mvnw.cmd test -Dtest=AdminResourceTest
```

#### Mac/Linux:

```bash
./mvnw test -Dtest=AdminResourceTest
```

### Paso 6: Verificar todos los tests

#### Windows:

```cmd
mvnw.cmd test
```

#### Mac/Linux:

```bash
./mvnw test
```

Todos los tests deberían pasar.

## Estructura del proyecto

```
expenses/
├── src/
│   ├── main/
│   │   ├── java/com/bcp/training/
│   │   │   ├── expenses/
│   │   │   │   ├── ExpensesApplication.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── Expense.java
│   │   │   │   ├── ExpensesService.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── AdminController.java
│   │   │   └── jwt/
│   │   │       ├── JwtGenerator.java
│   │   │       ├── JwtController.java
│   │   │       └── GenerateKeys.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/...
└── pom.xml
```

## Dependencias principales (Spring Boot 4)

- `spring-boot-starter-web` – REST API
- `spring-boot-starter-security` – Seguridad
- `spring-boot-starter-oauth2-resource-server` – Validación JWT
- `jjwt-api` / `jjwt-impl` – Generación y firma de JWTs

## Solución de problemas

- **No se encuentran las claves PEM:** ejecuta `GenerateKeys` y revisa que las rutas en `application.properties` coincidan con tu SO.
- **Tests 401/403:** revisa que los JWTs incluyan el claim `groups` con `USER` y/o `ADMIN` y que `SecurityConfig` use el conversor que mapea `groups` a `ROLE_*`.
- **Docker/Podman:** ejecuta `./mvnw package` (o `mvnw.cmd package` en Windows) antes de construir la imagen; si usas claves dentro del contenedor, configura rutas o variables de entorno.

## Referencias

- [Spring Security OAuth2 Resource Server (JWT)](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Boot 4 Security](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.security)

---

¡Felicitaciones al completar el laboratorio!
