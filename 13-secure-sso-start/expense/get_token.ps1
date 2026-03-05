# get_token.ps1 - Obtiene bearer token desde Keycloak (realm quarkus)
# Uso: . .\get_token.ps1 <usuario> <contraseña>
# Ejemplo: . .\get_token.ps1 superuser redhat

param(
    [Parameter(Mandatory=$true)]
    [string]$Username,
    [Parameter(Mandatory=$true)]
    [string]$Password
)

$OIDC_SERVER_URL = "http://localhost:8888"
$REALM = "quarkus"
$CLIENT_ID = "backend-service"
$CLIENT_SECRET = "secret"
$TOKEN_URL = "$OIDC_SERVER_URL/realms/$REALM/protocol/openid-connect/token"

$body = @{
    grant_type = "password"
    client_id = $CLIENT_ID
    client_secret = $CLIENT_SECRET
    username = $Username
    password = $Password
}

try {
    $response = Invoke-RestMethod -Uri $TOKEN_URL -Method Post -Body $body -ContentType "application/x-www-form-urlencoded"
    $env:TOKEN = $response.access_token
    if ($env:TOKEN) {
        Write-Host "Token obtenido correctamente." -ForegroundColor Green
    } else {
        Write-Host "Error: no se encontró access_token en la respuesta." -ForegroundColor Red
    }
} catch {
    Write-Host "Error obteniendo token: $_" -ForegroundColor Red
}
