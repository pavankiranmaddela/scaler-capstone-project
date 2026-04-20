package com.pk.ev.vehicle.catalog.chargingconfig.dto;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ChargingConfigDto {
    // ═══════════════════════════════════════════════════════════
    // CHARGING CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    public record CreateChargingConfigRequest(
            @NotBlank @Size(max = 100)             String configLabel,
            @NotNull @DecimalMin("1.0") BigDecimal onboardChargerKw,
            @NotNull ConnectorType connectorType,
            @NotNull CurrentType currentType,
            @Positive Integer chargeTimeFullMinutes,
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
}
