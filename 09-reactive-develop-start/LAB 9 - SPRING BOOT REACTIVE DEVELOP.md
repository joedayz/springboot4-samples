# LAB 9 - Spring Boot Reactive Develop - Guía Paso a Paso

## 📋 Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Configuración Inicial del Proyecto](#configuración-inicial-del-proyecto)
3. [Configurar Base de Datos](#configurar-base-de-datos)
4. [Implementar Endpoints Reactivos](#implementar-endpoints-reactivos)
5. [Ejecutar la Aplicación](#ejecutar-la-aplicación)
6. [Probar los Endpoints](#probar-los-endpoints)
7. [Ejecutar Tests](#ejecutar-tests)
8. [Construir y Ejecutar con Docker](#construir-y-ejecutar-con-docker)
9. [Construir y Ejecutar con Podman](#construir-y-ejecutar-con-podman)
10. [Solución de Problemas](#solución-de-problemas)

---

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Java 21** o superior
- **Maven 3.8+** (o usar el wrapper incluido)
- **Docker** o **Podman** (para ejecutar PostgreSQL)
- **Git** (opcional, para clonar el repositorio)

### Verificar Instalaciones

#### Linux / macOS:
```bash
java -version
mvn -version
docker --version
# o
podman --version
```

#### Windows (PowerShell):
```powershell
java -version
mvn -version
docker --version
# o
podman --version
```

#### Windows (CMD):
```cmd
java -version
mvn -version
docker --version
REM o
podman --version
```

---

## 🚀 Configuración Inicial del Proyecto

### Paso 1: Navegar al Directorio del Proyecto

#### Linux / macOS:
```bash
cd 09-reactive-develop-start/suggestions
```

#### Windows (PowerShell):
```powershell
cd 09-reactive-develop-start\suggestions
```

#### Windows (CMD):
```cmd
cd 09-reactive-develop-start\suggestions
```

### Paso 2: Verificar Estructura del Proyecto

#### Linux / macOS / Windows (PowerShell):
```bash
ls -la
# o en PowerShell:
Get-ChildItem
```

#### Windows (CMD):
```cmd
dir
```

Deberías ver archivos como `pom.xml`, `compose.yml`, y el directorio `src/`.

---

## 🗄️ Configurar Base de Datos

### Paso 3: Verificar Configuración de PostgreSQL

El proyecto usa **Spring Boot Docker Compose** (equivalente a Quarkus Dev Services). Al ejecutar la aplicación, Spring Boot arranca PostgreSQL automáticamente desde `compose.yml`.

#### Verificar compose.yml

El archivo `compose.yml` en el directorio `suggestions/` debe contener:

```yaml
services:
  postgres:
    image: postgres:14.1
    environment:
      POSTGRES_DB: suggestions
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
```

#### Verificar application.yml

El archivo `src/main/resources/application.yml` debe tener:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/suggestions
    username: postgres
    password: postgres
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

**Nota:** Si usas **Podman**, consulta la sección [Solución de Problemas](#problema-2-spring-boot-docker-compose-con-podman).

---

## 💻 Implementar Endpoints Reactivos

### Paso 4: Revisar la Entidad Suggestion

Abre el archivo `src/main/java/com/bcp/training/Suggestion.java` y verifica que tenga la siguiente estructura:

```java
package com.bcp.training;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("suggestion")
public class Suggestion {

    @Id
    public Long id;
    public Long clientId;
    public Long itemId;

    public Suggestion() {
    }

    public Suggestion(Long clientId, Long itemId) {
        this.clientId = clientId;
        this.itemId = itemId;
    }
}
```

**Nota:** La entidad usa Spring Data R2DBC con `@Table` y `@Id`. Los campos `client_id` y `item_id` se mapean automáticamente desde la base de datos.

### Paso 5: Revisar el Repositorio

El archivo `src/main/java/com/bcp/training/SuggestionRepository.java` extiende `R2dbcRepository`:

```java
package com.bcp.training;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface SuggestionRepository extends R2dbcRepository<Suggestion, Long> {
}
```

Proporciona métodos como `save()`, `findById()`, `findAll()`, `deleteAll()` sin código adicional.

### Paso 6: Implementar Endpoints en SuggestionController

Abre el archivo `src/main/java/com/bcp/training/SuggestionController.java`. El controlador debe tener solo `deleteAll()` inicialmente. Agrega los siguientes métodos:

#### 6.1: Agregar Imports Necesarios

Al inicio del archivo, asegúrate de tener estos imports:

```java
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.*;
```

#### 6.2: Implementar Endpoint POST (Crear Sugerencia)

Agrega este método dentro de la clase `SuggestionController`:

```java
@PostMapping
public Mono<Suggestion> create(@RequestBody Suggestion newSuggestion) {
    return repository.save(newSuggestion);
}
```

**Explicación:**
- `@PostMapping`: Mapea peticiones POST a `/suggestion`
- `Mono<Suggestion>`: Retorna un único resultado de forma reactiva (equivalente a `Uni` en Quarkus)
- `repository.save()`: Persiste la entidad en PostgreSQL vía R2DBC

#### 6.3: Implementar Endpoint GET por ID

Agrega este método:

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

#### 6.4: Implementar Endpoint GET para Listar Todas

Agrega este método:

```java
@GetMapping
public Flux<Suggestion> list() {
    return repository.findAll();
}
```

**Explicación:**
- `Flux<Suggestion>`: Retorna un flujo de múltiples resultados (equivalente a `Multi` en Quarkus)
- `repository.findAll()`: Lista todas las sugerencias de forma reactiva

### Paso 7: Verificar el Código Completo

El archivo `SuggestionController.java` completo debería verse así:

```java
package com.bcp.training;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suggestion")
public class SuggestionController {

    private final SuggestionRepository repository;

    public SuggestionController(SuggestionRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Mono<Suggestion> create(@RequestBody Suggestion newSuggestion) {
        return repository.save(newSuggestion);
    }

    @GetMapping("/{id}")
    public Mono<Suggestion> get(@PathVariable Long id) {
        return repository.findById(id);
    }

    @GetMapping
    public Flux<Suggestion> list() {
        return repository.findAll();
    }

    @DeleteMapping
    public Mono<Long> deleteAll() {
        return repository.count().flatMap(count -> repository.deleteAll().thenReturn(count));
    }
}
```

---

## 🏃 Ejecutar la Aplicación

### Paso 8: Iniciar la Aplicación

Spring Boot Docker Compose arranca PostgreSQL automáticamente al iniciar la aplicación.

#### Linux / macOS:
```bash
mvn spring-boot:run
```

#### Windows (PowerShell):
```powershell
mvn spring-boot:run
```

#### Windows (CMD):
```cmd
mvn spring-boot:run
```

**Salida esperada:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
...
Started SuggestionsApplication in X.XXX seconds
```

**Nota:** La primera vez que ejecutes, Spring Boot descargará la imagen de PostgreSQL y creará el contenedor automáticamente (si usas Docker).

### Paso 9: Verificar que la Aplicación Está Corriendo

Abre tu navegador o usa curl:

#### Linux / macOS:
```bash
curl http://localhost:8080/suggestion
```

#### Windows (PowerShell):
```powershell
Invoke-WebRequest -Uri http://localhost:8080/suggestion -Method GET
```

#### Windows (CMD):
```cmd
curl http://localhost:8080/suggestion
```

Deberías recibir una respuesta vacía `[]` o un array vacío, lo cual es correcto si no hay sugerencias.

---

## 🧪 Probar los Endpoints

### Paso 10: Crear una Sugerencia

#### Linux / macOS:
```bash
curl -X POST http://localhost:8080/suggestion \
  -H "Content-Type: application/json" \
  -d '{"clientId": 1, "itemId": 103}'
```

#### Windows (PowerShell):
```powershell
$body = @{
    clientId = 1
    itemId = 103
} | ConvertTo-Json

Invoke-WebRequest -Uri http://localhost:8080/suggestion `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

#### Windows (CMD):
```cmd
curl -X POST http://localhost:8080/suggestion -H "Content-Type: application/json" -d "{\"clientId\": 1, \"itemId\": 103}"
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "clientId": 1,
  "itemId": 103
}
```

### Paso 11: Obtener una Sugerencia por ID

#### Linux / macOS:
```bash
curl http://localhost:8080/suggestion/1
```

#### Windows (PowerShell):
```powershell
Invoke-WebRequest -Uri http://localhost:8080/suggestion/1 -Method GET
```

#### Windows (CMD):
```cmd
curl http://localhost:8080/suggestion/1
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "clientId": 1,
  "itemId": 103
}
```

### Paso 12: Listar Todas las Sugerencias

#### Linux / macOS:
```bash
curl http://localhost:8080/suggestion
```

#### Windows (PowerShell):
```powershell
Invoke-WebRequest -Uri http://localhost:8080/suggestion -Method GET
```

#### Windows (CMD):
```cmd
curl http://localhost:8080/suggestion
```

**Respuesta esperada:**
```json
[
  {
    "id": 1,
    "clientId": 1,
    "itemId": 103
  }
]
```

### Paso 13: Eliminar Todas las Sugerencias

#### Linux / macOS:
```bash
curl -X DELETE http://localhost:8080/suggestion
```

#### Windows (PowerShell):
```powershell
Invoke-WebRequest -Uri http://localhost:8080/suggestion -Method DELETE
```

#### Windows (CMD):
```cmd
curl -X DELETE http://localhost:8080/suggestion
```

**Respuesta esperada:** Un número que indica cuántas sugerencias se eliminaron (ej. `1`).

---

## ✅ Ejecutar Tests

### Paso 14: Ejecutar Tests Unitarios

Requiere Docker o Podman. Los tests usan Testcontainers para levantar PostgreSQL.

#### Linux / macOS (Docker):
```bash
mvn test
```

#### Linux / macOS (Podman):
```bash
export DOCKER_HOST=unix://$HOME/.local/share/containers/podman/machine/qemu/podman.sock
mvn test
```

#### Windows (PowerShell - Docker):
```powershell
mvn test
```

#### Windows (PowerShell - Podman):
```powershell
$env:DOCKER_HOST = "npipe:////./pipe/docker_engine"
mvn test
```

#### Windows (CMD):
```cmd
mvn test
```

**Salida esperada:**
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Ver [CONTAINERS.md](../CONTAINERS.md) para más detalles sobre Podman.

---

## 🐳 Construir y Ejecutar con Docker

### Paso 15: Construir la Aplicación

Primero, detén la aplicación (Ctrl+C) y construye el proyecto:

#### Linux / macOS:
```bash
mvn clean package
```

#### Windows (PowerShell):
```powershell
mvn clean package
```

#### Windows (CMD):
```cmd
mvn clean package
```

**Nota:** Esto generará el JAR en `target/suggestions-1.0.0-SNAPSHOT.jar`.

### Paso 16: Construir Imagen Docker

Crea un `Dockerfile` en el directorio `suggestions/` si no existe:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Nota:** Para ejecutar en Docker, necesitas PostgreSQL. Usa docker-compose o ejecuta PostgreSQL por separado.

#### Linux / macOS / Windows:
```bash
docker build -t springboot/suggestions .
```

### Paso 17: Ejecutar con Docker Compose

Si tienes un `docker-compose.yml` con PostgreSQL y la aplicación:

```bash
docker compose up -d
```

---

## 🦫 Construir y Ejecutar con Podman

Podman es una alternativa a Docker que no requiere un daemon.

### Paso 18: Construir la Aplicación (igual que Paso 15)

### Paso 19: Construir Imagen Podman

#### Linux / macOS / Windows:
```bash
podman build -t springboot/suggestions .
```

### Paso 20: Ejecutar Contenedor Podman

#### Linux / macOS / Windows:
```bash
podman run -i --rm -p 8080:8080 springboot/suggestions
```

**Nota:** El contenedor necesita conectarse a PostgreSQL. Asegúrate de que PostgreSQL esté accesible (ej. `podman compose up -d` para la base de datos).

### Paso 21: Verificar Contenedores Podman

#### Listar contenedores en ejecución:
```bash
podman ps
```

#### Listar imágenes:
```bash
podman images
```

#### Detener un contenedor:
```bash
podman stop <container-id>
```

---

## 🔧 Solución de Problemas

### Problema 1: Error "Cannot find Maven"

**Solución:** Instala Maven o usa el wrapper si está incluido (`./mvnw` o `mvnw.cmd`).

#### Linux / macOS:
```bash
chmod +x mvnw
./mvnw --version
```

#### Windows:
El archivo `mvnw.cmd` debería funcionar directamente.

### Problema 2: Spring Boot Docker Compose con Podman

**Causa:** Spring Boot invoca `docker` directamente y no reconoce Podman.

**Solución A — Symlink (macOS/Linux):**
```bash
sudo ln -sf $(which podman) /usr/local/bin/docker
# Verificar: podman compose version
mvn spring-boot:run
```

**Solución B — Manual:** Levantar PostgreSQL y saltar Docker Compose:
```bash
podman compose up -d
mvn spring-boot:run -Dspring.docker.compose.skip=true
```

### Problema 3: Puerto 8080 ya está en uso

**Solución:** Cambia el puerto en `application.yml`:

```yaml
server:
  port: 8081
```

Luego actualiza las URLs de los endpoints a `http://localhost:8081`.

### Problema 4: Error de conexión a la base de datos

**Solución 1:** Verifica que Docker/Podman esté ejecutándose:

#### Docker:
```bash
docker ps
```

#### Podman:
```bash
podman ps
```

**Solución 2:** Si usas Podman, aplica la [Solución B del Problema 2](#problema-2-spring-boot-docker-compose-con-podman).

**Solución 3:** Verifica que `compose.yml` esté en el directorio `suggestions/` (donde ejecutas `mvn spring-boot:run`).

### Problema 5: Tests fallan

**Solución 1:** Para Podman, configura `DOCKER_HOST` antes de ejecutar tests:

#### macOS:
```bash
export DOCKER_HOST=unix://$HOME/.local/share/containers/podman/machine/qemu/podman.sock
```

#### Linux:
```bash
export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock
```

#### Windows PowerShell:
```powershell
$env:DOCKER_HOST = "npipe:////./pipe/docker_engine"
```

**Solución 2:** Verifica que todos los endpoints estén implementados correctamente.

**Solución 3:** Revisa los logs de error:

```bash
mvn test -X
```

### Problema 6: LinkError "failed to load the required native library"

**Causa:** Netty intenta usar transporte nativo (epoll/kqueue) que puede fallar en algunos entornos.

**Solución:** Ejecuta con:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dreactor.netty.native=false"
```

### Problema 7: Error "relation suggestion does not exist"

**Causa:** El esquema no se creó correctamente.

**Solución:** Verifica que `schema.sql` exista en `src/main/resources/` y que `spring.sql.init.mode` esté en `always` en `application.yml`.

---

## 📚 Recursos Adicionales

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [Project Reactor](https://projectreactor.io/)
- [Spring Boot Docker Compose](https://docs.spring.io/spring-boot/reference/features/dev-services.html)
- [Docker Documentation](https://docs.docker.com/)
- [Podman Documentation](https://podman.io/getting-started/)

---

## ✅ Checklist de Verificación

Antes de considerar el laboratorio completo, verifica:

- [ ] Base de datos configurada (compose.yml y application.yml)
- [ ] Entidad `Suggestion` con `@Table` y `@Id`
- [ ] Endpoint POST implementado
- [ ] Endpoint GET por ID implementado
- [ ] Endpoint GET para listar todas implementado
- [ ] Endpoint DELETE implementado (ya estaba)
- [ ] Aplicación inicia correctamente
- [ ] Todos los endpoints funcionan correctamente
- [ ] Tests unitarios pasan
- [ ] Imagen Docker/Podman se construye correctamente (opcional)

---

## 🎓 Resumen del Laboratorio

En este laboratorio has aprendido a:

1. ✅ Configurar un proyecto Spring Boot Reactivo
2. ✅ Usar Spring Data R2DBC con PostgreSQL
3. ✅ Configurar PostgreSQL con Docker Compose (equivalente a Quarkus Dev Services)
4. ✅ Implementar endpoints REST reactivos usando Project Reactor (`Mono` y `Flux`)
5. ✅ Usar R2dbcRepository para operaciones de base de datos
6. ✅ Probar endpoints con curl o PowerShell
7. ✅ Ejecutar tests con Testcontainers
8. ✅ Construir y ejecutar aplicaciones en contenedores Docker/Podman

---

## 📝 Comparación Quarkus vs Spring Boot 4

| Concepto | Quarkus | Spring Boot 4 |
|----------|---------|----------------|
| REST reactivo | JAX-RS + Mutiny | Spring WebFlux |
| Tipos reactivos | `Uni`, `Multi` | `Mono`, `Flux` |
| Persistencia reactiva | Hibernate Reactive Panache | Spring Data R2DBC |
| Base de datos (desarrollo) | Dev Services | Docker Compose |
| Base de datos (tests) | Dev Services | Testcontainers |

---

**¡Felicitaciones por completar el LAB 9 - Spring Boot Reactive Develop!** 🎉
