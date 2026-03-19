# LAB 21: SPRING BOOT 4 MONITORING LOGGING

**Autor:** José Díaz

Abre el proyecto `18-monitor-logging-solution`:

## 1. Configuración Inicial

1. Abre el proyecto con tu editor favorito.
2. Ejecuta la aplicación en modo desarrollo:

```bash
cd expenses
mvn spring-boot:run
```

Deberías ver una salida similar a la activación de Spring Boot y el listener HTTP en `http://localhost:8080`.

## 2. Loguear Mensajes de Error

Loguea un mensaje de error cuando un request al endpoint `GET /expenses/{name}` trata de obtener un expense que no existe.

### a. Modificar `ExpensesResource`

Abre la clase `ExpensesResource` y modifica el método `getByName` para loguear el error cuando se lanza `ExpenseNotFoundException`.

En Spring Boot, el catch debería quedar con un `log.error(...)` antes de lanzar la excepción HTTP:

```java
} catch (ExpenseNotFoundException e) {
    log.error(e.getMessage());
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
}
```

### b. Probar el Endpoint con un Expense Inexistente

En una terminal nueva, ejecuta:

```bash
curl -v http://localhost:8080/expenses/none
```

Deberías ver un `404 Not Found`.

### c. Verificar el Log de Error

Vuelve a la terminal donde la aplicación está ejecutándose y verifica que aparece un log en `ERROR` con el mensaje de `Expense not found: none`.

## 3. Loguear Mensajes de Debug y Ajustar el Log Level

### a. Agregar Log de Debug

En la clase `ExpensesResource`, modifica el método `getByName` para loguear el mensaje debug:

`Getting expense {name}`

Ejemplo (forma recomendada para que el mensaje sea determinista):

```java
log.debug("Getting expense " + name);
```

### b. Probar el Endpoint con un Expense Existente

Haz un request para obtener el expense llamado `joel-2`:

```bash
curl -s http://localhost:8080/expenses/joel-2
```

### c. Verificar que el Mensaje de Debug No se Muestra

Si el log level por defecto es `INFO`, el mensaje `DEBUG` no debería verse en consola.

### d. Configurar el Log Level a DEBUG (global)

Agrega/ajusta en `src/main/resources/application.properties`:

```properties
logging.level.root=DEBUG
```

### e. Reejecutar el Request

Repite el request al mismo endpoint.

### f. Verificar que el Mensaje de DEBUG se Muestra

Verifica que el mensaje `Getting expense joel-2` aparece en los logs en `DEBUG`.

## 4. Configurar el Log Level DEBUG Solo para un Paquete Específico

### a. Cambiar el Root Log Level a INFO

En `application.properties`, configura:

```properties
logging.level.root=INFO
```

### b. Configurar el Log Level DEBUG para el Paquete Específico

Agrega:

```properties
logging.level.com.bcp.training.expense=DEBUG
```

### c. Reejecutar el Request

Repite el request y verifica que los `DEBUG` relevantes son únicamente los del paquete del ejercicio (`com.bcp.training.expense`).

## 5. Personalizar el Logging en Modo de Desarrollo (Archivo)

### a. Configurar el Logging a Archivo

Agrega las siguientes líneas al archivo `src/main/resources/application.properties`:

**Mac/Linux**

```properties
logging.file.name=${HOME}/DO378/monitor-logging/dev.logs
logging.pattern.file=%d %-5p [%F] %m%n
```

**Windows (ejemplo)**

```properties
logging.file.name=C:\\Users\\josed\\DO378\\monitor-logging\\dev.logs
logging.pattern.file=%d %-5p [%F] %m%n
```

> Nota: en este proyecto, `logback-spring.xml` usa `FileAppender` (archivo fijo, sin “rolling”) para que el laboratorio sea reproducible.

Asegúrate de crear el directorio antes de ejecutar la aplicación:

```bash
mkdir -p $HOME/DO378/monitor-logging
```

### b. Reejecutar el Request

Repite el request:

```bash
curl -s http://localhost:8080/expenses/joel-2 > /dev/null
```

### c. Verificar el Archivo de Log

Ejecuta:

```bash
cat $HOME/DO378/monitor-logging/dev.logs
```

Deberías ver líneas similares a:

```text
2023-01-23 09:11:39,451 DEBUG [ExpensesResource.java] Getting expense joel-2
```

### d. Detener la Aplicación

Vuelve a la terminal y detén el proceso con `Ctrl+C`.

---

**Enjoy!**

José

