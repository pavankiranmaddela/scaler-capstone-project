package com.pk.ev.vehicle.catalog.make.service;

import com.pk.ev.vehicle.catalog.model.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.make.dto.VehicleMakeDtos.*;
import com.pk.ev.vehicle.catalog.make.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.make.mapper.VehicleMakeMapper;
import com.pk.ev.vehicle.catalog.make.model.MakeRegion;
import com.pk.ev.vehicle.catalog.make.model.VehicleMake;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.make.repository.MakeRegionRepository;
import com.pk.ev.vehicle.catalog.make.repository.VehicleMakeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleMakeServiceImpl implements VehicleMakeService {

    private final VehicleMakeRepository makeRepository;
    private final MakeRegionRepository regionRepository;
    private final VehicleMakeMapper mapper;

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public MakeResponse createMake(CreateMakeRequest request) {
        log.info("Creating vehicle make: {}", request.name());

        if (makeRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(
                    "A vehicle make with name '%s' already exists".formatted(request.name())
            );
        }

        VehicleMake make = mapper.toEntity(request);
        make = makeRepository.save(make);

        log.info("Created vehicle make with id={} slug={}", make.getId(), make.getSlug());
        return mapper.toResponse(make);
    }

    // ─── READ ────────────────────────────────────────────────────────────────

    @Override
    public PagedMakesResponse getAllMakes(MakeStatus status, String country, Pageable pageable) {
        Page<VehicleMake> page = makeRepository.findAllByFilters(status, country, pageable);
        return mapper.toPagedResponse(page);
    }

    @Override
    public MakeResponse getMakeById(UUID makeId) {
        VehicleMake make = findMakeOrThrow(makeId);
        return mapper.toResponse(make);
    }

    @Override
    public List<VehicleModel> getModelsByMake(UUID makeId) {
        VehicleMake make = findMakeOrThrow(makeId);
        return make.getModels();
    }

    @Override
    public List<RegionResponse> getRegionsByMake(UUID makeId) {
        findMakeOrThrow(makeId);   // validate existence
        List<MakeRegion> regions = regionRepository.findByMakeId(makeId);
        return mapper.toRegionResponseList(regions);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public MakeResponse updateMake(UUID makeId, UpdateMakeRequest request) {
        log.info("Updating vehicle make id={}", makeId);

        VehicleMake make = findMakeOrThrow(makeId);

        // Guard against name collision with a different record
        if (request.name() != null
                && !request.name().equals(make.getName())
                && makeRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(
                    "A vehicle make with name '%s' already exists".formatted(request.name())
            );
        }

        mapper.applyUpdate(request, make);
        make = makeRepository.save(make);

        return mapper.toResponse(make);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteMake(UUID makeId) {
        log.info("Soft-deleting vehicle make id={}", makeId);

        VehicleMake make = findMakeOrThrow(makeId);

        // Cascade INACTIVE to all child models
        make.getModels().forEach(m -> m.setStatus(
                ModelStatus.INACTIVE)
        );

        make.setStatus(MakeStatus.INACTIVE);
        makeRepository.save(make);

        log.info("Vehicle make id={} set to INACTIVE along with {} models", makeId, make.getModels().size());
    }

    // ─── REGIONS ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<RegionResponse> associateRegions(UUID makeId, AssociateRegionsRequest request) {
        log.info("Associating {} regions with make id={}", request.regions().size(), makeId);

        VehicleMake make = findMakeOrThrow(makeId);

        List<MakeRegion> toSave = request.regions().stream()
                .filter(entry -> !regionRepository.existsByMakeIdAndRegionCode(makeId, entry.regionCode()))
                .map(entry -> MakeRegion.builder()
                        .make(make)
                        .regionCode(entry.regionCode())
                        .launchYear(entry.launchYear())
                        .build())
                .toList();

        regionRepository.saveAll(toSave);

        List<MakeRegion> all = regionRepository.findByMakeId(makeId);
        return mapper.toRegionResponseList(all);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VehicleMake findMakeOrThrow(UUID makeId) {
        return makeRepository.findById(makeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle make not found with id: " + makeId
                ));
    }
}
