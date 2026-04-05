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
// Charging Configuration Controller
// Base: /vehicle-models/{modelId}/charging-configurations
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/vehicle-models/{modelId}/charging-configurations")
@RequiredArgsConstructor
@Tag(name = "Charging Configurations", description = "Manage onboard charger configs — Group 3")
class ChargingConfigurationController {

    private final Group3Service service;

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a charging configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChargingConfigResponse> addChargingConfig(
            @PathVariable UUID modelId,
            @Valid @RequestBody CreateChargingConfigRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addChargingConfig(modelId, request));
    }

    @GetMapping
    @Operation(summary = "List charging configurations for a model")
    public ResponseEntity<List<ChargingConfigResponse>> getChargingConfigs(
            @PathVariable UUID modelId,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(service.getChargingConfigs(modelId, activeOnly));
    }

    @GetMapping("/{configId}")
    @Operation(summary = "Get a charging configuration with its specs")
    public ResponseEntity<ChargingConfigResponse> getChargingConfigById(
            @PathVariable UUID modelId, @PathVariable UUID configId
    ) {
        return ResponseEntity.ok(service.getChargingConfigById(modelId, configId));
    }

    @PutMapping("/{configId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a charging configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChargingConfigResponse> updateChargingConfig(
            @PathVariable UUID modelId, @PathVariable UUID configId,
            @Valid @RequestBody UpdateChargingConfigRequest request
    ) {
        return ResponseEntity.ok(service.updateChargingConfig(modelId, configId, request));
    }

    @DeleteMapping("/{configId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a charging configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteChargingConfig(
            @PathVariable UUID modelId, @PathVariable UUID configId
    ) {
        service.deleteChargingConfig(modelId, configId);
        return ResponseEntity.noContent().build();
    }

    // ─── Nested: Charging Specs under a Configuration ────────────────────────

    @PostMapping("/{configId}/specs")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a charging spec to a configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChargingSpecResponse> addChargingSpec(
            @PathVariable UUID modelId, @PathVariable UUID configId,
            @Valid @RequestBody CreateChargingSpecRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addChargingSpec(modelId, configId, request));
    }

    @GetMapping("/{configId}/specs")
    @Operation(summary = "List all specs for a charging configuration")
    public ResponseEntity<List<ChargingSpecResponse>> getChargingSpecs(
            @PathVariable UUID modelId, @PathVariable UUID configId
    ) {
        return ResponseEntity.ok(service.getChargingSpecs(modelId, configId));
    }

    @GetMapping("/{configId}/specs/summary")
    @Operation(summary = "Fastest AC + DC spec in a compact response")
    public ResponseEntity<ChargingSpecSummaryResponse> getChargingSpecSummary(
            @PathVariable UUID modelId, @PathVariable UUID configId
    ) {
        return ResponseEntity.ok(service.getChargingSpecSummary(modelId, configId));
    }

    @GetMapping("/{configId}/specs/{specId}")
    @Operation(summary = "Get a specific charging spec")
    public ResponseEntity<ChargingSpecResponse> getChargingSpecById(
            @PathVariable UUID modelId, @PathVariable UUID configId, @PathVariable UUID specId
    ) {
        return ResponseEntity.ok(service.getChargingSpecById(modelId, configId, specId));
    }

    @PutMapping("/{configId}/specs/{specId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a charging spec", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChargingSpecResponse> updateChargingSpec(
            @PathVariable UUID modelId, @PathVariable UUID configId, @PathVariable UUID specId,
            @Valid @RequestBody UpdateChargingSpecRequest request
    ) {
        return ResponseEntity.ok(service.updateChargingSpec(modelId, configId, specId, request));
    }

    @DeleteMapping("/{configId}/specs/{specId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a charging spec", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteChargingSpec(
            @PathVariable UUID modelId, @PathVariable UUID configId, @PathVariable UUID specId
    ) {
        service.deleteChargingSpec(modelId, configId, specId);
        return ResponseEntity.noContent().build();
    }
}

