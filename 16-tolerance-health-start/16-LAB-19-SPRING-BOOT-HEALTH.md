# LAB 19: SPRING BOOT 4 HEALTH

**Autor:** José Díaz  
**Github Repo:** https://github.com/joedayz/springboot4-samples.git

## Objetivo

Este laboratorio te guiará en la implementación de health checks (liveness y readiness) en una aplicación Spring Boot 4, que son esenciales para el despliegue en Kubernetes.

## Prerequisitos

- Proyecto `16-tolerance-health-start` abierto en tu editor favorito
- Terminal disponible (PowerShell en Windows, Terminal en Linux/Mac)
- Maven instalado y configurado
- Java JDK 21 instalado

## Pasos del Laboratorio

### 1. Abre el Proyecto

Abre el proyecto `16-tolerance-health-start/calculator` con tu editor favorito.

### 2. Revisa los Archivos del Proyecto

Revisa los siguientes archivos para entender la estructura del proyecto:

- **`com.bcp.training.service.StateService`**: Es un bean `@Service` que controla si la aplicación está viva.
- **`com.bcp.training.SolverResource`**: Es un `@RestController` que expone un endpoint REST que soluciona ecuaciones matemáticas.

### 3. Verificar las Dependencias en el pom.xml

En Spring Boot 4, los health checks se integran mediante **Spring Boot Actuator**. Verifica que el `pom.xml` incluye las siguientes dependencias:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**⚠️ Importante:** Debes usar `spring-boot-starter-actuator` (no `spring-boot-health`). El starter completo trae el módulo `spring-boot-actuator-autoconfigure` que es el que registra los endpoints HTTP `/actuator/health/*`. Si solo usas `spring-boot-health`, los beans `HealthIndicator` se registran pero **no se exponen como endpoints web**.

**Nota:** A diferencia de Quarkus donde se instala la extensión `smallrye-health`, en Spring Boot 4 se utiliza `spring-boot-starter-actuator`.

### 4. Verificar la Configuración de application.properties

Revisa el archivo `src/main/resources/application.properties`. Debe contener la siguiente configuración para habilitar los health checks y los grupos de liveness/readiness:

```properties
management.endpoints.web.exposure.include=health
management.endpoints.access.default=read-only
management.endpoint.health.probes.enabled=true
management.endpoint.health.probes.add-additional-paths=true
management.endpoint.health.show-details=always
management.endpoint.health.group.liveness.include=livenessState,appLiveness
management.endpoint.health.group.readiness.include=readinessState,appReadiness

server.port=8080
```

**Explicación de las propiedades:**
- `management.endpoints.web.exposure.include=health`: Expone el endpoint `/actuator/health`
- `management.endpoints.access.default=read-only`: **(Nuevo en Spring Boot 4)** Permite el acceso de lectura a los endpoints de actuator. Sin esto, los endpoints devuelven 404.
- `management.endpoint.health.probes.enabled=true`: Habilita los endpoints `/actuator/health/liveness` y `/actuator/health/readiness`
- `management.endpoint.health.probes.add-additional-paths=true`: Hace disponibles los probes en el puerto principal
- `management.endpoint.health.show-details=always`: Muestra los detalles completos de cada componente de health
- `management.endpoint.health.group.liveness.include`: Define qué indicadores forman parte del grupo liveness (`appLiveness` corresponde al bean `AppLivenessHealthIndicator`)
- `management.endpoint.health.group.readiness.include`: Define qué indicadores forman parte del grupo readiness (`appReadiness` corresponde al bean `AppReadinessHealthIndicator`)

**⚠️ Importante sobre los nombres de los grupos:** En Spring Boot 4, el nombre del bean se deriva del nombre de la clase quitando el sufijo `HealthIndicator`. Si nombras tu clase `LivenessHealthIndicator`, Spring la registra como `liveness`, lo cual **colisiona** con el grupo de health llamado `liveness`. Por eso las clases se llaman `AppLivenessHealthIndicator` y `AppReadinessHealthIndicator`, que se registran como `appLiveness` y `appReadiness` respectivamente.

