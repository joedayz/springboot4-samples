# Stack ELK para Logging en Spring Boot 4

Este proyecto incluye una configuración para coleccionar, almacenar y visualizar logs usando:

- **Fluentd**: para coleccionar y procesar logs
- **Elasticsearch**: para almacenar los logs
- **Kibana**: para visualizar y analizar los logs

## Inicio Rápido

### 1. Construir la aplicación

```bash
mvn clean package -DskipTests
```

### 2. Iniciar el stack completo

Con Docker:

```bash
bash ./start-elk.sh docker
# o
docker compose up -d
```

Con Podman:

```bash
bash ./start-elk.sh podman
# o
podman-compose up -d
```

### 3. Generar algunos logs

```bash
# Obtener un expense existente
curl http://localhost:8080/expenses/joel-2

# Intentar obtener uno inexistente (genera ERROR)
curl http://localhost:8080/expenses/nonexistent

# Obtener todos los expenses
curl http://localhost:8080/expenses
```

### 4. Visualizar logs en Kibana

1. Abre http://localhost:5601 en tu navegador
2. Ve a **Kibana** → **Data Views**
3. Haz clic en **Create data view**
4. Para **Name** e **Index pattern**, ingresa: `spring-*`
5. Selecciona `@timestamp` como time field (si está disponible)
6. Haz clic en **Create data view**
7. Ve a **Discover** para ver los logs

## Comandos Útiles

### Ver logs de los contenedores

Con Docker:

```bash
docker compose logs -f expenses-app
docker compose logs -f fluentd
docker compose logs -f elasticsearch
docker compose logs -f kibana
```

Con Podman:

```bash
podman-compose logs -f expenses-app
podman-compose logs -f fluentd
podman-compose logs -f elasticsearch
podman-compose logs -f kibana
```

### Consultar Elasticsearch directamente

Listar índices:

```bash
curl http://localhost:9200/_cat/indices?v
```

Buscar logs:

```bash
curl "http://localhost:9200/spring-*/_search?pretty" | jq
```

Contar logs:

```bash
curl "http://localhost:9200/spring-*/_count?pretty"
```

### Detener el stack

```bash
docker compose down
docker compose down -v
```

## Estructura del Proyecto

```text
expenses/
├── docker-compose.yml
├── Dockerfile
├── fluentd/
│   └── conf/
│       └── fluent.conf
├── logs/
│   └── app.log         # archivo inspeccionable (opcional)
└── start-elk.sh
```

## Formato de Logs

Los logs se generan usando el patrón configurado en `logback-spring.xml` y/o `application.properties`.
El archivo en `logs/app.log` se usa para inspección local. El envío al stack ELK se realiza via syslog (Fluentd → Elasticsearch).

## Personalización

Si cambias el formato de log que incluyes en syslog, puedes necesitar ajustar el procesamiento en `fluentd/conf/fluent.conf`.

