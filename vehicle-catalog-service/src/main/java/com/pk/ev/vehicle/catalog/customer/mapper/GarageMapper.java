package com.pk.ev.vehicle.catalog.customer.mapper;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.customer.domain.CustomerVehicle;
import com.pk.ev.vehicle.catalog.customer.dto.GarageDtos.*;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GarageMapper {

    public CustomerVehicleResponse toResponse(CustomerVehicle cv) {
        return new CustomerVehicleResponse(
                cv.getId(),
                cv.getUserId(),
                toVariantSummary(cv.getVariantListing()),
                cv.getNickname(),
                cv.getRegistrationNumber(),
                cv.getPurchaseYear(),
                cv.getIsPrimary(),
                cv.getAddedAt()
        );
    }

    public CustomerVehicleSummary toSummary(CustomerVehicle cv) {
        VariantListing vl = cv.getVariantListing();
        ChargingConfiguration cc = vl.getChargingConfiguration();

        String displayLabel = (cv.getNickname() != null && !cv.getNickname().isBlank())
                ? cv.getNickname()
                : vl.getDisplayLabel();

        return new CustomerVehicleSummary(
                cv.getId(),
                displayLabel,
                vl.getModel().getMake().getName(),
                vl.getModel().getName(),
                vl.getModel().getModelYear(),
                cv.getIsPrimary(),
                cc.getOnboardChargerKw(),
                cc.getConnectorType(),
                cv.getAddedAt()
        );
    }

    public GarageResponse toGarageResponse(List<CustomerVehicle> vehicles) {
        UUID primaryId = vehicles.stream()
                .filter(cv -> Boolean.TRUE.equals(cv.getIsPrimary()))
                .map(CustomerVehicle::getId)
                .findFirst()
                .orElse(null);

        return new GarageResponse(
                vehicles.stream().map(this::toSummary).toList(),
                vehicles.size(),
                primaryId
        );
    }

    public AdminGaragePageResponse toAdminPageResponse(Page<CustomerVehicle> page) {
        return new AdminGaragePageResponse(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private VariantSummaryInGarage toVariantSummary(VariantListing vl) {
        ChargingConfiguration cc = vl.getChargingConfiguration();
        return new VariantSummaryInGarage(
                vl.getId(),
                vl.getDisplayLabel(),
                vl.getModel().getMake().getName(),
                vl.getModel().getName(),
                vl.getModel().getModelYear(),
                vl.getTrim().getTrimName(),
                vl.getBatteryPack().getCapacityKwh(),
                vl.getBatteryPack().getRangeKm(),
                cc.getOnboardChargerKw(),
                cc.getConnectorType(),
                vl.getStatus()
        );
    }
}
