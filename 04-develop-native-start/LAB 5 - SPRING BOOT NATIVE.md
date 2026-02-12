# LAB 5: SPRING BOOT NATIVE

**Autor:** José Díaz  
**Github Repo:** (este repositorio)

## Objetivo

En este laboratorio aprenderás a:
- Entender Spring AOT (Ahead-of-Time) y la compilación nativa con GraalVM
- Configurar el plugin `native-maven-plugin` en Spring Boot 4
- Compilar la aplicación en modo JVM (baseline)
- Compilar un ejecutable nativo con GraalVM
- Construir imágenes de contenedor (JVM y nativa) con Cloud Native Buildpacks
- Construir imágenes de contenedor con Dockerfile
- Comparar tiempos de arranque entre JVM y nativo

## Prerequisitos

- **JDK 25** (GraalVM CE 25 recomendado): Spring Boot 4 requiere Java 25 como mínimo para compilación nativa
- **Docker** o **Podman**: Para construir imágenes de contenedor
- **Maven 3.9+**: Para compilar el proyecto

### Instalación de GraalVM 25

### Linux/Mac (SDKMAN!)

```bash
sdk install java 25-graalce
sdk use java 25-graalce
```

### Descarga manual

Descarga desde: https://www.graalvm.org/downloads/

Configura `JAVA_HOME` y `PATH`:

### Linux/Mac

```bash
export JAVA_HOME=/path/to/graalvm-jdk-25
export PATH=$JAVA_HOME/bin:$PATH
```

### Windows (CMD)

```cmd
set JAVA_HOME=C:\path\to\graalvm-jdk-25
set PATH=%JAVA_HOME%\bin;%PATH%
```

### Windows (PowerShell)

```powershell
$env:JAVA_HOME = "C:\path\to\graalvm-jdk-25"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

Verifica la instalación:

```bash
java -version
native-image --version
```

### Requisito adicional en Windows: Visual Studio 2022

En Windows, GraalVM Native Image necesita el compilador C/C++ de Microsoft para enlazar el ejecutable nativo. Debes instalar **Visual Studio 2022 Build Tools** (versión 17.6.0 o superior).

1. Descarga desde: https://visualstudio.microsoft.com/downloads/
2. En el instalador, selecciona **"Desktop development with C++"** (Desarrollo de escritorio con C++)
3. Asegúrate de que estén seleccionados:
   - **MSVC v143 - VS 2022 C++ x64/x86 build tools**
   - **Windows SDK** (cualquier versión reciente)

**Importante:** Después de instalar, ejecuta los comandos de compilación nativa desde el **"x64 Native Tools Command Prompt for VS 2022"** (disponible en el menú Inicio), NO desde un CMD o PowerShell normal.

### Windows: Abrir x64 Native Tools Command Prompt

1. Abre el menú Inicio
2. Busca: **"x64 Native Tools Command Prompt for VS 2022"**
3. Ejecuta los comandos de compilación nativa desde esa terminal

Alternativamente, puedes configurar el entorno manualmente en cualquier terminal:

```cmd
"C:\Program Files\Microsoft Visual Studio\2022\BuildTools\VC\Auxiliary\Build\vcvarsall.bat" x64
```

### Errores comunes

- **`NativeImageRequirementsException: Native Image must support at least Java 25`**: Tu JDK/GraalVM es una versión anterior. Debes actualizar a GraalVM 25.
- **`Failed to find 'vswhere.exe'`** (Windows): No tienes Visual Studio 2022 instalado. Instálalo con los Build Tools de C++ y usa el x64 Native Tools Command Prompt.
- **`cl.exe not found`** (Windows): No estás usando el x64 Native Tools Command Prompt. Abre esa terminal o ejecuta `vcvarsall.bat x64`.
- **`Builder lifecycle 'creator' failed with status code 51`** (Buildpacks): El builder de Paketo Buildpacks no soporta Java 25 aún. Usa el enfoque con **Dockerfile** (sección 7) en lugar de Buildpacks.

## 1. Cargar en su IDE el proyecto 04-develop-native-start

Abre el proyecto en tu IDE preferido. El proyecto contiene:
- `expense-function`: Servicio REST sencillo que gestiona gastos (sin persistencia)

### 1.1. Estructura del proyecto

```
expense-function/
├── pom.xml
└── src/main/
    ├── java/com/bcp/training/
    │   ├── ExpenseFunctionApplication.java
    │   ├── Expense.java
    │   ├── ExpenseRepository.java
    │   └── ExpenseController.java
    └── resources/
        └── application.yml
