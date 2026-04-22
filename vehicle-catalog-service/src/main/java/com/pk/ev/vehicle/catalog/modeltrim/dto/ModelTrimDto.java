package com.pk.ev.vehicle.catalog.modeltrim.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class ModelTrimDto {
    // ═══════════════════════════════════════════════════════════
    // MODEL TRIM
    // ═══════════════════════════════════════════════════════════

    public record CreateModelTrimRequest(
            @NotBlank @Size(max = 100) String trimName,
            @Size(max = 500)           String description,
            Boolean hasSunroof,
            Boolean hasAdas,
            Boolean hasConnectedCar,
            @Min(0) @Max(30)           Integer infotainmentSizeInches,
            @Min(0)                    Integer sortOrder
    ) {}

    public record UpdateModelTrimRequest(
            @Size(max = 100) String trimName,
            @Size(max = 500) String description,
            Boolean hasSunroof,
            Boolean hasAdas,
            Boolean hasConnectedCar,
            @Min(0) @Max(30) Integer infotainmentSizeInches,
            @Min(0)          Integer sortOrder,
            Boolean isActive
    ) {}

    public record ModelTrimResponse(
            UUID id,
            UUID modelId,
            String trimName,
            String description,
            Boolean hasSunroof,
            Boolean hasAdas,
            Boolean hasConnectedCar,
            Integer infotainmentSizeInches,
            Integer sortOrder,
            Boolean isActive,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
