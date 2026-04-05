package com.pk.ev.vehicle.catalog.chargingstandard.controller;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingstandard.dto.ChargingStandardDtos.*;
import com.pk.ev.vehicle.catalog.chargingstandard.enums.ChargingStandardType;
import com.pk.ev.vehicle.catalog.chargingstandard.service.ChargingStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Charging Standards", description = "Reference catalog of all charging protocols — Group 4")
public class ChargingStandardController {

    private final ChargingStandardService standardService;

    // ─── POST /charging-standards ────────────────────────────────────────────

    @Operation(
            summary = "Create a new charging standard (admin only)",
            description = "Adds a new protocol to the reference catalog. shortCode is normalised to uppercase.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Standard created"),
            @ApiResponse(responseCode = "409", description = "Duplicate name or shortCode")
    })
    @PostMapping("/charging-standards")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChargingStandardResponse> createStandard(
            @Valid @RequestBody CreateChargingStandardRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(standardService.createStandard(request));
    }

    // ─── GET /charging-standards ─────────────────────────────────────────────

    @Operation(summary = "List all supported charging standards — filterable and paginated")
    @GetMapping("/charging-standards")
    public ResponseEntity<PagedStandardsResponse> getAllStandards(
            @Parameter(description = "Filter by geographic region, e.g. India, Europe, Global")
            @RequestParam(required = false) String region,

            @Parameter(description = "Filter by current type — AC, DC, BOTH")
            @RequestParam(required = false) String currentType,

            @Parameter(description = "Filter by connector type — CCS2, TYPE2, etc.")
            @RequestParam(required = false) String connectorType,

            @Parameter(description = "Filter by deprecation status. Omit to return all.")
            @RequestParam(required = false) Boolean deprecated,

            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "50")   int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        ChargingStandardType ct = null;
        if (currentType != null) {
            ct = ChargingStandardType.valueOf(currentType.toUpperCase());
        }
        ConnectorType conn = null;
        if (connectorType != null) {
            conn = ConnectorType.valueOf(connectorType.toUpperCase());
        }

        StandardFilterParams filters = new StandardFilterParams(
                region, ct, conn, deprecated, page, size, sort
        );
        return ResponseEntity.ok(standardService.getAllStandards(filters));
    }

    // ─── GET /charging-standards/{standardId} ────────────────────────────────

    @Operation(summary = "Get full detail for a charging standard by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/charging-standards/{standardId}")
    public ResponseEntity<ChargingStandardResponse> getStandardById(
            @PathVariable UUID standardId
    ) {
        return ResponseEntity.ok(standardService.getStandardById(standardId));
    }

    // ─── GET /charging-standards/short-code/{shortCode} ──────────────────────

    @Operation(summary = "Get a charging standard by its short code, e.g. CCS2, BDC")
    @GetMapping("/charging-standards/short-code/{shortCode}")
    public ResponseEntity<ChargingStandardResponse> getByShortCode(
            @PathVariable String shortCode
    ) {
        return ResponseEntity.ok(standardService.getStandardByShortCode(shortCode));
    }

    // ─── PUT /charging-standards/{standardId} ────────────────────────────────

    @Operation(
            summary = "Update a charging standard",
            description = "Partial update — only provided fields are changed.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/charging-standards/{standardId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChargingStandardResponse> updateStandard(
            @PathVariable UUID standardId,
            @Valid @RequestBody UpdateChargingStandardRequest request
    ) {
        return ResponseEntity.ok(standardService.updateStandard(standardId, request));
    }

    // ─── DELETE /charging-standards/{standardId} — soft deprecate ────────────

    @Operation(
            summary = "Deprecate a charging standard (soft delete — sets isDeprecated=true)",
            description = "Deprecated standards remain queryable but are excluded from " +
                    "new compatibility checks unless explicitly requested.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/charging-standards/{standardId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deprecateStandard(
            @PathVariable UUID standardId
    ) {
        standardService.deprecateStandard(standardId);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /charging-standards/{standardId}/compatible-models ──────────────

    @Operation(
            summary = "List all vehicle variant listings compatible with this charging standard",
            description = "Traverses VehicleChargingSpec → ChargingConfiguration → VariantListing " +
                    "to find every vehicle that can use this connector."
    )
    @GetMapping("/charging-standards/{standardId}/compatible-models")
    public ResponseEntity<List<CompatibleModelSummary>> getCompatibleModels(
            @PathVariable UUID standardId
    ) {
        return ResponseEntity.ok(standardService.getCompatibleModels(standardId));
    }

    // ─── GET /connector-types ─────────────────────────────────────────────────

    @Operation(
            summary = "Flat list of all connector type enums with display metadata",
            description = "Returns the ConnectorType enum values enriched with display name, " +
                    "current type, and primary region. Used by dropdowns and filter UIs."
    )
    @GetMapping("/connector-types")
    public ResponseEntity<List<ConnectorTypeMetadata>> getConnectorTypes() {
        return ResponseEntity.ok(standardService.getConnectorTypes());
    }

    // ─── GET /charging-standards/regions ─────────────────────────────────────

    @Operation(summary = "Distinct list of geographic regions — used for filter dropdowns")
    @GetMapping("/charging-standards/regions")
    public ResponseEntity<RegionListResponse> getRegions() {
        return ResponseEntity.ok(new RegionListResponse(standardService.getDistinctRegions()));
    }

    // ─── GET /charging-standards/governing-bodies ────────────────────────────

    @Operation(summary = "Distinct list of governing bodies — used for filter dropdowns")
    @GetMapping("/charging-standards/governing-bodies")
    public ResponseEntity<GoverningBodyListResponse> getGoverningBodies() {
        return ResponseEntity.ok(new GoverningBodyListResponse(standardService.getDistinctGoverningBodies()));
    }
}