```

## 2. Verificar la aplicación en modo JVM

Antes de compilar nativo, verifica que la aplicación funciona en modo JVM.

### 2.1. Compila y ejecuta en modo JVM

### Linux/Mac

```bash
cd expense-function
mvn clean package -DskipTests
java -jar target/expense-function-1.0.0-SNAPSHOT.jar
```

### Windows (CMD)

```cmd
cd expense-function
mvn clean package -DskipTests
java -jar target\expense-function-1.0.0-SNAPSHOT.jar
```

### Windows (PowerShell)

```powershell
cd expense-function
mvn clean package -DskipTests
java -jar target\expense-function-1.0.0-SNAPSHOT.jar
```

### 2.2. Anota el tiempo de arranque

En la consola verás algo como:

```
Started ExpenseFunctionApplication in X.XXX seconds
```

**Anota este valor** para comparar después con el ejecutable nativo.

### 2.3. Verifica los endpoints

### Linux/Mac

```bash
curl http://localhost:8080/expenses
```

### Windows (CMD)

```cmd
curl http://localhost:8080/expenses
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri http://localhost:8080/expenses -Method GET | Select-Object -ExpandProperty Content
```

**Resultado esperado:** Un array JSON con los gastos iniciales.

Detén la aplicación con `Ctrl+C`.

## 3. Agregar soporte para compilación nativa

### 3.1. Abre el archivo `pom.xml`

Ubicado en: `expense-function/pom.xml`

### 3.2. Agrega el plugin `native-maven-plugin`

Dentro de `<build><plugins>`, agrega el plugin de GraalVM:

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
</plugin>
```

**Explicación:**
- El `spring-boot-starter-parent` ya define un perfil Maven llamado `native` que configura automáticamente este plugin
- Al activar el perfil con `-Pnative`, se habilita:
  - **Spring AOT processing**: Genera código optimizado en tiempo de compilación
  - **GraalVM native-image**: Compila a un ejecutable nativo
- No necesitas especificar versión porque el parent POM ya la gestiona

**Resultado esperado:** El `pom.xml` debe tener ambos plugins:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.graalvm.buildtools</groupId>
            <artifactId>native-maven-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

## 4. Compilar ejecutable nativo (local)

### Prerequisitos

Para compilar nativamente de forma local necesitas:
- **GraalVM JDK 25** instalado y configurado como `JAVA_HOME` (ver sección Prerequisitos al inicio)
- El comando `native-image` disponible en el PATH

Verifica tu instalación:

### Linux/Mac

```bash
java -version
native-image --version
```

### Windows (x64 Native Tools Command Prompt)

```cmd
java -version
native-image --version
```

**Nota:** En Windows, usa el **"x64 Native Tools Command Prompt for VS 2022"** para compilar nativamente (ver sección Prerequisitos).

**Nota:** Si no tienes GraalVM instalado localmente, puedes saltar al paso 5 para compilar dentro de un contenedor (solo requiere Docker).

### 4.1. Compila el ejecutable nativo

### Linux/Mac

```bash
mvn -Pnative native:compile -DskipTests
```

### Windows (x64 Native Tools Command Prompt)

**Importante:** Abre el **"x64 Native Tools Command Prompt for VS 2022"** desde el menú Inicio antes de ejecutar:

```cmd
mvn -Pnative native:compile -DskipTests
```

**Nota:** La compilación nativa toma varios minutos (3-10 min dependiendo de tu hardware). Es significativamente más lenta que la compilación JVM, pero el resultado es un ejecutable que arranca mucho más rápido.

### 4.2. Ejecuta el binario nativo

### Linux/Mac

```bash
./target/expense-function
```

### Windows

```cmd
target\expense-function.exe
```

**Nota:** El ejecutable nativo en Windows se puede ejecutar desde cualquier terminal (CMD, PowerShell o Native Tools). Solo la **compilación** requiere el x64 Native Tools Command Prompt.

### 4.3. Compara el tiempo de arranque

Verás algo como:

```
Started ExpenseFunctionApplication in 0.XXX seconds
```

**Compara** este valor con el tiempo de arranque en modo JVM (paso 2.2). Típicamente el arranque nativo es **10-50x más rápido**.

### 4.4. Verifica los endpoints

