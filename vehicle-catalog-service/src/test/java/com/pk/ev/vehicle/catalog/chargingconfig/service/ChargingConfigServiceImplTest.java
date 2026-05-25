package com.pk.ev.vehicle.catalog.chargingconfig.service;

import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.ChargingConfigResponse;
import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.CreateChargingConfigRequest;
import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.UpdateChargingConfigRequest;
import com.pk.ev.vehicle.catalog.chargingconfig.mapper.ChargingConfigMapper;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingconfig.repository.ChargingConfigurationRepository;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.vehiclemodel.service.VehicleModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ChargingConfigServiceImplTest {

    @Mock
    private VehicleModelService modelService;
    @Mock
    private ChargingConfigurationRepository repository;
    @Mock
    private ChargingConfigMapper mapper;

    private ChargingConfigServiceImpl service;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new ChargingConfigServiceImpl(modelService, repository, mapper);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void addChargingConfig_createsWhenNotDuplicate() {
        UUID modelId = UUID.randomUUID();
        VehicleModel model = new VehicleModel();
        model.setId(modelId);

        CreateChargingConfigRequest req = new CreateChargingConfigRequest(
                "Label",
                new BigDecimal("3.30"),
                null,
                null,
                100,
                70,
                true
        );

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(repository.existsByModelIdAndOnboardChargerKwAndConnectorType(eq(modelId), any(), any())).thenReturn(false);

        ChargingConfiguration toSave = new ChargingConfiguration();
        ChargingConfiguration saved = new ChargingConfiguration();
        saved.setId(UUID.randomUUID());

        when(mapper.toChargingConfigEntity(req, model)).thenReturn(toSave);
        when(repository.save(toSave)).thenReturn(saved);
        ChargingConfigResponse resp = new ChargingConfigResponse(saved.getId(), modelId, "Label", new BigDecimal("3.30"), null, null, 100, 70, true, true, List.of(), null, null);
        when(mapper.toChargingConfigResponse(saved)).thenReturn(resp);

        ChargingConfigResponse out = service.addChargingConfig(modelId, req);

        assertThat(out).isSameAs(resp);
        verify(repository).save(toSave);
    }

    @Test
    void addChargingConfig_throwsOnDuplicate() {
        UUID modelId = UUID.randomUUID();
        when(modelService.findModelOrThrow(modelId)).thenReturn(new VehicleModel());
        when(repository.existsByModelIdAndOnboardChargerKwAndConnectorType(eq(modelId), any(), any())).thenReturn(true);

        CreateChargingConfigRequest req = new CreateChargingConfigRequest("L", new BigDecimal("1.0"), null, null, null, null, null);

        assertThatThrownBy(() -> service.addChargingConfig(modelId, req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getChargingConfigs_returnsActiveOrAll() {
        UUID modelId = UUID.randomUUID();
        when(modelService.findModelOrThrow(modelId)).thenReturn(new VehicleModel());
        ChargingConfiguration c1 = new ChargingConfiguration();
        c1.setId(UUID.randomUUID());
        when(repository.findByModelIdAndIsActiveTrue(modelId)).thenReturn(List.of(c1));
        when(repository.findByModelId(modelId)).thenReturn(List.of(c1));
        when(mapper.toChargingConfigResponse(c1)).thenReturn(new ChargingConfigResponse(c1.getId(), modelId, "L", new BigDecimal("3.3"), null, null, null, null, false, true, List.of(), null, null));

        List<ChargingConfigResponse> active = service.getChargingConfigs(modelId, true);
        assertThat(active).hasSize(1);

        List<ChargingConfigResponse> all = service.getChargingConfigs(modelId, false);
        assertThat(all).hasSize(1);
    }

    @Test
    void getChargingConfigById_returnsWhenModelMatches() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        VehicleModel model = new VehicleModel(); model.setId(modelId);
        ChargingConfiguration cfg = new ChargingConfiguration(); cfg.setId(configId); cfg.setModel(model);

        when(repository.findByIdWithSpecs(configId)).thenReturn(Optional.of(cfg));
        when(mapper.toChargingConfigResponse(cfg)).thenReturn(new ChargingConfigResponse(configId, modelId, "L", new BigDecimal("3.3"), null, null, null, null, false, true, List.of(), null, null));

        ChargingConfigResponse out = service.getChargingConfigById(modelId, configId);
        assertThat(out).isNotNull();
        assertThat(out.id()).isEqualTo(configId);
    }

    @Test
    void getChargingConfigById_throwsWhenModelMismatchOrNotFound() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        VehicleModel otherModel = new VehicleModel(); otherModel.setId(UUID.randomUUID());
        ChargingConfiguration cfg = new ChargingConfiguration(); cfg.setId(configId); cfg.setModel(otherModel);

        when(repository.findByIdWithSpecs(configId)).thenReturn(Optional.of(cfg));

        assertThatThrownBy(() -> service.getChargingConfigById(modelId, configId)).isInstanceOf(ResourceNotFoundException.class);

        when(repository.findByIdWithSpecs(configId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getChargingConfigById(modelId, configId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateChargingConfig_updatesAndReturns() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        ChargingConfiguration cfg = new ChargingConfiguration(); cfg.setId(configId);

        when(repository.findByIdAndModelId(configId, modelId)).thenReturn(Optional.of(cfg));
        when(repository.save(cfg)).thenReturn(cfg);
        when(mapper.toChargingConfigResponse(cfg)).thenReturn(new ChargingConfigResponse(configId, modelId, "L", new BigDecimal("3.3"), null, null, null, null, false, true, List.of(), null, null));

        ChargingConfigResponse out = service.updateChargingConfig(modelId, configId, new UpdateChargingConfigRequest(null, null, null, null, null, null, null, null));
        assertThat(out).isNotNull();
        verify(repository).save(cfg);
    }

    @Test
    void deleteChargingConfig_setsInactiveAndSaves() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        ChargingConfiguration cfg = new ChargingConfiguration(); cfg.setId(configId); cfg.setIsActive(true);

        when(repository.findByIdAndModelId(configId, modelId)).thenReturn(Optional.of(cfg));
        when(repository.save(cfg)).thenReturn(cfg);

        service.deleteChargingConfig(modelId, configId);

        ArgumentCaptor<ChargingConfiguration> captor = ArgumentCaptor.forClass(ChargingConfiguration.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isFalse();
    }
}

