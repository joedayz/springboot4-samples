#!/usr/bin/env pwsh

while ($true) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/cpu/predict" -Method Get
        $responseString = $response | ConvertTo-Json -Compress

        if ($responseString.Length -gt 50) {
            $trimmed = $responseString.Substring(0, 50) + "...}"
        } else {
            $trimmed = $responseString
        }

        Write-Host $trimmed
        Start-Sleep -Seconds 1
    }
    catch {
        Write-Host $_.Exception.Message
        Start-Sleep -Seconds 1
    }
}
