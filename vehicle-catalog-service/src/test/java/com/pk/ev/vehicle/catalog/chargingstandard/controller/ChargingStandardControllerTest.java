package com.pk.ev.vehicle.catalog.chargingstandard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.ev.vehicle.catalog.chargingstandard.dto.ChargingStandardDtos.*;
import com.pk.ev.vehicle.catalog.chargingstandard.enums.ChargingStandardType;
import com.pk.ev.vehicle.catalog.chargingstandard.service.ChargingStandardService;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChargingStandardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChargingStandardService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        ChargingStandardController controller = new ChargingStandardController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void createStandard_returnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        CreateChargingStandardRequest req = new CreateChargingStandardRequest(
                "CCS Combo 2", "CCS2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                "Europe", "IEC", "2.0", "Fast DC", null
        );

        ChargingStandardResponse resp = new ChargingStandardResponse(
                id, "CCS Combo 2", "CCS2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                "Europe", "IEC", "2.0", "Fast DC", null, false, Instant.now(), Instant.now()
        );

        when(service.createStandard(any())).thenReturn(resp);

        mockMvc.perform(post("/charging-standards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.shortCode").value("CCS2"))
                .andExpect(jsonPath("$.maxWattage").value(350000));
    }

    @Test
    void getAllStandards_returnsList() throws Exception {
        UUID id = UUID.randomUUID();
        ChargingStandardSummary summary = new ChargingStandardSummary(
                id, "Type 2", "TYPE2", ConnectorType.TYPE2, ChargingStandardType.AC, 22000, "Europe", false
        );

        PagedStandardsResponse paged = new PagedStandardsResponse(
                List.of(summary), 0, 50, 1, 1, true
        );

        when(service.getAllStandards(any())).thenReturn(paged);

        mockMvc.perform(get("/charging-standards")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllStandards_withFilters() throws Exception {
        PagedStandardsResponse paged = new PagedStandardsResponse(List.of(), 0, 50, 0, 0, true);
        when(service.getAllStandards(any())).thenReturn(paged);

        mockMvc.perform(get("/charging-standards")
                        .param("region", "Europe")
                        .param("currentType", "AC")
                        .param("connectorType", "TYPE2")
                        .param("deprecated", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getStandardById_returnsDetail() throws Exception {
        UUID id = UUID.randomUUID();
        ChargingStandardResponse resp = new ChargingStandardResponse(
                id, "CCS Combo 2", "CCS2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                "Europe", "IEC", "2.0", "Fast DC", null, false, Instant.now(), Instant.now()
        );

        when(service.getStandardById(id)).thenReturn(resp);

        mockMvc.perform(get("/charging-standards/{standardId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("CCS Combo 2"))
                .andExpect(jsonPath("$.shortCode").value("CCS2"));
    }

    @Test
    void getByShortCode_returnsDetail() throws Exception {
        UUID id = UUID.randomUUID();
        ChargingStandardResponse resp = new ChargingStandardResponse(
                id, "Type 2", "TYPE2", ConnectorType.TYPE2, ChargingStandardType.AC, 22000,
                "Europe", "IEC", "1.0", "AC Standard", null, false, Instant.now(), Instant.now()
        );

        when(service.getStandardByShortCode("TYPE2")).thenReturn(resp);

        mockMvc.perform(get("/charging-standards/short-code/{shortCode}", "TYPE2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("TYPE2"))
                .andExpect(jsonPath("$.name").value("Type 2"));
    }

    @Test
    void updateStandard_returnsUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateChargingStandardRequest req = new UpdateChargingStandardRequest(
                "New Name", null, null, null, null, null, null, null, null, null, null
        );

        ChargingStandardResponse resp = new ChargingStandardResponse(
                id, "New Name", "CCS2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                "Europe", "IEC", "2.0", "Fast DC", null, false, Instant.now(), Instant.now()
        );

        when(service.updateStandard(eq(id), any())).thenReturn(resp);

        mockMvc.perform(put("/charging-standards/{standardId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void deprecateStandard_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).deprecateStandard(id);

        mockMvc.perform(delete("/charging-standards/{standardId}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCompatibleModels_returnsModels() throws Exception {
        UUID id = UUID.randomUUID();
        CompatibleModelSummary model = new CompatibleModelSummary(
                UUID.randomUUID(), "Tesla Model 3", UUID.randomUUID(), "Model 3", 2024, "Tesla", 150000, 45
        );

        when(service.getCompatibleModels(id)).thenReturn(List.of(model));

        mockMvc.perform(get("/charging-standards/{standardId}/compatible-models", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].modelName").value("Model 3"));
    }

    @Test
    void getConnectorTypes_returnsList() throws Exception {
        ConnectorTypeMetadata ccs2 = new ConnectorTypeMetadata(
                "CCS2", "CCS Combo 2", ChargingStandardType.BOTH, "Europe/India", null
        );

        when(service.getConnectorTypes()).thenReturn(List.of(ccs2));

        mockMvc.perform(get("/connector-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("CCS2"))
                .andExpect(jsonPath("$[0].displayName").value("CCS Combo 2"));
    }

    @Test
    void getRegions_returnsRegionList() throws Exception {
        when(service.getDistinctRegions()).thenReturn(List.of("Europe", "India", "USA"));

        mockMvc.perform(get("/charging-standards/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regions.length()").value(3))
                .andExpect(jsonPath("$.regions[0]").value("Europe"));
    }

    @Test
    void getGoverningBodies_returnsBodyList() throws Exception {
        when(service.getDistinctGoverningBodies()).thenReturn(List.of("IEC", "SAE", "BIS"));

        mockMvc.perform(get("/charging-standards/governing-bodies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.governingBodies.length()").value(3))
                .andExpect(jsonPath("$.governingBodies[0]").value("IEC"));
    }
}

