param(
    [int]$Port = 8888,
    [switch]$ResetAuth
)

$ErrorActionPreference = "Stop"

$PlayerUuid = "df4bb739-8a30-4b24-b6c1-73d13d48205e"

Write-Host "Stopping leftover Hytale server processes..."

Get-CimInstance Win32_Process |
        Where-Object {
            $_.Name -eq "java.exe" -and
                    $_.CommandLine -like "*HytaleServer.jar*"
        } |
        ForEach-Object {
            Write-Host "Stopping old Hytale server process $($_.ProcessId)..."
            Stop-Process -Id $_.ProcessId -Force
        }

Start-Sleep -Seconds 2

if ($ResetAuth) {
    Write-Host "Deleting stored auth token..."

    if (Test-Path "server\auth.enc") {
        Remove-Item "server\auth.enc" -Force
        Write-Host "Deleted server\auth.enc"
    } else {
        Write-Host "No server\auth.enc found."
    }

    Write-Host "Auth was reset. After the server boots, run /auth login in the server console."
}

Write-Host "Building and deploying GoddessTrial..."
.\gradlew.bat clean build deployToServer

if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed. Server will not be started."
}


Write-Host "Deleting stale backup files..."

Get-ChildItem "server" -Recurse -Filter "*.bak" -ErrorAction SilentlyContinue |
        Remove-Item -Force -ErrorAction SilentlyContinue

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
    java -jar .\HytaleServer.jar --assets .\Assets.zip --bind "0.0.0.0:$Port"
}
finally {
    Pop-Location
}