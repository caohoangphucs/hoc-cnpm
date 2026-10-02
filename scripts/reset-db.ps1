# Reset only the local/demo PostgreSQL database. This deletes existing demo data.
param([switch]$ResetDemo)
$ErrorActionPreference = 'Stop'
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
if (-not $ResetDemo) { throw 'Use -ResetDemo to explicitly reset the local/demo database.' }
Set-Location (Split-Path $PSScriptRoot -Parent)
docker compose up -d --wait cnpm-db
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL did not become healthy.' }
Get-Content -Raw -Encoding UTF8 database/01_schema.sql |
    docker compose exec -T cnpm-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 --single-transaction'
if ($LASTEXITCODE -ne 0) { throw 'Schema failed.' }
Get-Content -Raw -Encoding UTF8 database/02_seed.sql |
    docker compose exec -T cnpm-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 --single-transaction'
if ($LASTEXITCODE -ne 0) { throw 'Seed failed.' }
$verification = "SELECT COUNT(*) AS tables FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';"
$verification | docker compose exec -T cnpm-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1'
if ($LASTEXITCODE -ne 0) { throw 'Verification failed.' }
Write-Host 'PostgreSQL demo reset completed.' -ForegroundColor Green
