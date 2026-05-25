package com.pk.ev.vehicle.catalog.modeltrim.service;

import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.modeltrim.dto.ModelTrimDto.*;
import com.pk.ev.vehicle.catalog.modeltrim.mapper.ModelTrimMapper;
import com.pk.ev.vehicle.catalog.modeltrim.model.ModelTrim;
import com.pk.ev.vehicle.catalog.modeltrim.repository.ModelTrimRepository;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.vehiclemodel.service.VehicleModelService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModelTrimServiceImplTest {

    @Mock private VehicleModelService modelService;
    @Mock private ModelTrimRepository trimRepository;
    @Mock private ModelTrimMapper modelTrimMapper;

    private ModelTrimServiceImpl service;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new ModelTrimServiceImpl(modelService, trimRepository, modelTrimMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VehicleModel buildModel(UUID modelId) {
        VehicleMake make = new VehicleMake();
        make.setId(UUID.randomUUID());
        make.setName("Tata");

        VehicleModel model = new VehicleModel();
        model.setId(modelId);
        model.setName("Tiago EV");
        model.setMake(make);
        return model;
    }

    private ModelTrim buildTrim(UUID trimId, UUID modelId, String name, boolean active) {
        VehicleModel model = buildModel(modelId);
        return ModelTrim.builder()
                .id(trimId)
                .model(model)
                .trimName(name)
                .description("Desc")
                .hasSunroof(false)
                .hasAdas(false)
                .hasConnectedCar(false)
                .sortOrder(0)
                .isActive(active)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private ModelTrimResponse buildResponse(UUID trimId, UUID modelId, String name) {
        return new ModelTrimResponse(trimId, modelId, name, null,
                false, false, false, null, 0, true, Instant.now(), Instant.now());
    }

    // ─── addTrim ─────────────────────────────────────────────────────────────

    @Test
    void addTrim_savesAndReturnsResponse() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        VehicleModel model = buildModel(modelId);
        CreateModelTrimRequest req = new CreateModelTrimRequest(
                "XZ+", "Top trim", true, true, true, 10, 3);
        ModelTrim entity = buildTrim(trimId, modelId, "XZ+", true);
        ModelTrimResponse expected = buildResponse(trimId, modelId, "XZ+");

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimRepository.existsByModelIdAndTrimName(modelId, "XZ+")).thenReturn(false);
        when(modelTrimMapper.toModelTrimEntity(req, model)).thenReturn(entity);
        when(trimRepository.save(entity)).thenReturn(entity);
        when(modelTrimMapper.toModelTrimResponse(entity)).thenReturn(expected);

        ModelTrimResponse result = service.addTrim(modelId, req);

        assertThat(result).isEqualTo(expected);
        verify(trimRepository).save(entity);
        verify(modelTrimMapper).toModelTrimResponse(entity);
    }

    @Test
    void addTrim_throwsDuplicate_whenTrimNameAlreadyExists() {
        UUID modelId = UUID.randomUUID();
        VehicleModel model = buildModel(modelId);
        CreateModelTrimRequest req = new CreateModelTrimRequest(
                "XZ+", null, null, null, null, null, null);

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimRepository.existsByModelIdAndTrimName(modelId, "XZ+")).thenReturn(true);

        assertThatThrownBy(() -> service.addTrim(modelId, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("XZ+")
                .hasMessageContaining(modelId.toString());
    }

    @Test
    void addTrim_throwsResourceNotFound_whenModelMissing() {
        UUID modelId = UUID.randomUUID();
        CreateModelTrimRequest req = new CreateModelTrimRequest(
                "XE", null, null, null, null, null, null);

        when(modelService.findModelOrThrow(modelId))
                .thenThrow(new ResourceNotFoundException("Model not found: " + modelId));

        assertThatThrownBy(() -> service.addTrim(modelId, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(modelId.toString());
    }

    // ─── getTrims ─────────────────────────────────────────────────────────────

    @Test
    void getTrims_activeOnly_queriesActiveRepository() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        VehicleModel model = buildModel(modelId);
        ModelTrim trim = buildTrim(trimId, modelId, "XZ+", true);
        ModelTrimResponse response = buildResponse(trimId, modelId, "XZ+");

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimRepository.findByModelIdAndIsActiveTrueOrderBySortOrderAsc(modelId))
                .thenReturn(List.of(trim));
        when(modelTrimMapper.toModelTrimResponse(trim)).thenReturn(response);

        List<ModelTrimResponse> result = service.getTrims(modelId, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(response);
        verify(trimRepository).findByModelIdAndIsActiveTrueOrderBySortOrderAsc(modelId);
        verify(trimRepository, never()).findByModelIdOrderBySortOrderAsc(any());
    }

    @Test
    void getTrims_allTrims_queriesUnfilteredRepository() {
        UUID modelId = UUID.randomUUID();
        UUID trimId1 = UUID.randomUUID();
        UUID trimId2 = UUID.randomUUID();
        VehicleModel model = buildModel(modelId);
        ModelTrim active   = buildTrim(trimId1, modelId, "XZ+", true);
        ModelTrim inactive = buildTrim(trimId2, modelId, "XE",  false);

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimRepository.findByModelIdOrderBySortOrderAsc(modelId))
                .thenReturn(List.of(active, inactive));
        when(modelTrimMapper.toModelTrimResponse(active))
                .thenReturn(buildResponse(trimId1, modelId, "XZ+"));
        when(modelTrimMapper.toModelTrimResponse(inactive))
                .thenReturn(buildResponse(trimId2, modelId, "XE"));

        List<ModelTrimResponse> result = service.getTrims(modelId, false);

        assertThat(result).hasSize(2);
        verify(trimRepository).findByModelIdOrderBySortOrderAsc(modelId);
        verify(trimRepository, never()).findByModelIdAndIsActiveTrueOrderBySortOrderAsc(any());
    }

    @Test
    void getTrims_returnsEmptyList_whenNoTrimsExist() {
        UUID modelId = UUID.randomUUID();
        VehicleModel model = buildModel(modelId);

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimRepository.findByModelIdAndIsActiveTrueOrderBySortOrderAsc(modelId))
                .thenReturn(List.of());

        List<ModelTrimResponse> result = service.getTrims(modelId, true);

        assertThat(result).isEmpty();
    }

    @Test
    void getTrims_throwsResourceNotFound_whenModelMissing() {
        UUID modelId = UUID.randomUUID();

        when(modelService.findModelOrThrow(modelId))
                .thenThrow(new ResourceNotFoundException("Model not found: " + modelId));

        assertThatThrownBy(() -> service.getTrims(modelId, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getTrimById ─────────────────────────────────────────────────────────

    @Test
    void getTrimById_returnsResponse_whenFound() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrim trim = buildTrim(trimId, modelId, "XT", true);
        ModelTrimResponse expected = buildResponse(trimId, modelId, "XT");

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.of(trim));
        when(modelTrimMapper.toModelTrimResponse(trim)).thenReturn(expected);

        ModelTrimResponse result = service.getTrimById(modelId, trimId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getTrimById_throwsResourceNotFound_whenTrimMissing() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTrimById(modelId, trimId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(trimId.toString())
                .hasMessageContaining(modelId.toString());
    }

    // ─── updateTrim ───────────────────────────────────────────────────────────

    @Test
    void updateTrim_appliesUpdateAndSaves() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrim trim = buildTrim(trimId, modelId, "XE", true);
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                "XT", "Updated", null, null, null, null, null, null);
        ModelTrimResponse expected = buildResponse(trimId, modelId, "XT");

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.of(trim));
        when(trimRepository.existsByModelIdAndTrimName(modelId, "XT")).thenReturn(false);
        when(trimRepository.save(trim)).thenReturn(trim);
        when(modelTrimMapper.toModelTrimResponse(trim)).thenReturn(expected);

        ModelTrimResponse result = service.updateTrim(modelId, trimId, req);

        assertThat(result).isEqualTo(expected);
        verify(modelTrimMapper).applyModelTrimUpdate(req, trim);
        verify(trimRepository).save(trim);
    }

    @Test
    void updateTrim_throwsDuplicate_whenNewNameAlreadyTaken() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrim trim = buildTrim(trimId, modelId, "XE", true);
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                "XZ+", null, null, null, null, null, null, null);

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.of(trim));
        when(trimRepository.existsByModelIdAndTrimName(modelId, "XZ+")).thenReturn(true);

        assertThatThrownBy(() -> service.updateTrim(modelId, trimId, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("XZ+");
    }

    @Test
    void updateTrim_doesNotCheckDuplicate_whenNameUnchanged() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrim trim = buildTrim(trimId, modelId, "XZ+", true);
        // same name as existing trim — should NOT trigger duplicate check
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                "XZ+", "New desc", null, null, null, null, null, null);
        ModelTrimResponse expected = buildResponse(trimId, modelId, "XZ+");

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.of(trim));
        when(trimRepository.save(trim)).thenReturn(trim);
        when(modelTrimMapper.toModelTrimResponse(trim)).thenReturn(expected);

        service.updateTrim(modelId, trimId, req);

        verify(trimRepository, never()).existsByModelIdAndTrimName(any(), any());
    }

    @Test
    void updateTrim_doesNotCheckDuplicate_whenTrimNameIsNull() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrim trim = buildTrim(trimId, modelId, "XZ+", true);
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                null, "New desc", null, null, null, null, null, null);
        ModelTrimResponse expected = buildResponse(trimId, modelId, "XZ+");

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.of(trim));
        when(trimRepository.save(trim)).thenReturn(trim);
        when(modelTrimMapper.toModelTrimResponse(trim)).thenReturn(expected);

        service.updateTrim(modelId, trimId, req);

        verify(trimRepository, never()).existsByModelIdAndTrimName(any(), any());
    }

    @Test
    void updateTrim_throwsResourceNotFound_whenTrimMissing() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        UpdateModelTrimRequest req = new UpdateModelTrimRequest(
                "XT", null, null, null, null, null, null, null);

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTrim(modelId, trimId, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(trimId.toString());
    }

    // ─── deleteTrim ───────────────────────────────────────────────────────────

    @Test
    void deleteTrim_setsIsActiveFalse() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrim trim = buildTrim(trimId, modelId, "XZ+", true);

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.of(trim));
        when(trimRepository.save(trim)).thenReturn(trim);

        service.deleteTrim(modelId, trimId);

        assertThat(trim.getIsActive()).isFalse();
        verify(trimRepository).save(trim);
    }

    @Test
    void deleteTrim_isIdempotent_whenAlreadyInactive() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrim trim = buildTrim(trimId, modelId, "XZ+", false); // already inactive

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.of(trim));
        when(trimRepository.save(trim)).thenReturn(trim);

        service.deleteTrim(modelId, trimId);

        assertThat(trim.getIsActive()).isFalse();
        verify(trimRepository).save(trim);
    }

    @Test
    void deleteTrim_throwsResourceNotFound_whenTrimMissing() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteTrim(modelId, trimId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(trimId.toString());
    }

    // ─── findTrimOrThrow ──────────────────────────────────────────────────────

    @Test
    void findTrimOrThrow_returnsTrim_whenFound() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrim trim = buildTrim(trimId, modelId, "XT", true);

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.of(trim));

        ModelTrim result = service.findTrimOrThrow(modelId, trimId);

        assertThat(result).isEqualTo(trim);
    }

    @Test
    void findTrimOrThrow_throwsResourceNotFound_whenMissing() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();

        when(trimRepository.findByIdAndModelId(trimId, modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findTrimOrThrow(modelId, trimId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(trimId.toString())
                .hasMessageContaining(modelId.toString());
    }
}
