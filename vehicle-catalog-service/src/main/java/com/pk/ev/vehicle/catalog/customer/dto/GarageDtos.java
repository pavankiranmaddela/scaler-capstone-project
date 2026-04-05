package com.pk.ev.vehicle.catalog.customer.dto;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.variant.enums.VariantStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class GarageDtos {

    private GarageDtos() {}

    // ─── Request DTOs ─────────────────────────────────────────────────────────

    public record AddVehicleRequest(
            @NotNull(message = "variantListingId is required")
            UUID variantListingId,

            @Size(max = 100, message = "Nickname must be 100 characters or fewer")
            String nickname,

            @Size(max = 20, message = "Registration number must be 20 characters or fewer")
            @Pattern(regexp = "^[A-Z0-9]{1,20}$",
                    message = "Registration number must contain only uppercase letters and digits")
            String registrationNumber,

            @Min(value = 1990, message = "Purchase year must be 1990 or later")
            @Max(value = 2100, message = "Purchase year must be realistic")
            Integer purchaseYear,

            /** If true, any existing primary vehicle is automatically demoted */
            Boolean isPrimary
    ) {}

    public record UpdateVehicleRequest(
            @Size(max = 100) String nickname,

            @Size(max = 20)
            @Pattern(regexp = "^[A-Z0-9]{1,20}$",
                    message = "Registration number must contain only uppercase letters and digits")
            String registrationNumber,

            @Min(1990) @Max(2100)
            Integer purchaseYear,

            /** Setting to true demotes the current primary vehicle */
            Boolean isPrimary
    ) {}

    // ─── Response DTOs ────────────────────────────────────────────────────────

    /** Concise catalog summary embedded in garage responses */
    public record VariantSummaryInGarage(
            UUID variantListingId,
            String displayLabel,
            String makeName,
            String modelName,
            Integer modelYear,
            String trimName,
            BigDecimal batteryCapacityKwh,
            Integer rangeKm,
            BigDecimal onboardChargerKw,
            ConnectorType connectorType,
            VariantStatus variantStatus
    ) {}

    /** Full garage entry response */
    public record CustomerVehicleResponse(
            UUID id,
            UUID userId,
            VariantSummaryInGarage variant,
            String nickname,
            String registrationNumber,
            Integer purchaseYear,
            Boolean isPrimary,
            Instant addedAt
    ) {}

    /** Lightweight response used in list views */
    public record CustomerVehicleSummary(
            UUID id,
            String displayLabel,   // nickname if set, otherwise variant displayLabel
            String makeName,
            String modelName,
            Integer modelYear,
            Boolean isPrimary,
            BigDecimal onboardChargerKw,
            ConnectorType connectorType,
            Instant addedAt
    ) {}

    /** Response for GET /garage/vehicles (list) */
    public record GarageResponse(
            List<CustomerVehicleSummary> vehicles,
            int totalCount,
            UUID primaryVehicleId  // null if no primary set
    ) {}

    /** Admin paginated view */
    public record AdminGaragePageResponse(
            List<CustomerVehicleResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}

    /**
     * Compatible stations response for
     * GET /garage/vehicles/{vehicleId}/compatible-stations.
     *
     * Wraps the compatibility engine's station list and adds
     * garage-specific context (which vehicle, its primary status).
     */
    public record CompatibleStationsResponse(
            UUID customerVehicleId,
            String vehicleDisplayLabel,
            Boolean isPrimary,
            List<CompatibleStationEntry> stations,
            int totalCount
    ) {}

    public record CompatibleStationEntry(
            UUID stationId,
            Integer maxAchievableWattage,
            Integer estimatedCharge10To80Pct,
            ConnectorType matchedConnectorType
    ) {}
}
