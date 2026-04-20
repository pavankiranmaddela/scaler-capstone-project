package com.pk.ev.vehicle.catalog.chargingspec.service;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingconfig.service.ChargingConfigService;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;
import com.pk.ev.vehicle.catalog.chargingspec.mapper.ChargingSpecMapper;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.chargingspec.repository.VehicleChargingSpecRepository;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class ChargingSpecServiceImpl implements ChargingSpecService {

    private final ChargingConfigService chargingConfigService;

    private final VehicleChargingSpecRepository chargingSpecRepository;

    private final ChargingSpecMapper chargingSpecMapper;

    // ═══════════════════════════════════════════════════════════
    // VEHICLE CHARGING SPEC
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ChargingSpecResponse addChargingSpec(UUID modelId, UUID configId, CreateChargingSpecRequest req) {
        ChargingConfiguration config = chargingConfigService.findChargingConfigOrThrow(modelId, configId);
        if (chargingSpecRepository.existsByChargingConfigurationIdAndConnectorType(configId, req.connectorType())) {
            throw new DuplicateResourceException(
                    "Spec for connector %s already exists on config %s"
                            .formatted(req.connectorType(), configId));
        }
        VehicleChargingSpec saved = chargingSpecRepository.save(chargingSpecMapper.toChargingSpecEntity(req, config));
        log.info("Created VehicleChargingSpec id={} for configId={}", saved.getId(), configId);
        return chargingSpecMapper.toChargingSpecResponse(saved);
    }

    @Override
    public List<ChargingSpecResponse> getChargingSpecs(UUID modelId, UUID configId) {
        chargingConfigService.findChargingConfigOrThrow(modelId, configId);
        return chargingSpecRepository.findByChargingConfigurationId(configId)
                .stream().map(chargingSpecMapper::toChargingSpecResponse).toList();
    }

    @Override
    public ChargingSpecResponse getChargingSpecById(UUID modelId, UUID configId, UUID specId) {
        chargingConfigService.findChargingConfigOrThrow(modelId, configId);
        VehicleChargingSpec spec = chargingSpecRepository.findByIdAndChargingConfigurationId(specId, configId)
                .orElseThrow(() -> new ResourceNotFoundException("ChargingSpec not found: " + specId));
        return chargingSpecMapper.toChargingSpecResponse(spec);
    }

    @Override
    @Transactional
    public ChargingSpecResponse updateChargingSpec(UUID modelId, UUID configId, UUID specId,
                                                   UpdateChargingSpecRequest req) {
        chargingConfigService.findChargingConfigOrThrow(modelId, configId);
        VehicleChargingSpec spec = chargingSpecRepository.findByIdAndChargingConfigurationId(specId, configId)
                .orElseThrow(() -> new ResourceNotFoundException("ChargingSpec not found: " + specId));
        chargingSpecMapper.applyChargingSpecUpdate(req, spec);
        return chargingSpecMapper.toChargingSpecResponse(chargingSpecRepository.save(spec));
    }

    @Override
    @Transactional
    public void deleteChargingSpec(UUID modelId, UUID configId, UUID specId) {
        chargingConfigService.findChargingConfigOrThrow(modelId, configId);
        VehicleChargingSpec spec = chargingSpecRepository.findByIdAndChargingConfigurationId(specId, configId)
                .orElseThrow(() -> new ResourceNotFoundException("ChargingSpec not found: " + specId));
        chargingSpecRepository.delete(spec);
    }

    @Override
    public ChargingSpecSummaryResponse getChargingSpecSummary(UUID modelId, UUID configId) {
        chargingConfigService.findChargingConfigOrThrow(modelId, configId);
        ChargingSpecResponse fastestAc = chargingSpecRepository.findFastestAcSpec(configId)
                .map(chargingSpecMapper::toChargingSpecResponse).orElse(null);
        ChargingSpecResponse fastestDc = chargingSpecRepository.findFastestDcSpec(configId)
                .map(chargingSpecMapper::toChargingSpecResponse).orElse(null);
        return new ChargingSpecSummaryResponse(fastestAc, fastestDc);
    }
}
