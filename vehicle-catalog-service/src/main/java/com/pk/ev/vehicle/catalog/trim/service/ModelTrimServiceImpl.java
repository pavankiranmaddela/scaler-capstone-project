package com.pk.ev.vehicle.catalog.trim.service;

import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.model.service.VehicleModelService;
import com.pk.ev.vehicle.catalog.trim.dto.ModelTrimDto.*;
import com.pk.ev.vehicle.catalog.trim.mapper.ModelTrimMapper;
import com.pk.ev.vehicle.catalog.trim.model.ModelTrim;
import com.pk.ev.vehicle.catalog.trim.repository.ModelTrimRepository;
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
public class ModelTrimServiceImpl implements ModelTrimService {

    private final VehicleModelService modelService;

    private final ModelTrimRepository trimRepository;

    private final ModelTrimMapper modelTrimMapper;

    // ═══════════════════════════════════════════════════════════
    // MODEL TRIM
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ModelTrimResponse addTrim(UUID modelId, CreateModelTrimRequest req) {
        VehicleModel model = modelService.findModelOrThrow(modelId);
        if (trimRepository.existsByModelIdAndTrimName(modelId, req.trimName())) {
            throw new DuplicateResourceException(
                    "Trim '%s' already exists for model %s".formatted(req.trimName(), modelId));
        }
        ModelTrim saved = trimRepository.save(modelTrimMapper.toModelTrimEntity(req, model));
        log.info("Created ModelTrim id={} '{}' for modelId={}", saved.getId(), saved.getTrimName(), modelId);
        return modelTrimMapper.toModelTrimResponse(saved);
    }

    @Override
    public List<ModelTrimResponse> getTrims(UUID modelId, boolean activeOnly) {
        modelService.findModelOrThrow(modelId);
        List<ModelTrim> trims = activeOnly
                ? trimRepository.findByModelIdAndIsActiveTrueOrderBySortOrderAsc(modelId)
                : trimRepository.findByModelIdOrderBySortOrderAsc(modelId);
        return trims.stream().map(modelTrimMapper::toModelTrimResponse).toList();
    }

    @Override
    public ModelTrimResponse getTrimById(UUID modelId, UUID trimId) {
        return modelTrimMapper.toModelTrimResponse(findTrimOrThrow(modelId, trimId));
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
        modelTrimMapper.applyModelTrimUpdate(req, trim);
        return modelTrimMapper.toModelTrimResponse(trimRepository.save(trim));
    }

    @Override
    @Transactional
    public void deleteTrim(UUID modelId, UUID trimId) {
        ModelTrim trim = findTrimOrThrow(modelId, trimId);
        trim.setIsActive(false);
        trimRepository.save(trim);
    }

    public ModelTrim findTrimOrThrow(UUID modelId, UUID trimId) {
        return trimRepository.findByIdAndModelId(trimId, modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ModelTrim %s not found under model %s".formatted(trimId, modelId)));
    }
}
