package com.pk.ev.vehicle.catalog.battery.service;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.battery.dtos.BatteryPackDtos.*;

import java.util.List;
import java.util.UUID;

public interface BatteryPackService {
    // ─── Battery Pack ─────────────────────────────────────────────────────────
    BatteryPackResponse addBatteryPack(UUID modelId, CreateBatteryPackRequest request);
    List<BatteryPackResponse> getBatteryPacks(UUID modelId, boolean activeOnly);
    BatteryPackResponse getBatteryPackById(UUID modelId, UUID packId);
    BatteryPackResponse updateBatteryPack(UUID modelId, UUID packId, UpdateBatteryPackRequest request);
    void deleteBatteryPack(UUID modelId, UUID packId);
    BatteryPack findBatteryPackOrThrow(UUID modelId, UUID packId);
}
