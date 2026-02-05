# 01 - develop config (solution)

Solucion completa para la demo de configuracion con Spring Boot 4.

## Ejecutar en modo desarrollo

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=255 -Dspring-boot.run.profiles=dev
```

> Nota: el proyecto incluye `spring-boot-starter-web` para mantener el proceso activo
> (escucha en el puerto 8080) y `CommandLineRunner` para imprimir el resultado.

## Empaquetar y ejecutar

```bash
mvn package
SPRING_PROFILES_ACTIVE=dev EXPENSE_RANGE_LOW=700 java -jar target/expense-validator-1.0.0-SNAPSHOT.jar 255
```
