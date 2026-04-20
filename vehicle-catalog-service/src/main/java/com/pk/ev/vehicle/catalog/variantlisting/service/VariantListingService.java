package com.pk.ev.vehicle.catalog.variantlisting.service;

import com.pk.ev.vehicle.catalog.variantlisting.dtos.VariantListingDto.*;

import java.util.List;
import java.util.UUID;

public interface VariantListingService {
    // ─── Variant Listing ──────────────────────────────────────────────────────
    VariantListingResponse createVariantListing(CreateVariantListingRequest request);
    PagedVariantResponse getVariantListings(VariantFilterParams filters);
    VariantListingResponse getVariantListingById(UUID variantId);
    List<VariantListingResponse> getVariantListingsByModel(UUID modelId, String status);
    VariantListingResponse updateVariantListing(UUID variantId, UpdateVariantListingRequest request);
    void deleteVariantListing(UUID variantId);
}
