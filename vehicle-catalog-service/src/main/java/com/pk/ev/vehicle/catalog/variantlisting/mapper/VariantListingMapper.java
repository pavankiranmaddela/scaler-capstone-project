package com.pk.ev.vehicle.catalog.variantlisting.mapper;

import com.pk.ev.vehicle.catalog.battery.mapper.BatteryPackMapper;
import com.pk.ev.vehicle.catalog.chargingconfig.mapper.ChargingConfigMapper;
import com.pk.ev.vehicle.catalog.variantlisting.dtos.VariantListingDto.*;
import com.pk.ev.vehicle.catalog.trim.mapper.ModelTrimMapper;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class VariantListingMapper {

    private final ModelTrimMapper modelTrimMapper;

    private final BatteryPackMapper batteryPackMapper;

    private final ChargingConfigMapper chargingConfigMapper;

    // ─── VariantListing ──────────────────────────────────────────────────────

    public VariantListingResponse toVariantListingResponse(VariantListing vl) {
        return new VariantListingResponse(
                vl.getId(), vl.getDisplayLabel(),
                modelTrimMapper.toModelTrimResponse(vl.getTrim()),
                batteryPackMapper.toBatteryPackResponse(vl.getBatteryPack()),
                chargingConfigMapper.toChargingConfigResponse(vl.getChargingConfiguration()),
                vl.getPriceInr(), vl.getLaunchDate(),
                vl.getStatus(), vl.getWeightKg(), vl.getSortOrder(),
                vl.getCreatedAt(), vl.getUpdatedAt()
        );
    }

    public VariantListingSummary toVariantListingSummary(VariantListing vl) {
        return new VariantListingSummary(
                vl.getId(), vl.getDisplayLabel(),
                vl.getTrim().getTrimName(),
                vl.getBatteryPack().getCapacityKwh(),
                vl.getBatteryPack().getRangeKm(),
                vl.getChargingConfiguration().getOnboardChargerKw(),
                vl.getChargingConfiguration().getConnectorType(),
                vl.getPriceInr(), vl.getStatus()
        );
    }

    public PagedVariantResponse toPagedVariantResponse(Page<VariantListing> page) {
        return new PagedVariantResponse(
                page.getContent().stream().map(this::toVariantListingSummary).toList(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast()
        );
    }
}
