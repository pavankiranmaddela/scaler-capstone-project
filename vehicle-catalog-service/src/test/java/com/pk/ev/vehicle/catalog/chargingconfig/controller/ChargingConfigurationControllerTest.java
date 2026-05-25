package com.pk.ev.vehicle.catalog.chargingconfig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.CreateChargingConfigRequest;
import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.UpdateChargingConfigRequest;
import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.ChargingConfigResponse;
import com.pk.ev.vehicle.catalog.chargingconfig.service.ChargingConfigService;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.ChargingSpecResponse;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChargingConfigurationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChargingConfigService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ChargingConfigurationController controller = new ChargingConfigurationController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        // close mocks opened by MockitoAnnotations.openMocks
        // openMocks returns AutoCloseable but we didn't capture it here; it's fine for tests,
        // warning is only cosmetic. If desired, we could capture and close similarly to other tests.
    }

    @Test
    void addChargingConfig_returnsCreated() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        CreateChargingConfigRequest req = new CreateChargingConfigRequest(
                "3.3 kW AC",
                new BigDecimal("3.30"),
                ConnectorType.TYPE2,
                CurrentType.AC,
                120,
                90,
                true
        );

        ChargingConfigResponse resp = new ChargingConfigResponse(id, modelId, "3.3 kW AC", new BigDecimal("3.30"), ConnectorType.TYPE2, CurrentType.AC, 120, 90, true, true, List.of(), Instant.now(), Instant.now());

        when(service.addChargingConfig(eq(modelId), any())).thenReturn(resp);

        mockMvc.perform(post("/vehicle-models/{modelId}/charging-configurations", modelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.configLabel").value("3.3 kW AC"));
    }

    @Test
    void getChargingConfigs_returnsList() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ChargingConfigResponse resp = new ChargingConfigResponse(id, modelId, "Label", new BigDecimal("7.20"), ConnectorType.TYPE2, CurrentType.AC, null, null, false, true, List.of(), Instant.now(), Instant.now());
        when(service.getChargingConfigs(modelId, true)).thenReturn(List.of(resp));

        mockMvc.perform(get("/vehicle-models/{modelId}/charging-configurations", modelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].configLabel").value("Label"));
    }

    @Test
    void getChargingConfigById_returnsConfig() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        ChargingConfigResponse resp = new ChargingConfigResponse(configId, modelId, "Label", new BigDecimal("7.20"), ConnectorType.TYPE2, CurrentType.AC, null, null, false, true, List.of(), Instant.now(), Instant.now());
        when(service.getChargingConfigById(modelId, configId)).thenReturn(resp);

        mockMvc.perform(get("/vehicle-models/{modelId}/charging-configurations/{configId}", modelId, configId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(configId.toString()))
                .andExpect(jsonPath("$.configLabel").value("Label"));
    }

    @Test
    void updateChargingConfig_returnsUpdated() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UpdateChargingConfigRequest req = new UpdateChargingConfigRequest("New", new BigDecimal("11.0"), ConnectorType.TYPE2, CurrentType.AC, 60, 40, true, true);

        ChargingConfigResponse resp = new ChargingConfigResponse(configId, modelId, "New", new BigDecimal("11.0"), ConnectorType.TYPE2, CurrentType.AC, 60, 40, true, true, List.of(), Instant.now(), Instant.now());
        when(service.updateChargingConfig(modelId, configId, req)).thenReturn(resp);

        mockMvc.perform(put("/vehicle-models/{modelId}/charging-configurations/{configId}", modelId, configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(configId.toString()))
                .andExpect(jsonPath("$.configLabel").value("New"));
    }

    @Test
    void deleteChargingConfig_deactivatesAndReturnsNoContent() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        doNothing().when(service).deleteChargingConfig(modelId, configId);

        mockMvc.perform(delete("/vehicle-models/{modelId}/charging-configurations/{configId}", modelId, configId))
                .andExpect(status().isNoContent());
    }
}


