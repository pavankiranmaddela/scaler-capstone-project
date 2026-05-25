package com.pk.ev.vehicle.catalog.variantlisting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.modeltrim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.variantlisting.dtos.VariantListingDto.*;
import com.pk.ev.vehicle.catalog.variantlisting.service.VariantListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VariantListingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VariantListingService variantListingService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        try (var mocks = MockitoAnnotations.openMocks(this)) {
            // captures closeable
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        VariantListingController controller = new VariantListingController(variantListingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VariantListingResponse buildResponse(UUID id, String label, VariantStatus status) {
        return new VariantListingResponse(
                id, label,
                null, null, null,
                new BigDecimal("849000"),
                LocalDate.of(2023, 1, 15),
                status,
                1200, 1,
                Instant.now(), Instant.now()
        );
    }

    private VariantListingSummary buildSummary(UUID id, String label) {
        return new VariantListingSummary(
                id, label, "XE",
                new BigDecimal("19.2"), 250,
                new BigDecimal("3.3"), null,
                new BigDecimal("849000"), VariantStatus.ACTIVE
        );
    }

    // ─── POST /variant-listings ───────────────────────────────────────────────

    @Test
    void createVariantListing_returnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        UUID trimId = UUID.randomUUID();
        UUID batteryPackId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();

        CreateVariantListingRequest req = new CreateVariantListingRequest(
                modelId, trimId, batteryPackId, configId,
                new BigDecimal("849000"),
                LocalDate.of(2023, 1, 15),
                VariantStatus.ACTIVE, 1200, 1
        );
        VariantListingResponse resp = buildResponse(id, "Tiago EV XE 19.2 kWh 3.3 kW AC", VariantStatus.ACTIVE);

        when(variantListingService.createVariantListing(any())).thenReturn(resp);

        mockMvc.perform(post("/variant-listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.displayLabel").value("Tiago EV XE 19.2 kWh 3.3 kW AC"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createVariantListing_duplicateSku_propagates409() throws Exception {
        UUID modelId = UUID.randomUUID();
        CreateVariantListingRequest req = new CreateVariantListingRequest(
                modelId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("849000"), null, null, null, null
        );

        when(variantListingService.createVariantListing(any()))
                .thenThrow(new DuplicateResourceException("SKU already exists"));

        mockMvc.perform(post("/variant-listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void createVariantListing_missingRequiredFields_returns400() throws Exception {
        // modelId is @NotNull — sending empty object
        mockMvc.perform(post("/variant-listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /variant-listings ────────────────────────────────────────────────

    @Test
    void getVariantListings_noFilters_returnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        PagedVariantResponse paged = new PagedVariantResponse(
                List.of(buildSummary(id, "Tiago EV XE 19.2 kWh 3.3 kW AC")),
                0, 20, 1L, 1, true
        );

        when(variantListingService.getVariantListings(any())).thenReturn(paged);

        mockMvc.perform(get("/variant-listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void getVariantListings_withStatusFilter_parsesStatus() throws Exception {
        PagedVariantResponse paged = new PagedVariantResponse(List.of(), 0, 20, 0L, 0, true);
        when(variantListingService.getVariantListings(any())).thenReturn(paged);

        mockMvc.perform(get("/variant-listings").param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(variantListingService).getVariantListings(
                argThat(f -> f.status() == VariantStatus.ACTIVE));
    }

    @Test
    void getVariantListings_withPriceRange_passesFilters() throws Exception {
        PagedVariantResponse paged = new PagedVariantResponse(List.of(), 0, 20, 0L, 0, true);
        when(variantListingService.getVariantListings(any())).thenReturn(paged);

        mockMvc.perform(get("/variant-listings")
                        .param("minPrice", "500000")
                        .param("maxPrice", "1500000"))
                .andExpect(status().isOk());

        verify(variantListingService).getVariantListings(
                argThat(f -> f.minPrice().compareTo(new BigDecimal("500000")) == 0
                        && f.maxPrice().compareTo(new BigDecimal("1500000")) == 0));
    }

    @Test
    void getVariantListings_withPageAndSize_passesThrough() throws Exception {
        PagedVariantResponse paged = new PagedVariantResponse(List.of(), 2, 10, 0L, 0, true);
        when(variantListingService.getVariantListings(any())).thenReturn(paged);

        mockMvc.perform(get("/variant-listings")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(variantListingService).getVariantListings(
                argThat(f -> f.page() == 2 && f.size() == 10));
    }

    @Test
    void getVariantListings_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/variant-listings").param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /variant-listings/{variantId} ───────────────────────────────────

    @Test
    void getVariantListingById_returnsVariant() throws Exception {
        UUID id = UUID.randomUUID();
        VariantListingResponse resp = buildResponse(id, "Tiago EV XZ+ 24 kWh 7.2 kW AC", VariantStatus.ACTIVE);

        when(variantListingService.getVariantListingById(id)).thenReturn(resp);

        mockMvc.perform(get("/variant-listings/{variantId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.displayLabel").value("Tiago EV XZ+ 24 kWh 7.2 kW AC"));
    }

    @Test
    void getVariantListingById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(variantListingService.getVariantListingById(id))
                .thenThrow(new ResourceNotFoundException("VariantListing not found: " + id));

        mockMvc.perform(get("/variant-listings/{variantId}", id))
                .andExpect(status().isNotFound());
    }

    // ─── GET /variant-listings/by-model/{modelId} ────────────────────────────

    @Test
    void getByModel_returnsList() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID v1 = UUID.randomUUID();
        UUID v2 = UUID.randomUUID();

        List<VariantListingResponse> list = List.of(
                buildResponse(v1, "Tiago EV XE 19.2 kWh 3.3 kW AC", VariantStatus.ACTIVE),
                buildResponse(v2, "Tiago EV XT 24 kWh 3.3 kW AC", VariantStatus.ACTIVE)
        );

        when(variantListingService.getVariantListingsByModel(modelId, null)).thenReturn(list);

        mockMvc.perform(get("/variant-listings/by-model/{modelId}", modelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(v1.toString()))
                .andExpect(jsonPath("$[1].id").value(v2.toString()));
    }

    @Test
    void getByModel_withStatusParam_passesThroughToService() throws Exception {
        UUID modelId = UUID.randomUUID();
        when(variantListingService.getVariantListingsByModel(modelId, "ACTIVE"))
                .thenReturn(List.of());

        mockMvc.perform(get("/variant-listings/by-model/{modelId}", modelId)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(variantListingService).getVariantListingsByModel(modelId, "ACTIVE");
    }

    @Test
    void getByModel_modelNotFound_returns404() throws Exception {
        UUID modelId = UUID.randomUUID();

        when(variantListingService.getVariantListingsByModel(modelId, null))
                .thenThrow(new ResourceNotFoundException("VehicleModel not found: " + modelId));

        mockMvc.perform(get("/variant-listings/by-model/{modelId}", modelId))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /variant-listings/{variantId} ───────────────────────────────────

    @Test
    void updateVariantListing_returnsUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateVariantListingRequest req = new UpdateVariantListingRequest(
                new BigDecimal("950000"), null, VariantStatus.ACTIVE, null, null
        );
        VariantListingResponse resp = buildResponse(id, "Tiago EV XE 19.2 kWh 3.3 kW AC", VariantStatus.ACTIVE);

        when(variantListingService.updateVariantListing(eq(id), any())).thenReturn(resp);

        mockMvc.perform(put("/variant-listings/{variantId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void updateVariantListing_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateVariantListingRequest req = new UpdateVariantListingRequest(
                new BigDecimal("950000"), null, null, null, null
        );

        when(variantListingService.updateVariantListing(eq(id), any()))
                .thenThrow(new ResourceNotFoundException("VariantListing not found: " + id));

        mockMvc.perform(put("/variant-listings/{variantId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateVariantListing_allNullFields_stillCallsService() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateVariantListingRequest req = new UpdateVariantListingRequest(
                null, null, null, null, null
        );
        VariantListingResponse resp = buildResponse(id, "Tiago EV XE 19.2 kWh 3.3 kW AC", VariantStatus.ACTIVE);
        when(variantListingService.updateVariantListing(eq(id), any())).thenReturn(resp);

        mockMvc.perform(put("/variant-listings/{variantId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ─── DELETE /variant-listings/{variantId} ────────────────────────────────

    @Test
    void deleteVariantListing_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(variantListingService).deleteVariantListing(id);

        mockMvc.perform(delete("/variant-listings/{variantId}", id))
                .andExpect(status().isNoContent());

        verify(variantListingService).deleteVariantListing(id);
    }

    @Test
    void deleteVariantListing_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("VariantListing not found: " + id))
                .when(variantListingService).deleteVariantListing(id);

        mockMvc.perform(delete("/variant-listings/{variantId}", id))
                .andExpect(status().isNotFound());
    }
}
