package com.pk.ev.vehicle.catalog.variantlisting.service;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.battery.service.BatteryPackService;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingconfig.service.ChargingConfigService;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.modeltrim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.modeltrim.model.ModelTrim;
import com.pk.ev.vehicle.catalog.modeltrim.service.ModelTrimService;
import com.pk.ev.vehicle.catalog.variantlisting.dtos.VariantListingDto.*;
import com.pk.ev.vehicle.catalog.variantlisting.mapper.VariantListingMapper;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import com.pk.ev.vehicle.catalog.variantlisting.repository.VariantListingRepository;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.vehiclemodel.service.VehicleModelService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VariantListingServiceImplTest {

    @Mock private VariantListingRepository variantRepository;
    @Mock private VehicleModelService modelService;
    @Mock private BatteryPackService batteryPackService;
    @Mock private ModelTrimService trimService;
    @Mock private ChargingConfigService chargingConfigService;
    @Mock private VariantListingMapper variantListingMapper;

    private VariantListingServiceImpl service;
    private AutoCloseable mocks;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private UUID modelId;
    private UUID trimId;
    private UUID batteryPackId;
    private UUID configId;

    private VehicleModel model;
    private ModelTrim trim;
    private BatteryPack battery;
    private ChargingConfiguration config;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new VariantListingServiceImpl(
                variantRepository, modelService, batteryPackService,
                trimService, chargingConfigService, variantListingMapper
        );

        modelId = UUID.randomUUID();
        trimId = UUID.randomUUID();
        batteryPackId = UUID.randomUUID();
        configId = UUID.randomUUID();

        model = new VehicleModel();
        model.setId(modelId);
        model.setName("Tiago EV");

        trim = ModelTrim.builder()
                .id(trimId)
                .model(model)
                .trimName("XE")
                .build();

        battery = BatteryPack.builder()
                .id(batteryPackId)
                .model(model)
                .packName("Medium Range")
                .capacityKwh(new BigDecimal("19.2"))
                .rangeKm(250)
                .build();

        config = ChargingConfiguration.builder()
                .id(configId)
                .model(model)
                .configLabel("3.3 kW AC Standard")
                .onboardChargerKw(new BigDecimal("3.3"))
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VariantListing buildVariant(UUID id) {
        return VariantListing.builder()
                .id(id)
                .model(model).trim(trim).batteryPack(battery).chargingConfiguration(config)
                .displayLabel("Tiago EV XE 19 kWh 3.3 kW AC")
                .priceInr(new BigDecimal("849000"))
                .status(VariantStatus.ACTIVE)
                .sortOrder(1)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    private VariantListingResponse buildResponse(UUID id) {
        return new VariantListingResponse(
                id, "Tiago EV XE 19 kWh 3.3 kW AC",
                null, null, null,
                new BigDecimal("849000"),
                LocalDate.of(2023, 1, 1),
                VariantStatus.ACTIVE,
                1200, 1,
                Instant.now(), Instant.now()
        );
    }

    // ─── createVariantListing ─────────────────────────────────────────────────

    @Test
    void createVariantListing_success_savesAndReturnsResponse() {
        UUID variantId = UUID.randomUUID();
        CreateVariantListingRequest req = new CreateVariantListingRequest(
                modelId, trimId, batteryPackId, configId,
                new BigDecimal("849000"), LocalDate.of(2023, 1, 1),
                VariantStatus.ACTIVE, 1200, 1
        );

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimService.findTrimOrThrow(modelId, trimId)).thenReturn(trim);
        when(batteryPackService.findBatteryPackOrThrow(modelId, batteryPackId)).thenReturn(battery);
        when(chargingConfigService.findChargingConfigOrThrow(modelId, configId)).thenReturn(config);
        when(variantRepository.existsByModelIdAndTrimIdAndBatteryPackIdAndChargingConfigurationId(
                modelId, trimId, batteryPackId, configId)).thenReturn(false);

        VariantListing saved = buildVariant(variantId);
        when(variantRepository.save(any())).thenReturn(saved);

        VariantListingResponse expectedResponse = buildResponse(variantId);
        when(variantListingMapper.toVariantListingResponse(saved)).thenReturn(expectedResponse);

        VariantListingResponse result = service.createVariantListing(req);

        assertThat(result).isEqualTo(expectedResponse);
        verify(variantRepository).save(any(VariantListing.class));
    }

    @Test
    void createVariantListing_defaultsStatusToActive_whenNullProvided() {
        CreateVariantListingRequest req = new CreateVariantListingRequest(
                modelId, trimId, batteryPackId, configId,
                new BigDecimal("849000"), null, null, null, null
        );

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimService.findTrimOrThrow(modelId, trimId)).thenReturn(trim);
        when(batteryPackService.findBatteryPackOrThrow(modelId, batteryPackId)).thenReturn(battery);
        when(chargingConfigService.findChargingConfigOrThrow(modelId, configId)).thenReturn(config);
        when(variantRepository.existsByModelIdAndTrimIdAndBatteryPackIdAndChargingConfigurationId(
                any(), any(), any(), any())).thenReturn(false);

        VariantListing saved = buildVariant(UUID.randomUUID());
        when(variantRepository.save(any())).thenReturn(saved);
        when(variantListingMapper.toVariantListingResponse(saved)).thenReturn(buildResponse(saved.getId()));

        service.createVariantListing(req);

        ArgumentCaptor<VariantListing> captor = ArgumentCaptor.forClass(VariantListing.class);
        verify(variantRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VariantStatus.ACTIVE);
    }

    @Test
    void createVariantListing_defaultsSortOrderToZero_whenNullProvided() {
        CreateVariantListingRequest req = new CreateVariantListingRequest(
                modelId, trimId, batteryPackId, configId,
                new BigDecimal("849000"), null, VariantStatus.ACTIVE, null, null
        );

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimService.findTrimOrThrow(modelId, trimId)).thenReturn(trim);
        when(batteryPackService.findBatteryPackOrThrow(modelId, batteryPackId)).thenReturn(battery);
        when(chargingConfigService.findChargingConfigOrThrow(modelId, configId)).thenReturn(config);
        when(variantRepository.existsByModelIdAndTrimIdAndBatteryPackIdAndChargingConfigurationId(
                any(), any(), any(), any())).thenReturn(false);

        VariantListing saved = buildVariant(UUID.randomUUID());
        when(variantRepository.save(any())).thenReturn(saved);
        when(variantListingMapper.toVariantListingResponse(saved)).thenReturn(buildResponse(saved.getId()));

        service.createVariantListing(req);

        ArgumentCaptor<VariantListing> captor = ArgumentCaptor.forClass(VariantListing.class);
        verify(variantRepository).save(captor.capture());
        assertThat(captor.getValue().getSortOrder()).isEqualTo(0);
    }

    @Test
    void createVariantListing_duplicateSku_throwsDuplicateResourceException() {
        CreateVariantListingRequest req = new CreateVariantListingRequest(
                modelId, trimId, batteryPackId, configId,
                new BigDecimal("849000"), null, null, null, null
        );

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(trimService.findTrimOrThrow(modelId, trimId)).thenReturn(trim);
        when(batteryPackService.findBatteryPackOrThrow(modelId, batteryPackId)).thenReturn(battery);
        when(chargingConfigService.findChargingConfigOrThrow(modelId, configId)).thenReturn(config);
        when(variantRepository.existsByModelIdAndTrimIdAndBatteryPackIdAndChargingConfigurationId(
                modelId, trimId, batteryPackId, configId)).thenReturn(true);

        assertThatThrownBy(() -> service.createVariantListing(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(variantRepository, never()).save(any());
    }

    @Test
    void createVariantListing_modelNotFound_throwsResourceNotFoundException() {
        CreateVariantListingRequest req = new CreateVariantListingRequest(
                modelId, trimId, batteryPackId, configId, null, null, null, null, null
        );

        when(modelService.findModelOrThrow(modelId))
                .thenThrow(new ResourceNotFoundException("VehicleModel not found"));

        assertThatThrownBy(() -> service.createVariantListing(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("VehicleModel not found");
    }

    // ─── getVariantListings ───────────────────────────────────────────────────

    @Test
    void getVariantListings_noFilters_returnsPagedResponse() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId);
        Page<VariantListing> page = new PageImpl<>(List.of(variant));

        when(variantRepository.findAllByFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class))
        ).thenReturn(page);

        VariantFilterParams filters = new VariantFilterParams(
                null, null, null, null, null, null, 0, 20, "sortOrder"
        );
        PagedVariantResponse paged = new PagedVariantResponse(List.of(), 0, 20, 1L, 1, true);
        when(variantListingMapper.toPagedVariantResponse(page)).thenReturn(paged);

        PagedVariantResponse result = service.getVariantListings(filters);

        assertThat(result).isEqualTo(paged);
    }

    @Test
    void getVariantListings_withAllFilters_passesThemToRepository() {
        UUID filterModelId = UUID.randomUUID();
        UUID filterTrimId = UUID.randomUUID();
        BigDecimal minPrice = new BigDecimal("500000");
        BigDecimal maxPrice = new BigDecimal("1500000");
        BigDecimal minBattery = new BigDecimal("20.0");

        Page<VariantListing> emptyPage = Page.empty();
        when(variantRepository.findAllByFilters(
                eq(filterModelId), eq(filterTrimId),
                eq(minPrice), eq(maxPrice), eq(minBattery),
                eq(VariantStatus.ACTIVE), any(Pageable.class))
        ).thenReturn(emptyPage);

        PagedVariantResponse emptyPaged = new PagedVariantResponse(List.of(), 0, 20, 0L, 0, true);
        when(variantListingMapper.toPagedVariantResponse(emptyPage)).thenReturn(emptyPaged);

        VariantFilterParams filters = new VariantFilterParams(
                filterModelId, filterTrimId, minPrice, maxPrice, minBattery,
                VariantStatus.ACTIVE, 0, 20, "sortOrder"
        );

        PagedVariantResponse result = service.getVariantListings(filters);

        assertThat(result.totalElements()).isEqualTo(0);
    }

    @Test
    void getVariantListings_emptyResult_returnsEmptyPage() {
        Page<VariantListing> emptyPage = Page.empty();
        when(variantRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        PagedVariantResponse emptyPaged = new PagedVariantResponse(List.of(), 0, 20, 0L, 0, true);
        when(variantListingMapper.toPagedVariantResponse(emptyPage)).thenReturn(emptyPaged);

        VariantFilterParams filters = new VariantFilterParams(
                null, null, null, null, null, null, 0, 20, "sortOrder"
        );
        PagedVariantResponse result = service.getVariantListings(filters);

        assertThat(result.content()).isEmpty();
    }

    // ─── getVariantListingById ────────────────────────────────────────────────

    @Test
    void getVariantListingById_found_returnsResponse() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId);
        VariantListingResponse expected = buildResponse(variantId);

        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.of(variant));
        when(variantListingMapper.toVariantListingResponse(variant)).thenReturn(expected);

        VariantListingResponse result = service.getVariantListingById(variantId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getVariantListingById_notFound_throwsResourceNotFoundException() {
        UUID variantId = UUID.randomUUID();
        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVariantListingById(variantId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(variantId.toString());
    }

    // ─── getVariantListingsByModel ────────────────────────────────────────────

    @Test
    void getVariantListingsByModel_noStatus_returnsAllForModel() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId);
        VariantListingResponse expected = buildResponse(variantId);

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(variantRepository.findByModelIdWithDetails(modelId, null)).thenReturn(List.of(variant));
        when(variantListingMapper.toVariantListingResponse(variant)).thenReturn(expected);

        List<VariantListingResponse> result = service.getVariantListingsByModel(modelId, null);

        assertThat(result).hasSize(1).containsExactly(expected);
    }

    @Test
    void getVariantListingsByModel_withStatus_filtersCorrectly() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId);
        VariantListingResponse expected = buildResponse(variantId);

        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(variantRepository.findByModelIdWithDetails(modelId, VariantStatus.ACTIVE))
                .thenReturn(List.of(variant));
        when(variantListingMapper.toVariantListingResponse(variant)).thenReturn(expected);

        List<VariantListingResponse> result = service.getVariantListingsByModel(modelId, "ACTIVE");

        assertThat(result).hasSize(1);
        verify(variantRepository).findByModelIdWithDetails(modelId, VariantStatus.ACTIVE);
    }

    @Test
    void getVariantListingsByModel_blankStatus_treatedAsNull() {
        when(modelService.findModelOrThrow(modelId)).thenReturn(model);
        when(variantRepository.findByModelIdWithDetails(modelId, null)).thenReturn(List.of());

        List<VariantListingResponse> result = service.getVariantListingsByModel(modelId, "   ");

        assertThat(result).isEmpty();
        verify(variantRepository).findByModelIdWithDetails(modelId, null);
    }

    @Test
    void getVariantListingsByModel_modelNotFound_throwsResourceNotFoundException() {
        when(modelService.findModelOrThrow(modelId))
                .thenThrow(new ResourceNotFoundException("VehicleModel not found: " + modelId));

        assertThatThrownBy(() -> service.getVariantListingsByModel(modelId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── updateVariantListing ─────────────────────────────────────────────────

    @Test
    void updateVariantListing_updatesAllProvidedFields() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId);

        UpdateVariantListingRequest req = new UpdateVariantListingRequest(
                new BigDecimal("950000"),
                LocalDate.of(2024, 6, 1),
                VariantStatus.DISCONTINUED,
                1250, 2
        );

        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.of(variant));
        when(variantRepository.save(variant)).thenReturn(variant);
        when(variantListingMapper.toVariantListingResponse(variant)).thenReturn(buildResponse(variantId));

        service.updateVariantListing(variantId, req);

        assertThat(variant.getPriceInr()).isEqualByComparingTo("950000");
        assertThat(variant.getLaunchDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(variant.getStatus()).isEqualTo(VariantStatus.DISCONTINUED);
        assertThat(variant.getWeightKg()).isEqualTo(1250);
        assertThat(variant.getSortOrder()).isEqualTo(2);
    }

    @Test
    void updateVariantListing_nullFields_keepsOriginalValues() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId);
        BigDecimal originalPrice = variant.getPriceInr();

        UpdateVariantListingRequest req = new UpdateVariantListingRequest(
                null, null, null, null, null
        );

        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.of(variant));
        when(variantRepository.save(variant)).thenReturn(variant);
        when(variantListingMapper.toVariantListingResponse(variant)).thenReturn(buildResponse(variantId));

        service.updateVariantListing(variantId, req);

        assertThat(variant.getPriceInr()).isEqualByComparingTo(originalPrice);
        assertThat(variant.getStatus()).isEqualTo(VariantStatus.ACTIVE);
    }

    @Test
    void updateVariantListing_notFound_throwsResourceNotFoundException() {
        UUID variantId = UUID.randomUUID();
        when(variantRepository.findByIdWithAllDetails(variantId)).thenReturn(Optional.empty());

        UpdateVariantListingRequest req = new UpdateVariantListingRequest(
                new BigDecimal("950000"), null, null, null, null
        );

        assertThatThrownBy(() -> service.updateVariantListing(variantId, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(variantId.toString());
    }

    // ─── deleteVariantListing ─────────────────────────────────────────────────

    @Test
    void deleteVariantListing_setsStatusToDiscontinued() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId);

        when(variantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(variantRepository.save(variant)).thenReturn(variant);

        service.deleteVariantListing(variantId);

        assertThat(variant.getStatus()).isEqualTo(VariantStatus.DISCONTINUED);
        verify(variantRepository).save(variant);
    }

    @Test
    void deleteVariantListing_notFound_throwsResourceNotFoundException() {
        UUID variantId = UUID.randomUUID();
        when(variantRepository.findById(variantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteVariantListing(variantId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(variantId.toString());

        verify(variantRepository, never()).save(any());
    }

    @Test
    void deleteVariantListing_doesNotHardDelete() {
        UUID variantId = UUID.randomUUID();
        VariantListing variant = buildVariant(variantId);

        when(variantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(variantRepository.save(variant)).thenReturn(variant);

        service.deleteVariantListing(variantId);

        verify(variantRepository, never()).deleteById(any());
        verify(variantRepository, never()).delete(any());
    }
}
