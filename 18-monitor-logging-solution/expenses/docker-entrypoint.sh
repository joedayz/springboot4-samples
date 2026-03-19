#!/bin/bash
set -e

# Aseguramos que el directorio exista (docker-compose monta ./logs -> /var/log/expenses).
mkdir -p /var/log/expenses || true

exec "$@"

