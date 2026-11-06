$ErrorActionPreference = 'Stop'
& "$PSScriptRoot/smoke-test.ps1"
$topic = 'supplier-deactivated-events'
Write-Host 'Inspect the produced message at http://localhost:8090 -> Topics -> supplier-deactivated-events' -ForegroundColor Cyan
Write-Host 'Check supplierId, eventId, occurredAt, supplierName, reason are present.'
Write-Host 'To test a backward-compatible event, publish a JSON record with only supplierId (valid UUID) via Kafka UI.'
