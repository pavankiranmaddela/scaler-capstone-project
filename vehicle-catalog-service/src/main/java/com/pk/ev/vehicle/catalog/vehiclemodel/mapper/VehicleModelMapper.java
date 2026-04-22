package com.pk.ev.vehicle.catalog.vehiclemodel.mapper;

import com.pk.ev.vehicle.catalog.vehiclemodel.dtos.VehicleModelDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.ModelImage;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VehicleModelMapper {

    public ModelResponse toResponse(VehicleModel model) {
        return new ModelResponse(
                model.getId(),
                model.getMake().getId(),
                model.getMake().getName(),
                model.getName(),
                model.getModelYear(),
                model.getWeightKg(),
                model.getBodyType(),
                model.getSeatingCapacity(),
                model.getDriveType(),
                model.getStatus(),
                model.getImages().stream().map(this::toImageResponse).toList(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }

    public ModelSummaryResponse toSummary(VehicleModel model) {
        String primaryImageUrl = model.getImages().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .map(ModelImage::getUrl)
                .findFirst()
                .orElse(null);

        return new ModelSummaryResponse(
                model.getId(),
                model.getMake().getId(),
                model.getMake().getName(),
                model.getName(),
                model.getModelYear(),
                model.getBodyType(),
                model.getStatus(),
                primaryImageUrl
        );
    }

    public ModelImageResponse toImageResponse(ModelImage image) {
        return new ModelImageResponse(
                image.getId(),
                image.getUrl(),
                image.getIsPrimary(),
                image.getAngle(),
                image.getUploadedAt()
        );
    }

    public PagedModelsResponse toPagedResponse(Page<VehicleModel> page) {
        return new PagedModelsResponse(
                page.getContent().stream().map(this::toSummary).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public SearchModelsResponse toSearchResponse(Page<VehicleModel> page, String query) {
        return new SearchModelsResponse(
                page.getContent().stream().map(this::toSummary).toList(),
                query,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public VehicleModel toEntity(CreateModelRequest req, VehicleMake make) {
        return VehicleModel.builder()
                .make(make)
                .name(req.name())
                .modelYear(req.modelYear())
                .weightKg(req.weightKg())
                .bodyType(req.bodyType())
                .seatingCapacity(req.seatingCapacity())
                .driveType(req.driveType())
                .build();
    }

    public void applyUpdate(UpdateModelRequest req, VehicleModel model) {
        if (req.name()               != null) model.setName(req.name());
        if (req.modelYear()          != null) model.setModelYear(req.modelYear());
        if (req.weightKg()           != null) model.setWeightKg(req.weightKg());
        if (req.bodyType()           != null) model.setBodyType(req.bodyType());
        if (req.seatingCapacity()    != null) model.setSeatingCapacity(req.seatingCapacity());
        if (req.driveType()          != null) model.setDriveType(req.driveType());
        if (req.status()             != null) model.setStatus(req.status());
    }

    public List<ModelImageResponse> toImageResponseList(List<ModelImage> images) {
        return images.stream().map(this::toImageResponse).toList();
    }
}
