# LAB 1: SETUP (SPRING BOOT 4)

**Autor:** José Díaz

## 1. Crear un directorio para nuestro primer proyecto Spring Boot

### Linux/Mac

```bash
mkdir start-setup
```

### Windows (CMD)

```cmd
mkdir start-setup
```

### Windows (PowerShell)

```powershell
New-Item -ItemType Directory -Name start-setup
```

## 2. Crear una aplicación Spring Boot 4 usando Spring Initializr (curl)

### Linux/Mac

```bash
curl -L "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=4.0.0&groupId=com.bcp.training&artifactId=tenther&name=tenther&packageName=com.bcp.training&packaging=jar&javaVersion=21&dependencies=web" -o tenther.zip
unzip tenther.zip -d tenther
```

### Windows (CMD)

```cmd
curl -L "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=4.0.0&groupId=com.bcp.training&artifactId=tenther&name=tenther&packageName=com.bcp.training&packaging=jar&javaVersion=21&dependencies=web" -o tenther.zip
tar -xf tenther.zip -C tenther
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=4.0.0&groupId=com.bcp.training&artifactId=tenther&name=tenther&packageName=com.bcp.training&packaging=jar&javaVersion=21&dependencies=web" -OutFile tenther.zip
Expand-Archive -Path tenther.zip -DestinationPath tenther
```

> Si ya tienes el **Spring Boot CLI** instalado (comando `spring`), puedes usar `spring init`
> con los mismos parametros.

## 3. Navegar al directorio del proyecto

### Linux/Mac/Windows

```bash
cd tenther
```

## 4. Ejecutar la aplicacion en modo desarrollo

### Linux/Mac/Windows

```bash
mvn spring-boot:run
```

**O usando el wrapper de Maven (si esta disponible):**

```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows (CMD)
mvnw.cmd spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

## 4.1 Activar live reload / reinicio automatico (DevTools)

Para tener recarga rapida al guardar cambios:

1. Agrega la dependencia `spring-boot-devtools` al `pom.xml`.
2. Ejecuta la app con `mvn spring-boot:run`.

### Configuracion del IDE (requerida)

**IntelliJ IDEA**
- Activa **Build project automatically**.
- Activa **Allow auto-make to start even if developed application is currently running**.

**VS Code**
- Instala **Extension Pack for Java**.
- Asegura compilacion automatica al guardar (o usa `mvn spring-boot:run` y guarda para disparar la recompilacion).

> Nota: DevTools reinicia la aplicacion al detectar cambios en el classpath. No es exactamente hot reload
> como Quarkus, pero en la practica es similar para el flujo de desarrollo.

## 5. Crear el primer endpoint REST

Crear el archivo `src/main/java/com/bcp/training/HelloController.java`:

```java
package com.bcp.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/tenther/{number}")
    public String multiply(@PathVariable Integer number) {
        return String.valueOf(number * 10) + "\n";
    }
}
```

## 6. Invocar la aplicacion

### Curl

```bash
curl http://localhost:8080/tenther/7
```

### PowerShell

```powershell
Invoke-WebRequest http://localhost:8080/tenther/7
```

**Respuesta esperada:**

```
70
```

## 7. Detener la aplicacion

Para detener la aplicacion, presionar `Ctrl+C`.
