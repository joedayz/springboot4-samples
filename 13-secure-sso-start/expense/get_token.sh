#!/usr/bin/env bash
# Obtiene un access token de Keycloak (realm quarkus).
# Uso: source ./get_token.sh <usuario> <contraseña>
# Ejemplo: source ./get_token.sh superuser redhat

if [ $# -lt 2 ]; then
  echo 1>&2 "Uso: source $0 <usuario> <contraseña>"
  echo 1>&2 "  Usuarios: user/redhat, superuser/redhat"
  exit 1
fi

SERVER="http://localhost:8888/realms/quarkus/protocol/openid-connect/token"
CLIENT_ID="backend-service"
CLIENT_SECRET="secret"
USERNAME="$1"
PASSWORD="$2"

export TOKEN=$(curl -s -X POST "$SERVER" \
  --user "${CLIENT_ID}:${CLIENT_SECRET}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}" \
  -d "grant_type=password" \
  | jq -r '.access_token // empty')

if [ -z "$TOKEN" ]; then
  echo 1>&2 "No se obtuvo token. Revisa usuario/contraseña y que Keycloak esté en http://localhost:8888"
else
  echo 1>&2 "Token obtenido correctamente."
fi
