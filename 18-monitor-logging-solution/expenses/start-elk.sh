#!/bin/bash

# Script para iniciar el stack ELK (Elasticsearch, Kibana, Fluentd) para la app Spring Boot
# Uso: ./start-elk.sh [docker|podman]

set -e

if [ "$1" == "podman" ]; then
  CMD="podman"
  USE_PODMAN=true
elif [ "$1" == "docker" ]; then
  CMD="docker"
  USE_PODMAN=false
else
  # Detectar automáticamente
  if command -v podman &> /dev/null && command -v podman-compose &> /dev/null; then
    CMD="podman"
    USE_PODMAN=true
    echo "Usando Podman..."
  elif command -v docker &> /dev/null; then
    CMD="docker"
    USE_PODMAN=false
    echo "Usando Docker..."
  else
    echo "Error: No se encontró Docker ni Podman instalado"
    exit 1
  fi
fi

compose_cmd() {
  if [ "$USE_PODMAN" = true ]; then
    (cd "$SCRIPT_DIR" && podman-compose "$@")
  else
    (cd "$SCRIPT_DIR" && docker compose "$@")
  fi
}

echo "=========================================="
echo "Iniciando stack ELK con $CMD"
echo "=========================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo "1. Construyendo la aplicación Spring Boot..."
mvn clean package -DskipTests

echo "✓ Build completado correctamente"

mkdir -p logs
chmod 777 logs || true

echo ""
echo "2. Iniciando Elasticsearch, Kibana y Fluentd..."
compose_cmd up -d elasticsearch kibana fluentd

echo ""
echo "3. Esperando a que Elasticsearch esté listo..."
sleep 10

echo ""
echo "4. Construyendo y levantando la aplicación Spring Boot..."
compose_cmd build expenses-app
compose_cmd up -d expenses-app

echo ""
echo "=========================================="
echo "Stack ELK iniciado correctamente!"
echo "=========================================="
echo ""
echo "Servicios disponibles:"
echo "  - Aplicación Spring Boot: http://localhost:8080"
echo "  - Kibana: http://localhost:5601"
echo "  - Elasticsearch: http://localhost:9200"
echo ""
echo "Para ver los logs:"
echo "  docker compose logs -f expenses-app"
echo "  docker compose logs -f fluentd"

echo ""
echo "Para detener todo:"
if [ "$USE_PODMAN" = true ]; then
  echo "  podman-compose down"
else
  echo "  docker compose down"
fi

