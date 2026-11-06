# Event catalogue — designs only (NOT implemented)

All identifiers below are UUIDs. Events are immutable past-tense **facts**, never commands. Owner is responsible for schema governance; use JSON UTC timestamps. Avoid publishing PII in retained Kafka topics.

## 1. ProductPriceChanged

| Topic | Key | Owner | Trigger | Consumers |
|---|---|---|---|---|
| `product-price-changed-events` | `productId` UUID | Product Service | Price update committed | Order Service (recalculate *future* quotations), Analytics (trend report) |

```json
{
  "eventId": "8c833303-a03a-4fdc-8c2f-d1bc32c85475",
  "productId": "e0a88b1f-b077-4fc7-a43a-c5a651ff4df8",
  "occurredAt": "2026-11-06T13:30:00Z",
  "previousPrice": "19.99",
  "newPrice": "17.50",
  "currency": "EUR"
}
```

**Fields:** `eventId` UUID required for audit/dedup; `productId` UUID required for correlation; `occurredAt` UTC required for event-time interpretation; `previousPrice` and `newPrice` decimal strings required to preserve exact monetary values; `currency` ISO-4217 required because amounts without unit are ambiguous. Excludes product descriptions, images, supplier address and JPA relationships: none is needed to interpret the price change. Order Service must never retroactively change prices on orders already placed. Per-key ordering and idempotent `eventId` processing are required.

**Evolution:** adding optional `promotionId` is safe; renaming `newPrice` is breaking. Owner communicates additions, tests old consumers, monitors lag.

## 2. OrderPlaced

| Topic | Key | Owner | Trigger | Consumers |
|---|---|---|---|---|
| `order-placed-events` | `orderId` UUID | Order Service | Order accepted and persisted | Inventory (reserve stock), Notification (confirmation), Analytics |

```json
{
  "eventId": "69483b70-30ed-4dd6-83b3-577bdf313d38",
  "orderId": "95d20ba3-a6c6-4ed2-864d-71c5a1fb7dc8",
  "occurredAt": "2026-11-06T13:31:00Z",
  "items": [{"productId":"e0a88b1f-b077-4fc7-a43a-c5a651ff4df8","quantity":2,"unitPrice":"17.50"}],
  "currency": "EUR",
  "totalAmount": "35.00"
}
```

**Fields:** unique event and order IDs for idempotency/correlation; UTC business time; immutable list of ordered product IDs, strictly positive quantities and exact unit prices; currency and total for financial reconciliation. This is an order snapshot **at placement**, not a live product catalogue. Deliberately exclude buyer email, name, postal address, payment card information, and internal DB rows; notifications should fetch sensitive contact data over a suitably secured channel, not place it into a retained topic. Notification sends must be deduplicated by `eventId` or `orderId`. Use at-least-once delivery, avoid claiming exactly-once email sending.

**Evolution:** optional `salesChannel` addition is compatible; replacing `items` with a different structure is breaking, requiring parallel transition or v2 topic.

## 3. SupplierReactivated

| Topic | Key | Owner | Trigger | Consumers |
|---|---|---|---|---|
| `supplier-reactivated-events` | `supplierId` UUID | Supplier Service | Inactive supplier is explicitly reactivated | Product Service, Procurement projections, Analytics |

```json
{
  "eventId": "bd9b3530-f33d-4ca2-99ee-493385b5fc52",
  "supplierId": "6f1c2f8e-2a7d-4a1e-9a3f-9f0f7f6a1b22",
  "occurredAt": "2026-11-06T13:35:00Z",
  "supplierName": "North Warehouse"
}
```

**Fields:** stable event and supplier IDs, occurrence time, historical display name. No full entity, personal contacts or list of affected products. **Critical business rule:** this event must *not* automatically reactivate discontinued products; discontinued can be permanent for independent reasons. Product Service would require a separate explicit domain policy. Consumers should be tolerant to future fields and idempotent.

**Evolution:** optional `reactivationReason` is additive; changing the meaning of reactivation requires a new semantic event, never silently repurpose the old contract.
