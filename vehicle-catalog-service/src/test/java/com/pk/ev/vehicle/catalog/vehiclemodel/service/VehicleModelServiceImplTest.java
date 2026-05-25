package com.pk.ev.vehicle.catalog.vehiclemodel.service;

import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemake.repository.VehicleMakeRepository;
import com.pk.ev.vehicle.catalog.vehiclemodel.dtos.VehicleModelDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.BodyType;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.DriveType;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ImageAngle;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.vehiclemodel.mapper.VehicleModelMapper;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.ModelImage;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.vehiclemodel.repository.ModelImageRepository;
import com.pk.ev.vehicle.catalog.vehiclemodel.repository.VehicleModelRepository;
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

class VehicleModelServiceImplTest {

    @Mock private VehicleModelRepository modelRepository;
    @Mock private VehicleMakeRepository  makeRepository;
    @Mock private ModelImageRepository   imageRepository;
    @Mock private VehicleModelMapper     modelMapper;

    private VehicleModelServiceImpl service;
    private AutoCloseable mocks;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private UUID makeId;
    private UUID modelId;
    private VehicleMake make;
    private VehicleModel model;

    @BeforeEach
    void setUp() {
        mocks   = MockitoAnnotations.openMocks(this);
        service = new VehicleModelServiceImpl(modelRepository, makeRepository, imageRepository, modelMapper);

        makeId  = UUID.randomUUID();
        modelId = UUID.randomUUID();

        make = VehicleMake.builder()
                .id(makeId)
                .name("Tata Motors")
                .slug("tata-motors")
                .build();

        model = VehicleModel.builder()
                .id(modelId)
                .make(make)
                .name("Tiago EV")
                .modelYear(2023)
                .weightKg(1200)
                .bodyType(BodyType.HATCHBACK)
                .seatingCapacity(5)
                .driveType(DriveType.FWD)
                .status(ModelStatus.ACTIVE)
                .images(new ArrayList<>())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ModelResponse modelResponse(UUID id) {
        return new ModelResponse(id, makeId, "Tata Motors", "Tiago EV",
                2023, 1200, BodyType.HATCHBACK, 5, DriveType.FWD,
                ModelStatus.ACTIVE, List.of(), Instant.now(), Instant.now());
    }

    private ModelImage buildImage(UUID imageId, boolean isPrimary) {
        return ModelImage.builder()
                .id(imageId).model(model)
                .url("https://cdn.example.com/img.jpg")
                .isPrimary(isPrimary)
                .angle(ImageAngle.FRONT)
                .build();
    }

    // ─── createModel ─────────────────────────────────────────────────────────

    @Test
    void createModel_success_savesAndReturnsResponse() {
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Tiago EV", null, 2023, null, null, null, 1200,
                BodyType.HATCHBACK, 5, DriveType.FWD);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(modelRepository.existsByMakeIdAndNameAndModelYear(makeId, "Tiago EV", 2023))
                .thenReturn(false);
        when(modelMapper.toEntity(req, make)).thenReturn(model);
        when(modelRepository.save(model)).thenReturn(model);
        when(modelMapper.toResponse(model)).thenReturn(modelResponse(modelId));

        ModelResponse result = service.createModel(req);

        assertThat(result.id()).isEqualTo(modelId);
        verify(modelRepository).save(model);
    }

