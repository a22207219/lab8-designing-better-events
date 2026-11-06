package pt.ulusofona.store.product;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/** Consumer-owned projection: ignores new/unknown producer fields. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SupplierDeactivatedEvent(UUID supplierId) { }
