package com.pk.ev.vehicle.catalog.variantlisting.dtos;

import com.pk.ev.vehicle.catalog.battery.dtos.BatteryPackDtos;
import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.trim.dto.ModelTrimDto;
import com.pk.ev.vehicle.catalog.trim.enums.VariantStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class VariantListingDto {
    // ═══════════════════════════════════════════════════════════
    // VARIANT LISTING
    // ═══════════════════════════════════════════════════════════

    public record CreateVariantListingRequest(
            @NotNull UUID modelId,
            @NotNull UUID trimId,
            @NotNull UUID batteryPackId,
            @NotNull UUID chargingConfigurationId,
            @DecimalMin("0") BigDecimal priceInr,
            LocalDate launchDate,
            VariantStatus status,
            @Positive Integer weightKg,
            @Min(0) Integer sortOrder
    ) {}

    public record UpdateVariantListingRequest(
            @DecimalMin("0") BigDecimal priceInr,
            LocalDate launchDate,
            VariantStatus status,
            @Positive Integer weightKg,
            @Min(0) Integer sortOrder
    ) {}

    public record VariantListingResponse(
            UUID id,
            String displayLabel,
            ModelTrimDto.ModelTrimResponse trim,
            BatteryPackDtos.BatteryPackResponse batteryPack,
            ChargingConfigDto.ChargingConfigResponse chargingConfiguration,
            BigDecimal priceInr,
            LocalDate launchDate,
            VariantStatus status,
            Integer weightKg,
            Integer sortOrder,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record VariantListingSummary(
            UUID id,
            String displayLabel,
            String trimName,
            BigDecimal batteryCapacityKwh,
            Integer rangeKm,
            BigDecimal onboardChargerKw,
            ConnectorType connectorType,
            BigDecimal priceInr,
            VariantStatus status
    ) {}

    public record VariantFilterParams(
            UUID modelId,
            UUID trimId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minBatteryKwh,
            VariantStatus status,
            int page,
            int size,
            String sort
    ) {
        public VariantFilterParams {
            if (page < 0) page = 0;
            if (size < 1) size = 20;
            if (size > 100) size = 100;
            if (sort == null || sort.isBlank()) sort = "sortOrder";
        }
    }

    public record PagedVariantResponse(
            List<VariantListingSummary> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}
}
