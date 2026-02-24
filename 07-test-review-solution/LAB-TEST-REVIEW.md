# Laboratorio: Revisión de Pruebas

**Autor:** José Díaz  
**Proyecto:** Spring Boot 4 - 07-test-review-start

## Instrucciones

Está probando una aplicación basada en microservicios que implementa un sistema de gestión de conferencias. La aplicación consta de tres microservicios:

1. **Microservicio `schedule`:**
   - **Función:** Gestiona los horarios de las conferencias.
   - **Base de datos:** Almacena datos en una base de datos H2 en memoria.
   - **Razón del fallo inicial de las pruebas:** Las pruebas fallan inicialmente porque el endpoint HTTP de prueba no está configurado y el H2 no está funcionando correctamente en tests.

2. **Microservicio `speaker`:**
   - **Función:** Gestiona los oradores de las conferencias.
   - **Base de datos:** Almacena datos en una base de datos H2 en memoria.
   - **Inicialización:** Cuando el servicio se inicia, Spring Boot pobla la base de datos con datos de prueba.
   - **Razón del fallo inicial de las pruebas:** Las pruebas fallan inicialmente debido a una dependencia faltante y un escenario de prueba que requiere que la base de datos devuelva una lista vacía de oradores.

3. **Microservicio `session`:**
   - **Función:** Gestiona las sesiones de las conferencias.
   - **Base de datos:** Almacena datos en una base de datos PostgreSQL.
   - **Dependencias:** Este servicio depende del servicio `speaker` para obtener información de los oradores.
   - **Razón del fallo inicial de las pruebas:** Las pruebas fallan inicialmente porque no puede encontrar la imagen del contenedor PostgreSQL y el servicio `speaker` no es accesible.

**Objetivo Final:** Debe hacer que las pruebas pasen en cada uno de los tres servicios.

---

## Paso 1: Abrir el proyecto schedule y corregir la clase ScheduleResourceTest

### Instrucciones:
- Convierta las pruebas de esta clase en pruebas Spring Boot.
- Haga que las pruebas utilicen la URL base del ScheduleController.

### 1.1. Navegue al directorio del servicio schedule.

```bash
cd schedule
```

### 1.2. Abra el proyecto con el editor de su preferencia.

### 1.3. Verifique que cuatro pruebas estén fallando.

Ejecute las pruebas:

```bash
mvn test
```

### 1.4. Agregue `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)` y configure la base URL para RestAssured usando `@LocalServerPort`.

### 1.5. Verifique que las pruebas aún fallen por un error de conexión a la base de datos.

---

## Paso 2: Las pruebas del servicio schedule siguen fallando debido a un error de conexión a la base de datos

### Instrucción:
Modifique el archivo de configuración para que la propiedad de conexión H2 no se aplique al perfil de prueba.

### 2.1. Use el prefijo `spring.profiles.active` o configure `spring.datasource.url` solo para el perfil `prod`.

---

## Paso 3: Cambie al servicio speaker e inyecte la dependencia faltante DeterministicIdGenerator

### Instrucción:
Para inyectar esta dependencia, la clase `DeterministicIdGenerator` también debe actualizarse para ser un bean de test.

### 3.1. Use `@MockBean` o registre `DeterministicIdGenerator` como bean de test con `@TestConfiguration`.

---

## Paso 4: Actualice la prueba SpeakerResourceTest#testListEmptySpeakers

### Instrucción:
Mockee el repositorio para que retorne una lista vacía cuando se llame a `findAll()`.

---

## Paso 5: Abra el microservicio session y corrija la configuración de Testcontainers

### Instrucción:
Use la imagen de PostgreSQL: `postgres:14.1`

---

## Paso 6: Mockee el SpeakerService en testGetSessionWithSpeaker

### Instrucciones:
- La prueba falla porque el otro servicio **no es accesible**.
- Corrija la prueba **mockeando** el `SpeakerService` con `@MockBean`.
- El método mockeado debe **retornar un speaker** que cumpla con las expectativas de la prueba.

---

## Conclusión

¡Enjoy!

**José**