### 5. Crear un Liveness Health Check

El liveness check indica si la aplicación está funcionando. Si falla, Kubernetes reiniciará el contenedor.

1. Crea la clase `AppLivenessHealthIndicator.java`
2. Anota la clase con `@Component` e implementa la interfaz `HealthIndicator` de `org.springframework.boot.health.contributor`
3. Inyecta `StateService` por constructor e implementa el método `health()` para determinar si la aplicación está viva (up) o no (down)

**⚠️ Nota sobre imports en Spring Boot 4:** Las clases `Health` y `HealthIndicator` se movieron al paquete `org.springframework.boot.health.contributor` (antes estaban en `org.springframework.boot.actuate.health`).

**Implementación esperada:**

```java
package com.bcp.training;

import com.bcp.training.service.StateService;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AppLivenessHealthIndicator implements HealthIndicator {

    private static final String HEALTH_CHECK_NAME = "Liveness";
    private final StateService applicationState;

    public AppLivenessHealthIndicator(StateService applicationState) {
        this.applicationState = applicationState;
    }

    @Override
    public Health health() {
        return applicationState.isAlive()
                ? Health.up().withDetail(HEALTH_CHECK_NAME, "up").build()
                : Health.down().withDetail(HEALTH_CHECK_NAME, "down").build();
    }
}
```

**Diferencias con Quarkus:**
| Aspecto | Quarkus | Spring Boot 4 |
|---------|---------|---------------|
| Anotación | `@Liveness` + `@ApplicationScoped` | `@Component` |
| Interfaz | `HealthCheck` (MicroProfile) | `HealthIndicator` (`org.springframework.boot.health.contributor`) |
| Método | `call()` retorna `HealthCheckResponse` | `health()` retorna `Health` |
| Inyección | `@Inject` | Inyección por constructor |

### 6. Crear un Readiness Health Check

El readiness check indica si la aplicación está lista para recibir tráfico. Si falla, Kubernetes dejará de enviar tráfico al pod.

1. Crea la clase `AppReadinessHealthIndicator.java`
2. Anota la clase con `@Component`, implementa la interfaz `HealthIndicator` de `org.springframework.boot.health.contributor` e implementa el método `health()`
3. Las primeras 10 llamadas del endpoint readiness deben retornar una respuesta `DOWN` health check

**Implementación esperada:**

```java
package com.bcp.training;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AppReadinessHealthIndicator implements HealthIndicator {

    private static final String HEALTH_CHECK_NAME = "Readiness";
    private int counter = 0;

    @Override
    public Health health() {
        return ++counter >= 10
                ? Health.up().withDetail(HEALTH_CHECK_NAME, "up").build()
                : Health.down().withDetail(HEALTH_CHECK_NAME, "down").build();
    }
}
```

### 7. Verificar la Implementación de los Health Checks

Los primeros 10 requests al endpoint health deben retornar el estatus `DOWN`.

#### Endpoints de Health Checks

| Endpoint | URL |
|----------|-----|
| Health general | `http://localhost:8080/actuator/health` |
| Liveness | `http://localhost:8080/actuator/health/liveness` |
| Readiness | `http://localhost:8080/actuator/health/readiness` |

#### 7.1. Iniciar la Aplicación

Navega al directorio del proyecto `calculator` antes de ejecutar los comandos:

##### Windows (PowerShell)
```powershell
cd calculator
mvn spring-boot:run
```

##### Linux/Mac
```bash
cd calculator
mvn spring-boot:run
```

```powershell
cd calculator
mvn spring-boot:run
```

#### 7.2. Verificar el Endpoint de Health Checks

Abre una nueva terminal y usa los siguientes comandos para verificar que el endpoint `/actuator/health` retorna `DOWN` como el status actual de la aplicación.

