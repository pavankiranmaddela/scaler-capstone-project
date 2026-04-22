package com.pk.ev.vehicle.catalog.vehiclemodel.service;

import com.pk.ev.vehicle.catalog.vehiclemodel.dtos.VehicleModelDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface VehicleModelService {

    // POST /vehicle-models
    ModelResponse createModel(CreateModelRequest request);

    // GET /vehicle-models  (filtered + paginated)
    PagedModelsResponse getAllModels(ModelFilterParams filters);

    // GET /vehicle-models/{modelId}
    ModelResponse getModelById(UUID modelId);

    // PUT /vehicle-models/{modelId}
    ModelResponse updateModel(UUID modelId, UpdateModelRequest request);

    // DELETE /vehicle-models/{modelId}
    void deleteModel(UUID modelId);

    // PUT /vehicle-models/{modelId}/status
    ModelResponse updateModelStatus(UUID modelId, UpdateModelStatusRequest request);

    // POST /vehicle-models/{modelId}/images
    ModelImageResponse addImage(UUID modelId, UploadImageRequest request);

    // GET /vehicle-models/{modelId}/images
    List<ModelImageResponse> getImages(UUID modelId);

    // DELETE /vehicle-models/{modelId}/images/{imageId}
    void deleteImage(UUID modelId, UUID imageId);

    // GET /vehicle-models/search
    SearchModelsResponse search(String query, String status, Pageable pageable);

    VehicleModel findModelOrThrow(UUID modelId);
}
