package com.pk.ev.vehicle.catalog.variantlisting.mapper;

import com.pk.ev.vehicle.catalog.battery.mapper.BatteryPackMapper;
import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.chargingconfig.mapper.ChargingConfigMapper;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.modeltrim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.modeltrim.mapper.ModelTrimMapper;
import com.pk.ev.vehicle.catalog.modeltrim.model.ModelTrim;
import com.pk.ev.vehicle.catalog.variantlisting.dtos.VariantListingDto.*;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.battery.dtos.BatteryPackDtos.*;
import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.*;
import com.pk.ev.vehicle.catalog.modeltrim.dto.ModelTrimDto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VariantListingMapperTest {

    @Mock private ModelTrimMapper modelTrimMapper;
    @Mock private BatteryPackMapper batteryPackMapper;
    @Mock private ChargingConfigMapper chargingConfigMapper;

    private VariantListingMapper mapper;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private UUID modelId;
    private UUID trimId;
    private UUID batteryId;
    private UUID configId;
    private UUID variantId;

    private VehicleModel model;
    private ModelTrim trim;
    private BatteryPack battery;
    private ChargingConfiguration config;
    private VariantListing variant;

    private ModelTrimResponse trimResponse;
    private BatteryPackResponse batteryResponse;
    private ChargingConfigResponse configResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new VariantListingMapper(modelTrimMapper, batteryPackMapper, chargingConfigMapper);

        modelId   = UUID.randomUUID();
        trimId    = UUID.randomUUID();
        batteryId = UUID.randomUUID();
        configId  = UUID.randomUUID();
        variantId = UUID.randomUUID();

        model = new VehicleModel();
        model.setId(modelId);
        model.setName("Tiago EV");

        trim = ModelTrim.builder()
                .id(trimId).model(model).trimName("XZ+")
                .hasSunroof(true).hasAdas(true).hasConnectedCar(true)
                .sortOrder(3).isActive(true)
                .build();

        battery = BatteryPack.builder()
                .id(batteryId).model(model)
                .packName("Long Range")
                .capacityKwh(new BigDecimal("24.0"))
                .usableKwh(new BigDecimal("21.5"))
                .rangeKm(315)
                .isActive(true)
                .build();

        config = ChargingConfiguration.builder()
                .id(configId).model(model)
                .configLabel("7.2 kW AC Fast")
                .onboardChargerKw(new BigDecimal("7.2"))
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .isActive(true)
                .build();

        variant = VariantListing.builder()
                .id(variantId)
                .model(model).trim(trim).batteryPack(battery).chargingConfiguration(config)
                .displayLabel("Tiago EV XZ+ 24 kWh 7.2 kW AC")
                .priceInr(new BigDecimal("1129000"))
                .launchDate(LocalDate.of(2023, 6, 1))
                .status(VariantStatus.ACTIVE)
                .weightKg(1230)
                .sortOrder(5)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        trimResponse = new ModelTrimResponse(
                trimId, modelId, "XZ+", null, true, true, true, 10, 3, true, null, null
        );
        batteryResponse = new BatteryPackResponse(
                batteryId, modelId, "Long Range",
                new BigDecimal("24.0"), new BigDecimal("21.5"), 315,
                null, null, null, null, true, null, null
        );
        configResponse = new ChargingConfigResponse(
                configId, modelId, "7.2 kW AC Fast",
                new BigDecimal("7.2"), ConnectorType.TYPE2, CurrentType.AC,
                null, null, false, true, List.of(), null, null
        );

        when(modelTrimMapper.toModelTrimResponse(trim)).thenReturn(trimResponse);
        when(batteryPackMapper.toBatteryPackResponse(battery)).thenReturn(batteryResponse);
        when(chargingConfigMapper.toChargingConfigResponse(config)).thenReturn(configResponse);
    }

    // ─── toVariantListingResponse ─────────────────────────────────────────────

    @Test
    void toVariantListingResponse_mapsAllTopLevelFields() {
        VariantListingResponse response = mapper.toVariantListingResponse(variant);

        assertThat(response.id()).isEqualTo(variantId);
        assertThat(response.displayLabel()).isEqualTo("Tiago EV XZ+ 24 kWh 7.2 kW AC");
        assertThat(response.priceInr()).isEqualByComparingTo("1129000");
        assertThat(response.launchDate()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(response.status()).isEqualTo(VariantStatus.ACTIVE);
        assertThat(response.weightKg()).isEqualTo(1230);
        assertThat(response.sortOrder()).isEqualTo(5);
    }

    @Test
    void toVariantListingResponse_delegatesToSubMappers() {
        VariantListingResponse response = mapper.toVariantListingResponse(variant);

        assertThat(response.trim()).isEqualTo(trimResponse);
        assertThat(response.batteryPack()).isEqualTo(batteryResponse);
        assertThat(response.chargingConfiguration()).isEqualTo(configResponse);

        verify(modelTrimMapper).toModelTrimResponse(trim);
        verify(batteryPackMapper).toBatteryPackResponse(battery);
        verify(chargingConfigMapper).toChargingConfigResponse(config);
    }

    @Test
    void toVariantListingResponse_preservesTimestamps() {
        Instant createdAt = Instant.parse("2023-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-06-01T00:00:00Z");
        variant.setCreatedAt(createdAt);
        variant.setUpdatedAt(updatedAt);

        VariantListingResponse response = mapper.toVariantListingResponse(variant);

        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toVariantListingResponse_nullableFieldsHandled() {
        variant.setPriceInr(null);
        variant.setLaunchDate(null);
        variant.setWeightKg(null);

        VariantListingResponse response = mapper.toVariantListingResponse(variant);

        assertThat(response.priceInr()).isNull();
        assertThat(response.launchDate()).isNull();
        assertThat(response.weightKg()).isNull();
    }

    // ─── toVariantListingSummary ──────────────────────────────────────────────

    @Test
    void toVariantListingSummary_mapsAllFields() {
        VariantListingSummary summary = mapper.toVariantListingSummary(variant);

        assertThat(summary.id()).isEqualTo(variantId);
        assertThat(summary.displayLabel()).isEqualTo("Tiago EV XZ+ 24 kWh 7.2 kW AC");
        assertThat(summary.trimName()).isEqualTo("XZ+");
        assertThat(summary.batteryCapacityKwh()).isEqualByComparingTo("24.0");
        assertThat(summary.rangeKm()).isEqualTo(315);
        assertThat(summary.onboardChargerKw()).isEqualByComparingTo("7.2");
        assertThat(summary.connectorType()).isEqualTo(ConnectorType.TYPE2);
        assertThat(summary.priceInr()).isEqualByComparingTo("1129000");
        assertThat(summary.status()).isEqualTo(VariantStatus.ACTIVE);
    }

    @Test
    void toVariantListingSummary_extractsTrimNameDirectlyFromEntity() {
        trim.setTrimName("XZ+ Tech LUX");
        VariantListingSummary summary = mapper.toVariantListingSummary(variant);

        assertThat(summary.trimName()).isEqualTo("XZ+ Tech LUX");
        // No sub-mapper call for summary
        verifyNoInteractions(modelTrimMapper);
    }

    @Test
    void toVariantListingSummary_extractsBatteryAndConfigDirectlyFromEntity() {
        VariantListingSummary summary = mapper.toVariantListingSummary(variant);

        // Summary reads directly from entity — no sub-mapper delegation
        verifyNoInteractions(batteryPackMapper);
        verifyNoInteractions(chargingConfigMapper);
        assertThat(summary.batteryCapacityKwh()).isEqualByComparingTo(battery.getCapacityKwh());
        assertThat(summary.onboardChargerKw()).isEqualByComparingTo(config.getOnboardChargerKw());
    }

    @Test
    void toVariantListingSummary_nullPriceHandled() {
        variant.setPriceInr(null);
        VariantListingSummary summary = mapper.toVariantListingSummary(variant);
        assertThat(summary.priceInr()).isNull();
    }

    // ─── toPagedVariantResponse ───────────────────────────────────────────────

    @Test
    void toPagedVariantResponse_mapsPageMetadata() {
        Page<VariantListing> page = new PageImpl<>(
                List.of(variant),
                org.springframework.data.domain.PageRequest.of(1, 10),
                25
        );

        PagedVariantResponse response = mapper.toPagedVariantResponse(page);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(25);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.last()).isFalse();
    }

    @Test
    void toPagedVariantResponse_mapsContentViaSummaryMapper() {
        Page<VariantListing> page = new PageImpl<>(List.of(variant));

        PagedVariantResponse response = mapper.toPagedVariantResponse(page);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(variantId);
        assertThat(response.content().get(0).trimName()).isEqualTo("XZ+");
    }

    @Test
    void toPagedVariantResponse_emptyPage_returnsEmptyContent() {
        Page<VariantListing> emptyPage = Page.empty();

        PagedVariantResponse response = mapper.toPagedVariantResponse(emptyPage);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.last()).isTrue();
    }

    @Test
    void toPagedVariantResponse_multipleVariants_mapsAll() {
        VariantListing variant2 = VariantListing.builder()
                .id(UUID.randomUUID())
                .model(model).trim(trim).batteryPack(battery).chargingConfiguration(config)
                .displayLabel("Tiago EV XZ+ 24 kWh 3.3 kW AC")
                .priceInr(new BigDecimal("1079000"))
                .status(VariantStatus.ACTIVE)
                .sortOrder(4)
                .build();

        Page<VariantListing> page = new PageImpl<>(List.of(variant, variant2));

        PagedVariantResponse response = mapper.toPagedVariantResponse(page);

        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(2);
    }

    @Test
    void toPagedVariantResponse_lastPage_flaggedCorrectly() {
        Page<VariantListing> lastPage = new PageImpl<>(
                List.of(variant),
                org.springframework.data.domain.PageRequest.of(0, 20),
                1
        );

        PagedVariantResponse response = mapper.toPagedVariantResponse(lastPage);

        assertThat(response.last()).isTrue();
    }
}