**Nota:** En Spring Boot 4, los endpoints de health se sirven bajo `/actuator/health` en lugar de `/q/health` como en Quarkus.

##### Windows (PowerShell)
```powershell
# Opción 1: Usando un bucle while
while ($true) {
    Invoke-RestMethod -Uri http://localhost:8080/actuator/health | ConvertTo-Json
    Start-Sleep -Seconds 2
}
```

##### Linux/Mac
```bash
# Opción 1: Usando watch (disponible en Linux y la mayoría de distribuciones Mac)
watch -d -n 2 curl -s http://localhost:8080/actuator/health

# Opción 2: Si watch no está instalado en Mac, puedes instalarlo con:
# brew install watch
# O usar un bucle alternativo:
while true; do 
    echo "=== $(date) ==="
    curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
    sleep 2
done
```

```powershell
# Opción 1: Usando watch (disponible en Linux y la mayoría de distribuciones Mac)
watch -d -n 2 curl -s http://localhost:8080/actuator/health

# Opción 2: Si watch no está instalado en Mac, puedes instalarlo con:
# brew install watch
# O usar un bucle alternativo:
while true; do 
    echo "=== $(date) ==="
    curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
    sleep 2
done
```

**Nota:** Espera hasta que el contador del readiness llegue al límite especificado en la lógica de la aplicación (10 llamadas), y reporte `UP`.

#### 7.3. Verificar los Endpoints Específicos de Liveness y Readiness

También puedes verificar cada probe por separado:

##### Linux/Mac
```bash
# Liveness check
curl -s http://localhost:8080/actuator/health/liveness | jq .

# Readiness check
curl -s http://localhost:8080/actuator/health/readiness | jq .
```

```powershell
# Liveness check
curl -s http://localhost:8080/actuator/health/liveness | jq .

# Readiness check
curl -s http://localhost:8080/actuator/health/readiness | jq .
```

##### Windows (PowerShell)
```powershell
# Liveness check
Invoke-RestMethod -Uri http://localhost:8080/actuator/health/liveness | ConvertTo-Json

# Readiness check
Invoke-RestMethod -Uri http://localhost:8080/actuator/health/readiness | ConvertTo-Json
```

#### 7.4. Probar el Endpoint Crash

Abre una nueva terminal y usa el comando curl para llamar al endpoint `/crash`.

##### Windows (PowerShell)
```powershell
curl.exe http://localhost:8080/crash
# O usando Invoke-RestMethod
Invoke-RestMethod -Uri http://localhost:8080/crash
```

##### Linux/Mac
```bash
curl http://localhost:8080/crash
```

```powershell
curl http://localhost:8080/crash
```

#### 7.5. Verificar el Estado Después del Crash

1. Cierra la terminal donde se ejecutó el comando curl
2. Reejecuta el comando watch/curl y verifica la respuesta a los health checks
3. El status de los liveness checks deben ser `DOWN` después del crash

##### Windows (PowerShell)
```powershell
while ($true) {
    Invoke-RestMethod -Uri http://localhost:8080/actuator/health | ConvertTo-Json
    Start-Sleep -Seconds 2
}
```

##### Linux/Mac
```bash
# Opción 1: Usando watch
watch -d -n 2 curl -s http://localhost:8080/actuator/health

# Opción 2: Bucle alternativo si watch no está disponible
while true; do 
    echo "=== $(date) ==="
    curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
    sleep 2
done
```

```powershell
# Opción 1: Usando watch
watch -d -n 2 curl -s http://localhost:8080/actuator/health

# Opción 2: Bucle alternativo si watch no está disponible
while true; do 
    echo "=== $(date) ==="
    curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
    sleep 2
done
```

#### 7.6. Detener el Monitoreo

1. Detén el comando watch/curl presionando `CTRL+C`
2. Cierra la terminal

#### 7.7. ¿Qué Pasa con el Liveness Check en Kubernetes?

**⚠️ Observación Importante:**

