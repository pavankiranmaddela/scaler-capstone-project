package com.pk.ev.vehicle.catalog.customer.service;

import com.pk.ev.vehicle.catalog.compatibility.CompatibilityEngine;
import com.pk.ev.vehicle.catalog.station.StationConnector;
import com.pk.ev.vehicle.catalog.station.StationConnectorRepository;
import com.pk.ev.vehicle.catalog.customer.domain.CustomerVehicle;
import com.pk.ev.vehicle.catalog.customer.dto.GarageDtos.*;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;
import com.pk.ev.vehicle.catalog.customer.mapper.GarageMapper;
import com.pk.ev.vehicle.catalog.customer.repository.CustomerVehicleRepository;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import com.pk.ev.vehicle.catalog.variantlisting.repository.VariantListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GarageServiceImpl implements GarageService {

    private final CustomerVehicleRepository customerVehicleRepository;
    private final VariantListingRepository variantListingRepository;
    private final StationConnectorRepository stationConnectorRepository;
    private final CompatibilityEngine compatibilityEngine;
    private final GarageMapper mapper;

    // ─── ADD ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerVehicleResponse addVehicle(UUID userId, AddVehicleRequest request) {
        log.info("User {} adding vehicle variantListingId={}", userId, request.variantListingId());

        VariantListing variant = variantListingRepository
                .findByIdWithAllDetails(request.variantListingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "VariantListing not found: " + request.variantListingId()));

        boolean makePrimary = Boolean.TRUE.equals(request.isPrimary());

        // Enforce one-primary-per-user
        if (makePrimary) {
            customerVehicleRepository.clearPrimaryFlagForUser(userId);
        }

        // If this is the user's first vehicle, auto-set as primary
        boolean isFirstVehicle = customerVehicleRepository.countByUserId(userId) == 0;

        CustomerVehicle cv = CustomerVehicle.builder()
                .userId(userId)
                .variantListing(variant)
                .nickname(request.nickname())
                .registrationNumber(request.registrationNumber())
                .purchaseYear(request.purchaseYear())
                .isPrimary(makePrimary || isFirstVehicle)
                .build();

        CustomerVehicle saved = customerVehicleRepository.save(cv);
        log.info("User {} added CustomerVehicle id={} isPrimary={}",
                userId, saved.getId(), saved.getIsPrimary());

        return mapper.toResponse(saved);
    }

    // ─── LIST ─────────────────────────────────────────────────────────────────

    @Override
    public GarageResponse getGarage(UUID userId) {
        List<CustomerVehicle> vehicles =
                customerVehicleRepository.findByUserIdOrderByAddedAtDesc(userId);
        return mapper.toGarageResponse(vehicles);
    }

    // ─── SINGLE ──────────────────────────────────────────────────────────────

    @Override
    public CustomerVehicleResponse getVehicleById(UUID userId, UUID vehicleId) {
        return mapper.toResponse(findOwnedOrThrow(userId, vehicleId));
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerVehicleResponse updateVehicle(UUID userId, UUID vehicleId,
                                                 UpdateVehicleRequest request) {
        log.info("User {} updating CustomerVehicle id={}", userId, vehicleId);
        CustomerVehicle cv = findOwnedOrThrow(userId, vehicleId);

        if (request.nickname() != null)           cv.setNickname(request.nickname());
        if (request.registrationNumber() != null) cv.setRegistrationNumber(request.registrationNumber());
        if (request.purchaseYear() != null)        cv.setPurchaseYear(request.purchaseYear());

        // isPrimary update via the update endpoint as a convenience
        if (Boolean.TRUE.equals(request.isPrimary()) && !Boolean.TRUE.equals(cv.getIsPrimary())) {
            customerVehicleRepository.clearPrimaryFlagForUser(userId);
            cv.setIsPrimary(true);
        } else if (Boolean.FALSE.equals(request.isPrimary())) {
            cv.setIsPrimary(false);
        }

        return mapper.toResponse(customerVehicleRepository.save(cv));
    }

    // ─── REMOVE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void removeVehicle(UUID userId, UUID vehicleId) {
        log.info("User {} removing CustomerVehicle id={}", userId, vehicleId);
        CustomerVehicle cv = findOwnedOrThrow(userId, vehicleId);

        boolean wasPrimary = Boolean.TRUE.equals(cv.getIsPrimary());
        customerVehicleRepository.delete(cv);

        // If the deleted vehicle was primary, auto-promote the most recently added
        if (wasPrimary) {
            customerVehicleRepository
                    .findByUserIdOrderByAddedAtDesc(userId)
                    .stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setIsPrimary(true);
                        customerVehicleRepository.save(next);
                        log.info("Auto-promoted CustomerVehicle id={} as new primary for user {}",
                                next.getId(), userId);
                    });
        }
    }

    // ─── SET PRIMARY ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerVehicleResponse setPrimaryVehicle(UUID userId, UUID vehicleId) {
        log.info("User {} setting primary vehicle to id={}", userId, vehicleId);
        CustomerVehicle cv = findOwnedOrThrow(userId, vehicleId);

        if (Boolean.TRUE.equals(cv.getIsPrimary())) {
            // Already primary — idempotent, no-op
            return mapper.toResponse(cv);
        }

        customerVehicleRepository.clearPrimaryFlagForUser(userId);
        cv.setIsPrimary(true);
        return mapper.toResponse(customerVehicleRepository.save(cv));
    }

    // ─── COMPATIBLE STATIONS ─────────────────────────────────────────────────

    @Override
    public CompatibleStationsResponse getCompatibleStations(UUID userId, UUID vehicleId) {
        log.debug("Finding compatible stations for user={} vehicleId={}", userId, vehicleId);

        // Load with full charging spec chain for the compatibility engine
        CustomerVehicle cv = customerVehicleRepository
                .findByIdAndUserIdWithDetails(vehicleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CustomerVehicle not found: " + vehicleId));

        VariantListing variant = cv.getVariantListing();

        // Fetch all distinct station IDs that have a connector type the vehicle supports
        Set<UUID> candidateStationIds = variant.getChargingConfiguration()
                .getChargingSpecs().stream()
                .flatMap(spec -> stationConnectorRepository
                        .findStationIdsByConnectorType(spec.getConnectorType())
                        .stream())
                .collect(Collectors.toSet());

        if (candidateStationIds.isEmpty()) {
            return new CompatibleStationsResponse(
                    cv.getId(), resolveDisplayLabel(cv), cv.getIsPrimary(), List.of(), 0);
        }

        // Run compatibility check per candidate station and keep only compatible ones
        List<CompatibleStationEntry> entries = candidateStationIds.stream()
                .map(stationId -> {
                    List<StationConnector> connectors =
                            stationConnectorRepository.findByStationIdAndIsOperationalTrue(stationId);
                    CompatibilityResult result =
                            compatibilityEngine.checkVariantAgainstStation(variant, stationId, connectors);

                    if (!result.isCompatible()) return null;

                    // Pick the connector type from the best matched spec
                    var bestSpec = result.compatibleSpecs().stream()
                            .max(Comparator.comparingInt(
                                    MatchedSpec::maxAchievableWattage))
                            .orElse(null);

                    return new CompatibleStationEntry(
                            stationId,
                            result.maxAchievableWattage(),
                            result.estimatedCharge10To80Pct(),
                            bestSpec != null ? bestSpec.connectorType() : null
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(CompatibleStationEntry::maxAchievableWattage).reversed())
                .toList();

        return new CompatibleStationsResponse(
                cv.getId(), resolveDisplayLabel(cv), cv.getIsPrimary(), entries, entries.size());
    }

    // ─── ADMIN ───────────────────────────────────────────────────────────────

    @Override
    public AdminGaragePageResponse adminListVehicles(UUID filterUserId, Pageable pageable) {
        Page<CustomerVehicle> page =
                customerVehicleRepository.findAllByOptionalUserId(filterUserId, pageable);
        return mapper.toAdminPageResponse(page);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private CustomerVehicle findOwnedOrThrow(UUID userId, UUID vehicleId) {
        return customerVehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CustomerVehicle not found or not owned by user: " + vehicleId));
    }

    private String resolveDisplayLabel(CustomerVehicle cv) {
        return (cv.getNickname() != null && !cv.getNickname().isBlank())
                ? cv.getNickname()
                : cv.getVariantListing().getDisplayLabel();
    }
}