    @Test
    void createModel_makeNotFound_throwsResourceNotFoundException() {
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Tiago EV", null, 2023, null, null, null, null, null, null, null);

        when(makeRepository.findById(makeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createModel(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(makeId.toString());

        verify(modelRepository, never()).save(any());
    }

    @Test
    void createModel_duplicateModelYear_throwsDuplicateResourceException() {
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Tiago EV", null, 2023, null, null, null, null, null, null, null);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(modelRepository.existsByMakeIdAndNameAndModelYear(makeId, "Tiago EV", 2023))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createModel(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Tiago EV")
                .hasMessageContaining("2023");

        verify(modelRepository, never()).save(any());
    }

    @Test
    void createModel_delegatesToMapperForEntityCreation() {
        CreateModelRequest req = new CreateModelRequest(
                makeId, "Tiago EV", null, 2023, null, null, null, 1200,
                BodyType.HATCHBACK, 5, DriveType.FWD);

        when(makeRepository.findById(makeId)).thenReturn(Optional.of(make));
        when(modelRepository.existsByMakeIdAndNameAndModelYear(any(), any(), any())).thenReturn(false);
        when(modelMapper.toEntity(req, make)).thenReturn(model);
        when(modelRepository.save(model)).thenReturn(model);
        when(modelMapper.toResponse(model)).thenReturn(modelResponse(modelId));

        service.createModel(req);

        verify(modelMapper).toEntity(req, make);
    }

    // ─── getAllModels ─────────────────────────────────────────────────────────

    @Test
    void getAllModels_noFilters_returnsMappedPage() {
        Page<VehicleModel> page = new PageImpl<>(List.of(model));
        PagedModelsResponse expected = new PagedModelsResponse(List.of(), 0, 20, 1L, 1, true);

        when(modelRepository.findAllByFilters(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);
        when(modelMapper.toPagedResponse(page)).thenReturn(expected);

        ModelFilterParams filters = new ModelFilterParams(
                null, null, null, null, null, null, 0, 20, "name");

        PagedModelsResponse result = service.getAllModels(filters);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAllModels_withFilters_passesToRepository() {
        Page<VehicleModel> page = new PageImpl<>(List.of(model));
        PagedModelsResponse expected = new PagedModelsResponse(List.of(), 0, 10, 1L, 1, true);

        when(modelRepository.findAllByFilters(
                eq(makeId), eq(2023), eq(ModelStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);
        when(modelMapper.toPagedResponse(page)).thenReturn(expected);

        ModelFilterParams filters = new ModelFilterParams(
                makeId, 2023, null, null, null, ModelStatus.ACTIVE, 0, 10, "name");

        PagedModelsResponse result = service.getAllModels(filters);

        assertThat(result).isEqualTo(expected);
        verify(modelRepository).findAllByFilters(
                eq(makeId), eq(2023), eq(ModelStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void getAllModels_emptyPage_returnsEmptyContent() {
        Page<VehicleModel> emptyPage = Page.empty();
        PagedModelsResponse emptyPaged = new PagedModelsResponse(List.of(), 0, 20, 0L, 0, true);

        when(modelRepository.findAllByFilters(any(), any(), any(), any())).thenReturn(emptyPage);
        when(modelMapper.toPagedResponse(emptyPage)).thenReturn(emptyPaged);

        ModelFilterParams filters = new ModelFilterParams(
                null, null, null, null, null, null, 0, 20, "name");

        PagedModelsResponse result = service.getAllModels(filters);

        assertThat(result.content()).isEmpty();
    }

    // ─── getModelById ─────────────────────────────────────────────────────────

    @Test
    void getModelById_found_returnsMappedResponse() {
        ModelResponse expected = modelResponse(modelId);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelMapper.toResponse(model)).thenReturn(expected);

        ModelResponse result = service.getModelById(modelId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getModelById_notFound_throwsResourceNotFoundException() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getModelById(modelId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(modelId.toString());
    }

    // ─── updateModel ─────────────────────────────────────────────────────────

    @Test
    void updateModel_appliesChangesAndReturns() {
        UpdateModelRequest req = new UpdateModelRequest(
                "Tiago EV Pro", null, 2024, null, null, null, null, null, null, null, null);
        ModelResponse expected = modelResponse(modelId);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.existsByMakeIdAndNameAndModelYear(makeId, "Tiago EV Pro", 2024))
                .thenReturn(false);
        when(modelRepository.save(model)).thenReturn(model);
        when(modelMapper.toResponse(model)).thenReturn(expected);

        service.updateModel(modelId, req);

        verify(modelMapper).applyUpdate(req, model);
        verify(modelRepository).save(model);
    }

    @Test
    void updateModel_sameNameAndYear_doesNotThrowDuplicate() {
        // Updating to the same name+year of the same record is allowed
        UpdateModelRequest req = new UpdateModelRequest(
                "Tiago EV", null, 2023, null, null, null, null, null, null, null, null);
        ModelResponse expected = modelResponse(modelId);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.existsByMakeIdAndNameAndModelYear(makeId, "Tiago EV", 2023))
                .thenReturn(true); // exists but it's the same record
        when(modelRepository.save(model)).thenReturn(model);
        when(modelMapper.toResponse(model)).thenReturn(expected);

        assertThatNoException().isThrownBy(() -> service.updateModel(modelId, req));
    }

    @Test
    void updateModel_differentNameYearAlreadyTaken_throwsDuplicate() {
        // A *different* model with "Nexon EV 2023" exists
        UpdateModelRequest req = new UpdateModelRequest(
                "Nexon EV", null, 2023, null, null, null, null, null, null, null, null);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.existsByMakeIdAndNameAndModelYear(makeId, "Nexon EV", 2023))
                .thenReturn(true); // exists and it's a different record

        assertThatThrownBy(() -> service.updateModel(modelId, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Nexon EV");
    }

    @Test
    void updateModel_onlyYearProvided_skipsNameDuplicateCheck() {
        // Only year is provided — name is null, so no collision check
        UpdateModelRequest req = new UpdateModelRequest(
                null, null, 2025, null, null, null, null, null, null, null, null);
        ModelResponse expected = modelResponse(modelId);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.save(model)).thenReturn(model);
        when(modelMapper.toResponse(model)).thenReturn(expected);

        service.updateModel(modelId, req);

        verify(modelRepository, never()).existsByMakeIdAndNameAndModelYear(any(), any(), any());
    }

    @Test
    void updateModel_modelNotFound_throws() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());
        UpdateModelRequest req = new UpdateModelRequest(
                null, null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.updateModel(modelId, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteModel ─────────────────────────────────────────────────────────

    @Test
    void deleteModel_setsStatusToDiscontinued() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.save(model)).thenReturn(model);

        service.deleteModel(modelId);

        assertThat(model.getStatus()).isEqualTo(ModelStatus.DISCONTINUED);
        verify(modelRepository).save(model);
    }

    @Test
    void deleteModel_doesNotHardDelete() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.save(model)).thenReturn(model);

        service.deleteModel(modelId);

        verify(modelRepository, never()).deleteById(any());
        verify(modelRepository, never()).delete(any());
    }

    @Test
    void deleteModel_notFound_throws() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteModel(modelId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(modelId.toString());
    }

    // ─── updateModelStatus ───────────────────────────────────────────────────

    @Test
    void updateModelStatus_setsNewStatus() {
        UpdateModelStatusRequest req = new UpdateModelStatusRequest(ModelStatus.INACTIVE);
        ModelResponse expected = modelResponse(modelId);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.save(model)).thenReturn(model);
        when(modelMapper.toResponse(model)).thenReturn(expected);

        service.updateModelStatus(modelId, req);

        assertThat(model.getStatus()).isEqualTo(ModelStatus.INACTIVE);
        verify(modelRepository).save(model);
    }

    @Test
    void updateModelStatus_toDiscontinued_works() {
        UpdateModelStatusRequest req = new UpdateModelStatusRequest(ModelStatus.DISCONTINUED);
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.save(model)).thenReturn(model);
        when(modelMapper.toResponse(model)).thenReturn(modelResponse(modelId));

        service.updateModelStatus(modelId, req);

        assertThat(model.getStatus()).isEqualTo(ModelStatus.DISCONTINUED);
    }

    @Test
    void updateModelStatus_notFound_throws() {
        UpdateModelStatusRequest req = new UpdateModelStatusRequest(ModelStatus.INACTIVE);
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateModelStatus(modelId, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── addImage ────────────────────────────────────────────────────────────

    @Test
    void addImage_primaryFlag_clearsPreviousPrimary() {
        UUID imageId = UUID.randomUUID();
        UploadImageRequest req = new UploadImageRequest(
                "https://cdn.example.com/img.jpg", true, ImageAngle.FRONT);
        ModelImage savedImage = buildImage(imageId, true);
        ModelImageResponse expected = new ModelImageResponse(
                imageId, savedImage.getUrl(), true, ImageAngle.FRONT, Instant.now());

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(imageRepository.save(any(ModelImage.class))).thenReturn(savedImage);
        when(modelMapper.toImageResponse(savedImage)).thenReturn(expected);

        service.addImage(modelId, req);

        verify(imageRepository).clearPrimaryFlag(modelId);
        verify(imageRepository).save(any(ModelImage.class));
    }

    @Test
    void addImage_nonPrimary_doesNotClearPrimaryFlag() {
        UUID imageId = UUID.randomUUID();
        UploadImageRequest req = new UploadImageRequest(
                "https://cdn.example.com/img.jpg", false, ImageAngle.SIDE);
        ModelImage savedImage = buildImage(imageId, false);
        ModelImageResponse expected = new ModelImageResponse(
                imageId, savedImage.getUrl(), false, ImageAngle.SIDE, Instant.now());

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(imageRepository.save(any(ModelImage.class))).thenReturn(savedImage);
        when(modelMapper.toImageResponse(savedImage)).thenReturn(expected);

        service.addImage(modelId, req);

        verify(imageRepository, never()).clearPrimaryFlag(any());
    }

    @Test
    void addImage_setsCorrectFieldsOnEntity() {
        UploadImageRequest req = new UploadImageRequest(
                "https://cdn.example.com/img.jpg", true, ImageAngle.REAR);
        UUID imageId = UUID.randomUUID();
        ModelImage savedImage = buildImage(imageId, true);
        ModelImageResponse expected = new ModelImageResponse(
                imageId, savedImage.getUrl(), true, ImageAngle.REAR, Instant.now());

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(imageRepository.save(any(ModelImage.class))).thenReturn(savedImage);
        when(modelMapper.toImageResponse(savedImage)).thenReturn(expected);

        service.addImage(modelId, req);

        ArgumentCaptor<ModelImage> captor = ArgumentCaptor.forClass(ModelImage.class);
        verify(imageRepository).save(captor.capture());
        assertThat(captor.getValue().getUrl()).isEqualTo("https://cdn.example.com/img.jpg");
        assertThat(captor.getValue().getIsPrimary()).isTrue();
        assertThat(captor.getValue().getAngle()).isEqualTo(ImageAngle.REAR);
    }

    @Test
    void addImage_modelNotFound_throws() {
        UploadImageRequest req = new UploadImageRequest(
                "https://cdn.example.com/img.jpg", false, null);
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addImage(modelId, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getImages ────────────────────────────────────────────────────────────

    @Test
    void getImages_returnsAllImagesForModel() {
        UUID img1 = UUID.randomUUID();
        UUID img2 = UUID.randomUUID();
        List<ModelImage> images = List.of(buildImage(img1, true), buildImage(img2, false));
        List<ModelImageResponse> expected = List.of(
                new ModelImageResponse(img1, "https://cdn.example.com/img.jpg", true, ImageAngle.FRONT, null),
                new ModelImageResponse(img2, "https://cdn.example.com/img.jpg", false, ImageAngle.FRONT, null));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(imageRepository.findByModelId(modelId)).thenReturn(images);
        when(modelMapper.toImageResponseList(images)).thenReturn(expected);

        List<ModelImageResponse> result = service.getImages(modelId);

        assertThat(result).hasSize(2);
        verify(imageRepository).findByModelId(modelId);
    }

    @Test
    void getImages_noImages_returnsEmpty() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(imageRepository.findByModelId(modelId)).thenReturn(List.of());
        when(modelMapper.toImageResponseList(List.of())).thenReturn(List.of());

        List<ModelImageResponse> result = service.getImages(modelId);

        assertThat(result).isEmpty();
    }

    @Test
    void getImages_modelNotFound_throws() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getImages(modelId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteImage ─────────────────────────────────────────────────────────

    @Test
    void deleteImage_deletesImageFromRepository() {
        UUID imageId = UUID.randomUUID();
        ModelImage image = buildImage(imageId, false);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(imageRepository.findByIdAndModelId(imageId, modelId)).thenReturn(Optional.of(image));

        service.deleteImage(modelId, imageId);

        verify(imageRepository).delete(image);
    }

    @Test
    void deleteImage_imageNotFound_throws() {
        UUID imageId = UUID.randomUUID();

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(imageRepository.findByIdAndModelId(imageId, modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteImage(modelId, imageId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(imageId.toString());
    }

    @Test
    void deleteImage_modelNotFound_throws() {
        UUID imageId = UUID.randomUUID();

        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteImage(modelId, imageId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(imageRepository, never()).delete(any());
    }

    // ─── search ───────────────────────────────────────────────────────────────

    @Test
    void search_noStatus_passesNullStatusToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<VehicleModel> page = new PageImpl<>(List.of(model));
        SearchModelsResponse expected = new SearchModelsResponse(List.of(), "tiago", 0, 20, 1L, 1);

        when(modelRepository.searchByKeyword("tiago", null, pageable)).thenReturn(page);
        when(modelMapper.toSearchResponse(page, "tiago")).thenReturn(expected);

        SearchModelsResponse result = service.search("tiago", null, pageable);

        assertThat(result).isEqualTo(expected);
        verify(modelRepository).searchByKeyword("tiago", null, pageable);
    }

    @Test
    void search_withStatus_parsesAndPasses() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<VehicleModel> page = new PageImpl<>(List.of(model));
        SearchModelsResponse expected = new SearchModelsResponse(List.of(), "ev", 0, 20, 1L, 1);

        when(modelRepository.searchByKeyword("ev", ModelStatus.ACTIVE, pageable)).thenReturn(page);
        when(modelMapper.toSearchResponse(page, "ev")).thenReturn(expected);

        SearchModelsResponse result = service.search("ev", "ACTIVE", pageable);

        assertThat(result).isEqualTo(expected);
        verify(modelRepository).searchByKeyword("ev", ModelStatus.ACTIVE, pageable);
    }

    @Test
    void search_blankStatus_treatedAsNull() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<VehicleModel> page = new PageImpl<>(List.of(model));
        SearchModelsResponse expected = new SearchModelsResponse(List.of(), "nexon", 0, 20, 1L, 1);

        when(modelRepository.searchByKeyword("nexon", null, pageable)).thenReturn(page);
        when(modelMapper.toSearchResponse(page, "nexon")).thenReturn(expected);

        service.search("nexon", "   ", pageable);

        verify(modelRepository).searchByKeyword("nexon", null, pageable);
    }

    @Test
    void search_invalidStatus_throwsIllegalArgumentException() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> service.search("ev", "INVALID", pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status value");
    }

    @Test
    void search_emptyResult_returnsEmptyContent() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<VehicleModel> emptyPage = Page.empty();
        SearchModelsResponse expected = new SearchModelsResponse(List.of(), "xyz", 0, 20, 0L, 0);

        when(modelRepository.searchByKeyword("xyz", null, pageable)).thenReturn(emptyPage);
        when(modelMapper.toSearchResponse(emptyPage, "xyz")).thenReturn(expected);

        SearchModelsResponse result = service.search("xyz", null, pageable);

        assertThat(result.content()).isEmpty();
    }

    // ─── findModelOrThrow ─────────────────────────────────────────────────────

    @Test
    void findModelOrThrow_found_returnsModel() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));

        VehicleModel result = service.findModelOrThrow(modelId);

        assertThat(result).isEqualTo(model);
    }

    @Test
    void findModelOrThrow_notFound_throwsWithIdInMessage() {
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findModelOrThrow(modelId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(modelId.toString());
    }
}
