package com.pk.ev.vehicle.catalog.customer.service;

import com.pk.ev.vehicle.catalog.customer.dto.GarageDtos.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GarageService {

    // POST /garage/vehicles
    CustomerVehicleResponse addVehicle(UUID userId, AddVehicleRequest request);

    // GET /garage/vehicles  (customer — own garage)
    GarageResponse getGarage(UUID userId);

    // GET /garage/vehicles/{vehicleId}  (customer)
    CustomerVehicleResponse getVehicleById(UUID userId, UUID vehicleId);

    // PUT /garage/vehicles/{vehicleId}
    CustomerVehicleResponse updateVehicle(UUID userId, UUID vehicleId, UpdateVehicleRequest request);

    // DELETE /garage/vehicles/{vehicleId}
    void removeVehicle(UUID userId, UUID vehicleId);

    // PUT /garage/vehicles/{vehicleId}/set-primary
    CustomerVehicleResponse setPrimaryVehicle(UUID userId, UUID vehicleId);

    // GET /garage/vehicles/{vehicleId}/compatible-stations
    CompatibleStationsResponse getCompatibleStations(UUID userId, UUID vehicleId);

    // GET /garage/vehicles  (Admin — paginated, userId filter)
    AdminGaragePageResponse adminListVehicles(UUID filterUserId, Pageable pageable);
}
