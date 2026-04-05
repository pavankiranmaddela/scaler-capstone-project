package com.pk.ev.vehicle.catalog.make.service;

import com.pk.ev.vehicle.catalog.make.dto.VehicleMakeDtos.*;
import com.pk.ev.vehicle.catalog.make.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
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
}
