package com.pk.ev.vehicle.catalog.compatibility;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.modeltrim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.station.StationConnector;
import com.pk.ev.vehicle.catalog.station.StationConnectorRepository;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import com.pk.ev.vehicle.catalog.variantlisting.repository.VariantListingRepository;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CompatibilityServiceImplTest {

    @Mock private VariantListingRepository variantRepository;
    @Mock private StationConnectorRepository connectorRepository;
    @Mock private CompatibilityEngine engine;

    private CompatibilityServiceImpl service;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new CompatibilityServiceImpl(variantRepository, connectorRepository, engine);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VariantListing buildVariant(UUID id, String label) {
        VehicleMake make = new VehicleMake();
        make.setId(UUID.randomUUID());
        make.setName("Tata");

        VehicleModel model = new VehicleModel();
        model.setId(UUID.randomUUID());
        model.setName("Tiago EV");
        model.setMake(make);
        model.setModelYear(2024);

        VehicleChargingSpec spec = VehicleChargingSpec.builder()
                .id(UUID.randomUUID())
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .maxAcceptedWattage(7200)
                .chargeTime10To80Pct(60)
                .cableIncluded(false)
                .build();

        ChargingConfiguration config = ChargingConfiguration.builder()
                .id(UUID.randomUUID())
                .chargingSpecs(List.of(spec))
                .build();

        return VariantListing.builder()
                .id(id)
                .displayLabel(label)
                .model(model)
                .chargingConfiguration(config)
                .build();
    }

    private StationConnector buildConnector(ConnectorType type, int maxWattage) {
        return StationConnector.builder()
                .id(UUID.randomUUID())
                .stationId(UUID.randomUUID())
                .connectorType(type)
                .currentType(CurrentType.AC)
                .maxWattage(maxWattage)
                .isOperational(true)
                .build();
    }

    private CompatibilityResult compatibleResult(UUID variantId, String label, UUID stationId) {
        return new CompatibilityResult(
                variantId, label, stationId, null,
                true, List.of(), 7200, 60, null);
    }

    private CompatibilityResult incompatibleResult(UUID variantId, UUID stationId, String reason) {
        return new CompatibilityResult(
                variantId, "Unknown variant", stationId, null,
                false, List.of(), null, null, reason);
    }

    // ─── checkVariantAgainstStation ──────────────────────────────────────────

    @Test
    void checkVariantAgainstStation_returnsResult_whenVariantAndConnectorsFound() {
        UUID variantId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId, "Tiago EV XZ+");
        List<StationConnector> connectors = List.of(buildConnector(ConnectorType.TYPE2, 22000));
        CompatibilityResult expected = compatibleResult(variantId, "Tiago EV XZ+", stationId);

        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.of(variant));
        when(connectorRepository.findByStationIdAndIsOperationalTrue(stationId)).thenReturn(connectors);
        when(engine.checkVariantAgainstStation(variant, stationId, connectors)).thenReturn(expected);

        CompatibilityResult result = service.checkVariantAgainstStation(variantId, stationId);

        assertThat(result).isEqualTo(expected);
        verify(engine).checkVariantAgainstStation(variant, stationId, connectors);
    }

    @Test
    void checkVariantAgainstStation_throwsResourceNotFound_whenVariantMissing() {
        UUID variantId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkVariantAgainstStation(variantId, stationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(variantId.toString());
    }

    // ─── checkVariantAgainstConnector ────────────────────────────────────────

    @Test
    void checkVariantAgainstConnector_delegatesToEngine() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId, "Tiago EV XE");
        CompatibilityResult expected = new CompatibilityResult(
                variantId, "Tiago EV XE", null, ConnectorType.TYPE2,
                true, List.of(), 7200, 60, null);

        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.of(variant));
        when(engine.checkVariantAgainstConnectorType(variant, ConnectorType.TYPE2, 22000))
                .thenReturn(expected);

        CompatibilityResult result = service.checkVariantAgainstConnector(variantId, ConnectorType.TYPE2, 22000);

        assertThat(result).isEqualTo(expected);
        verify(engine).checkVariantAgainstConnectorType(variant, ConnectorType.TYPE2, 22000);
    }

    @Test
    void checkVariantAgainstConnector_withNullWattage_delegatesToEngine() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId, "Tiago EV XE");
        CompatibilityResult expected = new CompatibilityResult(
                variantId, "Tiago EV XE", null, ConnectorType.TYPE2,
                true, List.of(), 7200, 60, null);

        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.of(variant));
        when(engine.checkVariantAgainstConnectorType(variant, ConnectorType.TYPE2, null))
                .thenReturn(expected);

        CompatibilityResult result = service.checkVariantAgainstConnector(variantId, ConnectorType.TYPE2, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void checkVariantAgainstConnector_throwsResourceNotFound_whenVariantMissing() {
        UUID variantId = UUID.randomUUID();

        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkVariantAgainstConnector(variantId, ConnectorType.CCS2, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getCompatibleVariantsForStation ─────────────────────────────────────

    @Test
    void getCompatibleVariantsForStation_returnsEmpty_whenNoOperationalConnectors() {
        UUID stationId = UUID.randomUUID();

        when(connectorRepository.findByStationIdAndIsOperationalTrue(stationId)).thenReturn(List.of());

        StationCompatibleVariants result = service.getCompatibleVariantsForStation(stationId);

        assertThat(result.stationId()).isEqualTo(stationId);
        assertThat(result.compatibleVariants()).isEmpty();
        assertThat(result.totalCount()).isZero();
        verifyNoInteractions(variantRepository);
    }

    @Test
    void getCompatibleVariantsForStation_returnsCompatibleSummaries() {
        UUID stationId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId, "Tiago EV XZ+");
        StationConnector sc = buildConnector(ConnectorType.TYPE2, 22000);
        CompatibilityResult engineResult = compatibleResult(variantId, "Tiago EV XZ+", stationId);

        Page<VariantListing> page = new PageImpl<>(List.of(variant));
        when(connectorRepository.findByStationIdAndIsOperationalTrue(stationId)).thenReturn(List.of(sc));
        when(variantRepository.findAllByFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(VariantStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);
        when(engine.findCompatibleVariantsForStation(List.of(variant), stationId, List.of(sc)))
                .thenReturn(List.of(engineResult));

        StationCompatibleVariants result = service.getCompatibleVariantsForStation(stationId);

        assertThat(result.stationId()).isEqualTo(stationId);
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.compatibleVariants()).hasSize(1);

        CompatibleVariantSummary summary = result.compatibleVariants().get(0);
        assertThat(summary.variantListingId()).isEqualTo(variantId);
        assertThat(summary.displayLabel()).isEqualTo("Tiago EV XZ+");
        assertThat(summary.makeName()).isEqualTo("Tata");
        assertThat(summary.modelName()).isEqualTo("Tiago EV");
        assertThat(summary.modelYear()).isEqualTo(2024);
        assertThat(summary.maxAchievableWattage()).isEqualTo(7200);
    }

    @Test
    void getCompatibleVariantsForStation_returnsEmpty_whenEngineFindsNoMatch() {
        UUID stationId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId, "Tiago EV XZ+");
        StationConnector sc = buildConnector(ConnectorType.CCS2, 50000);

        Page<VariantListing> page = new PageImpl<>(List.of(variant));
        when(connectorRepository.findByStationIdAndIsOperationalTrue(stationId)).thenReturn(List.of(sc));
        when(variantRepository.findAllByFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(VariantStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);
        when(engine.findCompatibleVariantsForStation(anyList(), eq(stationId), anyList()))
                .thenReturn(List.of());

        StationCompatibleVariants result = service.getCompatibleVariantsForStation(stationId);

        assertThat(result.compatibleVariants()).isEmpty();
        assertThat(result.totalCount()).isZero();
    }

    // ─── getCompatibleVariantsForConnector ───────────────────────────────────

    @Test
    void getCompatibleVariantsForConnector_returnsSummaries() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId, "Tiago EV XZ+");
        CompatibilityResult engineResult = new CompatibilityResult(
                variantId, "Tiago EV XZ+", null, ConnectorType.TYPE2,
                true, List.of(), 7200, 60, null);

        Page<VariantListing> page = new PageImpl<>(List.of(variant));
        when(variantRepository.findAllByFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(VariantStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);
        when(engine.findCompatibleVariantsForConnector(List.of(variant), ConnectorType.TYPE2, null))
                .thenReturn(List.of(engineResult));

        ConnectorCompatibleVariants result =
                service.getCompatibleVariantsForConnector(ConnectorType.TYPE2, null);

        assertThat(result.connectorType()).isEqualTo(ConnectorType.TYPE2);
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.compatibleVariants()).hasSize(1);
        assertThat(result.compatibleVariants().get(0).variantListingId()).isEqualTo(variantId);
    }

    @Test
    void getCompatibleVariantsForConnector_returnsEmpty_whenNoMatch() {
        Page<VariantListing> page = new PageImpl<>(List.of());
        when(variantRepository.findAllByFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(VariantStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);
        when(engine.findCompatibleVariantsForConnector(anyList(), eq(ConnectorType.CCS2), isNull()))
                .thenReturn(List.of());

        ConnectorCompatibleVariants result =
                service.getCompatibleVariantsForConnector(ConnectorType.CCS2, null);

        assertThat(result.compatibleVariants()).isEmpty();
        assertThat(result.totalCount()).isZero();
    }

    // ─── bulkCheck ───────────────────────────────────────────────────────────

    @Test
    void bulkCheck_returnsResultsWithCorrectCounts() {
        UUID variantId1 = UUID.randomUUID();
        UUID variantId2 = UUID.randomUUID();
        UUID stationId1 = UUID.randomUUID();
        UUID stationId2 = UUID.randomUUID();

        VariantListing v1 = buildVariant(variantId1, "Tiago EV XZ+");
        VariantListing v2 = buildVariant(variantId2, "Tiago EV XE");

        List<StationConnector> sc1 = List.of(buildConnector(ConnectorType.TYPE2, 22000));
        List<StationConnector> sc2 = List.of(buildConnector(ConnectorType.CCS2, 50000));

        CompatibilityResult r1 = compatibleResult(variantId1, "Tiago EV XZ+", stationId1);
        CompatibilityResult r2 = incompatibleResult(variantId2, stationId2, "No match");

        when(variantRepository.findByIdWithAllDetails(variantId1)).thenReturn(Optional.of(v1));
        when(variantRepository.findByIdWithAllDetails(variantId2)).thenReturn(Optional.of(v2));
        when(connectorRepository.findByStationIdAndIsOperationalTrue(stationId1)).thenReturn(sc1);
        when(connectorRepository.findByStationIdAndIsOperationalTrue(stationId2)).thenReturn(sc2);
        when(engine.checkVariantAgainstStation(v1, stationId1, sc1)).thenReturn(r1);
        when(engine.checkVariantAgainstStation(v2, stationId2, sc2)).thenReturn(r2);

        BulkCompatibilityRequest request = new BulkCompatibilityRequest(List.of(
                new BulkCompatibilityRequest.CompatibilityPair(variantId1, stationId1),
                new BulkCompatibilityRequest.CompatibilityPair(variantId2, stationId2)
        ));

        BulkCompatibilityResponse response = service.bulkCheck(request);

        assertThat(response.totalChecked()).isEqualTo(2);
        assertThat(response.compatibleCount()).isEqualTo(1);
        assertThat(response.incompatibleCount()).isEqualTo(1);
        assertThat(response.results()).hasSize(2);
    }

    @Test
    void bulkCheck_doesNotFailBatch_whenVariantNotFound() {
        UUID missingVariantId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        when(variantRepository.findByIdWithAllDetails(missingVariantId))
                .thenReturn(Optional.empty());

        BulkCompatibilityRequest request = new BulkCompatibilityRequest(List.of(
                new BulkCompatibilityRequest.CompatibilityPair(missingVariantId, stationId)
        ));

        BulkCompatibilityResponse response = service.bulkCheck(request);

        assertThat(response.totalChecked()).isEqualTo(1);
        assertThat(response.compatibleCount()).isZero();
        assertThat(response.incompatibleCount()).isEqualTo(1);
        assertThat(response.results().get(0).isCompatible()).isFalse();
        assertThat(response.results().get(0).incompatibilityReason()).contains("not found");
    }

    @Test
    void bulkCheck_emptyRequest_returnsZeroCounts() {
        BulkCompatibilityRequest request = new BulkCompatibilityRequest(List.of());

        BulkCompatibilityResponse response = service.bulkCheck(request);

        assertThat(response.totalChecked()).isZero();
        assertThat(response.compatibleCount()).isZero();
        assertThat(response.incompatibleCount()).isZero();
        assertThat(response.results()).isEmpty();
    }
}
