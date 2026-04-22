package com.pk.ev.vehicle.catalog.battery.mapper;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.battery.dtos.BatteryPackDtos.*;
import org.springframework.stereotype.Component;

@Component
public class BatteryPackMapper {
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
}
