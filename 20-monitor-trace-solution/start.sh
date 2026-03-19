#!/bin/bash

# Arranca los 3 microservicios en paralelo.
# Uso: ./start.sh

set -e

start_service() {
  local service_dir="$1"
  (cd "$service_dir" && mvn -q spring-boot:run) &
  echo $!
}

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Starting services..."

PIDS=()
PIDS+=("$(start_service "$ROOT_DIR/solver")")
PIDS+=("$(start_service "$ROOT_DIR/adder")")
PIDS+=("$(start_service "$ROOT_DIR/multiplier")")

echo "Services started."
echo "Press ENTER to stop..."
read -r

echo "Stopping services..."
for pid in "${PIDS[@]}"; do
  kill "$pid" 2>/dev/null || true
done

echo "Stopped."

