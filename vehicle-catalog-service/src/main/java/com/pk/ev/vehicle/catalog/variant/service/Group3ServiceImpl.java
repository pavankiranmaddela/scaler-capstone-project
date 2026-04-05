package com.pk.ev.vehicle.catalog.variant.service;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.battery.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.battery.repository.BatteryPackRepository;
import com.pk.ev.vehicle.catalog.battery.repository.ChargingConfigurationRepository;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.chargingspec.repository.VehicleChargingSpecRepository;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.model.repository.VehicleModelRepository;
import com.pk.ev.vehicle.catalog.variant.dto.Group3Dtos.*;
import com.pk.ev.vehicle.catalog.variant.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.variant.mapper.Group3Mapper;
import com.pk.ev.vehicle.catalog.variant.model.*;
import com.pk.ev.vehicle.catalog.variant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class Group3ServiceImpl implements Group3Service {

    private final VehicleModelRepository modelRepository;
    private final BatteryPackRepository batteryPackRepository;
    private final ModelTrimRepository           trimRepository;
    private final ChargingConfigurationRepository configRepository;
    private final VehicleChargingSpecRepository specRepository;
    private final VariantListingRepository      variantRepository;
    private final Group3Mapper                  mapper;

    // ═══════════════════════════════════════════════════════════
    // BATTERY PACK
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public BatteryPackResponse addBatteryPack(UUID modelId, CreateBatteryPackRequest req) {
        VehicleModel model = findModelOrThrow(modelId);
        if (batteryPackRepository.existsByModelIdAndCapacityKwh(modelId, req.capacityKwh())) {
            throw new DuplicateResourceException(
                    "BatteryPack with capacity %s kWh already exists for model %s"
                            .formatted(req.capacityKwh(), modelId));
        }
        BatteryPack saved = batteryPackRepository.save(mapper.toBatteryPackEntity(req, model));
        log.info("Created BatteryPack id={} for modelId={}", saved.getId(), modelId);
        return mapper.toBatteryPackResponse(saved);
    }

    @Override
    public List<BatteryPackResponse> getBatteryPacks(UUID modelId, boolean activeOnly) {
        findModelOrThrow(modelId);
        List<BatteryPack> packs = activeOnly
                ? batteryPackRepository.findByModelIdAndIsActiveTrue(modelId)
                : batteryPackRepository.findByModelId(modelId);
        return packs.stream().map(mapper::toBatteryPackResponse).toList();
    }

    @Override
    public BatteryPackResponse getBatteryPackById(UUID modelId, UUID packId) {
        return mapper.toBatteryPackResponse(findBatteryPackOrThrow(modelId, packId));
    }

    @Override
    @Transactional
    public BatteryPackResponse updateBatteryPack(UUID modelId, UUID packId, UpdateBatteryPackRequest req) {
        BatteryPack bp = findBatteryPackOrThrow(modelId, packId);
        mapper.applyBatteryPackUpdate(req, bp);
        return mapper.toBatteryPackResponse(batteryPackRepository.save(bp));
    }

    @Override
    @Transactional
    public void deleteBatteryPack(UUID modelId, UUID packId) {
        BatteryPack bp = findBatteryPackOrThrow(modelId, packId);
        bp.setIsActive(false);
        batteryPackRepository.save(bp);
        log.info("Deactivated BatteryPack id={}", packId);
    }

    // ═══════════════════════════════════════════════════════════
    // MODEL TRIM
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ModelTrimResponse addTrim(UUID modelId, CreateModelTrimRequest req) {
        VehicleModel model = findModelOrThrow(modelId);
        if (trimRepository.existsByModelIdAndTrimName(modelId, req.trimName())) {
            throw new DuplicateResourceException(
                    "Trim '%s' already exists for model %s".formatted(req.trimName(), modelId));
        }
        ModelTrim saved = trimRepository.save(mapper.toModelTrimEntity(req, model));
        log.info("Created ModelTrim id={} '{}' for modelId={}", saved.getId(), saved.getTrimName(), modelId);
        return mapper.toModelTrimResponse(saved);
    }

    @Override
    public List<ModelTrimResponse> getTrims(UUID modelId, boolean activeOnly) {
        findModelOrThrow(modelId);
        List<ModelTrim> trims = activeOnly
                ? trimRepository.findByModelIdAndIsActiveTrueOrderBySortOrderAsc(modelId)
                : trimRepository.findByModelIdOrderBySortOrderAsc(modelId);
        return trims.stream().map(mapper::toModelTrimResponse).toList();
    }

    @Override
    public ModelTrimResponse getTrimById(UUID modelId, UUID trimId) {
        return mapper.toModelTrimResponse(findTrimOrThrow(modelId, trimId));
    }

    @Override
    @Transactional
    public ModelTrimResponse updateTrim(UUID modelId, UUID trimId, UpdateModelTrimRequest req) {
        ModelTrim trim = findTrimOrThrow(modelId, trimId);
        if (req.trimName() != null && !req.trimName().equals(trim.getTrimName())
                && trimRepository.existsByModelIdAndTrimName(modelId, req.trimName())) {
            throw new DuplicateResourceException(
                    "Trim '%s' already exists for model %s".formatted(req.trimName(), modelId));
        }
        mapper.applyModelTrimUpdate(req, trim);
        return mapper.toModelTrimResponse(trimRepository.save(trim));
    }

    @Override
    @Transactional
    public void deleteTrim(UUID modelId, UUID trimId) {
        ModelTrim trim = findTrimOrThrow(modelId, trimId);
        trim.setIsActive(false);
        trimRepository.save(trim);
    }

    // ═══════════════════════════════════════════════════════════
    // CHARGING CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ChargingConfigResponse addChargingConfig(UUID modelId, CreateChargingConfigRequest req) {
        VehicleModel model = findModelOrThrow(modelId);
        if (configRepository.existsByModelIdAndOnboardChargerKwAndConnectorType(
                modelId, req.onboardChargerKw(), req.connectorType())) {
            throw new DuplicateResourceException(
                    "ChargingConfig %s kW %s already exists for model %s"
                            .formatted(req.onboardChargerKw(), req.connectorType(), modelId));
        }
        ChargingConfiguration saved = configRepository.save(mapper.toChargingConfigEntity(req, model));
        log.info("Created ChargingConfiguration id={} for modelId={}", saved.getId(), modelId);
        return mapper.toChargingConfigResponse(saved);
    }

    @Override
    public List<ChargingConfigResponse> getChargingConfigs(UUID modelId, boolean activeOnly) {
        findModelOrThrow(modelId);
        List<ChargingConfiguration> configs = activeOnly
                ? configRepository.findByModelIdAndIsActiveTrue(modelId)
                : configRepository.findByModelId(modelId);
        return configs.stream().map(mapper::toChargingConfigResponse).toList();
    }

    @Override
    public ChargingConfigResponse getChargingConfigById(UUID modelId, UUID configId) {
        ChargingConfiguration config = configRepository.findByIdWithSpecs(configId)
                .filter(c -> c.getModel().getId().equals(modelId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ChargingConfiguration not found: " + configId));
        return mapper.toChargingConfigResponse(config);
    }

    @Override
    @Transactional
    public ChargingConfigResponse updateChargingConfig(UUID modelId, UUID configId, UpdateChargingConfigRequest req) {
        ChargingConfiguration config = findChargingConfigOrThrow(modelId, configId);
        mapper.applyChargingConfigUpdate(req, config);
        return mapper.toChargingConfigResponse(configRepository.save(config));
    }

    @Override
    @Transactional
    public void deleteChargingConfig(UUID modelId, UUID configId) {
        ChargingConfiguration config = findChargingConfigOrThrow(modelId, configId);
        config.setIsActive(false);
        configRepository.save(config);
    }

    // ═══════════════════════════════════════════════════════════
    // VEHICLE CHARGING SPEC
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ChargingSpecResponse addChargingSpec(UUID modelId, UUID configId, CreateChargingSpecRequest req) {
        ChargingConfiguration config = findChargingConfigOrThrow(modelId, configId);
        if (specRepository.existsByChargingConfigurationIdAndConnectorType(configId, req.connectorType())) {
            throw new DuplicateResourceException(
                    "Spec for connector %s already exists on config %s"
                            .formatted(req.connectorType(), configId));
        }
        VehicleChargingSpec saved = specRepository.save(mapper.toChargingSpecEntity(req, config));
        log.info("Created VehicleChargingSpec id={} for configId={}", saved.getId(), configId);
        return mapper.toChargingSpecResponse(saved);
    }

    @Override
    public List<ChargingSpecResponse> getChargingSpecs(UUID modelId, UUID configId) {
        findChargingConfigOrThrow(modelId, configId);
        return specRepository.findByChargingConfigurationId(configId)
                .stream().map(mapper::toChargingSpecResponse).toList();
    }

    @Override
    public ChargingSpecResponse getChargingSpecById(UUID modelId, UUID configId, UUID specId) {
        findChargingConfigOrThrow(modelId, configId);
        VehicleChargingSpec spec = specRepository.findByIdAndChargingConfigurationId(specId, configId)
                .orElseThrow(() -> new ResourceNotFoundException("ChargingSpec not found: " + specId));
        return mapper.toChargingSpecResponse(spec);
    }

    @Override
    @Transactional
    public ChargingSpecResponse updateChargingSpec(UUID modelId, UUID configId, UUID specId,
                                                   UpdateChargingSpecRequest req) {
        findChargingConfigOrThrow(modelId, configId);
        VehicleChargingSpec spec = specRepository.findByIdAndChargingConfigurationId(specId, configId)
                .orElseThrow(() -> new ResourceNotFoundException("ChargingSpec not found: " + specId));
        mapper.applyChargingSpecUpdate(req, spec);
        return mapper.toChargingSpecResponse(specRepository.save(spec));
    }

    @Override
    @Transactional
    public void deleteChargingSpec(UUID modelId, UUID configId, UUID specId) {
        findChargingConfigOrThrow(modelId, configId);
        VehicleChargingSpec spec = specRepository.findByIdAndChargingConfigurationId(specId, configId)
                .orElseThrow(() -> new ResourceNotFoundException("ChargingSpec not found: " + specId));
        specRepository.delete(spec);
    }

    @Override
    public ChargingSpecSummaryResponse getChargingSpecSummary(UUID modelId, UUID configId) {
        findChargingConfigOrThrow(modelId, configId);
        ChargingSpecResponse fastestAc = specRepository.findFastestAcSpec(configId)
                .map(mapper::toChargingSpecResponse).orElse(null);
        ChargingSpecResponse fastestDc = specRepository.findFastestDcSpec(configId)
                .map(mapper::toChargingSpecResponse).orElse(null);
        return new ChargingSpecSummaryResponse(fastestAc, fastestDc);
    }

    // ═══════════════════════════════════════════════════════════
    // VARIANT LISTING
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public VariantListingResponse createVariantListing(CreateVariantListingRequest req) {
        VehicleModel model = findModelOrThrow(req.modelId());
        ModelTrim trim     = findTrimOrThrow(req.modelId(), req.trimId());
        BatteryPack bp     = findBatteryPackOrThrow(req.modelId(), req.batteryPackId());
        ChargingConfiguration config = findChargingConfigOrThrow(req.modelId(), req.chargingConfigurationId());

        if (variantRepository.existsByModelIdAndTrimIdAndBatteryPackIdAndChargingConfigurationId(
                req.modelId(), req.trimId(), req.batteryPackId(), req.chargingConfigurationId())) {
            throw new DuplicateResourceException(
                    "A VariantListing with this exact Trim + Battery + ChargingConfig combination already exists");
        }

        VariantListing variant = VariantListing.builder()
                .model(model).trim(trim).batteryPack(bp).chargingConfiguration(config)
                .priceInr(req.priceInr()).launchDate(req.launchDate())
                .status(req.status() != null ? req.status() : VariantStatus.ACTIVE)
                .weightKg(req.weightKg())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .build();

        // Build and store display label
        variant.setDisplayLabel(variant.buildDisplayLabel());

        VariantListing saved = variantRepository.save(variant);
        log.info("Created VariantListing id={} '{}'", saved.getId(), saved.getDisplayLabel());
        return mapper.toVariantListingResponse(saved);
    }

    @Override
    public PagedVariantResponse getVariantListings(VariantFilterParams filters) {
        Pageable pageable = PageRequest.of(
                filters.page(), filters.size(),
                Sort.by(filters.sort()).ascending()
        );
        Page<VariantListing> page = variantRepository.findAllByFilters(
                filters.modelId(), filters.trimId(),
                filters.minPrice(), filters.maxPrice(),
                filters.minBatteryKwh(), filters.status(),
                pageable
        );
        return mapper.toPagedVariantResponse(page);
    }

    @Override
    public VariantListingResponse getVariantListingById(UUID variantId) {
        VariantListing vl = variantRepository.findByIdWithAllDetails(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("VariantListing not found: " + variantId));
        return mapper.toVariantListingResponse(vl);
    }

    @Override
    public List<VariantListingResponse> getVariantListingsByModel(UUID modelId, String statusParam) {
        findModelOrThrow(modelId);
        VariantStatus status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = VariantStatus.valueOf(statusParam.toUpperCase());
        }
        return variantRepository.findByModelIdWithDetails(modelId, status)
                .stream().map(mapper::toVariantListingResponse).toList();
    }

    @Override
    @Transactional
    public VariantListingResponse updateVariantListing(UUID variantId, UpdateVariantListingRequest req) {
        VariantListing vl = variantRepository.findByIdWithAllDetails(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("VariantListing not found: " + variantId));
        if (req.priceInr()    != null) vl.setPriceInr(req.priceInr());
        if (req.launchDate()  != null) vl.setLaunchDate(req.launchDate());
        if (req.status()      != null) vl.setStatus(req.status());
        if (req.weightKg()    != null) vl.setWeightKg(req.weightKg());
        if (req.sortOrder()   != null) vl.setSortOrder(req.sortOrder());
        return mapper.toVariantListingResponse(variantRepository.save(vl));
    }

    @Override
    @Transactional
    public void deleteVariantListing(UUID variantId) {
        VariantListing vl = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("VariantListing not found: " + variantId));
        vl.setStatus(VariantStatus.DISCONTINUED);
        variantRepository.save(vl);
        log.info("VariantListing id={} set to DISCONTINUED", variantId);
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    private VehicleModel findModelOrThrow(UUID modelId) {
        return modelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleModel not found: " + modelId));
    }

    private BatteryPack findBatteryPackOrThrow(UUID modelId, UUID packId) {
        return batteryPackRepository.findByIdAndModelId(packId, modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BatteryPack %s not found under model %s".formatted(packId, modelId)));
    }

    private ModelTrim findTrimOrThrow(UUID modelId, UUID trimId) {
        return trimRepository.findByIdAndModelId(trimId, modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ModelTrim %s not found under model %s".formatted(trimId, modelId)));
    }

    private ChargingConfiguration findChargingConfigOrThrow(UUID modelId, UUID configId) {
        return configRepository.findByIdAndModelId(configId, modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ChargingConfiguration %s not found under model %s".formatted(configId, modelId)));
    }
}
