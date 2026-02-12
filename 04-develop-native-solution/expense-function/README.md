# expense-function (Native Solution)

Este proyecto es la **solución completa** del laboratorio de compilación nativa con Spring Boot 4 y GraalVM.

## Tecnologías

- Spring Boot 4.0.2
- GraalVM Native Image (via `native-maven-plugin`)
- Spring AOT (Ahead-of-Time processing)
- springdoc-openapi (Swagger UI)

## Ejecutar en modo JVM

```bash
cd expense-function
mvn spring-boot:run
```

## Compilar ejecutable nativo (requiere GraalVM)

```bash
mvn -Pnative native:compile -DskipTests
./target/expense-function
```

## Construir imagen de contenedor nativa (Buildpacks)

```bash
mvn -Pnative spring-boot:build-image -DskipTests
docker run --rm -p 8080:8080 expense-function:1.0.0-SNAPSHOT
```

## Construir con Dockerfile

```bash
# JVM
docker build -f src/main/docker/Dockerfile.jvm -t expense-function:jvm .

# Native
docker build -f src/main/docker/Dockerfile.native -t expense-function:native .
```

## Endpoints

- **API REST**: http://localhost:8080/expenses
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
