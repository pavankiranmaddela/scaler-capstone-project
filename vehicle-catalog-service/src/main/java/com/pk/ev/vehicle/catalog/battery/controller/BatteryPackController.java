package com.pk.ev.vehicle.catalog.battery.controller;

import com.pk.ev.vehicle.catalog.variant.dto.Group3Dtos.*;
import com.pk.ev.vehicle.catalog.variant.service.Group3Service;
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
// Battery Pack Controller
// Base: /vehicle-models/{modelId}/battery-packs
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/vehicle-models/{modelId}/battery-packs")
@RequiredArgsConstructor
@Tag(name = "Battery Packs", description = "Manage battery pack configs per vehicle model — Group 3")
class BatteryPackController {

    private final Group3Service service;

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a battery pack to a model", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BatteryPackResponse> addBatteryPack(
            @PathVariable UUID modelId,
            @Valid @RequestBody CreateBatteryPackRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addBatteryPack(modelId, request));
    }

    @GetMapping
    @Operation(summary = "List battery packs for a model")
    public ResponseEntity<List<BatteryPackResponse>> getBatteryPacks(
            @PathVariable UUID modelId,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(service.getBatteryPacks(modelId, activeOnly));
    }

    @GetMapping("/{packId}")
    @Operation(summary = "Get a battery pack by ID")
    public ResponseEntity<BatteryPackResponse> getBatteryPackById(
            @PathVariable UUID modelId, @PathVariable UUID packId
    ) {
        return ResponseEntity.ok(service.getBatteryPackById(modelId, packId));
    }

    @PutMapping("/{packId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a battery pack", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BatteryPackResponse> updateBatteryPack(
            @PathVariable UUID modelId, @PathVariable UUID packId,
            @Valid @RequestBody UpdateBatteryPackRequest request
    ) {
        return ResponseEntity.ok(service.updateBatteryPack(modelId, packId, request));
    }

    @DeleteMapping("/{packId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a battery pack", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteBatteryPack(
            @PathVariable UUID modelId, @PathVariable UUID packId
    ) {
        service.deleteBatteryPack(modelId, packId);
        return ResponseEntity.noContent().build();
    }
}
