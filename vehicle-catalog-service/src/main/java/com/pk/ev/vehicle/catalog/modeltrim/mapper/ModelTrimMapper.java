package com.pk.ev.vehicle.catalog.modeltrim.mapper;

import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.modeltrim.dto.ModelTrimDto.*;
import com.pk.ev.vehicle.catalog.modeltrim.model.ModelTrim;
import org.springframework.stereotype.Component;

@Component
public class ModelTrimMapper {
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
}
