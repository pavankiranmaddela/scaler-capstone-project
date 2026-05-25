package com.pk.ev.vehicle.catalog.chargingspec.service;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingconfig.service.ChargingConfigService;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.chargingspec.mapper.ChargingSpecMapper;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.chargingspec.repository.VehicleChargingSpecRepository;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ChargingSpecServiceImplTest {

    @Mock
    private ChargingConfigService configService;
    @Mock
    private VehicleChargingSpecRepository repository;
    @Mock
    private ChargingSpecMapper mapper;

    private ChargingSpecServiceImpl service;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new ChargingSpecServiceImpl(configService, repository, mapper);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void addChargingSpec_createsWhenNotDuplicate() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        ChargingConfiguration config = new ChargingConfiguration();
        config.setId(configId);

        CreateChargingSpecRequest req = new CreateChargingSpecRequest(
                UUID.randomUUID(), ConnectorType.TYPE2, CurrentType.AC, 7200, 3300, 90, 480, true, "AC Standard"
        );

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(config);
        when(repository.existsByChargingConfigurationIdAndConnectorType(configId, ConnectorType.TYPE2)).thenReturn(false);

        VehicleChargingSpec toSave = new VehicleChargingSpec();
        VehicleChargingSpec saved = new VehicleChargingSpec();
        saved.setId(UUID.randomUUID());

        when(mapper.toChargingSpecEntity(req, config)).thenReturn(toSave);
        when(repository.save(toSave)).thenReturn(saved);
        ChargingSpecResponse resp = new ChargingSpecResponse(
                saved.getId(), configId, null, ConnectorType.TYPE2, CurrentType.AC, 7200, 3300, 90, 480, true, "AC Standard", null, null
        );
        when(mapper.toChargingSpecResponse(saved)).thenReturn(resp);

        ChargingSpecResponse result = service.addChargingSpec(modelId, configId, req);

        assertThat(result).isSameAs(resp);
        verify(repository).save(toSave);
    }

    @Test
    void addChargingSpec_throwsOnDuplicate() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();

        CreateChargingSpecRequest req = new CreateChargingSpecRequest(
                null, ConnectorType.TYPE2, CurrentType.AC, 7200, null, null, null, null, null
        );

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(new ChargingConfiguration());
        when(repository.existsByChargingConfigurationIdAndConnectorType(configId, ConnectorType.TYPE2)).thenReturn(true);

        assertThatThrownBy(() -> service.addChargingSpec(modelId, configId, req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getChargingSpecs_returnsList() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(new ChargingConfiguration());

        VehicleChargingSpec spec1 = new VehicleChargingSpec();
        spec1.setId(UUID.randomUUID());
        when(repository.findByChargingConfigurationId(configId)).thenReturn(List.of(spec1));

        ChargingSpecResponse resp = new ChargingSpecResponse(
                spec1.getId(), configId, null, ConnectorType.TYPE2, CurrentType.AC, 3300, 3300, null, 480, false, null, null, null
        );
        when(mapper.toChargingSpecResponse(spec1)).thenReturn(resp);

        List<ChargingSpecResponse> result = service.getChargingSpecs(modelId, configId);

        assertThat(result).hasSize(1);
        assertThat(result).element(0).isSameAs(resp);
    }

    @Test
    void getChargingSpecById_returnsWhenFound() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(new ChargingConfiguration());

        VehicleChargingSpec spec = new VehicleChargingSpec();
        spec.setId(specId);
        when(repository.findByIdAndChargingConfigurationId(specId, configId)).thenReturn(Optional.of(spec));

        ChargingSpecResponse resp = new ChargingSpecResponse(
                specId, configId, null, ConnectorType.CCS2, CurrentType.DC, 11000, null, 45, 120, false, null, null, null
        );
        when(mapper.toChargingSpecResponse(spec)).thenReturn(resp);

        ChargingSpecResponse result = service.getChargingSpecById(modelId, configId, specId);

        assertThat(result).isSameAs(resp);
    }

    @Test
    void getChargingSpecById_throwsWhenNotFound() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(new ChargingConfiguration());
        when(repository.findByIdAndChargingConfigurationId(specId, configId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getChargingSpecById(modelId, configId, specId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ChargingSpec not found");
    }

    @Test
    void updateChargingSpec_updatesAndReturns() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(new ChargingConfiguration());

        VehicleChargingSpec spec = new VehicleChargingSpec();
        spec.setId(specId);
        when(repository.findByIdAndChargingConfigurationId(specId, configId)).thenReturn(Optional.of(spec));

        UpdateChargingSpecRequest req = new UpdateChargingSpecRequest(
                null, ConnectorType.CCS1, null, 9000, null, null, null, null, "Updated"
        );

        when(repository.save(spec)).thenReturn(spec);
        ChargingSpecResponse resp = new ChargingSpecResponse(
                specId, configId, null, ConnectorType.CCS1, CurrentType.AC, 9000, null, null, null, false, "Updated", null, null
        );
        when(mapper.toChargingSpecResponse(spec)).thenReturn(resp);

        ChargingSpecResponse result = service.updateChargingSpec(modelId, configId, specId, req);

        assertThat(result).isSameAs(resp);
        verify(mapper).applyChargingSpecUpdate(req, spec);
        verify(repository).save(spec);
    }

    @Test
    void deleteChargingSpec_deletesSpec() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(new ChargingConfiguration());

        VehicleChargingSpec spec = new VehicleChargingSpec();
        spec.setId(specId);
        when(repository.findByIdAndChargingConfigurationId(specId, configId)).thenReturn(Optional.of(spec));

        service.deleteChargingSpec(modelId, configId, specId);

        verify(repository).delete(spec);
    }

    @Test
    void getChargingSpecSummary_returnsFastestAcAndDc() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(new ChargingConfiguration());

        VehicleChargingSpec acSpec = new VehicleChargingSpec();
        acSpec.setId(UUID.randomUUID());
        acSpec.setCurrentType(CurrentType.AC);
        acSpec.setMaxAcceptedWattage(7200);

        VehicleChargingSpec dcSpec = new VehicleChargingSpec();
        dcSpec.setId(UUID.randomUUID());
        dcSpec.setCurrentType(CurrentType.DC);
        dcSpec.setMaxAcceptedWattage(150000);

        when(repository.findFastestAcSpec(configId)).thenReturn(Optional.of(acSpec));
        when(repository.findFastestDcSpec(configId)).thenReturn(Optional.of(dcSpec));

        ChargingSpecResponse acResp = new ChargingSpecResponse(
                acSpec.getId(), configId, null, ConnectorType.TYPE2, CurrentType.AC, 7200, null, null, null, false, null, null, null
        );
        ChargingSpecResponse dcResp = new ChargingSpecResponse(
                dcSpec.getId(), configId, null, ConnectorType.CCS2, CurrentType.DC, 150000, null, null, null, false, null, null, null
        );

        when(mapper.toChargingSpecResponse(acSpec)).thenReturn(acResp);
        when(mapper.toChargingSpecResponse(dcSpec)).thenReturn(dcResp);

        ChargingSpecSummaryResponse result = service.getChargingSpecSummary(modelId, configId);

        assertThat(result).isNotNull();
        assertThat(result.fastestAc()).isSameAs(acResp);
        assertThat(result.fastestDc()).isSameAs(dcResp);
    }

    @Test
    void getChargingSpecSummary_handlesAbsentSpecs() {
        UUID modelId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();

        when(configService.findChargingConfigOrThrow(modelId, configId)).thenReturn(new ChargingConfiguration());
        when(repository.findFastestAcSpec(configId)).thenReturn(Optional.empty());
        when(repository.findFastestDcSpec(configId)).thenReturn(Optional.empty());

        ChargingSpecSummaryResponse result = service.getChargingSpecSummary(modelId, configId);

        assertThat(result).isNotNull();
        assertThat(result.fastestAc()).isNull();
        assertThat(result.fastestDc()).isNull();
    }
}

