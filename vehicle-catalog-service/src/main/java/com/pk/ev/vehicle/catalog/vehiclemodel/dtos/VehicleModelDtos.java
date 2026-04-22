package com.pk.ev.vehicle.catalog.vehiclemodel.dtos;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class VehicleModelDtos {

    private VehicleModelDtos() {}

    // ─── Request DTOs ────────────────────────────────────────────────────────

    public record CreateModelRequest(
            @NotNull(message = "makeId is required")
            UUID makeId,

            @NotBlank(message = "name is required")
            @Size(max = 150)
            String name,

            @Size(max = 100)
            String variant,

            @NotNull(message = "modelYear is required")
            @Min(1990) @Max(2100)
            Integer modelYear,

            @DecimalMin("0.1")
            BigDecimal batteryCapacityKwh,

            @DecimalMin("0.1")
            BigDecimal usableBatteryKwh,

            @Positive
            Integer rangeKm,

            @Positive
            Integer weightKg,

            BodyType bodyType,

            @Min(1) @Max(20)
            Integer seatingCapacity,

            DriveType driveType
    ) {}

    public record UpdateModelRequest(
            @Size(max = 150)
            String name,

            @Size(max = 100)
            String variant,

            @Min(1990) @Max(2100)
            Integer modelYear,

            @DecimalMin("0.1")
            BigDecimal batteryCapacityKwh,

            @DecimalMin("0.1")
            BigDecimal usableBatteryKwh,

            @Positive
            Integer rangeKm,

            @Positive
            Integer weightKg,

            BodyType bodyType,

            @Min(1) @Max(20)
            Integer seatingCapacity,

            DriveType driveType,

            ModelStatus status
    ) {}

    public record UpdateModelStatusRequest(
            @NotNull(message = "status is required")
            ModelStatus status
    ) {}

    public record UploadImageRequest(
            @NotBlank(message = "url is required")
            @Size(max = 500)
            String url,

            @NotNull
            Boolean isPrimary,

            ImageAngle angle
    ) {}

    // ─── Query Params (used as @ModelAttribute in controller) ─────────────────

    public record ModelFilterParams(
            UUID makeId,
            Integer year,
            ConnectorType connectorType,
            BigDecimal minBatteryKwh,
            BigDecimal maxBatteryKwh,
            ModelStatus status,
            int page,
            int size,
            String sort
    ) {
        public ModelFilterParams {
            if (page < 0)  page = 0;
            if (size < 1)  size = 20;
            if (size > 100) size = 100;
            if (sort == null || sort.isBlank()) sort = "name";
        }
    }

    // ─── Response DTOs ────────────────────────────────────────────────────────

    public record ModelResponse(
            UUID id,
            UUID makeId,
            String makeName,
            String name,
            Integer modelYear,
            Integer weightKg,
            BodyType bodyType,
            Integer seatingCapacity,
            DriveType driveType,
            ModelStatus status,
            List<ModelImageResponse> images,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record ModelSummaryResponse(
            UUID id,
            UUID makeId,
            String makeName,
            String name,
            Integer modelYear,
            BodyType bodyType,
            ModelStatus status,
            String primaryImageUrl
    ) {}

    public record ModelImageResponse(
            UUID id,
            String url,
            Boolean isPrimary,
            ImageAngle angle,
            Instant uploadedAt
    ) {}

    public record PagedModelsResponse(
            List<ModelSummaryResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}

    public record SearchModelsResponse(
            List<ModelSummaryResponse> content,
            String query,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
