package pt.ulusofona.store.supplier;
import java.time.Instant;
import java.util.UUID;
/** Published fact; supplierId remains unchanged for Lab 7 consumers. */
public record SupplierDeactivatedEvent(UUID supplierId, UUID eventId, Instant occurredAt,
                                       String supplierName, String reason) { }
