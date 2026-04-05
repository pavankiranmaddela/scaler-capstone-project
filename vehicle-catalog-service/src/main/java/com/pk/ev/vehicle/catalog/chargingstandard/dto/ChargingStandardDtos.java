package com.pk.ev.vehicle.catalog.chargingstandard.dto;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingstandard.enums.ChargingStandardType;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ChargingStandardDtos {

    private ChargingStandardDtos() {}

    // ─── Request DTOs ────────────────────────────────────────────────────────

    public record CreateChargingStandardRequest(
            @NotBlank @Size(max = 150)
            String name,

            @NotBlank @Size(max = 30)
            String shortCode,

            @NotNull
            ConnectorType connectorType,

            @NotNull
            ChargingStandardType currentType,

            @NotNull @Positive
            Integer maxWattage,

            @Size(max = 100)
            String geographicRegion,

            @Size(max = 100)
            String governingBody,

            @Size(max = 20)
            String version,

            @Size(max = 500)
            String description,

            @Size(max = 500)
            String iconUrl
    ) {}

    public record UpdateChargingStandardRequest(
            @Size(max = 150) String name,
            @Size(max = 30)  String shortCode,
            ConnectorType connectorType,
            ChargingStandardType currentType,
            @Positive Integer maxWattage,
            @Size(max = 100) String geographicRegion,
            @Size(max = 100) String governingBody,
            @Size(max = 20)  String version,
            @Size(max = 500) String description,
            @Size(max = 500) String iconUrl,
            Boolean isDeprecated
    ) {}

    // ─── Filter params ────────────────────────────────────────────────────────

    public record StandardFilterParams(
            String region,
            ChargingStandardType currentType,
            ConnectorType connectorType,
            Boolean deprecated,
            int page,
            int size,
            String sort
    ) {
        public StandardFilterParams {
            if (page < 0)  page  = 0;
            if (size < 1)  size  = 50;
            if (size > 200) size = 200;
            if (sort == null || sort.isBlank()) sort = "name";
        }
    }

    // ─── Response DTOs ────────────────────────────────────────────────────────

    public record ChargingStandardResponse(
            UUID id,
            String name,
            String shortCode,
            ConnectorType connectorType,
            ChargingStandardType currentType,
            Integer maxWattage,
            String geographicRegion,
            String governingBody,
            String version,
            String description,
            String iconUrl,
            Boolean isDeprecated,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record ChargingStandardSummary(
            UUID id,
            String name,
            String shortCode,
            ConnectorType connectorType,
            ChargingStandardType currentType,
            Integer maxWattage,
            String geographicRegion,
            Boolean isDeprecated
    ) {}

    public record PagedStandardsResponse(
            List<ChargingStandardSummary> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}

    // ─── Compatible models (used by GET /charging-standards/{id}/compatible-models) ─

    public record CompatibleModelSummary(
            UUID variantListingId,
            String displayLabel,
            UUID modelId,
            String modelName,
            Integer modelYear,
            String makeName,
            Integer maxAcceptedWattage,
            Integer chargeTime10To80Pct
    ) {}

    // ─── GET /connector-types — flat enum list with metadata ──────────────────

    public record ConnectorTypeMetadata(
            String code,            // enum name — e.g. "CCS2"
            String displayName,     // e.g. "CCS Combo 2"
            ChargingStandardType currentType,
            String primaryRegion,
            String iconUrl
    ) {}

    // ─── Reference lookup helpers ─────────────────────────────────────────────

    public record RegionListResponse(List<String> regions) {}

    public record GoverningBodyListResponse(List<String> governingBodies) {}
}
