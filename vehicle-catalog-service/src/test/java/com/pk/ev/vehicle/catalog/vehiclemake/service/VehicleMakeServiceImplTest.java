package com.pk.ev.vehicle.catalog.vehiclemake.service;

import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.vehiclemake.dto.VehicleMakeDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemake.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.vehiclemake.mapper.VehicleMakeMapper;
import com.pk.ev.vehicle.catalog.vehiclemake.model.MakeRegion;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemake.repository.MakeRegionRepository;
import com.pk.ev.vehicle.catalog.vehiclemake.repository.VehicleMakeRepository;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VehicleMakeServiceImplTest {

    @Mock private VehicleMakeRepository makeRepository;
    @Mock private MakeRegionRepository  regionRepository;
    @Mock private VehicleMakeMapper     mapper;

    private VehicleMakeServiceImpl service;
    private AutoCloseable mocks;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private UUID makeId;
    private VehicleMake make;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new VehicleMakeServiceImpl(makeRepository, regionRepository, mapper);

        makeId = UUID.randomUUID();
        make   = VehicleMake.builder()
                .id(makeId)
                .name("Tata Motors")
                .slug("tata-motors")
                .countryOfOrigin("IN")
                .status(MakeStatus.ACTIVE)
                .models(new ArrayList<>())
                .regions(new ArrayList<>())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private MakeResponse makeResponse(UUID id, String name) {
        return new MakeResponse(id, name, name.toLowerCase(), "IN",
                null, null, MakeStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private MakeRegion region(UUID id, String code, Integer year) {
        return MakeRegion.builder().id(id).make(make).regionCode(code).launchYear(year).build();
    }

    // ─── createMake ──────────────────────────────────────────────────────────

    @Test
    void createMake_success_savesAndReturnsResponse() {
        CreateMakeRequest req = new CreateMakeRequest("Tata Motors", "IN", null, null);
        MakeResponse expected = makeResponse(makeId, "Tata Motors");

        when(makeRepository.existsByName("Tata Motors")).thenReturn(false);
        when(mapper.toEntity(req)).thenReturn(make);
        when(makeRepository.save(make)).thenReturn(make);
        when(mapper.toResponse(make)).thenReturn(expected);

        MakeResponse result = service.createMake(req);

        assertThat(result).isEqualTo(expected);
        verify(makeRepository).save(make);
    }

    @Test
    void createMake_duplicateName_throwsDuplicateResourceException() {
        CreateMakeRequest req = new CreateMakeRequest("Tata Motors", "IN", null, null);

        when(makeRepository.existsByName("Tata Motors")).thenReturn(true);

        assertThatThrownBy(() -> service.createMake(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Tata Motors");

        verify(makeRepository, never()).save(any());
    }

    @Test
    void createMake_delegatesToMapperForEntityCreation() {
        CreateMakeRequest req = new CreateMakeRequest("Mahindra", "IN", "https://logo.png", null);
        MakeResponse expected = makeResponse(makeId, "Mahindra");

        when(makeRepository.existsByName("Mahindra")).thenReturn(false);
        when(mapper.toEntity(req)).thenReturn(make);
        when(makeRepository.save(make)).thenReturn(make);
        when(mapper.toResponse(make)).thenReturn(expected);

        service.createMake(req);

        verify(mapper).toEntity(req);
    }

    // ─── getAllMakes ──────────────────────────────────────────────────────────

    @Test
    void getAllMakes_noFilters_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<VehicleMake> page = new PageImpl<>(List.of(make));
        PagedMakesResponse paged = new PagedMakesResponse(List.of(), 0, 20, 1L, 1, true);

        when(makeRepository.findAllByFilters(null, null, pageable)).thenReturn(page);
        when(mapper.toPagedResponse(page)).thenReturn(paged);

        PagedMakesResponse result = service.getAllMakes(null, null, pageable);

        assertThat(result).isEqualTo(paged);
        verify(makeRepository).findAllByFilters(null, null, pageable);
    }

    @Test
    void getAllMakes_withStatusAndCountry_passesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<VehicleMake> page = new PageImpl<>(List.of(make));
        PagedMakesResponse paged = new PagedMakesResponse(List.of(), 0, 10, 1L, 1, true);

        when(makeRepository.findAllByFilters(MakeStatus.ACTIVE, "IN", pageable)).thenReturn(page);
        when(mapper.toPagedResponse(page)).thenReturn(paged);

        PagedMakesResponse result = service.getAllMakes(MakeStatus.ACTIVE, "IN", pageable);

        assertThat(result).isEqualTo(paged);
        verify(makeRepository).findAllByFilters(MakeStatus.ACTIVE, "IN", pageable);
    }

    @Test
    void getAllMakes_emptyRepository_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<VehicleMake> emptyPage = Page.empty();
        PagedMakesResponse emptyPaged = new PagedMakesResponse(List.of(), 0, 20, 0L, 0, true);

        when(makeRepository.findAllByFilters(null, null, pageable)).thenReturn(emptyPage);
        when(mapper.toPagedResponse(emptyPage)).thenReturn(emptyPaged);

        PagedMakesResponse result = service.getAllMakes(null, null, pageable);

        assertThat(result.content()).isEmpty();
    }

    // ─── getMakeById ─────────────────────────────────────────────────────────

    @Test
    void getMakeById_found_returnsMappedResponse() {
        MakeResponse expected = makeResponse(makeId, "Tata Motors");

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(mapper.toResponse(make)).thenReturn(expected);

        MakeResponse result = service.getMakeById(makeId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getMakeById_notFound_throwsResourceNotFoundException() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMakeById(makeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(makeId.toString());
    }

    // ─── updateMake ──────────────────────────────────────────────────────────

    @Test
    void updateMake_updatesAndReturnsResponse() {
        UpdateMakeRequest req = new UpdateMakeRequest("Tata EV", null, null, null, null);
        MakeResponse expected = makeResponse(makeId, "Tata EV");

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(makeRepository.existsByName("Tata EV")).thenReturn(false);
        when(makeRepository.save(make)).thenReturn(make);
        when(mapper.toResponse(make)).thenReturn(expected);

        MakeResponse result = service.updateMake(makeId, req);

        assertThat(result).isEqualTo(expected);
        verify(mapper).applyUpdate(req, make);
        verify(makeRepository).save(make);
    }

    @Test
    void updateMake_sameNameAsExisting_doesNotCheckDuplicate() {
        // req.name() == make.getName() — no duplicate check needed
        UpdateMakeRequest req = new UpdateMakeRequest("Tata Motors", null, null, null, null);
        MakeResponse expected = makeResponse(makeId, "Tata Motors");

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(makeRepository.save(make)).thenReturn(make);
        when(mapper.toResponse(make)).thenReturn(expected);

        service.updateMake(makeId, req);

        verify(makeRepository, never()).existsByName(any());
    }

    @Test
    void updateMake_differentNameAlreadyTaken_throwsDuplicateResourceException() {
        UpdateMakeRequest req = new UpdateMakeRequest("Mahindra", null, null, null, null);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(makeRepository.existsByName("Mahindra")).thenReturn(true);

        assertThatThrownBy(() -> service.updateMake(makeId, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mahindra");

        verify(makeRepository, never()).save(any());
    }

    @Test
    void updateMake_nullName_skipsNameDuplicateCheck() {
        UpdateMakeRequest req = new UpdateMakeRequest(null, "US", null, null, MakeStatus.INACTIVE);
        MakeResponse expected = makeResponse(makeId, "Tata Motors");

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(makeRepository.save(make)).thenReturn(make);
        when(mapper.toResponse(make)).thenReturn(expected);

        service.updateMake(makeId, req);

        verify(makeRepository, never()).existsByName(any());
    }

    @Test
    void updateMake_makeNotFound_throwsResourceNotFoundException() {
        UpdateMakeRequest req = new UpdateMakeRequest("X", null, null, null, null);
        when(makeRepository.findById(makeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMake(makeId, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteMake ──────────────────────────────────────────────────────────

    @Test
    void deleteMake_setsMakeStatusToInactive() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(makeRepository.save(make)).thenReturn(make);

        service.deleteMake(makeId);

        assertThat(make.getStatus()).isEqualTo(MakeStatus.INACTIVE);
        verify(makeRepository).save(make);
    }

    @Test
    void deleteMake_cascadesToChildModels() {
        VehicleModel model1 = new VehicleModel();
        model1.setId(UUID.randomUUID());
        model1.setStatus(ModelStatus.ACTIVE);

        VehicleModel model2 = new VehicleModel();
        model2.setId(UUID.randomUUID());
        model2.setStatus(ModelStatus.ACTIVE);

        make.getModels().addAll(List.of(model1, model2));

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(makeRepository.save(make)).thenReturn(make);

        service.deleteMake(makeId);

        assertThat(model1.getStatus()).isEqualTo(ModelStatus.INACTIVE);
        assertThat(model2.getStatus()).isEqualTo(ModelStatus.INACTIVE);
    }

    @Test
    void deleteMake_noChildModels_stillSoftDeletes() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(makeRepository.save(make)).thenReturn(make);

        service.deleteMake(makeId);

        assertThat(make.getStatus()).isEqualTo(MakeStatus.INACTIVE);
        verify(makeRepository).save(make);
    }

    @Test
    void deleteMake_doesNotHardDelete() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(makeRepository.save(make)).thenReturn(make);

        service.deleteMake(makeId);

        verify(makeRepository, never()).deleteById(any());
        verify(makeRepository, never()).delete(any());
    }

    @Test
    void deleteMake_makeNotFound_throwsResourceNotFoundException() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMake(makeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(makeId.toString());
    }

    // ─── getModelsByMake ──────────────────────────────────────────────────────

    @Test
    void getModelsByMake_returnsModelsFromMake() {
        VehicleModel model = new VehicleModel();
        model.setId(UUID.randomUUID());
        model.setName("Tiago EV");
        make.getModels().add(model);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));

        List<VehicleModel> result = service.getModelsByMake(makeId);

        assertThat(result).hasSize(1).containsExactly(model);
    }

    @Test
    void getModelsByMake_noModels_returnsEmptyList() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));

        List<VehicleModel> result = service.getModelsByMake(makeId);

        assertThat(result).isEmpty();
    }

    @Test
    void getModelsByMake_makeNotFound_throwsResourceNotFoundException() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getModelsByMake(makeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── associateRegions ─────────────────────────────────────────────────────

    @Test
    void associateRegions_savesNewRegions() {
        AssociateRegionsRequest req = new AssociateRegionsRequest(
                List.of(new AssociateRegionsRequest.RegionEntry("IN", 2020))
        );
        MakeRegion saved = region(UUID.randomUUID(), "IN", 2020);
        RegionResponse resp = new RegionResponse(saved.getId(), "IN", 2020);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(regionRepository.existsByMakeIdAndRegionCode(makeId, "IN")).thenReturn(false);
        when(regionRepository.findByMakeId(makeId)).thenReturn(List.of(saved));
        when(mapper.toRegionResponseList(List.of(saved))).thenReturn(List.of(resp));

        List<RegionResponse> result = service.associateRegions(makeId, req);

        verify(regionRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 1));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).regionCode()).isEqualTo("IN");
    }

    @Test
    void associateRegions_skipsAlreadyExistingRegions() {
        AssociateRegionsRequest req = new AssociateRegionsRequest(
                List.of(
                        new AssociateRegionsRequest.RegionEntry("IN", 2020), // exists
                        new AssociateRegionsRequest.RegionEntry("DE", 2022)  // new
                )
        );
        MakeRegion existing = region(UUID.randomUUID(), "IN", 2020);
        MakeRegion newRegion = region(UUID.randomUUID(), "DE", 2022);
        List<MakeRegion> all = List.of(existing, newRegion);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(regionRepository.existsByMakeIdAndRegionCode(makeId, "IN")).thenReturn(true);
        when(regionRepository.existsByMakeIdAndRegionCode(makeId, "DE")).thenReturn(false);
        when(regionRepository.findByMakeId(makeId)).thenReturn(all);
        when(mapper.toRegionResponseList(all)).thenReturn(
                List.of(new RegionResponse(existing.getId(), "IN", 2020),
                        new RegionResponse(newRegion.getId(), "DE", 2022)));

        List<RegionResponse> result = service.associateRegions(makeId, req);

        // Only "DE" should be saved
        verify(regionRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 1));
        assertThat(result).hasSize(2);
    }

    @Test
    void associateRegions_allAlreadyExist_savesEmpty() {
        AssociateRegionsRequest req = new AssociateRegionsRequest(
                List.of(new AssociateRegionsRequest.RegionEntry("IN", 2020))
        );
        MakeRegion existing = region(UUID.randomUUID(), "IN", 2020);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(regionRepository.existsByMakeIdAndRegionCode(makeId, "IN")).thenReturn(true);
        when(regionRepository.findByMakeId(makeId)).thenReturn(List.of(existing));
        when(mapper.toRegionResponseList(any())).thenReturn(
                List.of(new RegionResponse(existing.getId(), "IN", 2020)));

        service.associateRegions(makeId, req);

        verify(regionRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
    }

    @Test
    void associateRegions_makeNotFound_throwsResourceNotFoundException() {
        AssociateRegionsRequest req = new AssociateRegionsRequest(
                List.of(new AssociateRegionsRequest.RegionEntry("IN", 2020))
        );
        when(makeRepository.findById(makeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.associateRegions(makeId, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getRegionsByMake ─────────────────────────────────────────────────────

    @Test
    void getRegionsByMake_returnsRegionList() {
        MakeRegion r1 = region(UUID.randomUUID(), "IN", 2020);
        MakeRegion r2 = region(UUID.randomUUID(), "DE", 2022);
        RegionResponse rr1 = new RegionResponse(r1.getId(), "IN", 2020);
        RegionResponse rr2 = new RegionResponse(r2.getId(), "DE", 2022);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(regionRepository.findByMakeId(makeId)).thenReturn(List.of(r1, r2));
        when(mapper.toRegionResponseList(List.of(r1, r2))).thenReturn(List.of(rr1, rr2));

        List<RegionResponse> result = service.getRegionsByMake(makeId);

        assertThat(result).hasSize(2)
                .extracting(RegionResponse::regionCode)
                .containsExactlyInAnyOrder("IN", "DE");
    }

    @Test
    void getRegionsByMake_noRegions_returnsEmpty() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(regionRepository.findByMakeId(makeId)).thenReturn(List.of());
        when(mapper.toRegionResponseList(List.of())).thenReturn(List.of());

        List<RegionResponse> result = service.getRegionsByMake(makeId);

        assertThat(result).isEmpty();
    }

    @Test
    void getRegionsByMake_makeNotFound_throwsResourceNotFoundException() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRegionsByMake(makeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── findMakeOrThrow ─────────────────────────────────────────────────────

    @Test
    void findMakeOrThrow_found_returnsMake() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));

        VehicleMake result = service.findMakeOrThrow(makeId);

        assertThat(result).isEqualTo(make);
    }

    @Test
    void findMakeOrThrow_notFound_throwsWithMakeIdInMessage() {
        when(makeRepository.findById(makeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findMakeOrThrow(makeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(makeId.toString());
    }
}
