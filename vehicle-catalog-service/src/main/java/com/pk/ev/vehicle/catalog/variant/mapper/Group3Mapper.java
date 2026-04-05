package com.pk.ev.vehicle.catalog.variant.mapper;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.battery.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.variant.dto.Group3Dtos.*;
import com.pk.ev.vehicle.catalog.variant.model.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Group3Mapper {

    // ─── BatteryPack ─────────────────────────────────────────────────────────

    public BatteryPackResponse toBatteryPackResponse(BatteryPack bp) {
        return new BatteryPackResponse(
                bp.getId(), bp.getModel().getId(), bp.getPackName(),
                bp.getCapacityKwh(), bp.getUsableKwh(), bp.getRangeKm(),
                bp.getChemistry(), bp.getCellsConfiguration(),
                bp.getWarrantyYears(), bp.getWarrantyKm(),
                bp.getIsActive(), bp.getCreatedAt(), bp.getUpdatedAt()
        );
    }

    public BatteryPack toBatteryPackEntity(CreateBatteryPackRequest req, VehicleModel model) {
        return BatteryPack.builder()
                .model(model)
                .packName(req.packName())
                .capacityKwh(req.capacityKwh())
                .usableKwh(req.usableKwh())
                .rangeKm(req.rangeKm())
                .chemistry(req.chemistry())
                .cellsConfiguration(req.cellsConfiguration())
                .warrantyYears(req.warrantyYears())
                .warrantyKm(req.warrantyKm())
                .build();
    }

    public void applyBatteryPackUpdate(UpdateBatteryPackRequest req, BatteryPack bp) {
        if (req.packName()            != null) bp.setPackName(req.packName());
        if (req.capacityKwh()         != null) bp.setCapacityKwh(req.capacityKwh());
        if (req.usableKwh()           != null) bp.setUsableKwh(req.usableKwh());
        if (req.rangeKm()             != null) bp.setRangeKm(req.rangeKm());
        if (req.chemistry()           != null) bp.setChemistry(req.chemistry());
        if (req.cellsConfiguration()  != null) bp.setCellsConfiguration(req.cellsConfiguration());
        if (req.warrantyYears()       != null) bp.setWarrantyYears(req.warrantyYears());
        if (req.warrantyKm()          != null) bp.setWarrantyKm(req.warrantyKm());
        if (req.isActive()            != null) bp.setIsActive(req.isActive());
    }

    // ─── ModelTrim ───────────────────────────────────────────────────────────

    public ModelTrimResponse toModelTrimResponse(ModelTrim trim) {
        return new ModelTrimResponse(
                trim.getId(), trim.getModel().getId(), trim.getTrimName(),
                trim.getDescription(), trim.getHasSunroof(), trim.getHasAdas(),
                trim.getHasConnectedCar(), trim.getInfotainmentSizeInches(),
                trim.getSortOrder(), trim.getIsActive(),
                trim.getCreatedAt(), trim.getUpdatedAt()
        );
    }

    public ModelTrim toModelTrimEntity(CreateModelTrimRequest req, VehicleModel model) {
        return ModelTrim.builder()
                .model(model)
                .trimName(req.trimName())
                .description(req.description())
                .hasSunroof(req.hasSunroof() != null ? req.hasSunroof() : false)
                .hasAdas(req.hasAdas() != null ? req.hasAdas() : false)
                .hasConnectedCar(req.hasConnectedCar() != null ? req.hasConnectedCar() : false)
                .infotainmentSizeInches(req.infotainmentSizeInches())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .build();
    }

    public void applyModelTrimUpdate(UpdateModelTrimRequest req, ModelTrim trim) {
        if (req.trimName()                != null) trim.setTrimName(req.trimName());
        if (req.description()             != null) trim.setDescription(req.description());
        if (req.hasSunroof()              != null) trim.setHasSunroof(req.hasSunroof());
        if (req.hasAdas()                 != null) trim.setHasAdas(req.hasAdas());
        if (req.hasConnectedCar()         != null) trim.setHasConnectedCar(req.hasConnectedCar());
        if (req.infotainmentSizeInches()  != null) trim.setInfotainmentSizeInches(req.infotainmentSizeInches());
        if (req.sortOrder()               != null) trim.setSortOrder(req.sortOrder());
        if (req.isActive()                != null) trim.setIsActive(req.isActive());
    }

    // ─── ChargingConfiguration ───────────────────────────────────────────────

    public ChargingConfigResponse toChargingConfigResponse(ChargingConfiguration config) {
        List<ChargingSpecResponse> specs = config.getChargingSpecs() == null
                ? List.of()
                : config.getChargingSpecs().stream().map(this::toChargingSpecResponse).toList();

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

    // ─── VariantListing ──────────────────────────────────────────────────────

    public VariantListingResponse toVariantListingResponse(VariantListing vl) {
        return new VariantListingResponse(
                vl.getId(), vl.getDisplayLabel(),
                toModelTrimResponse(vl.getTrim()),
                toBatteryPackResponse(vl.getBatteryPack()),
                toChargingConfigResponse(vl.getChargingConfiguration()),
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
