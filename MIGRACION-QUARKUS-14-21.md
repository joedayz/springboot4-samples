# Migración proyectos 14–21 (Quarkus → Spring Boot 4)

Proyectos migrados desde el repositorio **quarkus-bcp-2025** a **Spring Boot 4.0.2** en este repo.

## Proyectos migrados

| Proyecto | Descripción | Módulos |
|----------|-------------|---------|
| **14-deploy-k8s-start** | Deploy en K8s | `expense`, `expense-service`, `expense-client` |
| **15-tolerance-policies-start** | Políticas de tolerancia (Resilience4j) | `monitor` |
| **16-tolerance-health-start** | Health liveness/readiness | `calculator` |
| **17-tolerance-review-start** | Revisión tolerancia: speaker (JPA) + session (JPA, RestClient a speaker) | `speaker`, `session` |
| **18-monitor-logging-start** | Logging | `expenses` |
| **18-monitor-logging-solution** | Logging + ELK | `expenses` |
| **19-monitor-metrics-start** | Métricas (Actuator, Prometheus) | `expense-service` |
| **19-monitor-metrics-solution** | Métricas + Dashboards (Prometheus, Grafana) | `expense-service` |
| **20-monitor-trace-start** | Trazabilidad (adder, multiplier, solver) | `adder`, `multiplier`, `solver` |
| **21-monitor-review-start** | Revisión monitor: speakers, sessions, dashboard React | `speakers`, `sessions`, `dashboard` |

## Equivalencias Quarkus → Spring Boot 4

| Quarkus | Spring Boot 4 |
|---------|----------------|
| `quarkus-rest` (JAX-RS) | `spring-boot-starter-web` (@RestController, @GetMapping, etc.) |
| `quarkus-hibernate-orm-panache` | `spring-boot-starter-data-jpa` + JpaRepository |
| `quarkus-arc` / @ApplicationScoped | @Service, @Component, @Repository |
| `quarkus-smallrye-health` | `spring-boot-starter-actuator` + HealthIndicator |
| `quarkus-smallrye-fault-tolerance` | Resilience4j (`resilience4j-spring-boot3`) |
| MicroProfile REST Client | RestClient (Spring 6.1+) o WebClient |
| `@ConfigMapping` | @ConfigurationProperties |
| `quarkus-rest-jackson` | Incluido en spring-boot-starter-web |
| `quarkus-smallrye-openapi` | springdoc-openapi-starter-webmvc-ui |

## Cómo ejecutar

Cada carpeta (14, 15, 16, 18, 19, 20) contiene uno o más módulos Maven. Por ejemplo:

```bash
# 14 – expense (puerto 8080)
cd 14-deploy-k8s-start/expense && mvn spring-boot:run

# 14 – expense-service (puerto 8081) y expense-client (8080)
cd 14-deploy-k8s-start/expense-service && mvn spring-boot:run
cd 14-deploy-k8s-start/expense-client && mvn spring-boot:run

# 20 – orden: solver (8080), adder (8081), multiplier (8082)
cd 20-monitor-trace-start/solver && mvn spring-boot:run
cd 20-monitor-trace-start/adder && mvn spring-boot:run
cd 20-monitor-trace-start/multiplier && mvn spring-boot:run
```

## Proyectos no migrados en este lote

Todos los proyectos 14–21 han sido migrados. Para OpenTelemetry/tracing (usado en el repo Quarkus en 20 y 21) se puede añadir en Spring Boot 4:

- `micrometer-tracing-bridge-otel`
- `opentelemetry-exporter-otlp`

y configurar el endpoint OTLP correspondiente.

## Notas

- Java 21 en todos los proyectos.
- Parent: `spring-boot-starter-parent` 4.0.2.
- El directorio de compilación está fuera del workspace de Cursor; para compilar y ejecutar usa la terminal en `/Users/josediaz/Projects/JoeDayz/springboot4-samples`.
