package com.pk.ev.vehicle.catalog.modeltrim.mapper;

import com.pk.ev.vehicle.catalog.modeltrim.dto.ModelTrimDto.*;
import com.pk.ev.vehicle.catalog.modeltrim.model.ModelTrim;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ModelTrimMapperTest {

    private ModelTrimMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ModelTrimMapper();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VehicleModel buildModel() {
        VehicleMake make = new VehicleMake();
        make.setId(UUID.randomUUID());
        make.setName("Tata");

        VehicleModel model = new VehicleModel();
        model.setId(UUID.randomUUID());
        model.setName("Tiago EV");
        model.setMake(make);
        return model;
    }

    private ModelTrim buildTrim(VehicleModel model) {
        return ModelTrim.builder()
                .id(UUID.randomUUID())
                .model(model)
                .trimName("XZ+")
                .description("Top trim with sunroof")
                .hasSunroof(true)
                .hasAdas(true)
                .hasConnectedCar(true)
                .infotainmentSizeInches(10)
                .sortOrder(3)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ─── toModelTrimResponse ─────────────────────────────────────────────────

    @Test
    void toModelTrimResponse_mapsAllFields() {
        VehicleModel model = buildModel();
        ModelTrim trim = buildTrim(model);

        ModelTrimResponse response = mapper.toModelTrimResponse(trim);

        assertThat(response.id()).isEqualTo(trim.getId());
        assertThat(response.modelId()).isEqualTo(model.getId());
        assertThat(response.trimName()).isEqualTo("XZ+");
        assertThat(response.description()).isEqualTo("Top trim with sunroof");
        assertThat(response.hasSunroof()).isTrue();
        assertThat(response.hasAdas()).isTrue();
        assertThat(response.hasConnectedCar()).isTrue();
        assertThat(response.infotainmentSizeInches()).isEqualTo(10);
        assertThat(response.sortOrder()).isEqualTo(3);
        assertThat(response.isActive()).isTrue();
        assertThat(response.createdAt()).isEqualTo(trim.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(trim.getUpdatedAt());
    }

    @Test
    void toModelTrimResponse_handlesNullOptionalFields() {
        VehicleModel model = buildModel();
        ModelTrim trim = ModelTrim.builder()
                .id(UUID.randomUUID())
                .model(model)
                .trimName("XE")
                .description(null)
                .infotainmentSizeInches(null)
                .build();

        ModelTrimResponse response = mapper.toModelTrimResponse(trim);

        assertThat(response.description()).isNull();
        assertThat(response.infotainmentSizeInches()).isNull();
        assertThat(response.trimName()).isEqualTo("XE");
    }

    @Test
    void toModelTrimResponse_inactiveTrim_mapsIsActiveFalse() {
        VehicleModel model = buildModel();
        ModelTrim trim = ModelTrim.builder()
                .id(UUID.randomUUID())
                .model(model)
                .trimName("XT")
                .isActive(false)
                .build();

        ModelTrimResponse response = mapper.toModelTrimResponse(trim);

        assertThat(response.isActive()).isFalse();
    }

    // ─── toModelTrimEntity ────────────────────────────────────────────────────

    @Test
    void toModelTrimEntity_mapsAllRequestFields() {
        VehicleModel model = buildModel();
        CreateModelTrimRequest req = new CreateModelTrimRequest(
                "XZ+", "Top trim", true, true, true, 10, 3);

        ModelTrim entity = mapper.toModelTrimEntity(req, model);

        assertThat(entity.getModel()).isEqualTo(model);
        assertThat(entity.getTrimName()).isEqualTo("XZ+");
        assertThat(entity.getDescription()).isEqualTo("Top trim");
        assertThat(entity.getHasSunroof()).isTrue();
        assertThat(entity.getHasAdas()).isTrue();
        assertThat(entity.getHasConnectedCar()).isTrue();
        assertThat(entity.getInfotainmentSizeInches()).isEqualTo(10);
        assertThat(entity.getSortOrder()).isEqualTo(3);
    }

    @Test
    void toModelTrimEntity_defaultsNullBooleans_toFalse() {
        VehicleModel model = buildModel();
        CreateModelTrimRequest req = new CreateModelTrimRequest(
                "XE", null, null, null, null, null, null);

        ModelTrim entity = mapper.toModelTrimEntity(req, model);

        assertThat(entity.getHasSunroof()).isFalse();
        assertThat(entity.getHasAdas()).isFalse();
        assertThat(entity.getHasConnectedCar()).isFalse();
    }

    @Test
    void toModelTrimEntity_defaultsNullSortOrder_toZero() {
        VehicleModel model = buildModel();
        CreateModelTrimRequest req = new CreateModelTrimRequest(
                "XE", null, null, null, null, null, null);

        ModelTrim entity = mapper.toModelTrimEntity(req, model);

        assertThat(entity.getSortOrder()).isZero();
    }

    @Test
    void toModelTrimEntity_usesTrueWhenBooleansProvided() {
        VehicleModel model = buildModel();
        CreateModelTrimRequest req = new CreateModelTrimRequest(
                "XZ+ Tech LUX", "Luxury", true, false, true, 7, 4);

        ModelTrim entity = mapper.toModelTrimEntity(req, model);

        assertThat(entity.getHasSunroof()).isTrue();
        assertThat(entity.getHasAdas()).isFalse();
        assertThat(entity.getHasConnectedCar()).isTrue();
        assertThat(entity.getSortOrder()).isEqualTo(4);
    }

    // ─── applyModelTrimUpdate ─────────────────────────────────────────────────

    @Test
    void applyModelTrimUpdate_updatesAllNonNullFields() {
        VehicleModel model = buildModel();
        ModelTrim trim = buildTrim(model);
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                "XT", "Updated desc", false, false, false, 8, 2, false);

        mapper.applyModelTrimUpdate(req, trim);

        assertThat(trim.getTrimName()).isEqualTo("XT");
        assertThat(trim.getDescription()).isEqualTo("Updated desc");
        assertThat(trim.getHasSunroof()).isFalse();
        assertThat(trim.getHasAdas()).isFalse();
        assertThat(trim.getHasConnectedCar()).isFalse();
        assertThat(trim.getInfotainmentSizeInches()).isEqualTo(8);
        assertThat(trim.getSortOrder()).isEqualTo(2);
        assertThat(trim.getIsActive()).isFalse();
    }

    @Test
    void applyModelTrimUpdate_doesNotChangeFields_whenAllNull() {
        VehicleModel model = buildModel();
        ModelTrim trim = buildTrim(model);
        String originalName = trim.getTrimName();
        String originalDesc = trim.getDescription();

        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                null, null, null, null, null, null, null, null);

        mapper.applyModelTrimUpdate(req, trim);

        assertThat(trim.getTrimName()).isEqualTo(originalName);
        assertThat(trim.getDescription()).isEqualTo(originalDesc);
        assertThat(trim.getHasSunroof()).isTrue();
        assertThat(trim.getIsActive()).isTrue();
    }

    @Test
    void applyModelTrimUpdate_updatesOnlySuppliedFields() {
        VehicleModel model = buildModel();
        ModelTrim trim = buildTrim(model);
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                "XT", null, null, null, null, null, null, null);

        mapper.applyModelTrimUpdate(req, trim);

        assertThat(trim.getTrimName()).isEqualTo("XT");
        // other fields untouched
        assertThat(trim.getHasSunroof()).isTrue();
        assertThat(trim.getDescription()).isEqualTo("Top trim with sunroof");
    }

    @Test
    void applyModelTrimUpdate_canDeactivateTrim() {
        VehicleModel model = buildModel();
        ModelTrim trim = buildTrim(model);
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                null, null, null, null, null, null, null, false);

        mapper.applyModelTrimUpdate(req, trim);

        assertThat(trim.getIsActive()).isFalse();
    }

    @Test
    void applyModelTrimUpdate_canReactivateTrim() {
        VehicleModel model = buildModel();
        ModelTrim trim = ModelTrim.builder()
                .id(UUID.randomUUID()).model(model).trimName("XE").isActive(false).build();
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                null, null, null, null, null, null, null, true);

        mapper.applyModelTrimUpdate(req, trim);

        assertThat(trim.getIsActive()).isTrue();
    }
}
