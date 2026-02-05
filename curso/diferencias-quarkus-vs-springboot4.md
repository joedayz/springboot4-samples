# Diferencias grandes: Quarkus vs Spring Boot 4

Esta guia resume las diferencias mas relevantes para quienes ya vieron Quarkus y ahora
quieren dominar Spring Boot 4.

## Filosofia y arquitectura
- **Quarkus**: build-time optimization, extensiones, foco en native y Kubernetes.
- **Spring Boot 4**: auto-configuracion madura, ecosistema enorme, flexibilidad total JVM.

## Tooling y dev mode
- **Quarkus**: `quarkus:dev` con Dev UI integrada.
- **Spring Boot**: `spring-boot:run` y DevTools (reinicio automatico, live reload).

## Configuracion y perfiles
- **Quarkus**: `application.properties` y perfiles `quarkus.profile`.
- **Spring Boot**: `application.yml/properties`, perfiles `spring.profiles.active`,
  `@ConfigurationProperties` y `@Value`.

## Extension vs starter
- **Quarkus**: extensiones con build steps y features a nivel de build.
- **Spring Boot**: starters + auto-config; menor acoplamiento al build time.

## REST y validacion
- **Quarkus**: RESTEasy Reactive, anotaciones Jakarta REST.
- **Spring Boot**: `@RestController`, `@RequestMapping`, `@Validated`.

## Acceso a datos
- **Quarkus**: Hibernate ORM + Panache, enfoque opinionado.
- **Spring Boot**: Spring Data JPA, repositorios mas expresivos y configurables.

## Testing
- **Quarkus**: `@QuarkusTest`, testcontainers integrados.
- **Spring Boot**: `@SpringBootTest`, slices (`@WebMvcTest`, `@DataJpaTest`),
  soporte nativo de Testcontainers.

## Reactive
- **Quarkus**: Mutiny y Vert.x, enfoque reactivo por defecto.
- **Spring Boot**: WebFlux y Reactor, opt-in y convivencia con MVC.

## Seguridad
- **Quarkus**: SmallRye JWT/OIDC, configuracion directa.
- **Spring Boot**: Spring Security + OAuth2 Resource Server / Client.

## Observabilidad
- **Quarkus**: MicroProfile Metrics/Tracing/Health.
- **Spring Boot**: Micrometer, Actuator, OpenTelemetry.

## Native y AOT
- **Quarkus**: soporte nativo first-class con GraalVM, configuraciones mas sencillas.
- **Spring Boot**: AOT y native mejorados, pero con mas consideraciones de reflection.

## Contenedores y despliegue
- **Quarkus**: imagenes optimizadas, rapido cold start.
- **Spring Boot**: buildpacks, layered jars, integracion con plataformas comunes.

## Criterio de comparacion por modulo
En cada modulo del curso se incluye una seccion:
- **Lo mismo**: funcionalidades equivalentes.
- **Lo diferente**: cambios de API, configuracion o comportamiento.
- **Lo mejor en Spring Boot**: buenas practicas recomendadas.
