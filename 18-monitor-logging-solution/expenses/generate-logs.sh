#!/bin/bash

# Script para generar logs en la aplicación Spring Boot
# Uso: ./generate-logs.sh

set -e

echo "=========================================="
echo "Generando logs en la aplicación Spring Boot"
echo "=========================================="
echo ""

# Verificar que la aplicación esté corriendo
if ! curl -s http://localhost:8080/expenses > /dev/null 2>&1; then
  echo "⚠️  La aplicación no está respondiendo en http://localhost:8080"
  echo "   Asegúrate de que el servicio 'expenses-app' esté corriendo:"
  echo "   docker compose ps"
  exit 1
fi

echo "✓ Aplicación está respondiendo"
echo ""

echo "1. Obteniendo todos los expenses (INFO)..."
curl -s http://localhost:8080/expenses > /dev/null
sleep 1

echo "2. Obteniendo un expense existente (DEBUG)..."
curl -s http://localhost:8080/expenses/joel-2 > /dev/null
sleep 1

echo "3. Intentando obtener un expense inexistente (ERROR)..."
curl -s http://localhost:8080/expenses/nonexistent > /dev/null
sleep 1

echo ""
echo "✓ Logs generados"
echo ""
echo "Espera 10-30 segundos para que Fluentd procese los logs..."
echo ""
echo "Luego verifica los índices:"
echo "  curl http://localhost:9200/_cat/indices?v"
echo ""
echo "Y verifica el archivo de log local:"
echo "  tail -f logs/app.log"

