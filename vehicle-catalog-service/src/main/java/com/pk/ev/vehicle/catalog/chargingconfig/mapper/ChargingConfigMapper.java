package com.pk.ev.vehicle.catalog.chargingconfig.mapper;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.mapper.ChargingSpecMapper;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.*;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ChargingConfigMapper {

    private final ChargingSpecMapper chargingSpecMapper;
    // ─── ChargingConfiguration ───────────────────────────────────────────────

    public ChargingConfigResponse toChargingConfigResponse(ChargingConfiguration config) {
        List<ChargingSpecResponse> specs = config.getChargingSpecs() == null
                ? List.of()
                : config.getChargingSpecs().stream().map(chargingSpecMapper::toChargingSpecResponse).toList();

        return new ChargingConfigResponse(
                config.getId(), config.getModel().getId(), config.getConfigLabel(),
                config.getOnboardChargerKw(), config.getConnectorType(), config.getCurrentType(),
                config.getChargeTimeFullMinutes(), config.getChargeTime10To80Minutes(),
                config.getCableIncluded(), config.getIsActive(),
                specs, config.getCreatedAt(), config.getUpdatedAt()
        );
    }

    public ChargingConfigSummary toChargingConfigSummary(ChargingConfiguration config) {
        return new ChargingConfigSummary(
                config.getId(), config.getConfigLabel(),
                config.getOnboardChargerKw(), config.getConnectorType(), config.getCurrentType()
        );
    }

    public ChargingConfiguration toChargingConfigEntity(CreateChargingConfigRequest req, VehicleModel model) {
        return ChargingConfiguration.builder()
                .model(model)
                .configLabel(req.configLabel())
                .onboardChargerKw(req.onboardChargerKw())
                .connectorType(req.connectorType())
                .currentType(req.currentType())
                .chargeTimeFullMinutes(req.chargeTimeFullMinutes())
                .chargeTime10To80Minutes(req.chargeTime10To80Minutes())
                .cableIncluded(req.cableIncluded() != null ? req.cableIncluded() : false)
                .build();
    }

    public void applyChargingConfigUpdate(UpdateChargingConfigRequest req, ChargingConfiguration config) {
        if (req.configLabel()             != null) config.setConfigLabel(req.configLabel());
        if (req.onboardChargerKw()        != null) config.setOnboardChargerKw(req.onboardChargerKw());
        if (req.connectorType()           != null) config.setConnectorType(req.connectorType());
        if (req.currentType()             != null) config.setCurrentType(req.currentType());
        if (req.chargeTimeFullMinutes()   != null) config.setChargeTimeFullMinutes(req.chargeTimeFullMinutes());
        if (req.chargeTime10To80Minutes() != null) config.setChargeTime10To80Minutes(req.chargeTime10To80Minutes());
        if (req.cableIncluded()           != null) config.setCableIncluded(req.cableIncluded());
        if (req.isActive()                != null) config.setIsActive(req.isActive());
    }
}
