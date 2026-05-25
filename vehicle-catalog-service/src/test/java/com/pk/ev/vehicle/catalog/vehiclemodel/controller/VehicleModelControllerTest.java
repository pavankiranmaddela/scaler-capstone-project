package com.pk.ev.vehicle.catalog.vehiclemodel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.vehiclemodel.dtos.VehicleModelDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.BodyType;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.DriveType;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ImageAngle;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.vehiclemodel.service.VehicleModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VehicleModelControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehicleModelService modelService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        try (var mocks = MockitoAnnotations.openMocks(this)) {
            // captured
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        VehicleModelController controller = new VehicleModelController(modelService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ModelResponse modelResponse(UUID id, UUID makeId, String name, ModelStatus status) {
        return new ModelResponse(
                id, makeId, "Tata Motors", name,
                2023, 1200, BodyType.HATCHBACK, 5, DriveType.FWD,
                status, List.of(),
                Instant.now(), Instant.now()
        );
    }

    private ModelSummaryResponse summary(UUID id, UUID makeId, String name) {
        return new ModelSummaryResponse(id, makeId, "Tata Motors", name, 2023,
                BodyType.HATCHBACK, ModelStatus.ACTIVE, null);
    }

    private ModelImageResponse imageResponse(UUID imageId) {
        return new ModelImageResponse(imageId, "https://cdn.example.com/img.jpg",
                true, ImageAngle.FRONT, Instant.now());
    }

    // ─── POST /vehicle-models ────────────────────────────────────────────────

    @Test
    void createModel_returnsCreated() throws Exception {
        UUID id     = UUID.randomUUID();
        UUID makeId = UUID.randomUUID();
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Tiago EV", null, 2023,
                null, null, null, 1200,
                BodyType.HATCHBACK, 5, DriveType.FWD);
        ModelResponse resp = modelResponse(id, makeId, "Tiago EV", ModelStatus.ACTIVE);

        when(modelService.createModel(any())).thenReturn(resp);

        mockMvc.perform(post("/vehicle-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Tiago EV"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createModel_duplicateModelYear_returns409() throws Exception {
        UUID makeId = UUID.randomUUID();
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Tiago EV", null, 2023, null, null, null, null, null, null, null);

        when(modelService.createModel(any()))
                .thenThrow(new DuplicateResourceException("Model 'Tiago EV' (2023) already exists"));

        mockMvc.perform(post("/vehicle-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void createModel_makeNotFound_returns404() throws Exception {
        UUID makeId = UUID.randomUUID();
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Tiago EV", null, 2023, null, null, null, null, null, null, null);

        when(modelService.createModel(any()))
                .thenThrow(new ResourceNotFoundException("Vehicle make not found with id: " + makeId));

        mockMvc.perform(post("/vehicle-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createModel_missingRequiredFields_returns400() throws Exception {
        // name and modelYear are @NotNull / @NotBlank
        mockMvc.perform(post("/vehicle-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"makeId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createModel_missingMakeId_returns400() throws Exception {
        mockMvc.perform(post("/vehicle-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tiago EV\",\"modelYear\":2023}"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /vehicle-models ─────────────────────────────────────────────────

    @Test
    void getAllModels_noFilters_returnsPage() throws Exception {
        UUID id     = UUID.randomUUID();
        UUID makeId = UUID.randomUUID();
        PagedModelsResponse paged = new PagedModelsResponse(
                List.of(summary(id, makeId, "Tiago EV")), 0, 20, 1L, 1, true);

        when(modelService.getAllModels(any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Tiago EV"));
    }

    @Test
    void getAllModels_withStatusFilter_parsesEnum() throws Exception {
        PagedModelsResponse paged = new PagedModelsResponse(List.of(), 0, 20, 0L, 0, true);
        when(modelService.getAllModels(any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-models").param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(modelService).getAllModels(argThat(f -> f.status() == ModelStatus.ACTIVE));
    }

    @Test
    void getAllModels_withMakeIdAndYearFilters_passesThrough() throws Exception {
        UUID makeId = UUID.randomUUID();
        PagedModelsResponse paged = new PagedModelsResponse(List.of(), 0, 20, 0L, 0, true);
        when(modelService.getAllModels(any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-models")
                        .param("makeId", makeId.toString())
                        .param("year", "2023"))
                .andExpect(status().isOk());

        verify(modelService).getAllModels(
                argThat(f -> makeId.equals(f.makeId()) && Integer.valueOf(2023).equals(f.year())));
    }

    @Test
    void getAllModels_withConnectorTypeFilter_parsesEnum() throws Exception {
        PagedModelsResponse paged = new PagedModelsResponse(List.of(), 0, 20, 0L, 0, true);
        when(modelService.getAllModels(any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-models").param("connectorType", "TYPE2"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllModels_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/vehicle-models").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllModels_invalidConnectorType_returns400() throws Exception {
        mockMvc.perform(get("/vehicle-models").param("connectorType", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllModels_emptyResult_returnsEmptyContent() throws Exception {
        PagedModelsResponse paged = new PagedModelsResponse(List.of(), 0, 20, 0L, 0, true);
        when(modelService.getAllModels(any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ─── GET /vehicle-models/search ──────────────────────────────────────────

    @Test
    void search_returnsResults() throws Exception {
        UUID id     = UUID.randomUUID();
        UUID makeId = UUID.randomUUID();
        SearchModelsResponse resp = new SearchModelsResponse(
                List.of(summary(id, makeId, "Tiago EV")),
                "Tiago", 0, 20, 1L, 1
        );

        when(modelService.search(eq("Tiago"), isNull(), any())).thenReturn(resp);

        mockMvc.perform(get("/vehicle-models/search").param("q", "Tiago"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Tiago"))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void search_withStatusFilter_passesThrough() throws Exception {
        SearchModelsResponse resp = new SearchModelsResponse(List.of(), "ev", 0, 20, 0L, 0);
        when(modelService.search(eq("ev"), eq("ACTIVE"), any())).thenReturn(resp);

        mockMvc.perform(get("/vehicle-models/search")
                        .param("q", "ev")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(modelService).search(eq("ev"), eq("ACTIVE"), any());
    }

    @Test
    void search_emptyResult_returnsEmptyContent() throws Exception {
        SearchModelsResponse resp = new SearchModelsResponse(List.of(), "xyz", 0, 20, 0L, 0);
        when(modelService.search(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(get("/vehicle-models/search").param("q", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ─── GET /vehicle-models/{modelId} ───────────────────────────────────────

    @Test
    void getModelById_found_returnsOk() throws Exception {
        UUID id     = UUID.randomUUID();
        UUID makeId = UUID.randomUUID();
        ModelResponse resp = modelResponse(id, makeId, "Tiago EV", ModelStatus.ACTIVE);

        when(modelService.getModelById(id)).thenReturn(resp);

        mockMvc.perform(get("/vehicle-models/{modelId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Tiago EV"));
    }

    @Test
    void getModelById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(modelService.getModelById(id))
                .thenThrow(new ResourceNotFoundException("Vehicle model not found with id: " + id));

        mockMvc.perform(get("/vehicle-models/{modelId}", id))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /vehicle-models/{modelId} ───────────────────────────────────────

    @Test
    void updateModel_returnsUpdated() throws Exception {
        UUID id     = UUID.randomUUID();
        UUID makeId = UUID.randomUUID();
        UpdateModelRequest req = new UpdateModelRequest(
                "Tiago EV Pro", null, 2024, null, null, null, null, null, null, null, null);
        ModelResponse resp = modelResponse(id, makeId, "Tiago EV Pro", ModelStatus.ACTIVE);

        when(modelService.updateModel(eq(id), any())).thenReturn(resp);

        mockMvc.perform(put("/vehicle-models/{modelId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tiago EV Pro"));
    }

    @Test
    void updateModel_duplicateNameYear_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateModelRequest req = new UpdateModelRequest(
                "Nexon EV", null, 2023, null, null, null, null, null, null, null, null);

        when(modelService.updateModel(eq(id), any()))
                .thenThrow(new DuplicateResourceException("Model 'Nexon EV' (2023) already exists"));

        mockMvc.perform(put("/vehicle-models/{modelId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateModel_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateModelRequest req = new UpdateModelRequest(
                null, null, null, null, null, null, null, null, null, null, null);

        when(modelService.updateModel(eq(id), any()))
                .thenThrow(new ResourceNotFoundException("Vehicle model not found with id: " + id));

        mockMvc.perform(put("/vehicle-models/{modelId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /vehicle-models/{modelId} ────────────────────────────────────

    @Test
    void deleteModel_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(modelService).deleteModel(id);

        mockMvc.perform(delete("/vehicle-models/{modelId}", id))
                .andExpect(status().isNoContent());

        verify(modelService).deleteModel(id);
    }

    @Test
    void deleteModel_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Vehicle model not found with id: " + id))
                .when(modelService).deleteModel(id);

        mockMvc.perform(delete("/vehicle-models/{modelId}", id))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /vehicle-models/{modelId}/status ────────────────────────────────

    @Test
    void updateModelStatus_returnsUpdated() throws Exception {
        UUID id     = UUID.randomUUID();
        UUID makeId = UUID.randomUUID();
        UpdateModelStatusRequest req = new UpdateModelStatusRequest(ModelStatus.INACTIVE);
        ModelResponse resp = modelResponse(id, makeId, "Tiago EV", ModelStatus.INACTIVE);

        when(modelService.updateModelStatus(eq(id), any())).thenReturn(resp);

        mockMvc.perform(put("/vehicle-models/{modelId}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void updateModelStatus_missingStatus_returns400() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/vehicle-models/{modelId}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateModelStatus_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateModelStatusRequest req = new UpdateModelStatusRequest(ModelStatus.DISCONTINUED);

        when(modelService.updateModelStatus(eq(id), any()))
                .thenThrow(new ResourceNotFoundException("Vehicle model not found with id: " + id));

        mockMvc.perform(put("/vehicle-models/{modelId}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ─── POST /vehicle-models/{modelId}/images ───────────────────────────────

    @Test
    void addImage_returnsCreated() throws Exception {
        UUID modelId  = UUID.randomUUID();
        UUID imageId  = UUID.randomUUID();
        UploadImageRequest req = new UploadImageRequest(
                "https://cdn.example.com/img.jpg", true, ImageAngle.FRONT);
        ModelImageResponse resp = imageResponse(imageId);

        when(modelService.addImage(eq(modelId), any())).thenReturn(resp);

        mockMvc.perform(post("/vehicle-models/{modelId}/images", modelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(imageId.toString()))
                .andExpect(jsonPath("$.isPrimary").value(true))
                .andExpect(jsonPath("$.angle").value("FRONT"));
    }

    @Test
    void addImage_missingUrl_returns400() throws Exception {
        UUID modelId = UUID.randomUUID();

        mockMvc.perform(post("/vehicle-models/{modelId}/images", modelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPrimary\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addImage_modelNotFound_returns404() throws Exception {
        UUID modelId = UUID.randomUUID();
        UploadImageRequest req = new UploadImageRequest(
                "https://cdn.example.com/img.jpg", false, null);

        when(modelService.addImage(eq(modelId), any()))
                .thenThrow(new ResourceNotFoundException("Vehicle model not found with id: " + modelId));

        mockMvc.perform(post("/vehicle-models/{modelId}/images", modelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ─── GET /vehicle-models/{modelId}/images ────────────────────────────────

    @Test
    void getImages_returnsList() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID img1    = UUID.randomUUID();
        UUID img2    = UUID.randomUUID();

        when(modelService.getImages(modelId))
                .thenReturn(List.of(imageResponse(img1), imageResponse(img2)));

        mockMvc.perform(get("/vehicle-models/{modelId}/images", modelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(img1.toString()));
    }

    @Test
    void getImages_noImages_returnsEmptyList() throws Exception {
        UUID modelId = UUID.randomUUID();
        when(modelService.getImages(modelId)).thenReturn(List.of());

        mockMvc.perform(get("/vehicle-models/{modelId}/images", modelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getImages_modelNotFound_returns404() throws Exception {
        UUID modelId = UUID.randomUUID();
        when(modelService.getImages(modelId))
                .thenThrow(new ResourceNotFoundException("Vehicle model not found with id: " + modelId));

        mockMvc.perform(get("/vehicle-models/{modelId}/images", modelId))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /vehicle-models/{modelId}/images/{imageId} ───────────────────

    @Test
    void deleteImage_returnsNoContent() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        doNothing().when(modelService).deleteImage(modelId, imageId);

        mockMvc.perform(delete("/vehicle-models/{modelId}/images/{imageId}", modelId, imageId))
                .andExpect(status().isNoContent());

        verify(modelService).deleteImage(modelId, imageId);
    }

    @Test
    void deleteImage_notFound_returns404() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException(
                "Image not found with id=%s for model id=%s".formatted(imageId, modelId)))
                .when(modelService).deleteImage(modelId, imageId);

        mockMvc.perform(delete("/vehicle-models/{modelId}/images/{imageId}", modelId, imageId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteImage_modelNotFound_returns404() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Vehicle model not found with id: " + modelId))
                .when(modelService).deleteImage(modelId, imageId);

        mockMvc.perform(delete("/vehicle-models/{modelId}/images/{imageId}", modelId, imageId))
                .andExpect(status().isNotFound());
    }
}
