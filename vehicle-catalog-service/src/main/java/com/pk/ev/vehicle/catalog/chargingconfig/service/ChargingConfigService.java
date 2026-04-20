package com.pk.ev.vehicle.catalog.chargingconfig.service;

import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.*;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;

import java.util.List;
import java.util.UUID;

public interface ChargingConfigService {

    // ─── Charging Configuration ───────────────────────────────────────────────
    ChargingConfigResponse addChargingConfig(UUID modelId, CreateChargingConfigRequest request);
    List<ChargingConfigResponse> getChargingConfigs(UUID modelId, boolean activeOnly);
    ChargingConfigResponse getChargingConfigById(UUID modelId, UUID configId);
    ChargingConfigResponse updateChargingConfig(UUID modelId, UUID configId, UpdateChargingConfigRequest request);
    void deleteChargingConfig(UUID modelId, UUID configId);
    ChargingConfiguration findChargingConfigOrThrow(UUID modelId, UUID configId);
}