### Linux/Mac

```bash
curl http://localhost:8080/expenses
```

### Windows (CMD)

```cmd
curl http://localhost:8080/expenses
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri http://localhost:8080/expenses -Method GET | Select-Object -ExpandProperty Content
```

Detén la aplicación con `Ctrl+C`.

## 5. Construir imágenes de contenedor con Dockerfile

Este es el método recomendado para Java 25, ya que los Buildpacks de Paketo pueden no tener soporte aún para esta versión.

El proyecto incluye dos Dockerfiles multi-stage en `src/main/docker/`:
- `Dockerfile.jvm`: Compila y ejecuta con JVM (Eclipse Temurin 25)
- `Dockerfile.native`: Compila con GraalVM 25 y ejecuta como binario nativo

### 5.1. Imagen JVM con Dockerfile

### Linux/Mac

```bash
docker build -f src/main/docker/Dockerfile.jvm -t expense-function:jvm .
```

### Windows (CMD/PowerShell)

```cmd
docker build -f src/main/docker/Dockerfile.jvm -t expense-function:jvm .
```

### 5.2. Ejecuta el contenedor JVM

### Linux/Mac

```bash
docker run --rm -p 8080:8080 expense-function:jvm
```

### Windows (CMD/PowerShell)

```cmd
docker run --rm -p 8080:8080 expense-function:jvm
```

Anota el tiempo de arranque: `Started ExpenseFunctionApplication in X.XXX seconds`

Detén el contenedor con `Ctrl+C`.

### 5.3. Imagen nativa con Dockerfile

**Nota:** Este build toma varios minutos (5-15 min) porque compila la imagen nativa con GraalVM dentro del contenedor. **No necesitas GraalVM instalado localmente**, solo Docker.

### Linux/Mac

```bash
docker build -f src/main/docker/Dockerfile.native -t expense-function:native .
```

### Windows (CMD/PowerShell)

```cmd
docker build -f src/main/docker/Dockerfile.native -t expense-function:native .
```

### 5.4. Ejecuta el contenedor nativo

### Linux/Mac

```bash
docker run --rm -p 8080:8080 expense-function:native
```

### Windows (CMD/PowerShell)

```cmd
docker run --rm -p 8080:8080 expense-function:native
```

Anota el tiempo de arranque: `Started ExpenseFunctionApplication in 0.XXX seconds`

### 5.5. Verifica los endpoints

### Linux/Mac

```bash
curl http://localhost:8080/expenses
```

### Windows (CMD)

```cmd
curl http://localhost:8080/expenses
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri http://localhost:8080/expenses -Method GET | Select-Object -ExpandProperty Content
```

Detén el contenedor con `Ctrl+C`.

### 5.6. Comparar tamaños de imagen

### Linux/Mac

```bash
docker images expense-function
```

### Windows (CMD/PowerShell)

```cmd
docker images expense-function
```

## 6. Construir con Buildpacks (alternativa)

Spring Boot también puede construir imágenes usando **Cloud Native Buildpacks** (Paketo) con `spring-boot:build-image`. Sin embargo, los Buildpacks pueden no soportar Java 25 aún.

**Si obtienes el error `Builder lifecycle 'creator' failed with status code 51`**, usa el método con Dockerfile (sección 5).

### 6.1. Imagen JVM con Buildpacks

### Linux/Mac

```bash
mvn spring-boot:build-image -DskipTests
```

### Windows (CMD/PowerShell)

```cmd
mvn spring-boot:build-image -DskipTests
```

### 6.2. Imagen nativa con Buildpacks

### Linux/Mac

```bash
mvn -Pnative spring-boot:build-image -DskipTests
```

### Windows (CMD/PowerShell)

```cmd
mvn -Pnative spring-boot:build-image -DskipTests
```

### 6.3. Ejecuta y compara

```bash
docker run --rm -p 8080:8080 expense-function:1.0.0-SNAPSHOT
```

## 7. Comparar tamaños de imagen

```bash
docker images expense-function
```

**Resultado esperado:**

| TAG | SIZE (aprox.) |
|---|---|
| `jvm` | ~300-400 MB |
| `native` | ~80-120 MB |

La imagen nativa es significativamente más pequeña porque no incluye la JVM completa.

## 8. Medir tiempos de arranque

### 8.1. Arranque JVM

### Linux/Mac

