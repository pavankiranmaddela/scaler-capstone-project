package com.pk.ev.vehicle.catalog.chargingstandard.service;

import com.pk.ev.vehicle.catalog.chargingstandard.dto.ChargingStandardDtos.*;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.chargingstandard.mapper.ChargingStandardMapper;
import com.pk.ev.vehicle.catalog.chargingstandard.model.ChargingStandard;
import com.pk.ev.vehicle.catalog.chargingstandard.repository.ChargingStandardRepository;
import com.pk.ev.vehicle.catalog.chargingspec.repository.VehicleChargingSpecRepository;
import com.pk.ev.vehicle.catalog.variant.repository.VariantListingRepository;
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
public class ChargingStandardServiceImpl implements ChargingStandardService {

    private final ChargingStandardRepository standardRepository;
    private final VehicleChargingSpecRepository specRepository;
    private final VariantListingRepository variantListingRepository;
    private final ChargingStandardMapper mapper;

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ChargingStandardResponse createStandard(CreateChargingStandardRequest request) {
        log.info("Creating ChargingStandard shortCode={}", request.shortCode());

        if (standardRepository.existsByShortCode(request.shortCode().toUpperCase())) {
            throw new DuplicateResourceException(
                    "ChargingStandard with shortCode '%s' already exists".formatted(request.shortCode()));
        }
        if (standardRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(
                    "ChargingStandard with name '%s' already exists".formatted(request.name()));
        }

        ChargingStandard saved = standardRepository.save(mapper.toEntity(request));
        log.info("Created ChargingStandard id={} shortCode={}", saved.getId(), saved.getShortCode());
        return mapper.toResponse(saved);
    }

    // ─── READ — paginated list ────────────────────────────────────────────────

    @Override
    public PagedStandardsResponse getAllStandards(StandardFilterParams filters) {
        Pageable pageable = PageRequest.of(
                filters.page(), filters.size(),
                Sort.by(filters.sort()).ascending()
        );
        Page<ChargingStandard> page = standardRepository.findAllByFilters(
                filters.region(), filters.currentType(),
                filters.connectorType(), filters.deprecated(),
                pageable
        );
        return mapper.toPagedResponse(page);
    }

    // ─── READ — single by ID ─────────────────────────────────────────────────

    @Override
    public ChargingStandardResponse getStandardById(UUID standardId) {
        return mapper.toResponse(findOrThrow(standardId));
    }

    // ─── READ — by short code ─────────────────────────────────────────────────

    @Override
    public ChargingStandardResponse getStandardByShortCode(String shortCode) {
        return standardRepository.findByShortCode(shortCode.toUpperCase())
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ChargingStandard not found with shortCode: " + shortCode));
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ChargingStandardResponse updateStandard(UUID standardId, UpdateChargingStandardRequest request) {
        log.info("Updating ChargingStandard id={}", standardId);
        ChargingStandard std = findOrThrow(standardId);

        // Guard uniqueness if shortCode is changing
        if (request.shortCode() != null) {
            String newCode = request.shortCode().toUpperCase();
            if (!newCode.equals(std.getShortCode()) && standardRepository.existsByShortCode(newCode)) {
                throw new DuplicateResourceException(
                        "ShortCode '%s' is already used by another standard".formatted(newCode));
            }
        }
        // Guard uniqueness if name is changing
        if (request.name() != null && !request.name().equals(std.getName())
                && standardRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(
                    "Name '%s' is already used by another standard".formatted(request.name()));
        }

        mapper.applyUpdate(request, std);
        return mapper.toResponse(standardRepository.save(std));
    }

    // ─── DELETE (soft — deprecate) ────────────────────────────────────────────

    @Override
    @Transactional
    public void deprecateStandard(UUID standardId) {
        log.info("Deprecating ChargingStandard id={}", standardId);
        ChargingStandard std = findOrThrow(standardId);

        // Warn if live vehicle charging specs still reference this standard
        long activeSpecCount = specRepository.findByChargingConfigurationId(standardId).size();
        if (activeSpecCount > 0) {
            log.warn("Deprecating standard id={} which is still referenced by {} VehicleChargingSpec records",
                    standardId, activeSpecCount);
        }

        std.setIsDeprecated(true);
        standardRepository.save(std);
    }

    // ─── COMPATIBLE MODELS ────────────────────────────────────────────────────

    @Override
    public List<CompatibleModelSummary> getCompatibleModels(UUID standardId) {
        // Ensure the standard exists
        findOrThrow(standardId);

        /*
         * Walk: ChargingStandard → VehicleChargingSpec (via chargingStandardId FK)
         *     → ChargingConfiguration → VariantListing → VehicleModel + VehicleMake
         *
         * We do this in-service rather than a JPQL join because chargingStandardId
         * is a loose UUID FK (not a JPA relationship). This keeps the catalog service
         * deployable independently.
         */
        return specRepository.findAll().stream()
                .filter(spec -> standardId.equals(spec.getChargingStandardId()))
                .flatMap(spec -> variantListingRepository
                        .findByModelIdWithDetails(
                                spec.getChargingConfiguration().getModel().getId(), null)
                        .stream()
                        .filter(vl -> vl.getChargingConfiguration().getId()
                                .equals(spec.getChargingConfiguration().getId()))
                        .map(vl -> new CompatibleModelSummary(
                                vl.getId(),
                                vl.getDisplayLabel(),
                                vl.getModel().getId(),
                                vl.getModel().getName(),
                                vl.getModel().getModelYear(),
                                vl.getModel().getMake().getName(),
                                spec.getMaxAcceptedWattage(),
                                spec.getChargeTime10To80Pct()
                        ))
                )
                .distinct()
                .toList();
    }

    // ─── CONNECTOR TYPES ─────────────────────────────────────────────────────

    @Override
    public List<ConnectorTypeMetadata> getConnectorTypes() {
        return mapper.getAllConnectorTypeMetadata();
    }

    // ─── REFERENCE LOOKUPS ────────────────────────────────────────────────────

    @Override
    public List<String> getDistinctRegions() {
        return standardRepository.findDistinctRegions();
    }

    @Override
    public List<String> getDistinctGoverningBodies() {
        return standardRepository.findDistinctGoverningBodies();
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private ChargingStandard findOrThrow(UUID id) {
        return standardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ChargingStandard not found with id: " + id));
    }
}
