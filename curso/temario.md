# Temario del curso Spring Boot 4

Duracion total aproximada: 19 horas + 4 horas opcionales.

## 00 - Introduccion al desarrollo de microservicios cloud-native con Spring Boot (1 hora)
- Que significa cloud-native hoy
- Evolucion de Spring → Boot 3 → Boot 4
- Monolito vs microservicios
- JVM tradicional vs Native Image
- Spring Boot vs Quarkus (comparativa honesta)
- Arquitectura del curso
- Hands-on: crear proyecto con Spring Initializr, estructura basica, primer endpoint REST

## 01 - Configuracion externa y perfiles (Spring Config) (1 hora)
- `application.yml` y perfiles
- Variables de entorno
- Configuracion por ambiente
- `@ConfigurationProperties`
- Spring Cloud Config (conceptos)
- Hands-on: configuracion por dev, test, prod, override via ENV / Kubernetes ConfigMap

## 02 - Desarrollo de APIs REST con Spring (2 horas)
- Spring MVC vs WebFlux
- `@RestController`
- Validaciones (`@Valid`)
- Manejo de errores (`@ControllerAdvice`)
- OpenAPI / Swagger
- Hands-on: CRUD REST, validacion de request, documentacion automatica

## 03 - Persistencia con Spring Boot (2 horas)
- Spring Data JPA
- Hibernate 6
- Mapeo de entidades
- Transacciones
- Introduccion a R2DBC (reactivo)
- Hands-on: CRUD con JPA, query methods, paginacion

## 04 - Aplicaciones nativas con Spring Boot y GraalVM (2 horas)
- Que es GraalVM Native Image
- AOT en Spring Boot 4
- Diferencias JVM vs Native
- Limitaciones reales
- Hands-on: compilar app nativa, comparar arranque y memoria, Docker + Native Image

## 05 - Testing en microservicios con Spring (2 horas)
- Testing en Spring Boot
- `@SpringBootTest`
- MockMvc / WebTestClient
- Testcontainers
- Tests de integracion reales
- Hands-on: test REST, test con base real (Testcontainers), test en pipeline

## 06 - Microservicios reactivos y asincronos (2 horas)
- Programacion reactiva
- Project Reactor (Mono, Flux)
- WebFlux
- R2DBC
- Backpressure
- Hands-on: endpoint reactivo, acceso no bloqueante a BD, comparacion MVC vs WebFlux

## 07 - Seguridad de microservicios con Spring Security (2 horas)
- Spring Security moderno (sin XML)
- JWT
- OAuth2 / OpenID Connect
- Integracion con Keycloak / Auth0
- Hands-on: proteger endpoints, roles y scopes, seguridad stateless

## 08 - Implementacion de microservicios en Kubernetes (2 horas)
- Dockerizacion de Spring Boot
- Liveness / Readiness probes
- ConfigMaps y Secrets
- Despliegue en Kubernetes
- Hands-on: build Docker, deploy en K8s, acceso via Service

## 09 - Tolerancia a fallos en microservicios (2 horas)
- Resilience4j
- Circuit Breaker
- Retry
- Rate Limiter
- Timeouts
- Hands-on: simular fallos, activar circuit breaker, metricas de resiliencia

## 10 - Observabilidad y monitoreo con Spring (2 horas)
- Micrometer
- Prometheus
- OpenTelemetry
- Tracing distribuido
- Logs estructurados
- Hands-on: metricas, traces, dashboards basicos

## 11 - Modulo extra: Integracion con Angular (2 horas)
- Consumo de API Spring desde Angular
- CORS
- JWT desde frontend
- Arquitectura frontend-backend

## 12 - Modulo extra: Integracion con React (2 horas)
- Fetch / Axios
- Manejo de auth
- SPA + microservicios
- Buenas practicas
