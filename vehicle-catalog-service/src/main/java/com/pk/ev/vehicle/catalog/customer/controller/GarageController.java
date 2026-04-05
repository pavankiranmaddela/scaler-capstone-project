package com.pk.ev.vehicle.catalog.customer.controller;

import com.pk.ev.vehicle.catalog.customer.dto.GarageDtos.*;
import com.pk.ev.vehicle.catalog.customer.service.GarageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
/*import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;*/
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Customer Garage — Group 6.
 *
 * All customer endpoints extract the authenticated userId from the JWT subject.
 * Customers can only see and modify their own garage entries.
 *
 * The Admin GET /garage/vehicles endpoint is on a separate path suffix (/admin)
 * so the two GET /garage/vehicles handlers don't collide.
 */
@RestController
@RequestMapping("/garage/vehicles")
@RequiredArgsConstructor
@Tag(name = "Customer Garage", description = "Personal vehicle garage — Group 6")
@SecurityRequirement(name = "bearerAuth")
public class GarageController {

    private final GarageService garageService;

    // ─── POST /garage/vehicles ────────────────────────────────────────────────

    @Operation(summary = "Add a vehicle to the customer's garage",
            description = """
                   Links the authenticated user to a VariantListing.
                   If this is the customer's first vehicle it is automatically
                   set as primary. Setting isPrimary=true demotes any existing
                   primary vehicle.
                   """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehicle added"),
            @ApiResponse(responseCode = "404", description = "VariantListing not found")
    })
    @PostMapping
    //@PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerVehicleResponse> addVehicle(
            //@AuthenticationPrincipal Jwt jwt,
            String subject,
            @Valid @RequestBody AddVehicleRequest request
    ) {
        //UUID userId = UUID.fromString(jwt.getSubject());
        UUID userId = UUID.fromString(subject);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(garageService.addVehicle(userId, request));
    }

    // ─── GET /garage/vehicles ─────────────────────────────────────────────────

    @Operation(summary = "List all vehicles in the customer's garage",
            description = "Returns all saved vehicles ordered newest-first, " +
                    "plus the primaryVehicleId for quick session defaulting.")
    @GetMapping
    //@PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<GarageResponse> getGarage(
            //@AuthenticationPrincipal Jwt jwt
            String subjectId
    ) {
        UUID userId = UUID.fromString(subjectId);
        return ResponseEntity.ok(garageService.getGarage(userId));
    }

    // ─── GET /garage/vehicles/{vehicleId} ─────────────────────────────────────

    @Operation(summary = "Get a single saved vehicle by ID",
            description = "Returns 404 if the vehicle does not exist or belongs to another user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Not found or not owned by caller")
    })
    @GetMapping("/{vehicleId}")
    //@PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerVehicleResponse> getVehicleById(
            //@AuthenticationPrincipal Jwt jwt,
            String subjectId,
            @PathVariable UUID vehicleId
    ) {
        UUID userId = UUID.fromString(subjectId);
        return ResponseEntity.ok(garageService.getVehicleById(userId, vehicleId));
    }

    // ─── PUT /garage/vehicles/{vehicleId} ─────────────────────────────────────

    @Operation(summary = "Update nickname, registration plate, or primary flag")
    @PutMapping("/{vehicleId}")
    //@PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerVehicleResponse> updateVehicle(
            //@AuthenticationPrincipal Jwt jwt,
            String subjectId,
            @PathVariable UUID vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        UUID userId = UUID.fromString(subjectId);
        return ResponseEntity.ok(garageService.updateVehicle(userId, vehicleId, request));
    }

    // ─── DELETE /garage/vehicles/{vehicleId} ──────────────────────────────────

    @Operation(summary = "Remove a vehicle from the garage",
            description = """
                   Hard deletes the garage entry. If the removed vehicle was primary,
                   the most recently added remaining vehicle is automatically promoted
                   to primary.
                   """)
    @DeleteMapping("/{vehicleId}")
    //@PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> removeVehicle(
            //@AuthenticationPrincipal Jwt jwt,
            String subjectId,
            @PathVariable UUID vehicleId
    ) {
        UUID userId = UUID.fromString(subjectId);
        garageService.removeVehicle(userId, vehicleId);
        return ResponseEntity.noContent().build();
    }

    // ─── PUT /garage/vehicles/{vehicleId}/set-primary ─────────────────────────

    @Operation(summary = "Mark this vehicle as the default for charging sessions",
            description = "Demotes the current primary vehicle and promotes this one. Idempotent.")
    @PutMapping("/{vehicleId}/set-primary")
    //@PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerVehicleResponse> setPrimary(
            //@AuthenticationPrincipal Jwt jwt,
            String subjectId,
            @PathVariable UUID vehicleId
    ) {
        UUID userId = UUID.fromString(subjectId);
        return ResponseEntity.ok(garageService.setPrimaryVehicle(userId, vehicleId));
    }

    // ─── GET /garage/vehicles/{vehicleId}/compatible-stations ─────────────────

    @Operation(summary = "Find charging stations compatible with this vehicle",
            description = """
                   Uses the Compatibility Engine to find all operational stations
                   that have at least one connector matching the vehicle's
                   ChargingConfiguration. Results are sorted by maxAchievableWattage
                   descending so the fastest chargers appear first.
                   """)
    @GetMapping("/{vehicleId}/compatible-stations")
    //@PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CompatibleStationsResponse> getCompatibleStations(
            //@AuthenticationPrincipal Jwt jwt,
            String subjectId,
            @PathVariable UUID vehicleId
    ) {
        UUID userId = UUID.fromString(subjectId);
        return ResponseEntity.ok(garageService.getCompatibleStations(userId, vehicleId));
    }

    // ─── GET /garage/vehicles/admin ───────────────────────────────────────────

    @Operation(summary = "Admin: paginated list of all garage entries across all users",
            description = "Optional userId param filters to a specific user's garage.")
    @GetMapping("/admin")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminGaragePageResponse> adminListVehicles(
            @Parameter(description = "Filter to a specific user's garage")
            @RequestParam(required = false) UUID userId,

            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "20")       int size,
            @RequestParam(defaultValue = "addedAt")  String sort
    ) {
        return ResponseEntity.ok(
                garageService.adminListVehicles(
                        userId,
                        PageRequest.of(page, size, Sort.by(sort).descending())
                )
        );
    }
}
