package com.pk.ev.vehicle.catalog.compatibility;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;
import com.pk.ev.vehicle.catalog.station.StationConnector;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CompatibilityEngineTest {

    private CompatibilityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CompatibilityEngine();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VehicleChargingSpec spec(ConnectorType type, int maxWattage, Integer chargeTime) {
        return VehicleChargingSpec.builder()
                .id(UUID.randomUUID())
                .connectorType(type)
                .currentType(CurrentType.AC)
                .maxAcceptedWattage(maxWattage)
                .chargeTime10To80Pct(chargeTime)
                .cableIncluded(false)
                .build();
    }

    private StationConnector connector(ConnectorType type, int maxWattage, boolean operational) {
        return StationConnector.builder()
                .id(UUID.randomUUID())
                .stationId(UUID.randomUUID())
                .connectorType(type)
                .currentType(CurrentType.AC)
                .maxWattage(maxWattage)
                .isOperational(operational)
                .build();
    }

    private VariantListing variant(List<VehicleChargingSpec> specs) {
        ChargingConfiguration config = ChargingConfiguration.builder()
                .id(UUID.randomUUID())
                .chargingSpecs(specs)
                .build();

        VariantListing v = VariantListing.builder()
                .id(UUID.randomUUID())
                .displayLabel("Test EV XZ+")
                .chargingConfiguration(config)
                .build();

        return v;
    }

    // ─── checkVariantAgainstStation ───────────────────────────────────────────

    @Test
    void checkVariantAgainstStation_compatible_whenConnectorTypeMatches() {
        VehicleChargingSpec s = spec(ConnectorType.TYPE2, 7200, 60);
        VariantListing v = variant(List.of(s));
        UUID stationId = UUID.randomUUID();
        StationConnector sc = connector(ConnectorType.TYPE2, 22000, true);

        CompatibilityResult result = engine.checkVariantAgainstStation(v, stationId, List.of(sc));

        assertThat(result.isCompatible()).isTrue();
        assertThat(result.compatibleSpecs()).hasSize(1);
        assertThat(result.maxAchievableWattage()).isEqualTo(7200); // min(7200, 22000)
        assertThat(result.incompatibilityReason()).isNull();
        assertThat(result.stationId()).isEqualTo(stationId);
    }

    @Test
    void checkVariantAgainstStation_incompatible_whenNoTypeMatch() {
        VehicleChargingSpec s = spec(ConnectorType.TYPE2, 7200, 60);
        VariantListing v = variant(List.of(s));
        UUID stationId = UUID.randomUUID();
        StationConnector sc = connector(ConnectorType.CCS2, 50000, true);

        CompatibilityResult result = engine.checkVariantAgainstStation(v, stationId, List.of(sc));

        assertThat(result.isCompatible()).isFalse();
        assertThat(result.compatibleSpecs()).isEmpty();
        assertThat(result.incompatibilityReason()).contains("TYPE2");
        assertThat(result.incompatibilityReason()).contains("CCS2");
    }

    @Test
    void checkVariantAgainstStation_incompatible_whenStationConnectorNotOperational() {
        VehicleChargingSpec s = spec(ConnectorType.TYPE2, 7200, 60);
        VariantListing v = variant(List.of(s));
        UUID stationId = UUID.randomUUID();
        StationConnector sc = connector(ConnectorType.TYPE2, 22000, false); // not operational

        CompatibilityResult result = engine.checkVariantAgainstStation(v, stationId, List.of(sc));

        assertThat(result.isCompatible()).isFalse();
    }

    @Test
    void checkVariantAgainstStation_incompatible_whenNoConnectorsAtStation() {
        VehicleChargingSpec s = spec(ConnectorType.CCS2, 50000, 40);
        VariantListing v = variant(List.of(s));
        UUID stationId = UUID.randomUUID();

        CompatibilityResult result = engine.checkVariantAgainstStation(v, stationId, List.of());

        assertThat(result.isCompatible()).isFalse();
        assertThat(result.incompatibilityReason()).contains("No operational connectors");
    }

    @Test
    void checkVariantAgainstStation_achievableWattage_isMinOfVehicleAndStation() {
        VehicleChargingSpec s = spec(ConnectorType.CCS2, 50000, 40);
        VariantListing v = variant(List.of(s));
        UUID stationId = UUID.randomUUID();
        StationConnector sc = connector(ConnectorType.CCS2, 30000, true); // station limited

        CompatibilityResult result = engine.checkVariantAgainstStation(v, stationId, List.of(sc));

        assertThat(result.isCompatible()).isTrue();
        assertThat(result.maxAchievableWattage()).isEqualTo(30000);
    }

    @Test
    void checkVariantAgainstStation_selectsBestConnectorWhenMultiple() {
        VehicleChargingSpec s = spec(ConnectorType.CCS2, 100000, 30);
        VariantListing v = variant(List.of(s));
        UUID stationId = UUID.randomUUID();
        StationConnector low  = connector(ConnectorType.CCS2, 50000,  true);
        StationConnector high = connector(ConnectorType.CCS2, 150000, true);

        CompatibilityResult result = engine.checkVariantAgainstStation(v, stationId, List.of(low, high));

        assertThat(result.isCompatible()).isTrue();
        assertThat(result.maxAchievableWattage()).isEqualTo(100000); // min(100000, 150000)
    }

    @Test
    void checkVariantAgainstStation_multipleSpecs_allMatchedReturned() {
        VehicleChargingSpec s1 = spec(ConnectorType.TYPE2, 7200, 120);
        VehicleChargingSpec s2 = spec(ConnectorType.CCS2, 50000, 40);
        VariantListing v = variant(List.of(s1, s2));
        UUID stationId = UUID.randomUUID();
        StationConnector sc1 = connector(ConnectorType.TYPE2, 22000, true);
        StationConnector sc2 = connector(ConnectorType.CCS2, 50000, true);

        CompatibilityResult result = engine.checkVariantAgainstStation(v, stationId, List.of(sc1, sc2));

        assertThat(result.isCompatible()).isTrue();
        assertThat(result.compatibleSpecs()).hasSize(2);
        assertThat(result.maxAchievableWattage()).isEqualTo(50000); // best spec wins
    }

    // ─── checkVariantAgainstConnectorType ────────────────────────────────────

    @Test
    void checkVariantAgainstConnectorType_compatible_whenTypeMatches() {
        VehicleChargingSpec s = spec(ConnectorType.TYPE2, 7200, 60);
        VariantListing v = variant(List.of(s));

        CompatibilityResult result = engine.checkVariantAgainstConnectorType(v, ConnectorType.TYPE2, null);

        assertThat(result.isCompatible()).isTrue();
        assertThat(result.connectorType()).isEqualTo(ConnectorType.TYPE2);
        assertThat(result.maxAchievableWattage()).isEqualTo(7200);
    }

    @Test
    void checkVariantAgainstConnectorType_incompatible_whenTypeMismatch() {
        VehicleChargingSpec s = spec(ConnectorType.TYPE2, 7200, 60);
        VariantListing v = variant(List.of(s));

        CompatibilityResult result = engine.checkVariantAgainstConnectorType(v, ConnectorType.CCS2, null);

        assertThat(result.isCompatible()).isFalse();
        assertThat(result.incompatibilityReason()).contains("CCS2");
    }

    @Test
    void checkVariantAgainstConnectorType_withStationMaxWattage_limitsAchievable() {
        VehicleChargingSpec s = spec(ConnectorType.CCS2, 100000, 30);
        VariantListing v = variant(List.of(s));

        CompatibilityResult result = engine.checkVariantAgainstConnectorType(v, ConnectorType.CCS2, 50000);

        assertThat(result.isCompatible()).isTrue();
        assertThat(result.maxAchievableWattage()).isEqualTo(50000);
    }

    @Test
    void checkVariantAgainstConnectorType_withoutStationMaxWattage_usesVehicleMax() {
        VehicleChargingSpec s = spec(ConnectorType.CCS2, 100000, 30);
        VariantListing v = variant(List.of(s));

        CompatibilityResult result = engine.checkVariantAgainstConnectorType(v, ConnectorType.CCS2, null);

        assertThat(result.isCompatible()).isTrue();
        assertThat(result.maxAchievableWattage()).isEqualTo(100000);
    }

    // ─── findCompatibleVariantsForStation ────────────────────────────────────

    @Test
    void findCompatibleVariantsForStation_returnsOnlyCompatible() {
        VariantListing v1 = variant(List.of(spec(ConnectorType.TYPE2, 7200, 60)));
        VariantListing v2 = variant(List.of(spec(ConnectorType.CCS2, 50000, 40)));
        UUID stationId = UUID.randomUUID();
        StationConnector sc = connector(ConnectorType.TYPE2, 22000, true);

        List<CompatibilityResult> results =
                engine.findCompatibleVariantsForStation(List.of(v1, v2), stationId, List.of(sc));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).variantListingId()).isEqualTo(v1.getId());
    }

    @Test
    void findCompatibleVariantsForStation_emptyWhenNoMatch() {
        VariantListing v = variant(List.of(spec(ConnectorType.TYPE2, 7200, 60)));
        UUID stationId = UUID.randomUUID();
        StationConnector sc = connector(ConnectorType.CCS2, 50000, true);

        List<CompatibilityResult> results =
                engine.findCompatibleVariantsForStation(List.of(v), stationId, List.of(sc));

        assertThat(results).isEmpty();
    }

    // ─── findCompatibleVariantsForConnector ──────────────────────────────────

    @Test
    void findCompatibleVariantsForConnector_returnsOnlyCompatible() {
        VariantListing v1 = variant(List.of(spec(ConnectorType.CCS2, 50000, 40)));
        VariantListing v2 = variant(List.of(spec(ConnectorType.TYPE2, 7200, 60)));

        List<CompatibilityResult> results =
                engine.findCompatibleVariantsForConnector(List.of(v1, v2), ConnectorType.CCS2, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).variantListingId()).isEqualTo(v1.getId());
    }

    // ─── interpolateChargeTime ────────────────────────────────────────────────

    @Test
    void interpolateChargeTime_returnsNullWhenRatedMinutesNull() {
        assertThat(CompatibilityEngine.interpolateChargeTime(null, 7200, 3600)).isNull();
    }

    @Test
    void interpolateChargeTime_returnsNullWhenAchievableWattageZero() {
        assertThat(CompatibilityEngine.interpolateChargeTime(60, 7200, 0)).isNull();
    }

    @Test
    void interpolateChargeTime_returnsRatedWhenAchievableEqualsMax() {
        assertThat(CompatibilityEngine.interpolateChargeTime(60, 7200, 7200)).isEqualTo(60);
    }

    @Test
    void interpolateChargeTime_scalesUpWhenAchievableIsLower() {
        // 60 min at 7200 W → at 3600 W (half) → 120 min
        assertThat(CompatibilityEngine.interpolateChargeTime(60, 7200, 3600)).isEqualTo(120);
    }

    @Test
    void interpolateChargeTime_returnsRatedWhenAchievableExceedsMax() {
        // achievable > vehicleMax — clamp to rated
        assertThat(CompatibilityEngine.interpolateChargeTime(60, 7200, 10000)).isEqualTo(60);
    }

    @Test
    void interpolateChargeTime_ceilsResult() {
        // 60 × (7200 / 5000) = 86.4 → ceiled to 87
        Integer result = CompatibilityEngine.interpolateChargeTime(60, 7200, 5000);
        assertThat(result).isEqualTo(87);
    }
}