Cuando ejecutas `curl http://localhost:8080/crash` y luego monitoreas el health check, notarás que el liveness check queda en estado `DOWN` permanentemente. Esto es el comportamiento esperado en tu entorno local.

**¿Por qué no se recupera automáticamente?**

En tu entorno local, cuando el liveness check falla, simplemente queda en `DOWN` porque no hay ningún sistema que reinicie la aplicación. Sin embargo, **en Kubernetes el comportamiento es completamente diferente**:

1. **Kubernetes monitorea el liveness probe** cada `period` segundos
2. Si el liveness probe falla continuamente, Kubernetes considera que el contenedor está en un estado "muerto" o "bloqueado"
3. **Kubernetes automáticamente reinicia el contenedor** (kill y restart)
4. Al reiniciarse, el contenedor vuelve a su estado inicial (`alive = true`), por lo que el liveness check vuelve a `UP`

**En resumen:**
- **Localmente**: El liveness check queda en `DOWN` hasta que reinicies manualmente la aplicación
- **En Kubernetes**: El liveness check en `DOWN` provoca el reinicio automático del pod, restaurando el estado inicial

## ¿Por Qué Son Importantes los Health Checks para Kubernetes?

Los health checks (liveness y readiness) son fundamentales para el funcionamiento correcto de aplicaciones en Kubernetes. Aquí te explicamos por qué:

### 🔴 Liveness Probe (Sonda de Vida)

**¿Qué es?**
El liveness probe indica si la aplicación está **funcionando correctamente**. Es como preguntar: "¿Está viva la aplicación?"

**¿Por qué es importante?**
- **Detección de deadlocks y bloqueos**: Si tu aplicación se bloquea pero el proceso sigue corriendo, Kubernetes lo detecta y reinicia el contenedor
- **Recuperación automática**: Kubernetes puede recuperar automáticamente aplicaciones que entran en estados inválidos sin intervención manual
- **Prevención de servicios "zombie"**: Evita que contenedores que parecen estar corriendo pero no responden correctamente sigan recibiendo tráfico

**¿Qué pasa cuando falla?**
```
Liveness DOWN → Kubernetes detecta el problema → 
Kubernetes mata el contenedor → Kubernetes crea un nuevo contenedor → 
Nuevo contenedor inicia con estado limpio → Liveness vuelve a UP
```

**Ejemplo práctico:**
En este laboratorio, cuando llamas a `/crash`, el liveness check pasa a `DOWN`. En Kubernetes:
- Kubernetes detecta que el liveness probe falla
- Espera el tiempo configurado (`failureThreshold`)
- Si continúa fallando, **reinicia el pod automáticamente**
- El nuevo pod inicia con `StateService.alive = true` (estado inicial)
- El servicio se recupera automáticamente sin intervención manual

### 🟡 Readiness Probe (Sonda de Preparación)

**¿Qué es?**
El readiness probe indica si la aplicación está **lista para recibir tráfico**. Es como preguntar: "¿Puedo enviar requests a esta aplicación?"

**¿Por qué es importante?**
- **Evita tráfico durante el inicio**: Kubernetes no envía tráfico hasta que la aplicación esté completamente lista
- **Evita tráfico durante mantenimiento**: Si la aplicación entra en modo mantenimiento, Kubernetes deja de enviar tráfico
- **Rolling updates más seguros**: Durante actualizaciones, Kubernetes espera a que el nuevo pod esté listo antes de enviar tráfico

**¿Qué pasa cuando falla?**
```
Readiness DOWN → Kubernetes remueve el pod del Service → 
No se envía tráfico al pod → Pod puede recuperarse sin afectar usuarios → 
Readiness vuelve a UP → Kubernetes vuelve a agregar el pod al Service
```

**Ejemplo práctico:**
En este laboratorio, las primeras 10 llamadas al readiness check retornan `DOWN`. En Kubernetes:
- Durante el inicio, Kubernetes espera hasta que el readiness check pase a `UP`
- Solo después de que el readiness esté `UP`, Kubernetes comienza a enviar tráfico al pod
- Esto evita que los usuarios reciban errores durante el arranque de la aplicación

