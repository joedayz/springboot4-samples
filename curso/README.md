# Curso Spring Boot 4 - temario y estructura

Este curso replica la progresion del repo `quarkus-bcp-2025`, pero con Spring Boot 4.
El objetivo es que cada modulo tenga un par `*-start` y `*-solution` con el mismo alcance
funcional, para comparar de forma directa Quarkus vs Spring Boot.

## Estructura propuesta por modulos

00. **setup**: tooling, SDKs, build, primera app.
01. **config**: perfiles, propiedades, config externalizada.
02. **rest**: controllers, validacion, manejo de errores, clientes HTTP.
03. **persist**: JPA, migraciones, repositorios, transacciones.
04. **native**: AOT, GraalVM, rendimiento y restricciones.
05. **testing**: unit, slice, integracion, testcontainers.
06. **reactive**: WebFlux, R2DBC, arquitectura reactiva.
07. **security**: JWT, OAuth2, SSO, resource server.
08. **resilience**: circuit breaker, retries, timeouts.
09. **observability**: logging, metrics, tracing.
10. **deploy**: contenedores, k8s, configs y secrets.

## Convencion de carpetas
- `NN-topic-start/`: proyecto base del modulo.
- `NN-topic-solution/`: solucion final del modulo.
- Cada proyecto incluye README con objetivos, pasos y comparativa Quarkus vs Spring Boot.

## Diferencias clave que se resaltan en cada modulo
Consulta `curso/diferencias-quarkus-vs-springboot4.md` para la comparativa general.
En cada modulo se agregan notas puntuales sobre:
- Dev mode y hot reload.
- Configuracion y perfiles.
- Extension vs starters.
- Build/packaging y native.
- Programacion reactiva y soporte.

## Siguiente paso sugerido
Implementar primero los modulos 01, 02 y 03 para tener la base de configuracion,
REST y persistencia, y luego avanzar a seguridad y observabilidad.
