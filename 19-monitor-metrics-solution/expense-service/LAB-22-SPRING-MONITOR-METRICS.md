# LAB 22: SPRING BOOT 4 MONITOR METRICS (SOLUTION)

Autor: José Díaz

Este módulo ya contiene la instrumentación completa:

- `callsToGetExpenses_total`: Counter del endpoint GET `/expenses`
- `callsToPostExpenses_total`: Counter del endpoint POST `/expenses`
- `expenseCreationTime_seconds`: Timer del endpoint POST `/expenses`
- `timeSinceLastGetExpenses`: Gauge (ms) desde la última llamada GET `/expenses`

## Verificación rápida

1. Ejecuta el servicio:

```bash
mvn spring-boot:run
```

2. Verifica el endpoint Prometheus:

```bash
curl http://localhost:8080/actuator/prometheus | grep -E "callsToGetExpenses_total|callsToPostExpenses_total|expenseCreationTime_seconds|timeSinceLastGetExpenses"
```

3. Levanta Prometheus y Grafana:

```bash
docker-compose up -d
```

4. Genera tráfico:

```bash
./scripts/simulate-traffic.sh
```

5. Abre Grafana: http://localhost:3000
   - Dashboard: `Expense Service Metrics Dashboard`

