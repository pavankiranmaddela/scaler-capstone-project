package com.pk.ev.vehicle.catalog.mocks;

import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;

public class CommonMocks {

    public static VehicleModel getVehicleModelMock() {
        VehicleModel model = new VehicleModel();
        model.setId(java.util.UUID.randomUUID());
        model.setMake(getMakeMock());
        model.setName("Test Model");
        return model;
    }

    public static VehicleMake getMakeMock() {
        VehicleMake make = new VehicleMake();
        make.setId(java.util.UUID.randomUUID());
        make.setName("Test Make");
        return make;
    }
}
