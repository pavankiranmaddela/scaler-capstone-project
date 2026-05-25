package com.pk.ev.vehicle.catalog.chargingspec.mapper;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChargingSpecMapperTest {

    private final ChargingSpecMapper mapper = new ChargingSpecMapper();

    @Test
    void toChargingSpecResponse_mapsAllFields() {
        ChargingConfiguration config = new ChargingConfiguration();
        config.setId(UUID.randomUUID());

        VehicleChargingSpec spec = VehicleChargingSpec.builder()
                .id(UUID.randomUUID())
                .chargingConfiguration(config)
                .chargingStandardId(UUID.randomUUID())
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .maxAcceptedWattage(7200)
                .onboardChargerWattage(3300)
                .chargeTime10To80Pct(90)
                .chargeTimeToFullMinutes(480)
                .cableIncluded(true)
                .notes("Standard AC")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ChargingSpecResponse resp = mapper.toChargingSpecResponse(spec);

        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(spec.getId());
        assertThat(resp.chargingConfigurationId()).isEqualTo(config.getId());
        assertThat(resp.chargingStandardId()).isEqualTo(spec.getChargingStandardId());
        assertThat(resp.connectorType()).isEqualTo(ConnectorType.TYPE2);
        assertThat(resp.currentType()).isEqualTo(CurrentType.AC);
        assertThat(resp.maxAcceptedWattage()).isEqualTo(7200);
        assertThat(resp.onboardChargerWattage()).isEqualTo(3300);
        assertThat(resp.chargeTime10To80Pct()).isEqualTo(90);
        assertThat(resp.chargeTimeToFullMinutes()).isEqualTo(480);
        assertThat(resp.cableIncluded()).isTrue();
        assertThat(resp.notes()).isEqualTo("Standard AC");
        assertThat(resp.createdAt()).isNotNull();
        assertThat(resp.updatedAt()).isNotNull();
    }

    @Test
    void toChargingSpecEntity_setsAllFieldsAndDefaults() {
        ChargingConfiguration config = new ChargingConfiguration();
        config.setId(UUID.randomUUID());

        CreateChargingSpecRequest req = new CreateChargingSpecRequest(
                UUID.randomUUID(),
                ConnectorType.CCS2,
                CurrentType.DC,
                11000,
                null,
                45,
                120,
                null,
                "DC Fast Charge"
        );

        VehicleChargingSpec entity = mapper.toChargingSpecEntity(req, config);

        assertThat(entity.getId()).isNull(); // not yet persisted
        assertThat(entity.getChargingConfiguration()).isSameAs(config);
        assertThat(entity.getChargingStandardId()).isEqualTo(req.chargingStandardId());
        assertThat(entity.getConnectorType()).isEqualTo(ConnectorType.CCS2);
        assertThat(entity.getCurrentType()).isEqualTo(CurrentType.DC);
        assertThat(entity.getMaxAcceptedWattage()).isEqualTo(11000);
        assertThat(entity.getOnboardChargerWattage()).isNull();
        assertThat(entity.getChargeTime10To80Pct()).isEqualTo(45);
        assertThat(entity.getChargeTimeToFullMinutes()).isEqualTo(120);
        assertThat(entity.getCableIncluded()).isFalse(); // default when null
        assertThat(entity.getNotes()).isEqualTo("DC Fast Charge");
    }

    @Test
    void toChargingSpecEntity_defaultsCableIncludedToFalse() {
        ChargingConfiguration config = new ChargingConfiguration();
        CreateChargingSpecRequest req = new CreateChargingSpecRequest(
                null, ConnectorType.TYPE1, CurrentType.AC, 3300, null, null, 480, null, null
        );

        VehicleChargingSpec entity = mapper.toChargingSpecEntity(req, config);

        assertThat(entity.getCableIncluded()).isFalse();
    }

    @Test
    void applyChargingSpecUpdate_updatesOnlyProvidedFields() {
        VehicleChargingSpec spec = VehicleChargingSpec.builder()
                .id(UUID.randomUUID())
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .maxAcceptedWattage(3300)
                .onboardChargerWattage(3300)
                .chargeTime10To80Pct(null)
                .chargeTimeToFullMinutes(480)
                .cableIncluded(false)
                .notes("Old")
                .build();

        UpdateChargingSpecRequest req = new UpdateChargingSpecRequest(
                null,
                ConnectorType.CCS1,
                null,
                7200,
                null,
                null,
                120,
                true,
                "Updated Notes"
        );

        mapper.applyChargingSpecUpdate(req, spec);

        assertThat(spec.getConnectorType()).isEqualTo(ConnectorType.CCS1);
        assertThat(spec.getCurrentType()).isEqualTo(CurrentType.AC); // unchanged
        assertThat(spec.getMaxAcceptedWattage()).isEqualTo(7200);
        assertThat(spec.getOnboardChargerWattage()).isEqualTo(3300); // unchanged
        assertThat(spec.getChargeTimeToFullMinutes()).isEqualTo(120);
        assertThat(spec.getCableIncluded()).isTrue();
        assertThat(spec.getNotes()).isEqualTo("Updated Notes");
    }

    @Test
    void applyChargingSpecUpdate_ignoresNullValues() {
        VehicleChargingSpec spec = VehicleChargingSpec.builder()
                .notes("Original")
                .cableIncluded(false)
                .build();

        UpdateChargingSpecRequest req = new UpdateChargingSpecRequest(
                null, null, null, null, null, null, null, null, null
        );

        mapper.applyChargingSpecUpdate(req, spec);

        assertThat(spec.getNotes()).isEqualTo("Original"); // unchanged
        assertThat(spec.getCableIncluded()).isFalse(); // unchanged
    }
}

