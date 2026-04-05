package com.pk.ev.vehicle.catalog.make.mapper;

import com.pk.ev.vehicle.catalog.make.dto.VehicleMakeDtos.*;
import com.pk.ev.vehicle.catalog.make.model.MakeRegion;
import com.pk.ev.vehicle.catalog.make.model.VehicleMake;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VehicleMakeMapper {

    public MakeResponse toResponse(VehicleMake make) {
        return new MakeResponse(
                make.getId(),
                make.getName(),
                make.getSlug(),
                make.getCountryOfOrigin(),
                make.getLogoUrl(),
                make.getWebsiteUrl(),
                make.getStatus(),
                make.getCreatedAt(),
                make.getUpdatedAt()
        );
    }

    public MakeSummaryResponse toSummary(VehicleMake make) {
        return new MakeSummaryResponse(
                make.getId(),
                make.getName(),
                make.getSlug(),
                make.getCountryOfOrigin(),
                make.getLogoUrl(),
                make.getStatus()
        );
    }

    public PagedMakesResponse toPagedResponse(Page<VehicleMake> page) {
        return new PagedMakesResponse(
                page.getContent().stream().map(this::toSummary).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public RegionResponse toRegionResponse(MakeRegion region) {
        return new RegionResponse(
                region.getId(),
                region.getRegionCode(),
                region.getLaunchYear()
        );
    }

    public List<RegionResponse> toRegionResponseList(List<MakeRegion> regions) {
        return regions.stream().map(this::toRegionResponse).toList();
    }

    public VehicleMake toEntity(CreateMakeRequest req) {
        return VehicleMake.builder()
                .name(req.name())
                .countryOfOrigin(req.countryOfOrigin())
                .logoUrl(req.logoUrl())
                .websiteUrl(req.websiteUrl())
                .build();
    }

    public void applyUpdate(UpdateMakeRequest req, VehicleMake make) {
        if (req.name() != null)            make.setName(req.name());
        if (req.countryOfOrigin() != null) make.setCountryOfOrigin(req.countryOfOrigin());
        if (req.logoUrl() != null)         make.setLogoUrl(req.logoUrl());
        if (req.websiteUrl() != null)      make.setWebsiteUrl(req.websiteUrl());
        if (req.status() != null)          make.setStatus(req.status());
    }
}
