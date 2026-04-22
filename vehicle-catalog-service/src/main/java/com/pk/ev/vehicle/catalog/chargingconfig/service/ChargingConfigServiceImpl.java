package com.pk.ev.vehicle.catalog.chargingconfig.service;

import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.*;
import com.pk.ev.vehicle.catalog.chargingconfig.mapper.ChargingConfigMapper;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingconfig.repository.ChargingConfigurationRepository;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.vehiclemodel.service.VehicleModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargingConfigServiceImpl implements ChargingConfigService {

    private final VehicleModelService modelService;

    private final ChargingConfigurationRepository configRepository;

    private final ChargingConfigMapper chargingConfigMapper;

    // ═══════════════════════════════════════════════════════════
    // CHARGING CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ChargingConfigResponse addChargingConfig(UUID modelId, CreateChargingConfigRequest req) {
        VehicleModel model = modelService.findModelOrThrow(modelId);
        if (configRepository.existsByModelIdAndOnboardChargerKwAndConnectorType(
                modelId, req.onboardChargerKw(), req.connectorType())) {
            throw new DuplicateResourceException(
                    "ChargingConfig %s kW %s already exists for model %s"
                            .formatted(req.onboardChargerKw(), req.connectorType(), modelId));
        }
        ChargingConfiguration saved = configRepository.save(chargingConfigMapper.toChargingConfigEntity(req, model));
        log.info("Created ChargingConfiguration id={} for modelId={}", saved.getId(), modelId);
        return chargingConfigMapper.toChargingConfigResponse(saved);
    }

    @Override
    public List<ChargingConfigResponse> getChargingConfigs(UUID modelId, boolean activeOnly) {
        modelService.findModelOrThrow(modelId);
        List<ChargingConfiguration> configs = activeOnly
                ? configRepository.findByModelIdAndIsActiveTrue(modelId)
                : configRepository.findByModelId(modelId);
        return configs.stream().map(chargingConfigMapper::toChargingConfigResponse).toList();
    }

    @Override
    public ChargingConfigResponse getChargingConfigById(UUID modelId, UUID configId) {
        ChargingConfiguration config = configRepository.findByIdWithSpecs(configId)
                .filter(c -> c.getModel().getId().equals(modelId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ChargingConfiguration not found: " + configId));
        return chargingConfigMapper.toChargingConfigResponse(config);
    }

    @Override
    @Transactional
    public ChargingConfigResponse updateChargingConfig(UUID modelId, UUID configId, UpdateChargingConfigRequest req) {
        ChargingConfiguration config = findChargingConfigOrThrow(modelId, configId);
        chargingConfigMapper.applyChargingConfigUpdate(req, config);
        return chargingConfigMapper.toChargingConfigResponse(configRepository.save(config));
    }

    @Override
    @Transactional
    public void deleteChargingConfig(UUID modelId, UUID configId) {
        ChargingConfiguration config = findChargingConfigOrThrow(modelId, configId);
        config.setIsActive(false);
        configRepository.save(config);
    }

    public ChargingConfiguration findChargingConfigOrThrow(UUID modelId, UUID configId) {
        return configRepository.findByIdAndModelId(configId, modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ChargingConfiguration %s not found under model %s".formatted(configId, modelId)));
    }
}
