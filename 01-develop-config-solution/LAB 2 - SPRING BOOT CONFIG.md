# LAB 2: SPRING BOOT CONFIG

**Autor:** José Díaz  
**Github Repo:** https://github.com/joedayz/springboot4-samples.git

## 1. Cargar en su IDE el proyecto 01-develop-config-start

## 2. Examina la clase `com.bcp.training.ExpenseValidatorCli`

- Esta clase define una aplicación de línea de comandos que recibe un argumento.
- La aplicación utiliza la clase `ExpenseValidator` para determinar si el argumento proporcionado está dentro de un rango definido de valores enteros y muestra un mensaje en la salida de la consola.

## 3. Ejecuta la aplicación en modo desarrollo

Usa el parámetro `-Dspring-boot.run.arguments=33` para pasar el valor `33` como argumento de línea de comandos.

### Linux/Mac

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=33
```

### Windows (CMD)

```cmd
mvn spring-boot:run -Dspring-boot.run.arguments=33
```

### Windows (PowerShell)

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments=33
```

**Salida esperada:**

```
...output omitted...
Range - High: 1000
Range - Low: 250
Invalid amount: 33
...output omitted...
```

## 4. Ejecuta nuevamente con otro argumento

Usa `255` como argumento y verifica la salida.

### Linux/Mac

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=255
```

### Windows (CMD)

```cmd
mvn spring-boot:run -Dspring-boot.run.arguments=255
```

### Windows (PowerShell)

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments=255
```

**Resultado esperado:**

```
Range - High: 1000
Range - Low: 250
Valid amount: 255
```

## 5. Reemplaza los valores codificados por propiedades de configuración

### 5.1. Crea la clase `ExpenseConfiguration`

- Crea la clase en el paquete `com.bcp.training`.
- Anota la clase con `@ConfigurationProperties(prefix = "expense")`.
- Define `false` como valor por defecto de la propiedad `debug-enabled`.

```java
package com.bcp.training;

import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Optional;

@ConfigurationProperties(prefix = "expense")
public record ExpenseConfiguration(
    @DefaultValue("false") boolean debugEnabled,
    Optional<String> debugMessage,
    int rangeHigh,
    int rangeLow
) {}
```

### 5.2. Habilita el escaneo de Configuration Properties

En la clase principal, agrega `@ConfigurationPropertiesScan`.

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class ExpenseValidatorApplication {
    // ...
}
```

## 6. Abre la clase `ExpenseValidator`

Inyecta `ExpenseConfiguration` y reemplaza los valores fijos.

## 7. Abre el archivo `src/main/resources/application.yml`

Define las propiedades:

```yaml
expense:
  debug-enabled: true
  range-high: 1024
  range-low: 64
```

## 8. Ejecuta nuevamente la aplicación

### Linux/Mac

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=255
```

### Windows (CMD)

```cmd
mvn spring-boot:run -Dspring-boot.run.arguments=255
```

### Windows (PowerShell)

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments=255
```

**Salida esperada:**

```
Valid amount: 255
```

**Nota:** El método `ExpenseValidator#isValidAmount()` utiliza el valor de la propiedad `debug-enabled` para imprimir mensajes de depuración. Si `debug-enabled` es `false`, no se ejecuta `debugRanges()`.

## 9. Usa expresiones de propiedades en los valores de configuración

### 9.1. Agrega la propiedad `expense.debug-message` al `application.yml`

```yaml
expense:
  debug-message: "Range [${expense.range-low}, ${expense.range-high}]"
```

### 9.2. Actualiza `debugRanges()` en `ExpenseValidator`

```java
public void debugRanges() {
    config.debugMessage().ifPresent(System.out::println);
}
```

## 10. Usa un perfil para sobrescribir propiedades

Agrega un bloque para `dev` en `application.yml`:

```yaml
---
spring:
  config:
    activate:
      on-profile: dev
expense:
  range-low: 500
```

Ejecuta con perfil `dev`:

### Linux/Mac

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run -Dspring-boot.run.arguments=255
```

### Windows (CMD)

```cmd
set SPRING_PROFILES_ACTIVE=dev && mvn spring-boot:run -Dspring-boot.run.arguments=255
```

### Windows (PowerShell)

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"; mvn spring-boot:run -Dspring-boot.run.arguments=255
```

**Salida esperada:**

```
Range [500, 1024]
Invalid amount: 255
```

## 11. Usa un archivo .env para sobrescribir propiedades (opcional)

### 11.1. Crea un archivo `.env` en la raíz del proyecto

```env
EXPENSE_RANGE_LOW=600
```

### 11.2. Importa el archivo en `application.yml`

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

Si estás usando el perfil `dev`, asegúrate de que `range-low` permita la
sobrescritura desde `.env`:

```yaml
---
spring:
  config:
    activate:
      on-profile: dev
expense:
  range-low: ${EXPENSE_RANGE_LOW:500}
```

Ejecuta con perfil `dev`:

### Linux/Mac

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run -Dspring-boot.run.arguments=255
```

### Windows (CMD)

```cmd
set SPRING_PROFILES_ACTIVE=dev && mvn spring-boot:run -Dspring-boot.run.arguments=255
```

### Windows (PowerShell)

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"; mvn spring-boot:run -Dspring-boot.run.arguments=255
```

**Salida esperada:**

```
Range [600, 1024]
Invalid amount: 255
```

## 12. Usa una variable de entorno para sobrescribir propiedades

Deten la aplicacion y ejecuta el jar con variables de entorno:

### Linux o Mac

```bash
mvn package \
&& SPRING_PROFILES_ACTIVE=dev \
EXPENSE_RANGE_LOW=700 \
java -jar target/expense-validator-1.0.0-SNAPSHOT.jar 255
```

### Windows (CMD)

```cmd
mvn package && set SPRING_PROFILES_ACTIVE=dev && set EXPENSE_RANGE_LOW=700 && java -jar target/expense-validator-1.0.0-SNAPSHOT.jar 255
```

### Windows (PowerShell)

```powershell
mvn package; $env:SPRING_PROFILES_ACTIVE="dev"; $env:EXPENSE_RANGE_LOW="700"; java -jar target/expense-validator-1.0.0-SNAPSHOT.jar 255
```

**Salida esperada:**

```
Range [700, 1024]
Invalid amount: 255
```

---

**Enjoy!**

**Joe**
