package com.pk.ev.vehicle.catalog.chargingspec.service;

import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;

import java.util.List;
import java.util.UUID;

public interface ChargingSpecService {
    // ─── Vehicle Charging Spec ────────────────────────────────────────────────
    ChargingSpecResponse addChargingSpec(UUID modelId, UUID configId, CreateChargingSpecRequest request);
    List<ChargingSpecResponse> getChargingSpecs(UUID modelId, UUID configId);
    ChargingSpecResponse getChargingSpecById(UUID modelId, UUID configId, UUID specId);
    ChargingSpecResponse updateChargingSpec(UUID modelId, UUID configId, UUID specId, UpdateChargingSpecRequest request);
    void deleteChargingSpec(UUID modelId, UUID configId, UUID specId);
    ChargingSpecSummaryResponse getChargingSpecSummary(UUID modelId, UUID configId);
}
