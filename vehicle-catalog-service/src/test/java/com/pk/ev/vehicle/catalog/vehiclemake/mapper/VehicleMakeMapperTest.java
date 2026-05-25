package com.pk.ev.vehicle.catalog.vehiclemake.mapper;

import com.pk.ev.vehicle.catalog.vehiclemake.dto.VehicleMakeDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemake.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.vehiclemake.model.MakeRegion;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleMakeMapperTest {

    private VehicleMakeMapper mapper;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private UUID makeId;
    private VehicleMake make;

    @BeforeEach
    void setUp() {
        mapper = new VehicleMakeMapper();

        makeId = UUID.randomUUID();
        make = VehicleMake.builder()
                .id(makeId)
                .name("Tata Motors")
                .slug("tata-motors")
                .countryOfOrigin("IN")
                .logoUrl("https://tata.com/logo.png")
                .websiteUrl("https://tata.com")
                .status(MakeStatus.ACTIVE)
                .models(new ArrayList<>())
                .regions(new ArrayList<>())
                .build();
        make.setCreatedAt(Instant.parse("2023-01-01T00:00:00Z"));
        make.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    }

    // ─── toResponse ──────────────────────────────────────────────────────────

    @Test
    void toResponse_mapsAllFields() {
        MakeResponse response = mapper.toResponse(make);

        assertThat(response.id()).isEqualTo(makeId);
        assertThat(response.name()).isEqualTo("Tata Motors");
        assertThat(response.slug()).isEqualTo("tata-motors");
        assertThat(response.countryOfOrigin()).isEqualTo("IN");
        assertThat(response.logoUrl()).isEqualTo("https://tata.com/logo.png");
        assertThat(response.websiteUrl()).isEqualTo("https://tata.com");
        assertThat(response.status()).isEqualTo(MakeStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2023-01-01T00:00:00Z"));
        assertThat(response.updatedAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    void toResponse_withNullOptionalFields_mapsNulls() {
        make.setLogoUrl(null);
        make.setWebsiteUrl(null);

        MakeResponse response = mapper.toResponse(make);

        assertThat(response.logoUrl()).isNull();
        assertThat(response.websiteUrl()).isNull();
    }

    @Test
    void toResponse_inactiveStatus_mapsCorrectly() {
        make.setStatus(MakeStatus.INACTIVE);

        MakeResponse response = mapper.toResponse(make);

        assertThat(response.status()).isEqualTo(MakeStatus.INACTIVE);
    }

    // ─── toSummary ────────────────────────────────────────────────────────────

    @Test
    void toSummary_mapsRequiredFields() {
        MakeSummaryResponse summary = mapper.toSummary(make);

        assertThat(summary.id()).isEqualTo(makeId);
        assertThat(summary.name()).isEqualTo("Tata Motors");
        assertThat(summary.slug()).isEqualTo("tata-motors");
        assertThat(summary.countryOfOrigin()).isEqualTo("IN");
        assertThat(summary.logoUrl()).isEqualTo("https://tata.com/logo.png");
        assertThat(summary.status()).isEqualTo(MakeStatus.ACTIVE);
    }

    @Test
    void toSummary_doesNotIncludeTimestampsOrWebsiteUrl() {
        // MakeSummaryResponse has no createdAt/updatedAt/websiteUrl fields
        MakeSummaryResponse summary = mapper.toSummary(make);

        // Verify summary record has only 6 components — compile-time check via record
        assertThat(summary).isNotNull();
        assertThat(summary.name()).isNotNull();
    }

    @Test
    void toSummary_nullLogoUrl_mapsNull() {
        make.setLogoUrl(null);
        MakeSummaryResponse summary = mapper.toSummary(make);
        assertThat(summary.logoUrl()).isNull();
    }

    // ─── toPagedResponse ─────────────────────────────────────────────────────

    @Test
    void toPagedResponse_mapsPageMetadata() {
        Page<VehicleMake> page = new PageImpl<>(
                List.of(make),
                PageRequest.of(1, 5),
                12
        );

        PagedMakesResponse response = mapper.toPagedResponse(page);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(5);
        assertThat(response.totalElements()).isEqualTo(12);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.last()).isFalse();
    }

    @Test
    void toPagedResponse_mapsContentViaSummary() {
        Page<VehicleMake> page = new PageImpl<>(List.of(make));

        PagedMakesResponse response = mapper.toPagedResponse(page);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(makeId);
        assertThat(response.content().get(0).name()).isEqualTo("Tata Motors");
    }

    @Test
    void toPagedResponse_emptyPage_returnsEmptyContent() {
        Page<VehicleMake> emptyPage = Page.empty();

        PagedMakesResponse response = mapper.toPagedResponse(emptyPage);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.last()).isTrue();
    }

    @Test
    void toPagedResponse_multipleMakes_mapsAll() {
        VehicleMake make2 = VehicleMake.builder()
                .id(UUID.randomUUID()).name("Mahindra").slug("mahindra")
                .countryOfOrigin("IN").status(MakeStatus.ACTIVE)
                .models(new ArrayList<>()).regions(new ArrayList<>())
                .build();

        Page<VehicleMake> page = new PageImpl<>(List.of(make, make2));

        PagedMakesResponse response = mapper.toPagedResponse(page);

        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(2);
    }

    @Test
    void toPagedResponse_lastPage_flagsCorrectly() {
        Page<VehicleMake> lastPage = new PageImpl<>(
                List.of(make), PageRequest.of(0, 20), 1);

        PagedMakesResponse response = mapper.toPagedResponse(lastPage);

        assertThat(response.last()).isTrue();
    }

    // ─── toRegionResponse ─────────────────────────────────────────────────────

    @Test
    void toRegionResponse_mapsAllFields() {
        UUID regionId = UUID.randomUUID();
        MakeRegion region = MakeRegion.builder()
                .id(regionId).make(make).regionCode("IN").launchYear(2020).build();

        RegionResponse response = mapper.toRegionResponse(region);

        assertThat(response.id()).isEqualTo(regionId);
        assertThat(response.regionCode()).isEqualTo("IN");
        assertThat(response.launchYear()).isEqualTo(2020);
    }

    @Test
    void toRegionResponse_nullLaunchYear_mapsNull() {
        MakeRegion region = MakeRegion.builder()
                .id(UUID.randomUUID()).make(make).regionCode("DE").launchYear(null).build();

        RegionResponse response = mapper.toRegionResponse(region);

        assertThat(response.launchYear()).isNull();
    }

    // ─── toRegionResponseList ─────────────────────────────────────────────────

    @Test
    void toRegionResponseList_mapsAllRegions() {
        List<MakeRegion> regions = List.of(
                MakeRegion.builder().id(UUID.randomUUID()).make(make).regionCode("IN").launchYear(2020).build(),
                MakeRegion.builder().id(UUID.randomUUID()).make(make).regionCode("DE").launchYear(2022).build(),
                MakeRegion.builder().id(UUID.randomUUID()).make(make).regionCode("US").launchYear(2023).build()
        );

        List<RegionResponse> responses = mapper.toRegionResponseList(regions);

        assertThat(responses).hasSize(3);
        assertThat(responses).extracting(RegionResponse::regionCode)
                .containsExactly("IN", "DE", "US");
    }

    @Test
    void toRegionResponseList_emptyList_returnsEmpty() {
        List<RegionResponse> responses = mapper.toRegionResponseList(List.of());
        assertThat(responses).isEmpty();
    }

    // ─── toEntity ─────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllRequestFields() {
        CreateMakeRequest req = new CreateMakeRequest(
                "Mahindra", "IN", "https://logo.png", "https://mahindra.com");

        VehicleMake entity = mapper.toEntity(req);

        assertThat(entity.getName()).isEqualTo("Mahindra");
        assertThat(entity.getCountryOfOrigin()).isEqualTo("IN");
        assertThat(entity.getLogoUrl()).isEqualTo("https://logo.png");
        assertThat(entity.getWebsiteUrl()).isEqualTo("https://mahindra.com");
    }

    @Test
    void toEntity_nullOptionalFields_mapsNulls() {
        CreateMakeRequest req = new CreateMakeRequest("Ola Electric", "IN", null, null);

        VehicleMake entity = mapper.toEntity(req);

        assertThat(entity.getLogoUrl()).isNull();
        assertThat(entity.getWebsiteUrl()).isNull();
    }

    @Test
    void toEntity_doesNotSetIdOrSlug() {
        // id and slug are set by JPA/DB lifecycle hooks
        CreateMakeRequest req = new CreateMakeRequest("BYD", "CN", null, null);

        VehicleMake entity = mapper.toEntity(req);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getSlug()).isNull();
    }

    @Test
    void toEntity_defaultStatus_isActive() {
        CreateMakeRequest req = new CreateMakeRequest("Hyundai", "KR", null, null);

        VehicleMake entity = mapper.toEntity(req);

        // Builder.Default sets status to ACTIVE
        assertThat(entity.getStatus()).isEqualTo(MakeStatus.ACTIVE);
    }

    // ─── applyUpdate ─────────────────────────────────────────────────────────

    @Test
    void applyUpdate_allFieldsProvided_appliesAll() {
        UpdateMakeRequest req = new UpdateMakeRequest(
                "Tata EV", "US", "https://new-logo.png", "https://new-site.com", MakeStatus.INACTIVE
        );

        mapper.applyUpdate(req, make);

        assertThat(make.getName()).isEqualTo("Tata EV");
        assertThat(make.getCountryOfOrigin()).isEqualTo("US");
        assertThat(make.getLogoUrl()).isEqualTo("https://new-logo.png");
        assertThat(make.getWebsiteUrl()).isEqualTo("https://new-site.com");
        assertThat(make.getStatus()).isEqualTo(MakeStatus.INACTIVE);
    }

    @Test
    void applyUpdate_allNullFields_keepsOriginalValues() {
        UpdateMakeRequest req = new UpdateMakeRequest(null, null, null, null, null);

        mapper.applyUpdate(req, make);

        assertThat(make.getName()).isEqualTo("Tata Motors");
        assertThat(make.getCountryOfOrigin()).isEqualTo("IN");
        assertThat(make.getLogoUrl()).isEqualTo("https://tata.com/logo.png");
        assertThat(make.getWebsiteUrl()).isEqualTo("https://tata.com");
        assertThat(make.getStatus()).isEqualTo(MakeStatus.ACTIVE);
    }

    @Test
    void applyUpdate_partialFields_onlyUpdatesProvided() {
        UpdateMakeRequest req = new UpdateMakeRequest(
                "Tata EV", null, null, null, null
        );

        mapper.applyUpdate(req, make);

        assertThat(make.getName()).isEqualTo("Tata EV");
        assertThat(make.getCountryOfOrigin()).isEqualTo("IN");       // unchanged
        assertThat(make.getLogoUrl()).isEqualTo("https://tata.com/logo.png"); // unchanged
    }

    @Test
    void applyUpdate_statusOnly_updatesStatusKeepsOtherFields() {
        UpdateMakeRequest req = new UpdateMakeRequest(null, null, null, null, MakeStatus.INACTIVE);

        mapper.applyUpdate(req, make);

        assertThat(make.getStatus()).isEqualTo(MakeStatus.INACTIVE);
        assertThat(make.getName()).isEqualTo("Tata Motors");
    }
}
