# LAB 22: SPRING BOOT 4 MONITOR METRICS

Autor: José Díaz

Objetivo

Este laboratorio te guía paso a paso para agregar métricas de monitoreo a una aplicación Spring Boot 4 usando Micrometer + Prometheus, y visualizar estas métricas en Grafana.

## Prerrequisitos

- Java 21 o superior
- Maven instalado
- Docker o Podman instalado
- Editor de código (VS Code, IntelliJ IDEA, etc.)

## Estructura

- `Expense` representa un gasto.
- `ExpenseService` contiene la lógica de persistencia (en memoria) y los delays simulados.
- `ExpenseResource` expone la API REST bajo `GET/POST/PUT/DELETE /expenses`.

## Paso 1: Habilitar Prometheus en Spring Boot (micrometer-registry-prometheus)

En este `start`, el endpoint `actuator/prometheus` puede no estar disponible hasta que agregues la dependencia del registry.

1. Abre `pom.xml` y agrega:

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. Reinicia la aplicación.

3. Verifica:

```bash
curl http://localhost:8080/actuator/prometheus
```

## Paso 2: Counters para GET y POST

### 2.1 Inyectar `MeterRegistry` en `ExpenseResource`

Agrega un `MeterRegistry` para poder registrar contadores:

```java
private final MeterRegistry registry;

public ExpenseResource(ExpenseService expenseService, MeterRegistry registry) {
    this.expenseService = expenseService;
    this.registry = registry;
}
```

### 2.2 Decorar el GET con `@Counted`

En el método `list()` agrega:

```java
@GetMapping
@Counted(value = "callsToGetExpenses")
public Set<Expense> list() {
    return expenseService.list();
}
```

Import:

```java
import io.micrometer.core.annotation.Counted;
```

### 2.3 Contador para el POST

En `create(...)` incrementa:

```java
registry.counter("callsToPostExpenses").increment();
return expenseService.create(expense);
```

### 2.4 Generar tráfico de prueba

Ejecuta el script:

```bash
./scripts/simulate-traffic.sh
```

### 2.5 Verificar las métricas (counters)

Ejecuta:

```bash
curl http://localhost:8080/actuator/prometheus | grep -E "callsToGetExpenses_total|callsToPostExpenses_total"
```

Deberías ver métricas con nombres tipo `..._total`.

## Paso 3: Timer para el POST

### 3.1 Medir tiempo de ejecución en `create(...)`

Actualiza el POST para registrar un `Timer` alrededor de `expenseService.create(expense)`:

```java
registry.counter("callsToPostExpenses").increment();

return registry.timer("expenseCreationTime")
    .recordCallable(() -> expenseService.create(expense));
```

### 3.2 Verificar las métricas del timer

```bash
curl http://localhost:8080/actuator/prometheus | grep "expenseCreationTime_seconds"
```

Micrometer suele exponer métricas tipo:

- `expenseCreationTime_seconds_count`
- `expenseCreationTime_seconds_sum`
- y variantes como `_max`.

> Los valores pueden variar porque el servicio simula delays aleatorios.

## Paso 4: Gauge `timeSinceLastGetExpenses`

### 4.1 Crear `StopWatch` y registrar el gauge en `initMeters`

Agrega un atributo:

```java
private final StopWatch stopWatch = StopWatch.createStarted();
```

Y un `@PostConstruct`:

```java
@PostConstruct
public void initMeters() {
    registry.gauge(
        "timeSinceLastGetExpenses",
        Tags.of("description", "Time since the last call to GET /expenses"),
        stopWatch,
        StopWatch::getTime
    );
}
```

Imports:

```java
import org.apache.commons.lang3.time.StopWatch;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
```

### 4.2 Resetear/arrancar el reloj en el GET

En `list()`:

```java
stopWatch.reset();
stopWatch.start();
return expenseService.list();
```

### 4.3 Verificar el gauge

```bash
curl http://localhost:8080/actuator/prometheus | grep "timeSinceLastGetExpenses"
```

> El valor estará en milisegundos.

## Paso 5: Visualizar en Grafana

### 5.1 Levantar stack de monitoreo

Desde `expense-service`:

```bash
docker-compose up -d
```

### 5.2 Abrir Grafana y dashboard

1. Abre http://localhost:3000
2. Login: `admin` / `admin`
3. Ve a **Dashboards** → **Expense Service Metrics Dashboard**

### 5.3 Genera tráfico otra vez

Ejecuta `./scripts/simulate-traffic.sh` para que aparezcan datos en Grafana.

## Resumen de métricas

- `callsToGetExpenses_total`: contador de llamadas GET a `/expenses`
- `callsToPostExpenses_total`: contador de llamadas POST a `/expenses`
- `expenseCreationTime_seconds`: timer del POST
- `timeSinceLastGetExpenses`: gauge en milisegundos desde la última llamada GET

