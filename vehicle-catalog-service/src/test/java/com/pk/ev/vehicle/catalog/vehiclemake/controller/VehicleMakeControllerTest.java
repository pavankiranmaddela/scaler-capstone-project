package com.pk.ev.vehicle.catalog.vehiclemake.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.vehiclemake.dto.VehicleMakeDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemake.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.vehiclemake.mapper.VehicleMakeMapper;
import com.pk.ev.vehicle.catalog.vehiclemake.service.VehicleMakeService;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
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

class VehicleMakeControllerTest {

    private MockMvc mockMvc;

    @Mock private VehicleMakeService makeService;
    @Mock private VehicleMakeMapper  mapper;

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
        VehicleMakeController controller = new VehicleMakeController(makeService, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private MakeResponse makeResponse(UUID id, String name, MakeStatus status) {
        return new MakeResponse(id, name, name.toLowerCase(), "IN",
                null, null, status, Instant.now(), Instant.now());
    }

    private MakeSummaryResponse summary(UUID id, String name) {
        return new MakeSummaryResponse(id, name, name.toLowerCase(), "IN", null, MakeStatus.ACTIVE);
    }

    // ─── POST /vehicle-makes ─────────────────────────────────────────────────

    @Test
    void createMake_returnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        CreateMakeRequest req = new CreateMakeRequest("Tata Motors", "IN", null, null);
        MakeResponse resp = makeResponse(id, "Tata Motors", MakeStatus.ACTIVE);

        when(makeService.createMake(any())).thenReturn(resp);

        mockMvc.perform(post("/vehicle-makes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Tata Motors"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createMake_withLogoAndWebsite_returnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        CreateMakeRequest req = new CreateMakeRequest(
                "Mahindra", "IN", "https://logo.png", "https://mahindra.com");
        MakeResponse resp = new MakeResponse(
                id, "Mahindra", "mahindra", "IN",
                "https://logo.png", "https://mahindra.com",
                MakeStatus.ACTIVE, Instant.now(), Instant.now());

        when(makeService.createMake(any())).thenReturn(resp);

        mockMvc.perform(post("/vehicle-makes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logoUrl").value("https://logo.png"))
                .andExpect(jsonPath("$.websiteUrl").value("https://mahindra.com"));
    }

    @Test
    void createMake_duplicateName_returns409() throws Exception {
        CreateMakeRequest req = new CreateMakeRequest("Tata Motors", "IN", null, null);

        when(makeService.createMake(any()))
                .thenThrow(new DuplicateResourceException("A vehicle make with name 'Tata Motors' already exists"));

        mockMvc.perform(post("/vehicle-makes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void createMake_blankName_returns400() throws Exception {
        mockMvc.perform(post("/vehicle-makes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"countryOfOrigin\":\"IN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMake_invalidCountryCode_returns400() throws Exception {
        // lowercase is invalid per @Pattern([A-Z]{2})
        mockMvc.perform(post("/vehicle-makes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tata\",\"countryOfOrigin\":\"in\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMake_missingCountry_returns400() throws Exception {
        mockMvc.perform(post("/vehicle-makes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tata\"}"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /vehicle-makes ──────────────────────────────────────────────────

    @Test
    void getAllMakes_noFilters_returnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        PagedMakesResponse paged = new PagedMakesResponse(
                List.of(summary(id, "Tata Motors")), 0, 20, 1L, 1, true);

        when(makeService.getAllMakes(isNull(), isNull(), any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-makes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void getAllMakes_withStatusFilter_passesToService() throws Exception {
        PagedMakesResponse paged = new PagedMakesResponse(List.of(), 0, 20, 0L, 0, true);
        when(makeService.getAllMakes(eq(MakeStatus.ACTIVE), isNull(), any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-makes").param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(makeService).getAllMakes(eq(MakeStatus.ACTIVE), isNull(), any());
    }

    @Test
    void getAllMakes_withCountryFilter_passesToService() throws Exception {
        PagedMakesResponse paged = new PagedMakesResponse(List.of(), 0, 20, 0L, 0, true);
        when(makeService.getAllMakes(isNull(), eq("IN"), any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-makes").param("country", "IN"))
                .andExpect(status().isOk());

        verify(makeService).getAllMakes(isNull(), eq("IN"), any());
    }

    @Test
    void getAllMakes_withPageAndSize_passesPageable() throws Exception {
        PagedMakesResponse paged = new PagedMakesResponse(List.of(), 1, 5, 0L, 0, true);
        when(makeService.getAllMakes(any(), any(), any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-makes")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllMakes_emptyResult_returnsEmptyContent() throws Exception {
        PagedMakesResponse paged = new PagedMakesResponse(List.of(), 0, 20, 0L, 0, true);
        when(makeService.getAllMakes(any(), any(), any())).thenReturn(paged);

        mockMvc.perform(get("/vehicle-makes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /vehicle-makes/{makeId} ─────────────────────────────────────────

    @Test
    void getMakeById_found_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        MakeResponse resp = makeResponse(id, "Tata Motors", MakeStatus.ACTIVE);

        when(makeService.getMakeById(id)).thenReturn(resp);

        mockMvc.perform(get("/vehicle-makes/{makeId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Tata Motors"));
    }

    @Test
    void getMakeById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(makeService.getMakeById(id))
                .thenThrow(new ResourceNotFoundException("Vehicle make not found with id: " + id));

        mockMvc.perform(get("/vehicle-makes/{makeId}", id))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /vehicle-makes/{makeId} ─────────────────────────────────────────

    @Test
    void updateMake_returnsUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMakeRequest req = new UpdateMakeRequest("Tata EV", null, null, null, null);
        MakeResponse resp = makeResponse(id, "Tata EV", MakeStatus.ACTIVE);

        when(makeService.updateMake(eq(id), any())).thenReturn(resp);

        mockMvc.perform(put("/vehicle-makes/{makeId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tata EV"));
    }

    @Test
    void updateMake_duplicateName_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMakeRequest req = new UpdateMakeRequest("Mahindra", null, null, null, null);

        when(makeService.updateMake(eq(id), any()))
                .thenThrow(new DuplicateResourceException("A vehicle make with name 'Mahindra' already exists"));

        mockMvc.perform(put("/vehicle-makes/{makeId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateMake_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMakeRequest req = new UpdateMakeRequest(null, null, null, null, MakeStatus.INACTIVE);

        when(makeService.updateMake(eq(id), any()))
                .thenThrow(new ResourceNotFoundException("Vehicle make not found with id: " + id));

        mockMvc.perform(put("/vehicle-makes/{makeId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMake_allNullFields_stillCallsService() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMakeRequest req = new UpdateMakeRequest(null, null, null, null, null);
        MakeResponse resp = makeResponse(id, "Tata Motors", MakeStatus.ACTIVE);
        when(makeService.updateMake(eq(id), any())).thenReturn(resp);

        mockMvc.perform(put("/vehicle-makes/{makeId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ─── DELETE /vehicle-makes/{makeId} ──────────────────────────────────────

    @Test
    void deleteMake_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(makeService).deleteMake(id);

        mockMvc.perform(delete("/vehicle-makes/{makeId}", id))
                .andExpect(status().isNoContent());

        verify(makeService).deleteMake(id);
    }

    @Test
    void deleteMake_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Vehicle make not found with id: " + id))
                .when(makeService).deleteMake(id);

        mockMvc.perform(delete("/vehicle-makes/{makeId}", id))
                .andExpect(status().isNotFound());
    }

    // ─── GET /vehicle-makes/{makeId}/models ──────────────────────────────────

    @Test
    void getModelsByMake_returnsList() throws Exception {
        UUID makeId = UUID.randomUUID();
        VehicleModel m1 = new VehicleModel();
        m1.setId(UUID.randomUUID());
        m1.setName("Tiago EV");

        when(makeService.getModelsByMake(makeId)).thenReturn(List.of(m1));

        mockMvc.perform(get("/vehicle-makes/{makeId}/models", makeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Tiago EV"));
    }

    @Test
    void getModelsByMake_notFound_returns404() throws Exception {
        UUID makeId = UUID.randomUUID();
        when(makeService.getModelsByMake(makeId))
                .thenThrow(new ResourceNotFoundException("Vehicle make not found with id: " + makeId));

        mockMvc.perform(get("/vehicle-makes/{makeId}/models", makeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getModelsByMake_emptyModels_returnsEmptyList() throws Exception {
        UUID makeId = UUID.randomUUID();
        when(makeService.getModelsByMake(makeId)).thenReturn(List.of());

        mockMvc.perform(get("/vehicle-makes/{makeId}/models", makeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── POST /vehicle-makes/{makeId}/regions ────────────────────────────────

    @Test
    void associateRegions_returnsCreatedWithRegionList() throws Exception {
        UUID makeId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        AssociateRegionsRequest req = new AssociateRegionsRequest(
                List.of(new AssociateRegionsRequest.RegionEntry("IN", 2020))
        );
        RegionResponse regionResp = new RegionResponse(regionId, "IN", 2020);

        when(makeService.associateRegions(eq(makeId), any())).thenReturn(List.of(regionResp));

        mockMvc.perform(post("/vehicle-makes/{makeId}/regions", makeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].regionCode").value("IN"))
                .andExpect(jsonPath("$[0].launchYear").value(2020));
    }

    @Test
    void associateRegions_multipleRegions_returnsAll() throws Exception {
        UUID makeId = UUID.randomUUID();
        AssociateRegionsRequest req = new AssociateRegionsRequest(List.of(
                new AssociateRegionsRequest.RegionEntry("IN", 2020),
                new AssociateRegionsRequest.RegionEntry("DE", 2022)
        ));
        List<RegionResponse> regions = List.of(
                new RegionResponse(UUID.randomUUID(), "IN", 2020),
                new RegionResponse(UUID.randomUUID(), "DE", 2022)
        );

        when(makeService.associateRegions(eq(makeId), any())).thenReturn(regions);

        mockMvc.perform(post("/vehicle-makes/{makeId}/regions", makeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void associateRegions_emptyList_returns400() throws Exception {
        UUID makeId = UUID.randomUUID();
        mockMvc.perform(post("/vehicle-makes/{makeId}/regions", makeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"regions\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void associateRegions_makeNotFound_returns404() throws Exception {
        UUID makeId = UUID.randomUUID();
        AssociateRegionsRequest req = new AssociateRegionsRequest(
                List.of(new AssociateRegionsRequest.RegionEntry("IN", 2020))
        );
        when(makeService.associateRegions(eq(makeId), any()))
                .thenThrow(new ResourceNotFoundException("Vehicle make not found with id: " + makeId));

        mockMvc.perform(post("/vehicle-makes/{makeId}/regions", makeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ─── GET /vehicle-makes/{makeId}/regions ─────────────────────────────────

    @Test
    void getRegionsByMake_returnsList() throws Exception {
        UUID makeId = UUID.randomUUID();
        RegionResponse regionResp = new RegionResponse(UUID.randomUUID(), "IN", 2020);

        when(makeService.getRegionsByMake(makeId)).thenReturn(List.of(regionResp));

        mockMvc.perform(get("/vehicle-makes/{makeId}/regions", makeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].regionCode").value("IN"));
    }

    @Test
    void getRegionsByMake_noRegions_returnsEmpty() throws Exception {
        UUID makeId = UUID.randomUUID();
        when(makeService.getRegionsByMake(makeId)).thenReturn(List.of());

        mockMvc.perform(get("/vehicle-makes/{makeId}/regions", makeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getRegionsByMake_makeNotFound_returns404() throws Exception {
        UUID makeId = UUID.randomUUID();
        when(makeService.getRegionsByMake(makeId))
                .thenThrow(new ResourceNotFoundException("Vehicle make not found with id: " + makeId));

        mockMvc.perform(get("/vehicle-makes/{makeId}/regions", makeId))
                .andExpect(status().isNotFound());
    }
}
