param(
    [int]$Port = 5520
)

$ErrorActionPreference = "Stop"

$PlayerUuid = "df4bb739-8a30-4b24-b6c1-73d13d48205e"

Write-Host "Building and deploying GoddessTrial..."
.\gradlew.bat clean build deployToServer

if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed. Server will not be started."
}

Write-Host "Deleting saved universe/player/world data..."

if (Test-Path "server\universe") {
    Remove-Item "server\universe" -Recurse -Force
    Write-Host "Deleted server\universe"
} else {
    Write-Host "No server\universe folder found."
}

Write-Host "Granting permanent OP permissions to $PlayerUuid..."

$permissions = @{
    users = @{
        $PlayerUuid = @{
            groups = @(
                "OP",
                "Adventure"
            )
        }
    }
    groups = @{
        Default = @()
        OP = @("*")
        Adventure = @()
    }
}

$permissionsJson = $permissions | ConvertTo-Json -Depth 10
Set-Content -Path "server\permissions.json" -Value $permissionsJson -Encoding UTF8

Write-Host "Starting fresh Hytale server on port $Port..."

Push-Location server
try {
    java -jar .\HytaleServer.jar --assets .\Assets.zip --bind 0.0.0.0:8888
}
finally {
    Pop-Location
}