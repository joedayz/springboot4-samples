# Prices Service

Servicio Python (Flask) que simula un proceso lento (~2 segundos) para el laboratorio de arquitectura reactiva.

## Ejecutar con docker-compose (recomendado)

Desde el directorio `08-reactive-architecture-solution`:

```bash
docker compose up -d
# o
podman compose up -d
```

## Construir y ejecutar manualmente

**Docker:**
```bash
docker build -f Containerfile -t prices:latest .
docker run -d --name prices -p 5500:5000 prices:latest
```

**Podman:**
```bash
podman build -f Containerfile -t prices:latest .
podman run -d --name prices -p 5500:5000 prices:latest
```

## Ejecutar localmente (sin contenedor)

```bash
pip install -r requirements.txt
flask run --host 0.0.0.0
```

Escucha en http://localhost:5000 (products debe apuntar a 5000 en ese caso).
