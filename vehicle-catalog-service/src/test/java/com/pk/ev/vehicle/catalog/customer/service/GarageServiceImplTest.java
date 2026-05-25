package com.pk.ev.vehicle.catalog.customer.service;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityEngine;
import com.pk.ev.vehicle.catalog.customer.domain.CustomerVehicle;
import com.pk.ev.vehicle.catalog.customer.dto.GarageDtos.*;
import com.pk.ev.vehicle.catalog.customer.mapper.GarageMapper;
import com.pk.ev.vehicle.catalog.customer.repository.CustomerVehicleRepository;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.modeltrim.model.ModelTrim;
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
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GarageServiceImplTest {

    @Mock private CustomerVehicleRepository customerVehicleRepository;
    @Mock private VariantListingRepository variantListingRepository;
    @Mock private StationConnectorRepository stationConnectorRepository;
    @Mock private CompatibilityEngine compatibilityEngine;
    @Mock private GarageMapper mapper;

    private GarageServiceImpl service;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new GarageServiceImpl(
                customerVehicleRepository, variantListingRepository,
                stationConnectorRepository, compatibilityEngine, mapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VariantListing buildVariant() {
        VehicleMake make = new VehicleMake();
        make.setId(UUID.randomUUID());
        make.setName("Tata");

        VehicleModel model = new VehicleModel();
        model.setId(UUID.randomUUID());
        model.setName("Tiago EV");
        model.setMake(make);
        model.setModelYear(2024);

        ModelTrim trim = new ModelTrim();
        trim.setId(UUID.randomUUID());
        trim.setTrimName("XZ+");

        VehicleChargingSpec spec = VehicleChargingSpec.builder()
                .id(UUID.randomUUID())
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .maxAcceptedWattage(7200)
                .chargeTime10To80Pct(60)
                .build();

        ChargingConfiguration config = ChargingConfiguration.builder()
                .id(UUID.randomUUID())
                .onboardChargerKw(new BigDecimal("7.2"))
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .chargingSpecs(List.of(spec))
                .build();

        BatteryPack battery = BatteryPack.builder()
                .id(UUID.randomUUID())
                .capacityKwh(new BigDecimal("24.0"))
                .rangeKm(315)
                .build();

        return VariantListing.builder()
                .id(UUID.randomUUID())
                .displayLabel("Tiago EV XZ+ 24 kWh 7.2 kW AC")
                .model(model)
                .trim(trim)
                .batteryPack(battery)
                .chargingConfiguration(config)
                .build();
    }

    private CustomerVehicle buildCustomerVehicle(UUID userId, VariantListing vl,
                                                  String nickname, boolean isPrimary) {
        return CustomerVehicle.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .variantListing(vl)
                .nickname(nickname)
                .registrationNumber("TS09EF1234")
                .purchaseYear(2023)
                .isPrimary(isPrimary)
                .addedAt(Instant.now())
                .build();
    }

    private CustomerVehicleResponse fakeResponse(UUID id) {
        return new CustomerVehicleResponse(id, UUID.randomUUID(), null, null, null, null, false, null);
    }

    // ─── addVehicle ──────────────────────────────────────────────────────────

    @Test
    void addVehicle_savesAndReturnsResponse() {
        UUID userId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        AddVehicleRequest request = new AddVehicleRequest(vl.getId(), "My Car", "TS09EF1234", 2023, false);

        when(variantListingRepository.findByIdWithAllDetails(vl.getId())).thenReturn(Optional.of(vl));
        when(customerVehicleRepository.countByUserId(userId)).thenReturn(1L); // not first
        CustomerVehicle saved = buildCustomerVehicle(userId, vl, "My Car", false);
        when(customerVehicleRepository.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(fakeResponse(saved.getId()));

        CustomerVehicleResponse result = service.addVehicle(userId, request);

        assertThat(result).isNotNull();
        verify(customerVehicleRepository).save(any());
        verify(mapper).toResponse(saved);
    }

    @Test
    void addVehicle_setsAsPrimary_whenRequestedExplicitly() {
        UUID userId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        AddVehicleRequest request = new AddVehicleRequest(vl.getId(), null, null, null, true);

        when(variantListingRepository.findByIdWithAllDetails(vl.getId())).thenReturn(Optional.of(vl));
        when(customerVehicleRepository.countByUserId(userId)).thenReturn(2L);
        CustomerVehicle saved = buildCustomerVehicle(userId, vl, null, true);
        when(customerVehicleRepository.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(fakeResponse(saved.getId()));

        service.addVehicle(userId, request);

        verify(customerVehicleRepository).clearPrimaryFlagForUser(userId);
    }

    @Test
    void addVehicle_autoSetsPrimary_whenFirstVehicle() {
        UUID userId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        AddVehicleRequest request = new AddVehicleRequest(vl.getId(), null, null, null, false);

        when(variantListingRepository.findByIdWithAllDetails(vl.getId())).thenReturn(Optional.of(vl));
        when(customerVehicleRepository.countByUserId(userId)).thenReturn(0L); // first vehicle
        CustomerVehicle saved = buildCustomerVehicle(userId, vl, null, true);
        when(customerVehicleRepository.save(any(CustomerVehicle.class))).thenAnswer(inv -> {
            CustomerVehicle cv = inv.getArgument(0);
            assertThat(cv.getIsPrimary()).isTrue(); // first vehicle auto-primary
            return saved;
        });
        when(mapper.toResponse(saved)).thenReturn(fakeResponse(saved.getId()));

        service.addVehicle(userId, request);

        verify(customerVehicleRepository, never()).clearPrimaryFlagForUser(any());
    }

    @Test
    void addVehicle_throwsResourceNotFound_whenVariantMissing() {
        UUID userId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        AddVehicleRequest request = new AddVehicleRequest(missingId, null, null, null, null);

        when(variantListingRepository.findByIdWithAllDetails(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addVehicle(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    // ─── getGarage ───────────────────────────────────────────────────────────

    @Test
    void getGarage_delegatesToRepositoryAndMapper() {
        UUID userId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        List<CustomerVehicle> vehicles = List.of(buildCustomerVehicle(userId, vl, null, true));
        GarageResponse expected = new GarageResponse(List.of(), 1, vehicles.get(0).getId());

        when(customerVehicleRepository.findByUserIdOrderByAddedAtDesc(userId)).thenReturn(vehicles);
        when(mapper.toGarageResponse(vehicles)).thenReturn(expected);

        GarageResponse result = service.getGarage(userId);

        assertThat(result).isEqualTo(expected);
        verify(customerVehicleRepository).findByUserIdOrderByAddedAtDesc(userId);
        verify(mapper).toGarageResponse(vehicles);
    }

    @Test
    void getGarage_returnsEmpty_whenNoVehicles() {
        UUID userId = UUID.randomUUID();
        GarageResponse empty = new GarageResponse(List.of(), 0, null);

        when(customerVehicleRepository.findByUserIdOrderByAddedAtDesc(userId)).thenReturn(List.of());
        when(mapper.toGarageResponse(List.of())).thenReturn(empty);

        GarageResponse result = service.getGarage(userId);

        assertThat(result.totalCount()).isZero();
        assertThat(result.primaryVehicleId()).isNull();
    }

    // ─── getVehicleById ───────────────────────────────────────────────────────

    @Test
    void getVehicleById_returnsResponse_whenOwned() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, false);
        CustomerVehicleResponse expected = fakeResponse(vehicleId);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(cv));
        when(mapper.toResponse(cv)).thenReturn(expected);

        CustomerVehicleResponse result = service.getVehicleById(userId, vehicleId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getVehicleById_throws_whenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVehicleById(userId, vehicleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(vehicleId.toString());
    }

    // ─── updateVehicle ────────────────────────────────────────────────────────

    @Test
    void updateVehicle_updatesNickname() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, "Old Name", false);
        UpdateVehicleRequest request = new UpdateVehicleRequest("New Name", null, null, null);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(cv));
        when(customerVehicleRepository.save(cv)).thenReturn(cv);
        when(mapper.toResponse(cv)).thenReturn(fakeResponse(vehicleId));

        service.updateVehicle(userId, vehicleId, request);

        assertThat(cv.getNickname()).isEqualTo("New Name");
        verify(customerVehicleRepository).save(cv);
    }

    @Test
    void updateVehicle_promotesPrimary_whenRequested() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, false);
        UpdateVehicleRequest request = new UpdateVehicleRequest(null, null, null, true);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(cv));
        when(customerVehicleRepository.save(cv)).thenReturn(cv);
        when(mapper.toResponse(cv)).thenReturn(fakeResponse(vehicleId));

        service.updateVehicle(userId, vehicleId, request);

        verify(customerVehicleRepository).clearPrimaryFlagForUser(userId);
        assertThat(cv.getIsPrimary()).isTrue();
    }

    @Test
    void updateVehicle_demotesPrimary_whenSetToFalse() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, true);
        UpdateVehicleRequest request = new UpdateVehicleRequest(null, null, null, false);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(cv));
        when(customerVehicleRepository.save(cv)).thenReturn(cv);
        when(mapper.toResponse(cv)).thenReturn(fakeResponse(vehicleId));

        service.updateVehicle(userId, vehicleId, request);

        assertThat(cv.getIsPrimary()).isFalse();
    }

    @Test
    void updateVehicle_throws_whenNotOwned() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UpdateVehicleRequest request = new UpdateVehicleRequest("Name", null, null, null);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateVehicle(userId, vehicleId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── removeVehicle ────────────────────────────────────────────────────────

    @Test
    void removeVehicle_deletesVehicle() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, false);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(cv));
        when(customerVehicleRepository.findByUserIdOrderByAddedAtDesc(userId)).thenReturn(List.of());

        service.removeVehicle(userId, vehicleId);

        verify(customerVehicleRepository).delete(cv);
    }

    @Test
    void removeVehicle_autoPromotesNextVehicle_whenPrimaryDeleted() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle primary = buildCustomerVehicle(userId, vl, null, true);
        CustomerVehicle next    = buildCustomerVehicle(userId, vl, null, false);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(primary));
        when(customerVehicleRepository.findByUserIdOrderByAddedAtDesc(userId)).thenReturn(List.of(next));
        when(customerVehicleRepository.save(next)).thenReturn(next);

        service.removeVehicle(userId, vehicleId);

        assertThat(next.getIsPrimary()).isTrue();
        verify(customerVehicleRepository).save(next);
    }

    @Test
    void removeVehicle_doesNotPromote_whenDeletedWasNotPrimary() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, false);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(cv));

        service.removeVehicle(userId, vehicleId);

        verify(customerVehicleRepository).delete(cv);
        // findByUserIdOrderByAddedAtDesc not called since wasPrimary=false
        verify(customerVehicleRepository, never()).findByUserIdOrderByAddedAtDesc(userId);
    }

    @Test
    void removeVehicle_throws_whenNotOwned() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeVehicle(userId, vehicleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── setPrimaryVehicle ────────────────────────────────────────────────────

    @Test
    void setPrimaryVehicle_clearsFlagsAndSets() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, false);

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(cv));
        when(customerVehicleRepository.save(cv)).thenReturn(cv);
        when(mapper.toResponse(cv)).thenReturn(fakeResponse(vehicleId));

        service.setPrimaryVehicle(userId, vehicleId);

        verify(customerVehicleRepository).clearPrimaryFlagForUser(userId);
        assertThat(cv.getIsPrimary()).isTrue();
        verify(customerVehicleRepository).save(cv);
    }

    @Test
    void setPrimaryVehicle_isIdempotent_whenAlreadyPrimary() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, true); // already primary

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(cv));
        when(mapper.toResponse(cv)).thenReturn(fakeResponse(vehicleId));

        service.setPrimaryVehicle(userId, vehicleId);

        verify(customerVehicleRepository, never()).clearPrimaryFlagForUser(any());
        verify(customerVehicleRepository, never()).save(any());
    }

    @Test
    void setPrimaryVehicle_throws_whenNotOwned() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        when(customerVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setPrimaryVehicle(userId, vehicleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getCompatibleStations ────────────────────────────────────────────────

    @Test
    void getCompatibleStations_returnsEmpty_whenNoMatchingStations() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, "My EV", true);

        when(customerVehicleRepository.findByIdAndUserIdWithDetails(vehicleId, userId))
                .thenReturn(Optional.of(cv));
        when(stationConnectorRepository.findStationIdsByConnectorType(ConnectorType.TYPE2))
                .thenReturn(List.of());

        CompatibleStationsResponse result = service.getCompatibleStations(userId, vehicleId);

        assertThat(result.customerVehicleId()).isEqualTo(cv.getId());
        assertThat(result.stations()).isEmpty();
        assertThat(result.totalCount()).isZero();
        assertThat(result.vehicleDisplayLabel()).isEqualTo("My EV");
    }

    @Test
    void getCompatibleStations_returnsCompatibleStations() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, true);

        StationConnector sc = StationConnector.builder()
                .id(UUID.randomUUID())
                .stationId(stationId)
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .maxWattage(22000)
                .isOperational(true)
                .build();

        MatchedSpec matchedSpec = new MatchedSpec(
                UUID.randomUUID(), ConnectorType.TYPE2, 7200, 22000, 7200, 60, false);
        CompatibilityResult engineResult = new CompatibilityResult(
                vl.getId(), "Tiago EV XZ+", stationId, null,
                true, List.of(matchedSpec), 7200, 60, null);

        when(customerVehicleRepository.findByIdAndUserIdWithDetails(vehicleId, userId))
                .thenReturn(Optional.of(cv));
        when(stationConnectorRepository.findStationIdsByConnectorType(ConnectorType.TYPE2))
                .thenReturn(List.of(stationId));
        when(stationConnectorRepository.findByStationIdAndIsOperationalTrue(stationId))
                .thenReturn(List.of(sc));
        when(compatibilityEngine.checkVariantAgainstStation(vl, stationId, List.of(sc)))
                .thenReturn(engineResult);

        CompatibleStationsResponse result = service.getCompatibleStations(userId, vehicleId);

        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.stations()).hasSize(1);
        assertThat(result.stations().get(0).stationId()).isEqualTo(stationId);
        assertThat(result.stations().get(0).maxAchievableWattage()).isEqualTo(7200);
        assertThat(result.stations().get(0).matchedConnectorType()).isEqualTo(ConnectorType.TYPE2);
    }

    @Test
    void getCompatibleStations_usesVariantLabel_whenNicknameNull() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VariantListing vl = buildVariant();
        CustomerVehicle cv = buildCustomerVehicle(userId, vl, null, false);

        when(customerVehicleRepository.findByIdAndUserIdWithDetails(vehicleId, userId))
                .thenReturn(Optional.of(cv));
        when(stationConnectorRepository.findStationIdsByConnectorType(ConnectorType.TYPE2))
                .thenReturn(List.of());

        CompatibleStationsResponse result = service.getCompatibleStations(userId, vehicleId);

        assertThat(result.vehicleDisplayLabel()).isEqualTo("Tiago EV XZ+ 24 kWh 7.2 kW AC");
    }

    @Test
    void getCompatibleStations_throws_whenVehicleNotFound() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        when(customerVehicleRepository.findByIdAndUserIdWithDetails(vehicleId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCompatibleStations(userId, vehicleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(vehicleId.toString());
    }

    // ─── adminListVehicles ────────────────────────────────────────────────────

    @Test
    void adminListVehicles_delegatesToRepositoryAndMapper() {
        UUID filterUserId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 20);
        Page<CustomerVehicle> page = new PageImpl<>(List.of());
        AdminGaragePageResponse expected = new AdminGaragePageResponse(List.of(), 0, 20, 0, 0, true);

        when(customerVehicleRepository.findAllByOptionalUserId(filterUserId, pageable)).thenReturn(page);
        when(mapper.toAdminPageResponse(page)).thenReturn(expected);

        AdminGaragePageResponse result = service.adminListVehicles(filterUserId, pageable);

        assertThat(result).isEqualTo(expected);
        verify(customerVehicleRepository).findAllByOptionalUserId(filterUserId, pageable);
    }

    @Test
    void adminListVehicles_withNullUserId_passesNullToRepo() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<CustomerVehicle> page = new PageImpl<>(List.of());
        AdminGaragePageResponse expected = new AdminGaragePageResponse(List.of(), 0, 20, 0, 0, true);

        when(customerVehicleRepository.findAllByOptionalUserId(null, pageable)).thenReturn(page);
        when(mapper.toAdminPageResponse(page)).thenReturn(expected);

        AdminGaragePageResponse result = service.adminListVehicles(null, pageable);

        assertThat(result).isEqualTo(expected);
        verify(customerVehicleRepository).findAllByOptionalUserId(null, pageable);
    }
}
