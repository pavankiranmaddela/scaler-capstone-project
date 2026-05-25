package com.pk.ev.vehicle.catalog.chargingspec.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.chargingspec.service.ChargingSpecService;
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

class ChargingSpecControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChargingSpecService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        try (var mocks = MockitoAnnotations.openMocks(this)) {
            // mocks captured for cleanup
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ChargingSpecController controller = new ChargingSpecController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void addChargingSpec_returnsCreated() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();
        UUID standardId = UUID.randomUUID();

        CreateChargingSpecRequest req = new CreateChargingSpecRequest(
                standardId, ConnectorType.TYPE2, CurrentType.AC, 7200, 3300, 90, 480, true, "AC"
        );

        ChargingSpecResponse resp = new ChargingSpecResponse(
                specId, configId, standardId, ConnectorType.TYPE2, CurrentType.AC, 7200, 3300, 90, 480, true, "AC", Instant.now(), Instant.now()
        );

        when(service.addChargingSpec(eq(modelId), eq(configId), any())).thenReturn(resp);

        mockMvc.perform(post("/vehicle-models/{modelId}/charging-configurations/{configId}/specs", modelId, configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(specId.toString()))
                .andExpect(jsonPath("$.connectorType").value("TYPE2"));
    }

    @Test
    void getChargingSpecs_returnsList() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        ChargingSpecResponse resp = new ChargingSpecResponse(
                specId, configId, null, ConnectorType.CCS2, CurrentType.DC, 11000, null, 45, 120, false, null, Instant.now(), Instant.now()
        );

        when(service.getChargingSpecs(modelId, configId)).thenReturn(List.of(resp));

        mockMvc.perform(get("/vehicle-models/{modelId}/charging-configurations/{configId}/specs", modelId, configId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(specId.toString()))
                .andExpect(jsonPath("$[0].connectorType").value("CCS2"));
    }

    @Test
    void getChargingSpecById_returnsSpec() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        ChargingSpecResponse resp = new ChargingSpecResponse(
                specId, configId, null, ConnectorType.TYPE1, CurrentType.AC, 3300, 3300, null, 480, false, null, Instant.now(), Instant.now()
        );

        when(service.getChargingSpecById(modelId, configId, specId)).thenReturn(resp);

        mockMvc.perform(get("/vehicle-models/{modelId}/charging-configurations/{configId}/specs/{specId}", modelId, configId, specId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(specId.toString()))
                .andExpect(jsonPath("$.connectorType").value("TYPE1"));
    }

    @Test
    void getChargingSpecSummary_returnsSummary() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID acSpecId = UUID.randomUUID();
        UUID dcSpecId = UUID.randomUUID();

        ChargingSpecResponse acResp = new ChargingSpecResponse(
                acSpecId, configId, null, ConnectorType.TYPE2, CurrentType.AC, 7200, null, null, null, false, null, null, null
        );
        ChargingSpecResponse dcResp = new ChargingSpecResponse(
                dcSpecId, configId, null, ConnectorType.CCS2, CurrentType.DC, 150000, null, null, null, false, null, null, null
        );

        ChargingSpecSummaryResponse summary = new ChargingSpecSummaryResponse(acResp, dcResp);

        when(service.getChargingSpecSummary(modelId, configId)).thenReturn(summary);

        mockMvc.perform(get("/vehicle-models/{modelId}/charging-configurations/{configId}/specs/summary", modelId, configId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fastestAc.id").value(acSpecId.toString()))
                .andExpect(jsonPath("$.fastestDc.id").value(dcSpecId.toString()));
    }

    @Test
    void updateChargingSpec_returnsUpdated() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        UpdateChargingSpecRequest req = new UpdateChargingSpecRequest(
                null, ConnectorType.CCS1, null, 9000, null, null, null, null, "Updated"
        );

        ChargingSpecResponse resp = new ChargingSpecResponse(
                specId, configId, null, ConnectorType.CCS1, CurrentType.AC, 9000, null, null, null, false, "Updated", Instant.now(), Instant.now()
        );

        when(service.updateChargingSpec(modelId, configId, specId, req)).thenReturn(resp);

        mockMvc.perform(put("/vehicle-models/{modelId}/charging-configurations/{configId}/specs/{specId}", modelId, configId, specId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(specId.toString()))
                .andExpect(jsonPath("$.connectorType").value("CCS1"))
                .andExpect(jsonPath("$.notes").value("Updated"));
    }

    @Test
    void deleteChargingSpec_returnsNoContent() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        doNothing().when(service).deleteChargingSpec(modelId, configId, specId);

        mockMvc.perform(delete("/vehicle-models/{modelId}/charging-configurations/{configId}/specs/{specId}", modelId, configId, specId))
                .andExpect(status().isNoContent());
    }
}