```bash
docker run --rm -p 8080:8080 expense-function:jvm
```

### Windows (CMD/PowerShell)

```cmd
docker run --rm -p 8080:8080 expense-function:jvm
```

Anota: `Started ExpenseFunctionApplication in X.XXX seconds`

### 8.2. Arranque nativo

### Linux/Mac

```bash
docker run --rm -p 8080:8080 expense-function:native
```

### Windows (CMD/PowerShell)

```cmd
docker run --rm -p 8080:8080 expense-function:native
```

Anota: `Started ExpenseFunctionApplication in 0.XXX seconds`

### 8.3. Tabla comparativa

| Métrica | JVM | Nativo |
|---|---|---|
| Tiempo de arranque | ~1-3 s | ~0.03-0.1 s |
| Tamaño de imagen | ~300-400 MB | ~80-120 MB |
| Uso de memoria (RSS) | ~150-250 MB | ~30-60 MB |
| Tiempo de compilación | ~5-10 s | ~3-10 min |
| Throughput (peak) | Mayor | Menor |

## 9. Diferencias clave: Spring Boot vs Quarkus (Native)

| Concepto | Quarkus | Spring Boot 4 |
|---|---|---|
| **AOT Processing** | Build-time por defecto | Spring AOT (con `-Pnative`) |
| **Compilación nativa** | `mvn -Pnative package` | `mvn -Pnative native:compile` |
| **Builder image** | Mandrel (Quarkus-optimized) | GraalVM CE / Paketo Buildpacks |
| **Container build** | `quarkus.native.container-build=true` | `mvn -Pnative spring-boot:build-image` |
| **Container runtime** | Podman/Docker configurable | Docker (Buildpacks) |
| **Plugin** | `quarkus-maven-plugin` | `native-maven-plugin` + `spring-boot-maven-plugin` |
| **Dev Services** | Integrado | Requiere Docker Compose Support |
| **Funciones HTTP** | Funqy (`@Funq`) | `@RestController` o Spring Cloud Function |
| **Reflexión hints** | Automático (Quarkus extensions) | Automático (Spring AOT) + `reflect-config.json` si necesario |
| **Tamaño ejecutable** | ~30-50 MB | ~70-100 MB |
| **Arranque nativo** | ~0.01-0.05 s | ~0.03-0.1 s |

## 10. Consideraciones importantes

### 10.1. Limitaciones del modo nativo

- **Sin reflexión dinámica**: Spring AOT genera las configuraciones necesarias, pero librerías de terceros pueden requerir hints adicionales
- **Sin class loading dinámico**: Las clases deben ser conocidas en tiempo de compilación
- **Sin proxies JDK dinámicos**: Se generan en AOT
- **Tiempo de compilación**: Mucho más lento que JVM (minutos vs segundos)
- **Throughput**: El peak throughput puede ser menor que en JVM (sin JIT compiler)

### 10.2. Cuándo usar nativo

- **Serverless / FaaS**: Donde el cold start importa
- **Microservicios**: Muchas instancias pequeñas
- **Edge computing**: Recursos limitados
- **CLI tools**: Herramientas de línea de comandos

### 10.3. Cuándo NO usar nativo

- **Aplicaciones monolíticas**: El throughput JVM es superior
- **Batch processing**: Procesos de larga duración donde el JIT optimizer brilla
- **Desarrollo**: Usar modo JVM con DevTools para desarrollo rápido

## Resumen

En este laboratorio has aprendido a:
- Entender Spring AOT y la compilación nativa con GraalVM
- Configurar `native-maven-plugin` en Spring Boot 4
- Compilar un ejecutable nativo localmente con `mvn -Pnative native:compile`
- Construir imágenes de contenedor nativas con Cloud Native Buildpacks
- Construir imágenes con Dockerfiles multi-stage
- Comparar tiempos de arranque, tamaño de imagen y uso de memoria
- Entender las diferencias entre Spring Boot y Quarkus para compilación nativa

## Próximos pasos

- Explora `@RegisterReflectionForBinding` para registrar clases que requieren reflexión
- Configura `reflect-config.json` para librerías de terceros
- Prueba Spring Cloud Function para serverless
- Usa `@NativeHint` para configuración avanzada de GraalVM
- Explora el perfil `nativeTest` para ejecutar tests en modo nativo
- Integra con AWS Lambda, Azure Functions o Google Cloud Functions

---

**Enjoy!**

**Joe**
