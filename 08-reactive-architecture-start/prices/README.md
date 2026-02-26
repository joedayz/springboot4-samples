# Prices Service

Servicio Python (Flask) que simula un proceso lento (~2 segundos) para el laboratorio de arquitectura reactiva.

## Ejecutar localmente (sin contenedor)

```bash
pip install -r requirements.txt
flask run --host 0.0.0.0
```

Escucha en http://localhost:5000

## Construir y ejecutar con contenedor

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

El servicio products espera prices en `http://localhost:5500` (mapeo 5500:5000).
