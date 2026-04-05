package com.pk.ev.vehicle.catalog.compatibility;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;
import com.pk.ev.vehicle.catalog.variant.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.variant.model.VariantListing;
import com.pk.ev.vehicle.catalog.variant.repository.VariantListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompatibilityServiceImpl implements CompatibilityService {

    private final VariantListingRepository variantRepository;
    private final StationConnectorRepository connectorRepository;
    private final CompatibilityEngine       engine;

    // ─── Vehicle ↔ Station ────────────────────────────────────────────────────

    @Override
    public CompatibilityResult checkVariantAgainstStation(UUID variantListingId, UUID stationId) {
        log.debug("Compatibility check: variantListingId={} stationId={}", variantListingId, stationId);

        VariantListing variant = loadVariantOrThrow(variantListingId);
        List<StationConnector> stationConnectors =
                connectorRepository.findByStationIdAndIsOperationalTrue(stationId);

        return engine.checkVariantAgainstStation(variant, stationId, stationConnectors);
    }

    // ─── Vehicle ↔ ConnectorType ──────────────────────────────────────────────

    @Override
    public CompatibilityResult checkVariantAgainstConnector(
            UUID variantListingId,
            ConnectorType connectorType,
            Integer stationMaxWattage
    ) {
        log.debug("Compatibility check: variantListingId={} connectorType={} maxWattage={}",
                variantListingId, connectorType, stationMaxWattage);

        VariantListing variant = loadVariantOrThrow(variantListingId);
        return engine.checkVariantAgainstConnectorType(variant, connectorType, stationMaxWattage);
    }

    // ─── Station → compatible vehicles ───────────────────────────────────────

    @Override
    public StationCompatibleVariants getCompatibleVariantsForStation(UUID stationId) {
        log.debug("Finding all compatible variants for stationId={}", stationId);

        List<StationConnector> stationConnectors =
                connectorRepository.findByStationIdAndIsOperationalTrue(stationId);

        if (stationConnectors.isEmpty()) {
            return new StationCompatibleVariants(stationId, List.of(), 0);
        }

        // Load all ACTIVE variant listings — full joins needed for the engine
        List<VariantListing> allVariants = variantRepository
                .findAllByFilters(null, null, null, null, null,
                        VariantStatus.ACTIVE,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        List<CompatibilityResult> compatibleResults =
                engine.findCompatibleVariantsForStation(allVariants, stationId, stationConnectors);

        List<CompatibleVariantSummary> summaries = compatibleResults.stream()
                .map(r -> new CompatibleVariantSummary(
                        r.variantListingId(),
                        r.variantDisplayLabel(),
                        getMakeName(allVariants, r.variantListingId()),
                        getModelName(allVariants, r.variantListingId()),
                        getModelYear(allVariants, r.variantListingId()),
                        r.maxAchievableWattage(),
                        r.estimatedCharge10To80Pct()
                ))
                .toList();

        return new StationCompatibleVariants(stationId, summaries, summaries.size());
    }

    // ─── ConnectorType → compatible vehicles ─────────────────────────────────

    @Override
    public ConnectorCompatibleVariants getCompatibleVariantsForConnector(
            ConnectorType connectorType,
            Integer maxWattage
    ) {
        log.debug("Finding all compatible variants for connectorType={} maxWattage={}",
                connectorType, maxWattage);

        List<VariantListing> allVariants = variantRepository
                .findAllByFilters(null, null, null, null, null,
                        VariantStatus.ACTIVE,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        List<CompatibilityResult> results =
                engine.findCompatibleVariantsForConnector(allVariants, connectorType, maxWattage);

        List<CompatibleVariantSummary> summaries = results.stream()
                .map(r -> new CompatibleVariantSummary(
                        r.variantListingId(),
                        r.variantDisplayLabel(),
                        getMakeName(allVariants, r.variantListingId()),
                        getModelName(allVariants, r.variantListingId()),
                        getModelYear(allVariants, r.variantListingId()),
                        r.maxAchievableWattage(),
                        r.estimatedCharge10To80Pct()
                ))
                .toList();

        return new ConnectorCompatibleVariants(connectorType, summaries, summaries.size());
    }

    // ─── Bulk check ──────────────────────────────────────────────────────────

    @Override
    public BulkCompatibilityResponse bulkCheck(BulkCompatibilityRequest request) {
        log.debug("Bulk compatibility check: {} pairs", request.pairs().size());

        // Run each pair in parallel — each check is independent and read-only
        List<CompatibilityResult> results = request.pairs().parallelStream()
                .map(pair -> {
                    try {
                        return checkVariantAgainstStation(pair.variantListingId(), pair.stationId());
                    } catch (ResourceNotFoundException ex) {
                        // Return an explicit incompatible result rather than failing the whole batch
                        return new CompatibilityResult(
                                pair.variantListingId(),
                                "Unknown variant",
                                pair.stationId(),
                                null,
                                false,
                                List.of(),
                                null,
                                null,
                                "Variant or station not found: " + ex.getMessage()
                        );
                    }
                })
                .collect(Collectors.toList());

        long compatibleCount   = results.stream().filter(CompatibilityResult::isCompatible).count();
        long incompatibleCount = results.size() - compatibleCount;

        return new BulkCompatibilityResponse(
                results,
                results.size(),
                (int) compatibleCount,
                (int) incompatibleCount
        );
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private VariantListing loadVariantOrThrow(UUID variantListingId) {
        return variantRepository.findByIdWithAllDetails(variantListingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "VariantListing not found: " + variantListingId));
    }

    private String getMakeName(List<VariantListing> variants, UUID variantId) {
        return variants.stream()
                .filter(v -> v.getId().equals(variantId))
                .map(v -> v.getModel().getMake().getName())
                .findFirst().orElse("Unknown");
    }

    private String getModelName(List<VariantListing> variants, UUID variantId) {
        return variants.stream()
                .filter(v -> v.getId().equals(variantId))
                .map(v -> v.getModel().getName())
                .findFirst().orElse("Unknown");
    }

    private Integer getModelYear(List<VariantListing> variants, UUID variantId) {
        return variants.stream()
                .filter(v -> v.getId().equals(variantId))
                .map(v -> v.getModel().getModelYear())
                .findFirst().orElse(null);
    }
}
