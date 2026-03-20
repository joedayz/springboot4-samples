# LAB 23: SPRING BOOT 4 MONITOR TRACE (SOLUTION)

Este módulo ya tiene tracing distribuido habilitado (OpenTelemetry + OTLP -> Jaeger) para los microservicios:

- `solver` (8080)
- `adder` (8081)
- `multiplier` (8082)

## Paso 1: Iniciar Jaeger

Ejecuta:

```bash
bash ./jaeger.sh
```

```powershell
bash ./jaeger.sh
```

Abre: http://localhost:16686

## Paso 2: Iniciar los 3 microservicios

Desde este directorio:

```bash
bash ./start.sh
```

```powershell
bash ./start.sh
```

## Paso 3: Generar trazas

1. Llama a `adder`:

```bash
curl http://localhost:8081/adder/5/3
```

```powershell
curl http://localhost:8081/adder/5/3
```

2. Llama a `multiplier`:

```bash
curl http://localhost:8082/multiplier/5/3
```

```powershell
curl http://localhost:8082/multiplier/5/3
```

3. Llama a `solver`:

```bash
curl "http://localhost:8080/solver/5*4+3"
```

```powershell
curl "http://localhost:8080/solver/5*4+3"
```

## Paso 4: Visualizar en Jaeger

En Jaeger (http://localhost:16686) usa **Find Traces** y filtra por el servicio (`adder`, `multiplier`, `solver`).