### 📊 Comparación: Liveness vs Readiness

| Aspecto | Liveness Probe | Readiness Probe |
|---------|---------------|-----------------|
| **Propósito** | ¿Está la aplicación funcionando? | ¿Está la aplicación lista para tráfico? |
| **Acción si falla** | Reinicia el contenedor | Remueve del Service (no reinicia) |
| **Cuándo usar** | Para detectar estados bloqueados | Para detectar si está lista para recibir requests |
| **Frecuencia** | Cada `period` segundos | Cada `period` segundos |
| **Impacto** | Más severo (reinicio) | Menos severo (solo remueve tráfico) |

### 🎯 Configuración en Kubernetes

Las propiedades que configuraste en `application.properties` habilitan los endpoints que Kubernetes consumirá:

```properties
management.endpoints.access.default=read-only
management.endpoint.health.probes.enabled=true
management.endpoint.health.group.liveness.include=livenessState,appLiveness
management.endpoint.health.group.readiness.include=readinessState,appReadiness
```

Esto expone los endpoints que se mapean a la configuración de probes en Kubernetes:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  periodSeconds: 2
  initialDelaySeconds: 5
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  periodSeconds: 2
  initialDelaySeconds: 5
  failureThreshold: 3
```

**Nota:** En Spring Boot 4, los endpoints de probes son `/actuator/health/liveness` y `/actuator/health/readiness`, a diferencia de Quarkus que usa `/q/health/live` y `/q/health/ready`.

### 🚀 Beneficios en Producción

1. **Alta Disponibilidad**: Los pods se recuperan automáticamente de fallos
2. **Mejor Experiencia de Usuario**: Los usuarios no reciben errores durante el inicio o mantenimiento
3. **Menos Intervención Manual**: Kubernetes maneja la recuperación automáticamente
4. **Rolling Updates Seguros**: Las actualizaciones son más seguras y sin downtime
5. **Detección Temprana de Problemas**: Los problemas se detectan y resuelven automáticamente

### ⚠️ Mejores Prácticas

1. **Liveness debe ser ligero**: No debe hacer operaciones pesadas que puedan afectar el rendimiento
2. **Readiness debe verificar dependencias**: Debe verificar que las conexiones a bases de datos, APIs externas, etc., estén funcionando
3. **Configura tiempos apropiados**: `period`, `timeout`, y `failureThreshold` deben ajustarse según tu aplicación
4. **No uses el mismo endpoint**: Liveness y readiness deben verificar cosas diferentes
5. **Considera startup probes**: Para aplicaciones que tardan mucho en iniciar, usa startup probes además de liveness

## Endpoints de Health Checks

Una vez implementados los health checks, Spring Boot Actuator expone automáticamente los siguientes endpoints:

- **`/actuator/health`**: Endpoint principal que muestra el estado general de todos los health checks
- **`/actuator/health/liveness`**: Endpoint específico para liveness checks
- **`/actuator/health/readiness`**: Endpoint específico para readiness checks

### Ejemplo de Respuesta del Endpoint `/actuator/health`

Cuando todos los checks están `UP`:
```json
{
  "status": "UP",
  "components": {
    "appLiveness": {
      "status": "UP",
      "details": {
        "Liveness": "up"
      }
    },
    "appReadiness": {
      "status": "UP",
      "details": {
        "Readiness": "up"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "livenessState": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    }
  },
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

Cuando algún check está `DOWN` (por ejemplo, después de llamar a `/crash`):
```json
{
  "status": "DOWN",
  "components": {
    "appLiveness": {
      "status": "DOWN",
      "details": {
        "Liveness": "down"
      }
    },
    "appReadiness": {
      "status": "UP",
      "details": {
        "Readiness": "up"
      }
    }
  },
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

**Nota:** Los detalles de los componentes se muestran gracias a la propiedad `management.endpoint.health.show-details=always` configurada en `application.properties`.

## Comparación: Quarkus vs Spring Boot 4

| Aspecto | Quarkus | Spring Boot 4 |
|---------|---------|---------------|
| **Dependencia** | `smallrye-health` | `spring-boot-starter-actuator` |
| **Endpoint base** | `/q/health` | `/actuator/health` |
| **Liveness endpoint** | `/q/health/live` | `/actuator/health/liveness` |
| **Readiness endpoint** | `/q/health/ready` | `/actuator/health/readiness` |
| **Anotación Liveness** | `@Liveness` | `@Component` + grupo en properties |
| **Anotación Readiness** | `@Readiness` | `@Component` + grupo en properties |
| **Interfaz** | `HealthCheck` (MicroProfile) | `HealthIndicator` (`org.springframework.boot.health.contributor`) |
| **Método** | `call()` → `HealthCheckResponse` | `health()` → `Health` |
| **Modo dev** | `mvn quarkus:dev` | `mvn spring-boot:run` |
| **Scope** | `@ApplicationScoped` (CDI) | Singleton por defecto (Spring) |

## Comandos Docker (Opcional)

Si necesitas ejecutar la aplicación en un contenedor, puedes usar los siguientes comandos:

### Construir la Imagen

Primero, construye la aplicación JAR:

##### Windows (PowerShell)
```powershell
cd calculator
mvn clean package
```

##### Linux/Mac
```bash
cd calculator
mvn clean package
```

```powershell
cd calculator
mvn clean package
```

Luego construye la imagen del contenedor:

```bash
# Desde el directorio calculator
docker build -t calculator:jvm .
```

```powershell
# Desde el directorio calculator
docker build -t calculator:jvm .
```

```bash
# Desde el directorio calculator
podman build -t calculator:jvm .
```

### Ejecutar el Contenedor

```bash
docker run -i --rm -p 8080:8080 calculator:jvm
```

```powershell
docker run -i --rm -p 8080:8080 calculator:jvm
```

```bash
podman run -i --rm -p 8080:8080 calculator:jvm
```

### Verificar Health Checks en el Contenedor

##### Windows (PowerShell)
```powershell
# Desde otra terminal
Invoke-RestMethod -Uri http://localhost:8080/actuator/health | ConvertTo-Json
# O usando curl
curl.exe http://localhost:8080/actuator/health
```

##### Linux/Mac
```bash
# Desde otra terminal
curl http://localhost:8080/actuator/health
```

```powershell
# Desde otra terminal
curl http://localhost:8080/actuator/health
```

## Resumen

En este laboratorio has aprendido a:

1. ✅ Configurar **Spring Boot Actuator** para habilitar health checks
2. ✅ Implementar un **Liveness Health Check** usando `HealthIndicator` que verifica si la aplicación está viva
3. ✅ Implementar un **Readiness Health Check** usando `HealthIndicator` que verifica si la aplicación está lista para recibir tráfico
4. ✅ Configurar los **grupos de liveness y readiness** en `application.properties`
5. ✅ Verificar el funcionamiento de los health checks usando curl
6. ✅ Entender cómo los health checks responden cuando la aplicación falla
7. ✅ Comprender la **importancia crítica** de los health checks para Kubernetes y cómo Kubernetes los utiliza para:
   - Reiniciar automáticamente contenedores con problemas (liveness)
   - Gestionar el tráfico durante el inicio y mantenimiento (readiness)
   - Mantener alta disponibilidad sin intervención manual

## Próximos Pasos

- Integrar estos health checks en un despliegue de Kubernetes
- Configurar probes de liveness y readiness en los manifiestos de Kubernetes
- Explorar health checks más complejos con métricas personalizadas
- Agregar `management.endpoint.health.show-details=always` para ver detalles completos

---

**¡Enjoy!**  
**José Díaz**
