package com.pk.ev.vehicle.catalog.variantlisting.service;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.battery.service.BatteryPackService;
import com.pk.ev.vehicle.catalog.chargingconfig.service.ChargingConfigService;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.model.service.VehicleModelService;
import com.pk.ev.vehicle.catalog.trim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.trim.model.ModelTrim;
import com.pk.ev.vehicle.catalog.trim.service.ModelTrimService;
import com.pk.ev.vehicle.catalog.variantlisting.dtos.VariantListingDto.*;
import com.pk.ev.vehicle.catalog.variantlisting.mapper.VariantListingMapper;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import com.pk.ev.vehicle.catalog.variantlisting.repository.VariantListingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class VariantListingServiceImpl implements VariantListingService {

    private final VariantListingRepository variantRepository;

    private final VehicleModelService modelService;

    private final BatteryPackService batteryPackService;

    private final ModelTrimService trimService;

    private final ChargingConfigService chargingConfigService;

    private final VariantListingMapper variantListingMapper;

    // ═══════════════════════════════════════════════════════════
    // VARIANT LISTING
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public VariantListingResponse createVariantListing(CreateVariantListingRequest req) {
        VehicleModel model = modelService.findModelOrThrow(req.modelId());
        ModelTrim trim     = trimService.findTrimOrThrow(req.modelId(), req.trimId());
        BatteryPack bp     = batteryPackService.findBatteryPackOrThrow(req.modelId(), req.batteryPackId());
        ChargingConfiguration config = chargingConfigService.findChargingConfigOrThrow(req.modelId(), req.chargingConfigurationId());

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
        return variantListingMapper.toVariantListingResponse(saved);
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
        return variantListingMapper.toPagedVariantResponse(page);
    }

    @Override
    public VariantListingResponse getVariantListingById(UUID variantId) {
        VariantListing vl = variantRepository.findByIdWithAllDetails(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("VariantListing not found: " + variantId));
        return variantListingMapper.toVariantListingResponse(vl);
    }

    @Override
    public List<VariantListingResponse> getVariantListingsByModel(UUID modelId, String statusParam) {
        modelService.findModelOrThrow(modelId);
        VariantStatus status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = VariantStatus.valueOf(statusParam.toUpperCase());
        }
        return variantRepository.findByModelIdWithDetails(modelId, status)
                .stream().map(variantListingMapper::toVariantListingResponse).toList();
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
        return variantListingMapper.toVariantListingResponse(variantRepository.save(vl));
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
}
