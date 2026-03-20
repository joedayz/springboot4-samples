# LAB 23: SPRING BOOT 4 MONITOR TRACE

Este laboratorio replica la idea de Quarkus: tracing distribuido con OpenTelemetry y visualización en Jaeger para 3 microservicios:

- `solver` (puerto 8080): parsea y resuelve expresiones; delega a `adder` o `multiplier`
- `adder` (puerto 8081): suma (y llama al solver)
- `multiplier` (puerto 8082): multiplicación (y llama al solver)

## Requisitos

- Java 21 o superior
- Maven
- Docker o Podman

## Paso 1: Iniciar Jaeger

Con Docker:

```bash
docker run --rm --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 16686:16686 \
  -p 14268:14268 \
  jaegertracing/all-in-one:1.57
```

```powershell
docker run --rm --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 16686:16686 \
  -p 14268:14268 \
  jaegertracing/all-in-one:1.57
```

```bash
podman run --rm --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 16686:16686 \
  -p 14268:14268 \
  jaegertracing/all-in-one:1.57
```

Abre: http://localhost:16686

## Paso 2: Activar Tracing en cada servicio

En este proyecto el tracing está en dependencias, pero viene desactivado con:

`management.tracing.enabled=false`

Ahora habilítalo en los 3 servicios (repite en `adder/`, `multiplier/` y `solver/`):

### Para `adder`

Edita `adder/src/main/resources/application.properties` y configura:

```properties
spring.application.name=adder
management.tracing.enabled=true
management.otlp.tracing.endpoint=http://localhost:4317
management.tracing.sampling.probability=1.0
logging.pattern.console=%d{HH:mm:ss} %-5level traceId=%X{traceId}, spanId=%X{spanId}, parentId=%X{parentId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n
```

### Para `multiplier`

Edita `multiplier/src/main/resources/application.properties` y configura (cambiando el `spring.application.name`):

```properties
spring.application.name=multiplier
management.tracing.enabled=true
management.otlp.tracing.endpoint=http://localhost:4317
management.tracing.sampling.probability=1.0
logging.pattern.console=%d{HH:mm:ss} %-5level traceId=%X{traceId}, spanId=%X{spanId}, parentId=%X{parentId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n
```

### Para `solver`

Edita `solver/src/main/resources/application.properties` y configura (cambiando el `spring.application.name`):

```properties
spring.application.name=solver
management.tracing.enabled=true
management.otlp.tracing.endpoint=http://localhost:4317
management.tracing.sampling.probability=1.0
logging.pattern.console=%d{HH:mm:ss} %-5level traceId=%X{traceId}, spanId=%X{spanId}, parentId=%X{parentId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n
```

## Paso 3: Iniciar los 3 microservicios

En terminales separadas:

```bash
# Terminal 1 - solver
cd solver
mvn spring-boot:run

# Terminal 2 - adder
cd ../adder
mvn spring-boot:run

# Terminal 3 - multiplier
cd ../multiplier
mvn spring-boot:run
```

```powershell
# Terminal 1 - solver
cd solver
mvn spring-boot:run

# Terminal 2 - adder
cd ../adder
mvn spring-boot:run

# Terminal 3 - multiplier
cd ../multiplier
mvn spring-boot:run
```

## Paso 4: Capturar trazas en Jaeger

1. Llama al endpoint de `adder`:

```bash
curl http://localhost:8081/adder/5/3
```

```powershell
curl http://localhost:8081/adder/5/3
```

2. Llama al endpoint de `multiplier`:

```bash
curl http://localhost:8082/multiplier/5/3
```

```powershell
curl http://localhost:8082/multiplier/5/3
```

3. Llama al endpoint de `solver`:

```bash
curl "http://localhost:8080/solver/5*4+3"
```

```powershell
curl "http://localhost:8080/solver/5*4+3"
```

En Jaeger (http://localhost:16686) selecciona el servicio y usa **Find Traces**.

