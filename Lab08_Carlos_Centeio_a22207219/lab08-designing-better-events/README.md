# Lab 08 — Designing Better Events

**Aluno:** Carlos Centeio  
**Número de aluno:** a22207219


**Supplied as a standalone continuation of the generated Lab 07 example.** It is not a verified copy of the student's earlier repository.

## Implemented

- Java 25 + Spring Boot 4.1.0, Supplier Service 8082, Product Service 8081, Kafka 4.3.1 KRaft, Kafka UI 8090, one database per service.
- UUID IDs end-to-end; `SupplierDeactivated` is a fact published to **the existing** `supplier-deactivated-events` topic, keyed by supplier UUID, only on active-to-inactive transition.
- Event contains `supplierId`, `eventId`, `occurredAt`, `supplierName`, `reason`; Product Service consumes only `supplierId` and ignores unknown fields.
- Legacy UUID one-field messages continue to be readable (fields newly added are not required). Idempotent update handles duplicates.
- Documentation under `docs/events`: rationale for included and excluded fields, three **design-only** events and a detailed versioning plan.

## Windows PowerShell

Install and start Docker Desktop. In the extracted project folder run:

```powershell
docker network inspect store-network 2>$null
if ($LASTEXITCODE -ne 0) { docker network create store-network }
docker compose up -d --build
docker compose ps
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

Kafka UI: http://localhost:8090; Supplier API: http://localhost:8082; Product API: http://localhost:8081.

Use UI topic `supplier-deactivated-events` to inspect the new messages. To test tolerant reading, use the Kafka UI producer tool to write a record with the supplier UUID key and a one-field JSON payload containing an existing supplier's UUID (or publish a replay of an earlier Lab 7 UUID message). Existing products will remain discontinued and the listener should not throw.

**Lab 08 evaluation warning:** Main GitBook page was accessible, but linked detailed delivery/rubric pages were not accessible during preparation; exact rubric conformity and deadline cannot be verified. Docker and Maven compilation were not available in the authoring environment. Run the smoke test and review your instructor's delivery instructions before submitting.

### Limitations

- Demo retains the Lab 7 DB/Kafka dual-write failure window; transactional outbox belongs to Lab 9.
- Only SupplierDeactivated is implemented. The other 3 event catalogue entries are intentionally **not implemented** according to Lab 08.
- Order Service is documented as a collaborating domain, not implemented inside this standalone starter. If submission requires modified existing Labs 4–7 repo, merge into that repository instead of submitting this as a replacement.

## Lab 07 → Lab 08 upgrade (same Kafka records and database IDs)

Both ZIPs use UUID identifiers, external Docker network `store-network` and the **same named data volumes** for Kafka and the two PostgreSQL databases. Do not run the stacks at the same time (they share published ports). In the Lab 07 folder, run `docker compose down` (without `-v`). Then in the Lab 08 folder, run `docker network create store-network` if needed, followed by `docker compose up -d --build`. Keep the named volumes to preserve old one-field Kafka events and supplier/product records. **Never** use `docker compose down -v` during an upgrade. Existing data made with the original earlier Long-ID ZIP must not be reused without an explicit migration.

Run `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1` in the current lab folder. JSON responses expose `discontinued` (Java getter `isDiscontinued()`).

**Verification scope:** This deliverable has undergone static checks, but Docker/Maven integration tests must be run on a host with Docker Desktop and Java 25. Instructor delivery rubric pages were unavailable for direct verification.
