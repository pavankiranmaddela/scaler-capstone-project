package com.pk.ev.vehicle.catalog.make.controller;

import com.pk.ev.vehicle.catalog.make.dto.VehicleMakeDtos.*;
import com.pk.ev.vehicle.catalog.make.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.make.mapper.VehicleMakeMapper;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.make.service.VehicleMakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.pk.ev.vehicle.catalog.constants.RoleConstants.ROLE_EV_APP_ADMIN;
import static com.pk.ev.vehicle.catalog.constants.RoleConstants.ROLE_EV_USER;

@RestController
@RequestMapping("/vehicle-makes")
@RequiredArgsConstructor
@Tag(name = "Vehicle Makes", description = "Manage EV manufacturer catalog — Group 1")
public class VehicleMakeController {

    private final VehicleMakeService makeService;
    private final VehicleMakeMapper  mapper;

    // ─── POST /vehicle-makes ─────────────────────────────────────────────────

    @PostMapping
    //@PreAuthorize("hasRole('ROLE_EV_APP_ADMIN')")
    public ResponseEntity<MakeResponse> createMake(
            @Valid @RequestBody CreateMakeRequest request
    ) {
        MakeResponse response = makeService.createMake(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── GET /vehicle-makes ──────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize(ROLE_EV_USER)
    public ResponseEntity<PagedMakesResponse> getAllMakes(
            @Parameter(description = "Filter by status (ACTIVE / INACTIVE)")
            @RequestParam(required = false) MakeStatus status,

            @Parameter(description = "Filter by ISO 3166-1 alpha-2 country code, e.g. IN")
            @RequestParam(required = false) String country,

            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return ResponseEntity.ok(makeService.getAllMakes(status, country, pageable));
    }

    // ─── GET /vehicle-makes/{makeId} ─────────────────────────────────────────
    @GetMapping("/{makeId}")
    @PreAuthorize(ROLE_EV_USER)
    public ResponseEntity<MakeResponse> getMakeById(
            @PathVariable UUID makeId
    ) {
        return ResponseEntity.ok(makeService.getMakeById(makeId));
    }

    // ─── PUT /vehicle-makes/{makeId} ─────────────────────────────────────────
    @PutMapping("/{makeId}")
    @PreAuthorize(ROLE_EV_APP_ADMIN)
    public ResponseEntity<MakeResponse> updateMake(
            @PathVariable UUID makeId,
            @Valid @RequestBody UpdateMakeRequest request
    ) {
        return ResponseEntity.ok(makeService.updateMake(makeId, request));
    }

    // ─── DELETE /vehicle-makes/{makeId} ──────────────────────────────────────
    @DeleteMapping("/{makeId}")
    @PreAuthorize(ROLE_EV_APP_ADMIN)
    public ResponseEntity<Void> deleteMake(
            @PathVariable UUID makeId
    ) {
        makeService.deleteMake(makeId);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /vehicle-makes/{makeId}/models ──────────────────────────────────

    @Operation(summary = "List all models under a vehicle make")
    @GetMapping("/{makeId}/models")
    public ResponseEntity<List<VehicleModel>> getModelsByMake(
            @PathVariable UUID makeId
    ) {
        // Returns VehicleModel stubs — Group 2 will decorate the response with its own DTOs
        return ResponseEntity.ok(makeService.getModelsByMake(makeId));
    }

    // ─── POST /vehicle-makes/{makeId}/regions ────────────────────────────────

    @Operation(
            summary = "Associate sale regions with a make",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{makeId}/regions")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RegionResponse>> associateRegions(
            @PathVariable UUID makeId,
            @Valid @RequestBody AssociateRegionsRequest request
    ) {
        List<RegionResponse> regions = makeService.associateRegions(makeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(regions);
    }

    // ─── GET /vehicle-makes/{makeId}/regions ─────────────────────────────────

    @Operation(summary = "List all regions where this make is sold")
    @GetMapping("/{makeId}/regions")
    public ResponseEntity<List<RegionResponse>> getRegionsByMake(
            @PathVariable UUID makeId
    ) {
        return ResponseEntity.ok(makeService.getRegionsByMake(makeId));
    }
}
