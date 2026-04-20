package com.pk.ev.vehicle.catalog.chargingspec.mapper;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;
import org.springframework.stereotype.Component;

@Component
public class ChargingSpecMapper {
    // ─── VehicleChargingSpec ─────────────────────────────────────────────────

    public ChargingSpecResponse toChargingSpecResponse(VehicleChargingSpec spec) {
        return new ChargingSpecResponse(
                spec.getId(), spec.getChargingConfiguration().getId(),
                spec.getChargingStandardId(), spec.getConnectorType(), spec.getCurrentType(),
                spec.getMaxAcceptedWattage(), spec.getOnboardChargerWattage(),
                spec.getChargeTime10To80Pct(), spec.getChargeTimeToFullMinutes(),
                spec.getCableIncluded(), spec.getNotes(),
                spec.getCreatedAt(), spec.getUpdatedAt()
        );
    }

    public VehicleChargingSpec toChargingSpecEntity(CreateChargingSpecRequest req,
                                                    ChargingConfiguration config) {
        return VehicleChargingSpec.builder()
                .chargingConfiguration(config)
                .chargingStandardId(req.chargingStandardId())
                .connectorType(req.connectorType())
                .currentType(req.currentType())
                .maxAcceptedWattage(req.maxAcceptedWattage())
                .onboardChargerWattage(req.onboardChargerWattage())
                .chargeTime10To80Pct(req.chargeTime10To80Pct())
                .chargeTimeToFullMinutes(req.chargeTimeToFullMinutes())
                .cableIncluded(req.cableIncluded() != null ? req.cableIncluded() : false)
                .notes(req.notes())
                .build();
    }

    public void applyChargingSpecUpdate(UpdateChargingSpecRequest req, VehicleChargingSpec spec) {
        if (req.chargingStandardId()      != null) spec.setChargingStandardId(req.chargingStandardId());
        if (req.connectorType()           != null) spec.setConnectorType(req.connectorType());
        if (req.currentType()             != null) spec.setCurrentType(req.currentType());
        if (req.maxAcceptedWattage()      != null) spec.setMaxAcceptedWattage(req.maxAcceptedWattage());
        if (req.onboardChargerWattage()   != null) spec.setOnboardChargerWattage(req.onboardChargerWattage());
        if (req.chargeTime10To80Pct()     != null) spec.setChargeTime10To80Pct(req.chargeTime10To80Pct());
        if (req.chargeTimeToFullMinutes() != null) spec.setChargeTimeToFullMinutes(req.chargeTimeToFullMinutes());
        if (req.cableIncluded()           != null) spec.setCableIncluded(req.cableIncluded());
        if (req.notes()                   != null) spec.setNotes(req.notes());
    }
}
