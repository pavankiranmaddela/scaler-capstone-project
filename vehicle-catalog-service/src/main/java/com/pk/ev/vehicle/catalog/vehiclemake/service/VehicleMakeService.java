package com.pk.ev.vehicle.catalog.vehiclemake.service;

import com.pk.ev.vehicle.catalog.vehiclemake.dto.VehicleMakeDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemake.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface VehicleMakeService {

    // POST /vehicle-makes
    MakeResponse createMake(CreateMakeRequest request);

    // GET /vehicle-makes
    PagedMakesResponse getAllMakes(MakeStatus status, String country, Pageable pageable);

    // GET /vehicle-makes/{makeId}
    MakeResponse getMakeById(UUID makeId);

    // PUT /vehicle-makes/{makeId}
    MakeResponse updateMake(UUID makeId, UpdateMakeRequest request);

    // DELETE /vehicle-makes/{makeId}
    void deleteMake(UUID makeId);

    // GET /vehicle-makes/{makeId}/models
    List<VehicleModel> getModelsByMake(UUID makeId);

    // POST /vehicle-makes/{makeId}/regions
    List<RegionResponse> associateRegions(UUID makeId, AssociateRegionsRequest request);

    // GET /vehicle-makes/{makeId}/regions
    List<RegionResponse> getRegionsByMake(UUID makeId);

    VehicleMake findMakeOrThrow(UUID makeId);
}
