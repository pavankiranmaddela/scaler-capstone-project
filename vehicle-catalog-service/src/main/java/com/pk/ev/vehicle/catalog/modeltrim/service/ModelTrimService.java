package com.pk.ev.vehicle.catalog.modeltrim.service;

import com.pk.ev.vehicle.catalog.modeltrim.dto.ModelTrimDto.*;
import com.pk.ev.vehicle.catalog.modeltrim.model.ModelTrim;

import java.util.List;
import java.util.UUID;

public interface ModelTrimService {
    // ─── Model Trim ───────────────────────────────────────────────────────────
    ModelTrimResponse addTrim(UUID modelId, CreateModelTrimRequest request);
    List<ModelTrimResponse> getTrims(UUID modelId, boolean activeOnly);
    ModelTrimResponse getTrimById(UUID modelId, UUID trimId);
    ModelTrimResponse updateTrim(UUID modelId, UUID trimId, UpdateModelTrimRequest request);
    void deleteTrim(UUID modelId, UUID trimId);
    ModelTrim findTrimOrThrow(UUID modelId, UUID trimId);
}
