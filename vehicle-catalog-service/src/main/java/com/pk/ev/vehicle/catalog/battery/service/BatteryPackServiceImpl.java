package com.pk.ev.vehicle.catalog.battery.service;

import com.pk.ev.vehicle.catalog.battery.mapper.BatteryPackMapper;
import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.battery.repository.BatteryPackRepository;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.vehiclemake.service.VehicleMakeService;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.vehiclemodel.service.VehicleModelService;
import com.pk.ev.vehicle.catalog.battery.dtos.BatteryPackDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BatteryPackServiceImpl implements BatteryPackService {

    private final BatteryPackRepository batteryPackRepository;

    private final VehicleModelService modelService;

    private final VehicleMakeService makeService;

    private final BatteryPackMapper batteryPackMapper;

    // ═══════════════════════════════════════════════════════════
    // BATTERY PACK
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public BatteryPackResponse addBatteryPack(UUID modelId, CreateBatteryPackRequest req) {
        VehicleModel model = modelService.findModelOrThrow(modelId);
        if (batteryPackRepository.existsByModelIdAndCapacityKwh(modelId, req.capacityKwh())) {
            throw new DuplicateResourceException(
                    "BatteryPack with capacity %s kWh already exists for model %s"
                            .formatted(req.capacityKwh(), modelId));
        }
        BatteryPack saved = batteryPackRepository.save(batteryPackMapper.toBatteryPackEntity(req, model));
        log.info("Created BatteryPack id={} for modelId={}", saved.getId(), modelId);
        return batteryPackMapper.toBatteryPackResponse(saved);
    }

    @Override
    public List<BatteryPackResponse> getBatteryPacks(UUID modelId, boolean activeOnly) {
        modelService.findModelOrThrow(modelId);
        List<BatteryPack> packs = activeOnly
                ? batteryPackRepository.findByModelIdAndIsActiveTrue(modelId)
                : batteryPackRepository.findByModelId(modelId);
        return packs.stream().map(batteryPackMapper::toBatteryPackResponse).toList();
    }

    @Override
    public BatteryPackResponse getBatteryPackById(UUID modelId, UUID packId) {
        return batteryPackMapper.toBatteryPackResponse(findBatteryPackOrThrow(modelId, packId));
    }

    @Override
    @Transactional
    public BatteryPackResponse updateBatteryPack(UUID modelId, UUID packId, UpdateBatteryPackRequest req) {
        BatteryPack bp = findBatteryPackOrThrow(modelId, packId);
        batteryPackMapper.applyBatteryPackUpdate(req, bp);
        return batteryPackMapper.toBatteryPackResponse(batteryPackRepository.save(bp));
    }

    @Override
    @Transactional
    public void deleteBatteryPack(UUID modelId, UUID packId) {
        BatteryPack bp = findBatteryPackOrThrow(modelId, packId);
        bp.setIsActive(false);
        batteryPackRepository.save(bp);
        log.info("Deactivated BatteryPack id={}", packId);
    }

    public BatteryPack findBatteryPackOrThrow(UUID modelId, UUID packId) {
        return batteryPackRepository.findByIdAndModelId(packId, modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BatteryPack %s not found under model %s".formatted(packId, modelId)));
    }


}
