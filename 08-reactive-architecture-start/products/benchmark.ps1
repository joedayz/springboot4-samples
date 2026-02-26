# Benchmark script for Windows PowerShell
# Sends multiple parallel requests to /products/1/priceHistory

$REQUESTS = if ($args[0]) { $args[0] } else { 10 }
$URL = "http://localhost:8080/products/1/priceHistory"

Write-Host ""
for ($i = 1; $i -le $REQUESTS; $i++) {
    Write-Host "Sending request..."
    Start-Job -ScriptBlock { param($u) Invoke-WebRequest -Uri $u -UseBasicParsing | Out-Null } -ArgumentList $URL
    Start-Sleep -Milliseconds 100
}
Get-Job | Wait-Job | Out-Null
Get-Job | Remove-Job
