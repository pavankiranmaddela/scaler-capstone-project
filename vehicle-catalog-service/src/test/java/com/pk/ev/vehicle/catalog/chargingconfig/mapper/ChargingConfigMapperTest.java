package com.pk.ev.vehicle.catalog.chargingconfig.mapper;

import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.*;
import com.pk.ev.vehicle.catalog.chargingconfig.mapper.ChargingConfigMapper;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.ChargingSpecResponse;
import com.pk.ev.vehicle.catalog.chargingspec.mapper.ChargingSpecMapper;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ChargingConfigMapperTest {

    @Mock
    private ChargingSpecMapper chargingSpecMapper;

    private ChargingConfigMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ChargingConfigMapper(chargingSpecMapper);
    }

    @Test
    void toChargingConfigResponse_mapsAllFieldsAndSpecs() {
        UUID modelId = UUID.randomUUID();
        VehicleModel model = new VehicleModel();
        model.setId(modelId);

        ChargingConfiguration config = ChargingConfiguration.builder()
                .id(UUID.randomUUID())
                .model(model)
                .configLabel("3.3 kW AC")
                .onboardChargerKw(new BigDecimal("3.30"))
                .connectorType(null)
                .currentType(null)
                .chargeTimeFullMinutes(120)
                .chargeTime10To80Minutes(90)
                .cableIncluded(true)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // prepare spec list and mapping (ChargingSpecResponse requires 13 fields)
        ChargingSpecResponse specResp = new ChargingSpecResponse(
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null
        );
        VehicleChargingSpec specEntity = new VehicleChargingSpec();
        config.getChargingSpecs().add(specEntity); // underlying element used for mapping
        when(chargingSpecMapper.toChargingSpecResponse(specEntity)).thenReturn(specResp);

        ChargingConfigResponse resp = mapper.toChargingConfigResponse(config);

        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(config.getId());
        assertThat(resp.modelId()).isEqualTo(modelId);
        assertThat(resp.configLabel()).isEqualTo("3.3 kW AC");
        assertThat(resp.onboardChargerKw()).isEqualByComparingTo(new BigDecimal("3.30"));
        assertThat(resp.chargeTimeFullMinutes()).isEqualTo(120);
        assertThat(resp.cableIncluded()).isTrue();
        assertThat(resp.chargingSpecs()).hasSize(1);
        assertThat(resp.createdAt()).isNotNull();
    }

    @Test
    void toChargingConfigSummary_returnsSubset() {
        ChargingConfiguration config = ChargingConfiguration.builder()
                .id(UUID.randomUUID())
                .configLabel("Label")
                .onboardChargerKw(new BigDecimal("7.20"))
                .connectorType(null)
                .currentType(null)
                .build();

        ChargingConfigSummary s = mapper.toChargingConfigSummary(config);
        assertThat(s.id()).isEqualTo(config.getId());
        assertThat(s.configLabel()).isEqualTo("Label");
        assertThat(s.onboardChargerKw()).isEqualByComparingTo(new BigDecimal("7.20"));
    }

    @Test
    void toChargingConfigEntity_setsDefaultsAndFields() {
        VehicleModel model = new VehicleModel();
        model.setId(UUID.randomUUID());

        CreateChargingConfigRequest req = new CreateChargingConfigRequest(
                "Label",
                new BigDecimal("11.00"),
                null,
                null,
                60,
                40,
                null
        );

        ChargingConfiguration entity = mapper.toChargingConfigEntity(req, model);
        assertThat(entity.getModel()).isSameAs(model);
        assertThat(entity.getConfigLabel()).isEqualTo("Label");
        assertThat(entity.getOnboardChargerKw()).isEqualByComparingTo(new BigDecimal("11.00"));
        assertThat(entity.getCableIncluded()).isFalse(); // default when null
    }

    @Test
    void applyChargingConfigUpdate_updatesOnlyProvidedFields() {
        ChargingConfiguration config = ChargingConfiguration.builder()
                .configLabel("Old")
                .onboardChargerKw(new BigDecimal("3.30"))
                .chargeTimeFullMinutes(100)
                .cableIncluded(false)
                .isActive(true)
                .build();

        UpdateChargingConfigRequest req = new UpdateChargingConfigRequest(
                "New",
                new BigDecimal("7.20"),
                null,
                null,
                55,
                null,
                true,
                false
        );

        mapper.applyChargingConfigUpdate(req, config);

        assertThat(config.getConfigLabel()).isEqualTo("New");
        assertThat(config.getOnboardChargerKw()).isEqualByComparingTo(new BigDecimal("7.20"));
        assertThat(config.getChargeTimeFullMinutes()).isEqualTo(55);
        assertThat(config.getCableIncluded()).isTrue();
        assertThat(config.getIsActive()).isFalse();
    }
}

