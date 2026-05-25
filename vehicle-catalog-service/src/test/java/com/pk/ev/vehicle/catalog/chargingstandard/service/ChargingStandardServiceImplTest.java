package com.pk.ev.vehicle.catalog.chargingstandard.service;

import com.pk.ev.vehicle.catalog.chargingstandard.dto.ChargingStandardDtos.*;
import com.pk.ev.vehicle.catalog.chargingstandard.enums.ChargingStandardType;
import com.pk.ev.vehicle.catalog.chargingstandard.mapper.ChargingStandardMapper;
import com.pk.ev.vehicle.catalog.chargingstandard.model.ChargingStandard;
import com.pk.ev.vehicle.catalog.chargingstandard.repository.ChargingStandardRepository;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.repository.VehicleChargingSpecRepository;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.variantlisting.repository.VariantListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ChargingStandardServiceImplTest {

    @Mock
    private ChargingStandardRepository repository;
    @Mock
    private VehicleChargingSpecRepository specRepository;
    @Mock
    private VariantListingRepository variantListingRepository;
    @Mock
    private ChargingStandardMapper mapper;

    private ChargingStandardServiceImpl service;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new ChargingStandardServiceImpl(repository, specRepository, variantListingRepository, mapper);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void createStandard_createsWhenUnique() {
        CreateChargingStandardRequest req = new CreateChargingStandardRequest(
                "CCS Combo 2", "ccs2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                "Europe", "IEC", "2.0", "Fast DC", null
        );

        when(repository.existsByShortCode("CCS2")).thenReturn(false);
        when(repository.existsByName("CCS Combo 2")).thenReturn(false);

        ChargingStandard saved = new ChargingStandard();
        saved.setId(UUID.randomUUID());
        when(mapper.toEntity(req)).thenReturn(new ChargingStandard());
        when(repository.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(new ChargingStandardResponse(
                saved.getId(), "CCS Combo 2", "CCS2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                "Europe", "IEC", "2.0", "Fast DC", null, false, null, null
        ));

        ChargingStandardResponse resp = service.createStandard(req);

        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(saved.getId());
        verify(repository).save(any());
    }

    @Test
    void createStandard_throwsOnDuplicateShortCode() {
        CreateChargingStandardRequest req = new CreateChargingStandardRequest(
                "CCS Combo 2", "CCS2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                null, null, null, null, null
        );

        when(repository.existsByShortCode("CCS2")).thenReturn(true);

        assertThatThrownBy(() -> service.createStandard(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CCS2");
    }

    @Test
    void createStandard_throwsOnDuplicateName() {
        CreateChargingStandardRequest req = new CreateChargingStandardRequest(
                "CCS Combo 2", "ccs2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                null, null, null, null, null
        );

        when(repository.existsByShortCode("CCS2")).thenReturn(false);
        when(repository.existsByName("CCS Combo 2")).thenReturn(true);

        assertThatThrownBy(() -> service.createStandard(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CCS Combo 2");
    }

    @Test
    void getAllStandards_returnsFilteredAndPaged() {
        StandardFilterParams filters = new StandardFilterParams(
                "Europe", ChargingStandardType.AC, ConnectorType.TYPE2, false, 0, 50, "name"
        );

        ChargingStandard std = ChargingStandard.builder()
                .id(UUID.randomUUID())
                .name("Type 2")
                .shortCode("TYPE2")
                .build();

        Page<ChargingStandard> page = new PageImpl<>(List.of(std), PageRequest.of(0, 50), 1);
        when(repository.findAllByFilters(eq("Europe"), eq(ChargingStandardType.AC), eq(ConnectorType.TYPE2), eq(false), any()))
                .thenReturn(page);
        when(mapper.toPagedResponse(page)).thenReturn(new PagedStandardsResponse(List.of(), 0, 50, 1, 1, true));

        PagedStandardsResponse resp = service.getAllStandards(filters);

        assertThat(resp.page()).isEqualTo(0);
        verify(repository).findAllByFilters(eq("Europe"), eq(ChargingStandardType.AC), eq(ConnectorType.TYPE2), eq(false), any());
    }

    @Test
    void getStandardById_returnsWhenFound() {
        UUID id = UUID.randomUUID();
        ChargingStandard std = new ChargingStandard();
        std.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(std));
        when(mapper.toResponse(std)).thenReturn(new ChargingStandardResponse(
                id, "Name", "CODE", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                null, null, null, null, null, false, null, null
        ));

        ChargingStandardResponse resp = service.getStandardById(id);

        assertThat(resp.id()).isEqualTo(id);
    }

    @Test
    void getStandardById_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStandardById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getStandardByShortCode_returnsWhenFound() {
        ChargingStandard std = new ChargingStandard();
        std.setId(UUID.randomUUID());

        when(repository.findByShortCode("CCS2")).thenReturn(Optional.of(std));
        when(mapper.toResponse(std)).thenReturn(new ChargingStandardResponse(
                std.getId(), "CCS Combo 2", "CCS2", ConnectorType.CCS2, ChargingStandardType.BOTH, 350000,
                null, null, null, null, null, false, null, null
        ));

        ChargingStandardResponse resp = service.getStandardByShortCode("ccs2");

        assertThat(resp.shortCode()).isEqualTo("CCS2");
    }

    @Test
    void getStandardByShortCode_throwsWhenNotFound() {
        when(repository.findByShortCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStandardByShortCode("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStandard_updatesWhenValid() {
        UUID id = UUID.randomUUID();
        ChargingStandard std = new ChargingStandard();
        std.setId(id);
        std.setName("Old");
        std.setShortCode("OLD");

        UpdateChargingStandardRequest req = new UpdateChargingStandardRequest(
                "New", "new", null, null, null, null, null, null, null, null, null
        );

        when(repository.findById(id)).thenReturn(Optional.of(std));
        when(repository.existsByShortCode("NEW")).thenReturn(false);
        when(repository.existsByName("New")).thenReturn(false);
        when(repository.save(std)).thenReturn(std);
        when(mapper.toResponse(std)).thenReturn(new ChargingStandardResponse(
                id, "New", "NEW", null, null, null, null, null, null, null, null, false, null, null
        ));

        ChargingStandardResponse resp = service.updateStandard(id, req);

        assertThat(resp.name()).isEqualTo("New");
        verify(mapper).applyUpdate(req, std);
    }

    @Test
    void updateStandard_guardsDuplicateShortCode() {
        UUID id = UUID.randomUUID();
        ChargingStandard std = new ChargingStandard();
        std.setShortCode("OLD");

        UpdateChargingStandardRequest req = new UpdateChargingStandardRequest(
                null, "new", null, null, null, null, null, null, null, null, null
        );

        when(repository.findById(id)).thenReturn(Optional.of(std));
        when(repository.existsByShortCode("NEW")).thenReturn(true);

        assertThatThrownBy(() -> service.updateStandard(id, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("NEW");
    }

    @Test
    void deprecateStandard_setsDeprecatedFlag() {
        UUID id = UUID.randomUUID();
        ChargingStandard std = new ChargingStandard();
        std.setId(id);
        std.setIsDeprecated(false);

        when(repository.findById(id)).thenReturn(Optional.of(std));
        when(specRepository.findByChargingConfigurationId(id)).thenReturn(List.of());

        service.deprecateStandard(id);

        assertThat(std.getIsDeprecated()).isTrue();
        verify(repository).save(std);
    }

    @Test
    void getConnectorTypes_delegatesToMapper() {
        service.getConnectorTypes();

        verify(mapper).getAllConnectorTypeMetadata();
    }

    @Test
    void getDistinctRegions_returnsFromRepository() {
        List<String> regions = List.of("Europe", "India", "USA");
        when(repository.findDistinctRegions()).thenReturn(regions);

        List<String> result = service.getDistinctRegions();

        assertThat(result).isEqualTo(regions);
    }

    @Test
    void getDistinctGoverningBodies_returnsFromRepository() {
        List<String> bodies = List.of("IEC", "SAE", "BIS");
        when(repository.findDistinctGoverningBodies()).thenReturn(bodies);

        List<String> result = service.getDistinctGoverningBodies();

        assertThat(result).isEqualTo(bodies);
    }
}

