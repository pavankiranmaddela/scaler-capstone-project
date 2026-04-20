package com.pk.ev.vehicle.catalog.model.service;

import com.pk.ev.vehicle.catalog.model.dtos.VehicleModelDtos.*;
import com.pk.ev.vehicle.catalog.model.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.model.mapper.VehicleModelMapper;
import com.pk.ev.vehicle.catalog.model.model.ModelImage;
import com.pk.ev.vehicle.catalog.make.model.VehicleMake;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.model.repository.ModelImageRepository;
import com.pk.ev.vehicle.catalog.make.repository.VehicleMakeRepository;
import com.pk.ev.vehicle.catalog.model.repository.VehicleModelRepository;
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
public class VehicleModelServiceImpl implements VehicleModelService {

    private final VehicleModelRepository modelRepository;
    private final VehicleMakeRepository  makeRepository;
    private final ModelImageRepository   imageRepository;
    private final VehicleModelMapper modelMapper;

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ModelResponse createModel(CreateModelRequest request) {
        log.info("Creating vehicle model '{}' year={} under makeId={}",
                request.name(), request.modelYear(), request.makeId());

        VehicleMake make = findMakeOrThrow(request.makeId());

        // Each model-year is a separate record; guard exact duplicates
        if (modelRepository.existsByMakeIdAndNameAndModelYear(
                request.makeId(), request.name(), request.modelYear())) {
            throw new DuplicateResourceException(
                    "Model '%s' (%d) already exists under make '%s'"
                            .formatted(request.name(), request.modelYear(), make.getName())
            );
        }

        VehicleModel model = modelMapper.toEntity(request, make);
        model = modelRepository.save(model);

        log.info("Created vehicle model id={}", model.getId());
        return modelMapper.toResponse(model);
    }

    // ─── READ — list ─────────────────────────────────────────────────────────

    @Override
    public PagedModelsResponse getAllModels(ModelFilterParams filters) {
        Pageable pageable = PageRequest.of(
                filters.page(), filters.size(),
                Sort.by(filters.sort()).ascending()
        );

        Page<VehicleModel> page = modelRepository.findAllByFilters(
                filters.makeId(),
                filters.year(),
                filters.status(),
                pageable
        );

        return modelMapper.toPagedResponse(page);
    }

    // ─── READ — single ───────────────────────────────────────────────────────

    @Override
    public ModelResponse getModelById(UUID modelId) {
        return modelMapper.toResponse(findModelOrThrow(modelId));
    }

    // ─── UPDATE — full ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public ModelResponse updateModel(UUID modelId, UpdateModelRequest request) {
        log.info("Updating vehicle model id={}", modelId);

        VehicleModel model = findModelOrThrow(modelId);

        // Guard duplicate name+year collision with a *different* record under the same make
        if (request.name() != null && request.modelYear() != null) {
            boolean collides = modelRepository
                    .existsByMakeIdAndNameAndModelYear(
                            model.getMake().getId(), request.name(), request.modelYear());
            if (collides && !(request.name().equals(model.getName())
                    && request.modelYear().equals(model.getModelYear()))) {
                throw new DuplicateResourceException(
                        "Model '%s' (%d) already exists under this make"
                                .formatted(request.name(), request.modelYear())
                );
            }
        }

        modelMapper.applyUpdate(request, model);
        return modelMapper.toResponse(modelRepository.save(model));
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteModel(UUID modelId) {
        log.info("Soft-deleting vehicle model id={}", modelId);
        VehicleModel model = findModelOrThrow(modelId);
        model.setStatus(ModelStatus.DISCONTINUED);
        modelRepository.save(model);
    }

    // ─── STATUS toggle ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public ModelResponse updateModelStatus(UUID modelId, UpdateModelStatusRequest request) {
        log.info("Changing status of model id={} to {}", modelId, request.status());
        VehicleModel model = findModelOrThrow(modelId);
        model.setStatus(request.status());
        return modelMapper.toResponse(modelRepository.save(model));
    }

    // ─── IMAGES — add ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ModelImageResponse addImage(UUID modelId, UploadImageRequest request) {
        log.info("Adding image to model id={} isPrimary={}", modelId, request.isPrimary());

        VehicleModel model = findModelOrThrow(modelId);

        // If this image is primary, clear existing primary flag first
        if (Boolean.TRUE.equals(request.isPrimary())) {
            imageRepository.clearPrimaryFlag(modelId);
        }

        ModelImage image = ModelImage.builder()
                .model(model)
                .url(request.url())
                .isPrimary(request.isPrimary())
                .angle(request.angle())
                .build();

        image = imageRepository.save(image);
        return modelMapper.toImageResponse(image);
    }

    // ─── IMAGES — list ───────────────────────────────────────────────────────

    @Override
    public List<ModelImageResponse> getImages(UUID modelId) {
        findModelOrThrow(modelId);   // validate existence
        return modelMapper.toImageResponseList(imageRepository.findByModelId(modelId));
    }

    // ─── IMAGES — delete ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteImage(UUID modelId, UUID imageId) {
        log.info("Deleting image id={} from model id={}", imageId, modelId);
        findModelOrThrow(modelId);

        ModelImage image = imageRepository.findByIdAndModelId(imageId, modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Image not found with id=%s for model id=%s".formatted(imageId, modelId)
                ));

        imageRepository.delete(image);
    }

    // ─── SEARCH ──────────────────────────────────────────────────────────────

    @Override
    public SearchModelsResponse search(String query, String statusParam, Pageable pageable) {
        ModelStatus status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try {
                status = ModelStatus.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value: " + statusParam);
            }
        }

        Page<VehicleModel> page = modelRepository.searchByKeyword(query, status, pageable);
        return modelMapper.toSearchResponse(page, query);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    public VehicleModel findModelOrThrow(UUID modelId) {
        return modelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle model not found with id: " + modelId
                ));
    }

    private VehicleMake findMakeOrThrow(UUID makeId) {
        return makeRepository.findById(makeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle make not found with id: " + makeId
                ));
    }
}
