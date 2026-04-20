package com.pk.ev.vehicle.catalog.battery.dtos;

import com.pk.ev.vehicle.catalog.battery.enums.BatteryChemistry;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class BatteryPackDtos {
    public record CreateBatteryPackRequest(
            @NotBlank String packName,
            @NotNull @DecimalMin("1.0") BigDecimal capacityKwh,
            @DecimalMin("1.0")         BigDecimal usableKwh,
            @Positive Integer rangeKm,
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
}
