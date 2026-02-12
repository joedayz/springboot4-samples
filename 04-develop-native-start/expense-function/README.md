# expense-function (Native Start)

Este proyecto es el **punto de partida** del laboratorio de compilación nativa con Spring Boot 4 y GraalVM.

Sigue las instrucciones en [LAB 5 - SPRING BOOT NATIVE.md](../LAB%205%20-%20SPRING%20BOOT%20NATIVE.md) para completar el laboratorio.

## Tecnologías

- Spring Boot 4.0.2
- Spring MVC
- springdoc-openapi (Swagger UI)

## Lo que vas a agregar

- Plugin `native-maven-plugin` para soporte GraalVM
- Compilación nativa con `mvn -Pnative native:compile`
- Imágenes de contenedor (JVM y nativa)
- Dockerfiles multi-stage

## Ejecutar (modo JVM)

```bash
cd expense-function
mvn spring-boot:run
```
