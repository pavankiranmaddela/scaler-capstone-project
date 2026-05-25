package com.pk.ev.vehicle.catalog.vehiclemodel.mapper;

import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemodel.dtos.VehicleModelDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.BodyType;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.DriveType;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ImageAngle;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.ModelImage;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleModelMapperTest {

    private VehicleModelMapper mapper;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private UUID makeId;
    private UUID modelId;
    private VehicleMake make;
    private VehicleModel model;

    @BeforeEach
    void setUp() {
        mapper  = new VehicleModelMapper();
        makeId  = UUID.randomUUID();
        modelId = UUID.randomUUID();

        make = VehicleMake.builder()
                .id(makeId).name("Tata Motors").slug("tata-motors").build();

        model = VehicleModel.builder()
                .id(modelId).make(make)
                .name("Tiago EV").modelYear(2023)
                .weightKg(1200).bodyType(BodyType.HATCHBACK)
                .seatingCapacity(5).driveType(DriveType.FWD)
                .status(ModelStatus.ACTIVE)
                .images(new ArrayList<>())
                .build();
        model.setCreatedAt(Instant.parse("2023-01-01T00:00:00Z"));
        model.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ModelImage buildImage(UUID id, boolean primary, ImageAngle angle) {
        ModelImage img = ModelImage.builder()
                .id(id).model(model)
                .url("https://cdn.example.com/" + id + ".jpg")
                .isPrimary(primary).angle(angle)
                .build();
        img.setUploadedAt(Instant.parse("2023-06-01T00:00:00Z"));
        return img;
    }

    // ─── toResponse ──────────────────────────────────────────────────────────

    @Test
    void toResponse_mapsAllScalarFields() {
        ModelResponse response = mapper.toResponse(model);

        assertThat(response.id()).isEqualTo(modelId);
        assertThat(response.makeId()).isEqualTo(makeId);
        assertThat(response.makeName()).isEqualTo("Tata Motors");
        assertThat(response.name()).isEqualTo("Tiago EV");
        assertThat(response.modelYear()).isEqualTo(2023);
        assertThat(response.weightKg()).isEqualTo(1200);
        assertThat(response.bodyType()).isEqualTo(BodyType.HATCHBACK);
        assertThat(response.seatingCapacity()).isEqualTo(5);
        assertThat(response.driveType()).isEqualTo(DriveType.FWD);
        assertThat(response.status()).isEqualTo(ModelStatus.ACTIVE);
    }

    @Test
    void toResponse_mapsTimestamps() {
        ModelResponse response = mapper.toResponse(model);

        assertThat(response.createdAt()).isEqualTo(Instant.parse("2023-01-01T00:00:00Z"));
        assertThat(response.updatedAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    void toResponse_noImages_returnsEmptyList() {
        ModelResponse response = mapper.toResponse(model);

        assertThat(response.images()).isEmpty();
    }

    @Test
    void toResponse_withImages_mapsEachImage() {
        UUID img1 = UUID.randomUUID();
        UUID img2 = UUID.randomUUID();
        model.getImages().add(buildImage(img1, true,  ImageAngle.FRONT));
        model.getImages().add(buildImage(img2, false, ImageAngle.REAR));

        ModelResponse response = mapper.toResponse(model);

        assertThat(response.images()).hasSize(2);
        assertThat(response.images().get(0).id()).isEqualTo(img1);
        assertThat(response.images().get(0).isPrimary()).isTrue();
        assertThat(response.images().get(1).id()).isEqualTo(img2);
        assertThat(response.images().get(1).angle()).isEqualTo(ImageAngle.REAR);
    }

    @Test
    void toResponse_nullableFields_mapNulls() {
        model.setWeightKg(null);
        model.setBodyType(null);
        model.setSeatingCapacity(null);
        model.setDriveType(null);

        ModelResponse response = mapper.toResponse(model);

        assertThat(response.weightKg()).isNull();
        assertThat(response.bodyType()).isNull();
        assertThat(response.seatingCapacity()).isNull();
        assertThat(response.driveType()).isNull();
    }

    // ─── toSummary ────────────────────────────────────────────────────────────

    @Test
    void toSummary_mapsAllFields() {
        ModelSummaryResponse summary = mapper.toSummary(model);

        assertThat(summary.id()).isEqualTo(modelId);
        assertThat(summary.makeId()).isEqualTo(makeId);
        assertThat(summary.makeName()).isEqualTo("Tata Motors");
        assertThat(summary.name()).isEqualTo("Tiago EV");
        assertThat(summary.modelYear()).isEqualTo(2023);
        assertThat(summary.bodyType()).isEqualTo(BodyType.HATCHBACK);
        assertThat(summary.status()).isEqualTo(ModelStatus.ACTIVE);
    }

    @Test
    void toSummary_primaryImagePresent_usesPrimaryUrl() {
        UUID primaryId = UUID.randomUUID();
        model.getImages().add(buildImage(UUID.randomUUID(), false, ImageAngle.REAR));
        model.getImages().add(buildImage(primaryId, true, ImageAngle.FRONT));

        ModelSummaryResponse summary = mapper.toSummary(model);

        assertThat(summary.primaryImageUrl())
                .isEqualTo("https://cdn.example.com/" + primaryId + ".jpg");
    }

    @Test
    void toSummary_noPrimaryImage_returnsNullUrl() {
        model.getImages().add(buildImage(UUID.randomUUID(), false, ImageAngle.SIDE));

        ModelSummaryResponse summary = mapper.toSummary(model);

        assertThat(summary.primaryImageUrl()).isNull();
    }

    @Test
    void toSummary_noImages_returnsNullUrl() {
        ModelSummaryResponse summary = mapper.toSummary(model);

        assertThat(summary.primaryImageUrl()).isNull();
    }

    @Test
    void toSummary_firstPrimaryWins_whenMultiplePrimary() {
        UUID first  = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        model.getImages().add(buildImage(first,  true, ImageAngle.FRONT));
        model.getImages().add(buildImage(second, true, ImageAngle.SIDE));

        ModelSummaryResponse summary = mapper.toSummary(model);

        // findFirst semantics — first in list wins
        assertThat(summary.primaryImageUrl())
                .isEqualTo("https://cdn.example.com/" + first + ".jpg");
    }

    // ─── toImageResponse ─────────────────────────────────────────────────────

    @Test
    void toImageResponse_mapsAllFields() {
        UUID imageId = UUID.randomUUID();
        ModelImage image = buildImage(imageId, true, ImageAngle.INTERIOR);

        ModelImageResponse response = mapper.toImageResponse(image);

        assertThat(response.id()).isEqualTo(imageId);
        assertThat(response.url()).isEqualTo("https://cdn.example.com/" + imageId + ".jpg");
        assertThat(response.isPrimary()).isTrue();
        assertThat(response.angle()).isEqualTo(ImageAngle.INTERIOR);
        assertThat(response.uploadedAt()).isEqualTo(Instant.parse("2023-06-01T00:00:00Z"));
    }

    @Test
    void toImageResponse_nullAngle_mapsNull() {
        ModelImage image = buildImage(UUID.randomUUID(), false, null);

        ModelImageResponse response = mapper.toImageResponse(image);

        assertThat(response.angle()).isNull();
    }

    @Test
    void toImageResponse_nonPrimaryFlag_mapsFalse() {
        ModelImage image = buildImage(UUID.randomUUID(), false, ImageAngle.CHARGING_PORT);

        ModelImageResponse response = mapper.toImageResponse(image);

        assertThat(response.isPrimary()).isFalse();
    }

    // ─── toPagedResponse ─────────────────────────────────────────────────────

    @Test
    void toPagedResponse_mapsPageMetadata() {
        Page<VehicleModel> page = new PageImpl<>(
                List.of(model), PageRequest.of(1, 5), 12);

        PagedModelsResponse response = mapper.toPagedResponse(page);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(5);
        assertThat(response.totalElements()).isEqualTo(12);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.last()).isFalse();
    }

    @Test
    void toPagedResponse_mapsContentViaSummary() {
        Page<VehicleModel> page = new PageImpl<>(List.of(model));

        PagedModelsResponse response = mapper.toPagedResponse(page);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(modelId);
        assertThat(response.content().get(0).name()).isEqualTo("Tiago EV");
    }

    @Test
    void toPagedResponse_emptyPage_returnsEmpty() {
        Page<VehicleModel> emptyPage = Page.empty();

        PagedModelsResponse response = mapper.toPagedResponse(emptyPage);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.last()).isTrue();
    }

    @Test
    void toPagedResponse_lastPage_flaggedCorrectly() {
        Page<VehicleModel> lastPage = new PageImpl<>(
                List.of(model), PageRequest.of(0, 20), 1);

        PagedModelsResponse response = mapper.toPagedResponse(lastPage);

        assertThat(response.last()).isTrue();
    }

    // ─── toSearchResponse ─────────────────────────────────────────────────────

    @Test
    void toSearchResponse_mapsQueryAndMetadata() {
        Page<VehicleModel> page = new PageImpl<>(
                List.of(model), PageRequest.of(0, 20), 1);

        SearchModelsResponse response = mapper.toSearchResponse(page, "Tiago");

        assertThat(response.query()).isEqualTo("Tiago");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
    }

    @Test
    void toSearchResponse_mapsContentViaSummary() {
        Page<VehicleModel> page = new PageImpl<>(List.of(model));

        SearchModelsResponse response = mapper.toSearchResponse(page, "ev");

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("Tiago EV");
    }

    @Test
    void toSearchResponse_emptyPage_returnsEmpty() {
        Page<VehicleModel> emptyPage = Page.empty();

        SearchModelsResponse response = mapper.toSearchResponse(emptyPage, "xyz");

        assertThat(response.content()).isEmpty();
        assertThat(response.query()).isEqualTo("xyz");
        assertThat(response.totalElements()).isEqualTo(0);
    }

    // ─── toEntity ─────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllRequestFields() {
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Nexon EV", null, 2024,
                null, null, 450, 1600,
                BodyType.SUV, 5, DriveType.RWD);

        VehicleModel entity = mapper.toEntity(req, make);

        assertThat(entity.getMake()).isEqualTo(make);
        assertThat(entity.getName()).isEqualTo("Nexon EV");
        assertThat(entity.getModelYear()).isEqualTo(2024);
        assertThat(entity.getWeightKg()).isEqualTo(1600);
        assertThat(entity.getBodyType()).isEqualTo(BodyType.SUV);
        assertThat(entity.getSeatingCapacity()).isEqualTo(5);
        assertThat(entity.getDriveType()).isEqualTo(DriveType.RWD);
    }

    @Test
    void toEntity_nullOptionalFields_mapNulls() {
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Nexon EV", null, 2024, null, null, null, null, null, null, null);

        VehicleModel entity = mapper.toEntity(req, make);

        assertThat(entity.getWeightKg()).isNull();
        assertThat(entity.getBodyType()).isNull();
        assertThat(entity.getSeatingCapacity()).isNull();
        assertThat(entity.getDriveType()).isNull();
    }

    @Test
    void toEntity_doesNotSetId() {
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Nexon EV", null, 2024, null, null, null, null, null, null, null);

        VehicleModel entity = mapper.toEntity(req, make);

        assertThat(entity.getId()).isNull();
    }

    @Test
    void toEntity_defaultStatus_isActive() {
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Nexon EV", null, 2024, null, null, null, null, null, null, null);

        VehicleModel entity = mapper.toEntity(req, make);

        // Builder.Default — status = ACTIVE
        assertThat(entity.getStatus()).isEqualTo(ModelStatus.ACTIVE);
    }

    // ─── applyUpdate ─────────────────────────────────────────────────────────

    @Test
    void applyUpdate_allFieldsProvided_appliesAll() {
        UpdateModelRequest req = new UpdateModelRequest(
                "Nexon EV Max", null, 2025, null, null, 480, 1700,
                BodyType.SUV, 7, DriveType.AWD, ModelStatus.INACTIVE);

        mapper.applyUpdate(req, model);

        assertThat(model.getName()).isEqualTo("Nexon EV Max");
        assertThat(model.getModelYear()).isEqualTo(2025);
        assertThat(model.getWeightKg()).isEqualTo(1700);
        assertThat(model.getBodyType()).isEqualTo(BodyType.SUV);
        assertThat(model.getSeatingCapacity()).isEqualTo(7);
        assertThat(model.getDriveType()).isEqualTo(DriveType.AWD);
        assertThat(model.getStatus()).isEqualTo(ModelStatus.INACTIVE);
    }

    @Test
    void applyUpdate_allNullFields_keepsOriginalValues() {
        UpdateModelRequest req = new UpdateModelRequest(
                null, null, null, null, null, null, null, null, null, null, null);

        mapper.applyUpdate(req, model);

        assertThat(model.getName()).isEqualTo("Tiago EV");
        assertThat(model.getModelYear()).isEqualTo(2023);
        assertThat(model.getStatus()).isEqualTo(ModelStatus.ACTIVE);
    }

    @Test
    void applyUpdate_partialFields_onlyUpdatesProvided() {
        UpdateModelRequest req = new UpdateModelRequest(
                "Tiago EV X", null, null, null, null, null, null, null, null, null, null);

        mapper.applyUpdate(req, model);

        assertThat(model.getName()).isEqualTo("Tiago EV X");
        assertThat(model.getModelYear()).isEqualTo(2023);      // unchanged
        assertThat(model.getBodyType()).isEqualTo(BodyType.HATCHBACK); // unchanged
    }

    @Test
    void applyUpdate_statusOnly_updatesStatusKeepsRest() {
        UpdateModelRequest req = new UpdateModelRequest(
                null, null, null, null, null, null, null, null, null, null, ModelStatus.DISCONTINUED);

        mapper.applyUpdate(req, model);

        assertThat(model.getStatus()).isEqualTo(ModelStatus.DISCONTINUED);
        assertThat(model.getName()).isEqualTo("Tiago EV");
    }

    // ─── toImageResponseList ─────────────────────────────────────────────────

    @Test
    void toImageResponseList_mapsAllImages() {
        List<ModelImage> images = List.of(
                buildImage(UUID.randomUUID(), true,  ImageAngle.FRONT),
                buildImage(UUID.randomUUID(), false, ImageAngle.REAR),
                buildImage(UUID.randomUUID(), false, ImageAngle.INTERIOR)
        );

        List<ModelImageResponse> responses = mapper.toImageResponseList(images);

        assertThat(responses).hasSize(3);
        assertThat(responses).extracting(ModelImageResponse::angle)
                .containsExactly(ImageAngle.FRONT, ImageAngle.REAR, ImageAngle.INTERIOR);
    }

    @Test
    void toImageResponseList_emptyList_returnsEmpty() {
        List<ModelImageResponse> responses = mapper.toImageResponseList(List.of());

        assertThat(responses).isEmpty();
    }
}
