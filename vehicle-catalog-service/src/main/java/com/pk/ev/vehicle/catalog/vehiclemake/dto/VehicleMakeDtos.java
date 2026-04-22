package com.pk.ev.vehicle.catalog.vehiclemake.dto;

import com.pk.ev.vehicle.catalog.vehiclemake.enums.MakeStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class VehicleMakeDtos {

    private VehicleMakeDtos() {}

    // ─── Request DTOs ────────────────────────────────────────────────────────

    public record CreateMakeRequest(
            @NotBlank(message = "Name is required")
            @Size(max = 150)
            String name,

            @NotBlank(message = "Country of origin is required")
            @Size(min = 2, max = 2, message = "Must be ISO 3166-1 alpha-2 code (2 chars)")
            @Pattern(regexp = "[A-Z]{2}", message = "Country code must be 2 uppercase letters")
            String countryOfOrigin,

            @Size(max = 500)
            String logoUrl,

            @Size(max = 300)
            String websiteUrl
    ) {}

    public record UpdateMakeRequest(
            @Size(max = 150)
            String name,

            @Size(min = 2, max = 2)
            @Pattern(regexp = "[A-Z]{2}", message = "Country code must be 2 uppercase letters")
            String countryOfOrigin,

            @Size(max = 500)
            String logoUrl,

            @Size(max = 300)
            String websiteUrl,

            MakeStatus status
    ) {}

    public record AssociateRegionsRequest(
            @NotEmpty(message = "At least one region is required")
            @Valid
            List<RegionEntry> regions
    ) {
        public record RegionEntry(
                @NotBlank
                @Size(min = 2, max = 2)
                @Pattern(regexp = "[A-Z]{2}")
                String regionCode,

                @Min(1900) @Max(2100)
                Integer launchYear
        ) {}
    }

    // ─── Response DTOs ───────────────────────────────────────────────────────

    public record MakeResponse(
            UUID id,
            String name,
            String slug,
            String countryOfOrigin,
            String logoUrl,
            String websiteUrl,
            MakeStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record MakeSummaryResponse(
            UUID id,
            String name,
            String slug,
            String countryOfOrigin,
            String logoUrl,
            MakeStatus status
    ) {}

    public record RegionResponse(
            UUID id,
            String regionCode,
            Integer launchYear
    ) {}

    public record PagedMakesResponse(
            List<MakeSummaryResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}
}
