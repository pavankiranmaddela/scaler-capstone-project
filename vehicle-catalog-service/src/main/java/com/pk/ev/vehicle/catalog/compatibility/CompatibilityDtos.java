package com.pk.ev.vehicle.catalog.compatibility;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public final class CompatibilityDtos {

    private CompatibilityDtos() {}

    // ─── Shared inner types ───────────────────────────────────────────────────

    /**
     * A matching charging spec from the vehicle side — returned inside every
     * CompatibilityResult when isCompatible=true.
     */
    public record MatchedSpec(
            UUID specId,
            ConnectorType connectorType,
            Integer vehicleMaxAcceptedWattage,   // what the vehicle can accept
            Integer stationConnectorMaxWattage,  // what the station can deliver
            Integer maxAchievableWattage,        // min(vehicle, station) — the actual usable wattage
            Integer estimatedCharge10To80Pct,    // interpolated minutes at actual wattage
            Boolean cableIncluded
    ) {}

    // ─── Response: single compatibility result ────────────────────────────────

    public record CompatibilityResult(
            UUID variantListingId,
            String variantDisplayLabel,

            // Either stationId or connectorType is set depending on the check type
            UUID stationId,
            ConnectorType connectorType,

            boolean isCompatible,
            List<MatchedSpec> compatibleSpecs,    // empty when isCompatible=false
            Integer maxAchievableWattage,         // best wattage across all matched specs; null if incompatible
            Integer estimatedCharge10To80Pct,     // at maxAchievableWattage; null if incompatible
            String incompatibilityReason          // null when compatible
    ) {}

    // ─── Response: station → all compatible vehicles ──────────────────────────

    public record StationCompatibleVariants(
            UUID stationId,
            List<CompatibleVariantSummary> compatibleVariants,
            int totalCount
    ) {}

    public record CompatibleVariantSummary(
            UUID variantListingId,
            String displayLabel,
            String makeName,
            String modelName,
            Integer modelYear,
            Integer maxAchievableWattage,
            Integer estimatedCharge10To80Pct
    ) {}

    // ─── Response: connector type → all compatible vehicles ───────────────────

    public record ConnectorCompatibleVariants(
            ConnectorType connectorType,
            List<CompatibleVariantSummary> compatibleVariants,
            int totalCount
    ) {}

    // ─── Request: bulk check ──────────────────────────────────────────────────

    public record BulkCompatibilityRequest(
            @NotNull
            List<CompatibilityPair> pairs
    ) {
        public record CompatibilityPair(
                @NotNull UUID variantListingId,
                @NotNull UUID stationId
        ) {}
    }

    public record BulkCompatibilityResponse(
            List<CompatibilityResult> results,
            int totalChecked,
            int compatibleCount,
            int incompatibleCount
    ) {}
}
