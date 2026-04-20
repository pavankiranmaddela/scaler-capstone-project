package com.pk.ev.vehicle.catalog.chargingspec.dto;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class ChargingSpecDto {
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
}
