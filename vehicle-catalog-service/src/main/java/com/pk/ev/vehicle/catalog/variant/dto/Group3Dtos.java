package com.pk.ev.vehicle.catalog.variant.dto;

import com.pk.ev.vehicle.catalog.battery.enums.BatteryChemistry;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.variant.enums.VariantStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class Group3Dtos {

    private Group3Dtos() {}

    // ═══════════════════════════════════════════════════════════
    // BATTERY PACK
    // ═══════════════════════════════════════════════════════════

    public record CreateBatteryPackRequest(
            @NotBlank  String packName,
            @NotNull @DecimalMin("1.0") BigDecimal capacityKwh,
            @DecimalMin("1.0")         BigDecimal usableKwh,
            @Positive                  Integer rangeKm,
            BatteryChemistry chemistry,
            @Size(max = 50)            String cellsConfiguration,
            @Min(0)                    Integer warrantyYears,
            @Positive                  Integer warrantyKm
    ) {}

    public record UpdateBatteryPackRequest(
            @Size(max = 100)           String packName,
            @DecimalMin("1.0")         BigDecimal capacityKwh,
            @DecimalMin("1.0")         BigDecimal usableKwh,
            @Positive                  Integer rangeKm,
            BatteryChemistry chemistry,
            @Size(max = 50)            String cellsConfiguration,
            @Min(0)                    Integer warrantyYears,
            @Positive                  Integer warrantyKm,
            Boolean isActive
    ) {}

    public record BatteryPackResponse(
            UUID id,
            UUID modelId,
            String packName,
            BigDecimal capacityKwh,
            BigDecimal usableKwh,
            Integer rangeKm,
            BatteryChemistry chemistry,
            String cellsConfiguration,
            Integer warrantyYears,
            Integer warrantyKm,
            Boolean isActive,
            Instant createdAt,
            Instant updatedAt
    ) {}

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

    // ═══════════════════════════════════════════════════════════
    // CHARGING CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    public record CreateChargingConfigRequest(
            @NotBlank @Size(max = 100)             String configLabel,
            @NotNull @DecimalMin("1.0")            BigDecimal onboardChargerKw,
            @NotNull ConnectorType connectorType,
            @NotNull CurrentType currentType,
            @Positive                              Integer chargeTimeFullMinutes,
            @Positive                              Integer chargeTime10To80Minutes,
            Boolean cableIncluded
    ) {}

    public record UpdateChargingConfigRequest(
            @Size(max = 100) String configLabel,
            @DecimalMin("1.0") BigDecimal onboardChargerKw,
            ConnectorType connectorType,
            CurrentType currentType,
            @Positive Integer chargeTimeFullMinutes,
            @Positive Integer chargeTime10To80Minutes,
            Boolean cableIncluded,
            Boolean isActive
    ) {}

    public record ChargingConfigResponse(
            UUID id,
            UUID modelId,
            String configLabel,
            BigDecimal onboardChargerKw,
            ConnectorType connectorType,
            CurrentType currentType,
            Integer chargeTimeFullMinutes,
            Integer chargeTime10To80Minutes,
            Boolean cableIncluded,
            Boolean isActive,
            List<ChargingSpecResponse> chargingSpecs,
            Instant createdAt,
            Instant updatedAt
    ) {}

    // Summary without the spec list — used in variant listing responses
    public record ChargingConfigSummary(
            UUID id,
            String configLabel,
            BigDecimal onboardChargerKw,
            ConnectorType connectorType,
            CurrentType currentType
    ) {}

    // ═══════════════════════════════════════════════════════════
    // VEHICLE CHARGING SPEC
    // ═══════════════════════════════════════════════════════════

    public record CreateChargingSpecRequest(
            UUID chargingStandardId,
            @NotNull ConnectorType connectorType,
            @NotNull CurrentType currentType,
            @NotNull @Positive Integer maxAcceptedWattage,
            @Positive Integer onboardChargerWattage,
            @Positive Integer chargeTime10To80Pct,
            @Positive Integer chargeTimeToFullMinutes,
            Boolean cableIncluded,
            @Size(max = 300) String notes
    ) {}

    public record UpdateChargingSpecRequest(
            UUID chargingStandardId,
            ConnectorType connectorType,
            CurrentType currentType,
            @Positive Integer maxAcceptedWattage,
            @Positive Integer onboardChargerWattage,
            @Positive Integer chargeTime10To80Pct,
            @Positive Integer chargeTimeToFullMinutes,
            Boolean cableIncluded,
            @Size(max = 300) String notes
    ) {}

    public record ChargingSpecResponse(
            UUID id,
            UUID chargingConfigurationId,
            UUID chargingStandardId,
            ConnectorType connectorType,
            CurrentType currentType,
            Integer maxAcceptedWattage,
            Integer onboardChargerWattage,
            Integer chargeTime10To80Pct,
            Integer chargeTimeToFullMinutes,
            Boolean cableIncluded,
            String notes,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record ChargingSpecSummaryResponse(
            ChargingSpecResponse fastestAc,
            ChargingSpecResponse fastestDc
    ) {}

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
            ModelTrimResponse trim,
            BatteryPackResponse batteryPack,
            ChargingConfigResponse chargingConfiguration,
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
