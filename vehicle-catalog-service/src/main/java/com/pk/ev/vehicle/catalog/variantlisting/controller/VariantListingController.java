package com.pk.ev.vehicle.catalog.variantlisting.controller;

import com.pk.ev.vehicle.catalog.trim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.variantlisting.dtos.VariantListingDto.*;
import com.pk.ev.vehicle.catalog.variantlisting.service.VariantListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// Variant Listing Controller
// Base: /variant-listings
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/variant-listings")
@RequiredArgsConstructor
@Tag(name = "Variant Listings", description = "Manage sellable SKUs (Trim + Battery + Charger)")
class VariantListingController {

    private final VariantListingService variantListingService;

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a variant listing (the sellable SKU)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<VariantListingResponse> createVariantListing(
            @Valid @RequestBody CreateVariantListingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(variantListingService.createVariantListing(request));
    }

    @GetMapping
    @Operation(summary = "Paginated, filterable list of all variant listings")
    public ResponseEntity<PagedVariantResponse> getVariantListings(
            @RequestParam(required = false) UUID modelId,
            @RequestParam(required = false) UUID trimId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minBatteryKwh,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(defaultValue = "sortOrder")  String sort
    ) {
        VariantStatus vs = null;
        if (status != null) vs = VariantStatus.valueOf(status.toUpperCase());

        VariantFilterParams filters = new VariantFilterParams(
                modelId, trimId, minPrice, maxPrice, minBatteryKwh, vs, page, size, sort
        );
        return ResponseEntity.ok(variantListingService.getVariantListings(filters));
    }

    @GetMapping("/{variantId}")
    @Operation(summary = "Get a variant listing with full detail")
    public ResponseEntity<VariantListingResponse> getVariantListingById(@PathVariable UUID variantId) {
        return ResponseEntity.ok(variantListingService.getVariantListingById(variantId));
    }

    @GetMapping("/by-model/{modelId}")
    @Operation(summary = "All variant listings for a specific model")
    public ResponseEntity<List<VariantListingResponse>> getByModel(
            @PathVariable UUID modelId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(variantListingService.getVariantListingsByModel(modelId, status));
    }

    @PutMapping("/{variantId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update price, status, or launch date of a variant listing",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<VariantListingResponse> updateVariantListing(
            @PathVariable UUID variantId,
            @Valid @RequestBody UpdateVariantListingRequest request
    ) {
        return ResponseEntity.ok(variantListingService.updateVariantListing(variantId, request));
    }

    @DeleteMapping("/{variantId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a variant listing (status → DISCONTINUED)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteVariantListing(@PathVariable UUID variantId) {
        variantListingService.deleteVariantListing(variantId);
        return ResponseEntity.noContent().build();
    }
}
