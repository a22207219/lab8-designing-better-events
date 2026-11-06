# SupplierDeactivated — implemented event

| Property | Contract |
|---|---|
| Event | `SupplierDeactivated` (fact; past tense) |
| Owner | Supplier Service |
| Topic | `supplier-deactivated-events` (same Lab 7 topic) |
| Kafka key | `supplierId` UUID string; preserves order per supplier |
| Publisher trigger | Only when an active supplier transitions to inactive |
| Consumer | Product Service (discontinue products for this supplier) |
| Delivery | At least once; idempotent database UPDATE |

## Additive payload example

```json
{
  "supplierId": "6f1c2f8e-2a7d-4a1e-9a3f-9f0f7f6a1b22",
  "eventId": "5e2f7c87-ef20-44cf-a1f1-e26d7aac39f0",
  "occurredAt": "2026-11-06T13:30:00Z",
  "supplierName": "North Warehouse",
  "reason": "Deactivated by operator"
}
```

## Why each field is included

- `supplierId` (**required**, UUID): stable aggregate reference and correlation key; Product Service uses this to discontinue matching products; retains Lab 7 compatibility.
- `eventId` (**new, optional for legacy readers**, UUID): unique publication identity, useful for deduplication, traceability and auditing. Not a database row id.
- `occurredAt` (**new, optional for legacy readers**, ISO-8601 UTC): when the business transition occurred; distinguishes historical replay from fresh events. Not consumer processing time.
- `supplierName` (**new, optional**, string): notification or audit consumers can describe the historical supplier without synchronous lookup. Snapshot at event time, not a live profile.
- `reason` (**new, optional**, string): brief business context; current implementation uses the explicit constant `Deactivated by operator`, NOT a claimed user-supplied reason. Future API can accept a validated reason; do not invent a cause.

## Deliberately omitted

- Supplier address, phone, personal contacts, email, tax IDs: not needed by known consumers, may be sensitive, and would persist in Kafka logs.
- Complete `Supplier` entity: couples consumers to JPA schema and leaks implementation details.
- List of product IDs: owned by Product Service and may be stale at emission time; would couple services.
- `isActive: false`: already conveyed by the event name, so redundant.
- Product/order state: owned by other domains; avoid cross-service snapshot coupling.
- `schemaVersion`: changes are additive on the same topic; compatibility is contract-tested. Explicit major version/topic is reserved for real breaking redesign.

## Compatibility and failure handling

- Existing Lab 7 records `{ "supplierId": "<uuid>" }` still deserialize because the consumer only needs supplierId.
- New records have extra fields; `@JsonIgnoreProperties(ignoreUnknown=true)` makes the consumer tolerant.
- If supplierId is missing, null, or nil UUID, processing fails visibly rather than silently discarding a business event.
- Duplicate events are safe because the UPDATE only touches non-discontinued products.
- Lab 7's **previous downloadable example** unfortunately used `Long` IDs. These cannot be converted to true UUIDs without an explicit data migration. The corrected Lab 07 ZIP uses UUIDs and shares named volumes with this Lab 08 ZIP. Do NOT reuse databases from the older Long-ID ZIP or assume old numeric-key messages work with UUID entities.
- This demo still uses DB-then-Kafka dual write, which has a failure window. Transactional outbox is the next lab's concern.
