package com.pk.ev.vehicle.catalog.compatibility;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/compatibility")
@RequiredArgsConstructor
@Tag(
        name = "Compatibility",
        description = "Query-only matchmaking engine — answers 'can this vehicle charge at this station?' — Group 5"
)
public class CompatibilityController {

    private final CompatibilityService compatibilityService;

    // ─── GET /compatibility/vehicle/{variantListingId}/station/{stationId} ────

    @Operation(
            summary = "Check if a vehicle variant can charge at a specific station",
            description = """
            Compares the variant's VehicleChargingSpecs (via its ChargingConfiguration)
            against the station's operational connectors. Returns:
            - isCompatible: true/false
            - compatibleSpecs: which specs match and at what wattage
            - maxAchievableWattage: min(vehicle max-accepted, station connector max)
            - estimatedCharge10To80Pct: interpolated charge time at actual wattage
            - incompatibilityReason: human-readable explanation when not compatible
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compatibility result returned (even if incompatible)"),
            @ApiResponse(responseCode = "404", description = "Variant listing not found")
    })
    @GetMapping("/vehicle/{variantListingId}/station/{stationId}")
    public ResponseEntity<CompatibilityResult> checkVariantAgainstStation(
            @PathVariable UUID variantListingId,
            @PathVariable UUID stationId
    ) {
        return ResponseEntity.ok(
                compatibilityService.checkVariantAgainstStation(variantListingId, stationId)
        );
    }

    // ─── GET /compatibility/vehicle/{variantListingId}/connector/{connectorType}

    @Operation(
            summary = "Check if a vehicle variant is compatible with a specific connector type",
            description = """
            Checks whether the variant has a VehicleChargingSpec that supports the given
            connector type. Optionally accepts a stationMaxWattage query param to compute
            achievable wattage for a specific station output without needing a stationId.
            """
    )
    @GetMapping("/vehicle/{variantListingId}/connector/{connectorType}")
    public ResponseEntity<CompatibilityResult> checkVariantAgainstConnector(
            @PathVariable UUID variantListingId,
            @PathVariable ConnectorType connectorType,

            @Parameter(description = "Optional — rated wattage of the specific station connector in Watts")
            @RequestParam(required = false) Integer stationMaxWattage
    ) {
        return ResponseEntity.ok(
                compatibilityService.checkVariantAgainstConnector(
                        variantListingId, connectorType, stationMaxWattage)
        );
    }

    // ─── GET /compatibility/station/{stationId}/vehicles ─────────────────────

    @Operation(
            summary = "List all vehicle variants that can charge at a given station",
            description = """
            Scans all ACTIVE variant listings and returns those compatible with
            at least one operational connector at the station.
            Results are enriched with maxAchievableWattage and estimated charge time.
            Used by the station detail page to show 'compatible vehicles'.
            """
    )
    @GetMapping("/station/{stationId}/vehicles")
    public ResponseEntity<StationCompatibleVariants> getCompatibleVariantsForStation(
            @PathVariable UUID stationId
    ) {
        return ResponseEntity.ok(
                compatibilityService.getCompatibleVariantsForStation(stationId)
        );
    }

    // ─── GET /compatibility/connector/{connectorType}/vehicles ────────────────

    @Operation(
            summary = "List all vehicle variants compatible with a connector type",
            description = """
            Returns every ACTIVE variant listing that has a VehicleChargingSpec
            for the given connector type. Optional maxWattage param limits results
            to variants where maxAchievableWattage meets the threshold.
            Used by the 'find charger for my car' flow in the mobile app.
            """
    )
    @GetMapping("/connector/{connectorType}/vehicles")
    public ResponseEntity<ConnectorCompatibleVariants> getCompatibleVariantsForConnector(
            @PathVariable ConnectorType connectorType,

            @Parameter(description = "Optional — minimum achievable wattage threshold in Watts")
            @RequestParam(required = false) Integer maxWattage
    ) {
        return ResponseEntity.ok(
                compatibilityService.getCompatibleVariantsForConnector(connectorType, maxWattage)
        );
    }

    // ─── POST /compatibility/bulk-check ──────────────────────────────────────

    @Operation(
            summary = "Batch compatibility check for multiple variant + station pairs",
            description = """
            Internal endpoint called by Reservation and Session services.
            Runs all pairs in parallel. If a variant or station is not found,
            the pair returns isCompatible=false with an explanatory reason
            rather than failing the whole batch.
            Returns totals: totalChecked, compatibleCount, incompatibleCount.
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/bulk-check")
    //@PreAuthorize("hasRole('INTERNAL') or hasRole('ADMIN')")
    public ResponseEntity<BulkCompatibilityResponse> bulkCheck(
            @Valid @RequestBody BulkCompatibilityRequest request
    ) {
        return ResponseEntity.ok(compatibilityService.bulkCheck(request));
    }
}
