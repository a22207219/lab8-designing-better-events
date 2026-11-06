# Versioning plan — ProductPriceChanged

## Contract and ownership

Product Service owns and publishes `ProductPriceChanged` on `product-price-changed-events`, keyed by productId UUID. Consumers have independently versioned deployments. The producer must keep existing fields and their types/semantics stable; downstream consumers ignore fields they do not use. The event documents **what happened** rather than telling an Order Service what to do.

## Non-breaking additive change (v1 compatible)

Introduce optional `promotionId` (UUID or null) for promotion reporting. Old consumers continue to read the unchanged required fields. New consumers tolerate its absence on replay of earlier messages. Publish updated contract docs, add fixture tests for old/new JSON, stage release and monitor failures/lag; do not backfill historical records with invented promotions.

## Breaking change

Renaming `newPrice` to `priceAfter` removes a required field from existing readers, even though both describe the same value. Changing from decimal-string to floating-point number also breaks type and precision expectations. **Do not silently rename** on the current topic.

## Migration options

1. Preferred, if meaning unchanged: publish **both** `newPrice` and `priceAfter` for a deprecation period, keeping `newPrice` authoritative until all readers are migrated. Coordinate deploys and contract tests. Remove `newPrice` only if past messages and active consumer versions have been handled explicitly.
2. If incompatible semantics cannot coexist: create `product-price-changed-v2-events`, design new schema and retention, dual-publish temporarily with an explicit migration window, and switch consumer groups after validating replay/dedup and event ordering across topics. Consumers must not process both copies as two independent business changes. Retire v1 only with evidence all subscribers migrated and retention/replay policies are agreed.

## Missing, unknown and obsolete fields

Old consumers must ignore unknown additive fields. New consumers need nullable/optional fields for data that older records lack, with defined defaults or explicit rejection when genuinely mandatory. Missing `productId`/`newPrice` is invalid, not a harmless optional value. Reject or dead-letter malformed records with logging and an incident recovery process. Deduplicate by eventId and preserve ordering per product key; independent topics do not provide cross-topic ordering.

## Quality gates

Run fixture-based producer/consumer contract tests with both pre-change and post-change examples; retain at least one historical v1 fixture. Review retention, access restrictions, observability, replays, rollback path and historical interpretation of timestamps before release.
