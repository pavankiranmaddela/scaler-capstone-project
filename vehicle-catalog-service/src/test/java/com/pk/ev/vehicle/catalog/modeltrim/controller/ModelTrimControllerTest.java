package com.pk.ev.vehicle.catalog.modeltrim.controller;

import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.modeltrim.dto.ModelTrimDto.*;
import com.pk.ev.vehicle.catalog.modeltrim.service.ModelTrimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ModelTrimControllerTest {

    @Mock
    private ModelTrimService modelTrimService;

    @InjectMocks
    private ModelTrimController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ModelTrimResponse fakeResponse(UUID trimId, UUID modelId, String name) {
        return new ModelTrimResponse(trimId, modelId, name, "Desc",
                false, false, false, null, 0, true, Instant.now(), Instant.now());
    }

    // ─── addTrim ─────────────────────────────────────────────────────────────

    @Test
    void addTrim_returns201WithBody() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        CreateModelTrimRequest request = new CreateModelTrimRequest(
                "XZ+", "Top trim", true, true, true, 10, 3);
        ModelTrimResponse expected = fakeResponse(trimId, modelId, "XZ+");

        when(modelTrimService.addTrim(modelId, request)).thenReturn(expected);

        ResponseEntity<ModelTrimResponse> response = controller.addTrim(modelId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(modelTrimService).addTrim(modelId, request);
    }

    @Test
    void addTrim_propagatesDuplicateException_whenNameTaken() {
        UUID modelId = UUID.randomUUID();
        CreateModelTrimRequest request = new CreateModelTrimRequest(
                "XZ+", null, null, null, null, null, null);

        when(modelTrimService.addTrim(modelId, request))
                .thenThrow(new DuplicateResourceException("Trim 'XZ+' already exists"));

        assertThatThrownBy(() -> controller.addTrim(modelId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("XZ+");
    }

    @Test
    void addTrim_propagatesResourceNotFound_whenModelMissing() {
        UUID modelId = UUID.randomUUID();
        CreateModelTrimRequest request = new CreateModelTrimRequest(
                "XE", null, null, null, null, null, null);

        when(modelTrimService.addTrim(modelId, request))
                .thenThrow(new ResourceNotFoundException("Model not found: " + modelId));

        assertThatThrownBy(() -> controller.addTrim(modelId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(modelId.toString());
    }

    // ─── getTrims ─────────────────────────────────────────────────────────────

    @Test
    void getTrims_returns200WithList() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        List<ModelTrimResponse> expected = List.of(fakeResponse(trimId, modelId, "XZ+"));

        when(modelTrimService.getTrims(modelId, true)).thenReturn(expected);

        ResponseEntity<List<ModelTrimResponse>> response = controller.getTrims(modelId, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(modelTrimService).getTrims(modelId, true);
    }

    @Test
    void getTrims_returnsAllTrims_whenActiveOnlyFalse() {
        UUID modelId = UUID.randomUUID();
        List<ModelTrimResponse> expected = List.of(
                fakeResponse(UUID.randomUUID(), modelId, "XZ+"),
                fakeResponse(UUID.randomUUID(), modelId, "XE")
        );

        when(modelTrimService.getTrims(modelId, false)).thenReturn(expected);

        ResponseEntity<List<ModelTrimResponse>> response = controller.getTrims(modelId, false);

        assertThat(response.getBody()).hasSize(2);
        verify(modelTrimService).getTrims(modelId, false);
    }

    @Test
    void getTrims_returnsEmptyList_whenNoTrims() {
        UUID modelId = UUID.randomUUID();

        when(modelTrimService.getTrims(modelId, true)).thenReturn(List.of());

        ResponseEntity<List<ModelTrimResponse>> response = controller.getTrims(modelId, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getTrims_propagatesResourceNotFound_whenModelMissing() {
        UUID modelId = UUID.randomUUID();

        when(modelTrimService.getTrims(modelId, true))
                .thenThrow(new ResourceNotFoundException("Model not found: " + modelId));

        assertThatThrownBy(() -> controller.getTrims(modelId, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(modelId.toString());
    }

    // ─── getTrimById ─────────────────────────────────────────────────────────

    @Test
    void getTrimById_returns200_whenFound() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        ModelTrimResponse expected = fakeResponse(trimId, modelId, "XT");

        when(modelTrimService.getTrimById(modelId, trimId)).thenReturn(expected);

        ResponseEntity<ModelTrimResponse> response = controller.getTrimById(modelId, trimId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(modelTrimService).getTrimById(modelId, trimId);
    }

    @Test
    void getTrimById_propagatesResourceNotFound_whenTrimMissing() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();

        when(modelTrimService.getTrimById(modelId, trimId))
                .thenThrow(new ResourceNotFoundException(
                        "ModelTrim %s not found under model %s".formatted(trimId, modelId)));

        assertThatThrownBy(() -> controller.getTrimById(modelId, trimId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(trimId.toString());
    }

    // ─── updateTrim ───────────────────────────────────────────────────────────

    @Test
    void updateTrim_returns200WithUpdatedResponse() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        UpdateModelTrimRequest request = new UpdateModelTrimRequest(
                "XT", "Updated desc", null, null, null, null, null, null);
        ModelTrimResponse expected = fakeResponse(trimId, modelId, "XT");

        when(modelTrimService.updateTrim(modelId, trimId, request)).thenReturn(expected);

        ResponseEntity<ModelTrimResponse> response = controller.updateTrim(modelId, trimId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(modelTrimService).updateTrim(modelId, trimId, request);
    }

    @Test
    void updateTrim_propagatesDuplicateException() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        UpdateModelTrimRequest request = new UpdateModelTrimRequest(
                "XZ+", null, null, null, null, null, null, null);

        when(modelTrimService.updateTrim(modelId, trimId, request))
                .thenThrow(new DuplicateResourceException("Trim 'XZ+' already exists"));

        assertThatThrownBy(() -> controller.updateTrim(modelId, trimId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("XZ+");
    }

    @Test
    void updateTrim_propagatesResourceNotFound_whenTrimMissing() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        UpdateModelTrimRequest request = new UpdateModelTrimRequest(
                null, null, null, null, null, null, null, null);

        when(modelTrimService.updateTrim(modelId, trimId, request))
                .thenThrow(new ResourceNotFoundException("ModelTrim not found: " + trimId));

        assertThatThrownBy(() -> controller.updateTrim(modelId, trimId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteTrim ───────────────────────────────────────────────────────────

    @Test
    void deleteTrim_returns204NoContent() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        doNothing().when(modelTrimService).deleteTrim(modelId, trimId);

        ResponseEntity<Void> response = controller.deleteTrim(modelId, trimId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(modelTrimService).deleteTrim(modelId, trimId);
    }

    @Test
    void deleteTrim_propagatesResourceNotFound_whenTrimMissing() {
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("ModelTrim not found: " + trimId))
                .when(modelTrimService).deleteTrim(modelId, trimId);

        assertThatThrownBy(() -> controller.deleteTrim(modelId, trimId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(trimId.toString());
    }
}
