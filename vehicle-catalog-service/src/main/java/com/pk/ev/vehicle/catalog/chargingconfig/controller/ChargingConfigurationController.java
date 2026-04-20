package com.pk.ev.vehicle.catalog.chargingconfig.controller;

import com.pk.ev.vehicle.catalog.chargingconfig.dto.ChargingConfigDto.*;
import com.pk.ev.vehicle.catalog.chargingconfig.service.ChargingConfigService;
import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto.*;
import io.swagger.v3.oas.annotations.Operation;
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

// ─────────────────────────────────────────────────────────────────────────────
// Charging Configuration Controller
// Base: /vehicle-models/{modelId}/charging-configurations
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/vehicle-models/{modelId}/charging-configurations")
@RequiredArgsConstructor
@Tag(name = "Charging Configurations", description = "Manage onboard charger configs — Group 3")
class ChargingConfigurationController {

    private final ChargingConfigService chargingConfigService;

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a charging configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChargingConfigResponse> addChargingConfig(
            @PathVariable UUID modelId,
            @Valid @RequestBody CreateChargingConfigRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chargingConfigService.addChargingConfig(modelId, request));
    }

    @GetMapping
    @Operation(summary = "List charging configurations for a model")
    public ResponseEntity<List<ChargingConfigResponse>> getChargingConfigs(
            @PathVariable UUID modelId,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(chargingConfigService.getChargingConfigs(modelId, activeOnly));
    }

    @GetMapping("/{configId}")
    @Operation(summary = "Get a charging configuration with its specs")
    public ResponseEntity<ChargingConfigResponse> getChargingConfigById(
            @PathVariable UUID modelId, @PathVariable UUID configId
    ) {
        return ResponseEntity.ok(chargingConfigService.getChargingConfigById(modelId, configId));
    }

    @PutMapping("/{configId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a charging configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChargingConfigResponse> updateChargingConfig(
            @PathVariable UUID modelId, @PathVariable UUID configId,
            @Valid @RequestBody UpdateChargingConfigRequest request
    ) {
        return ResponseEntity.ok(chargingConfigService.updateChargingConfig(modelId, configId, request));
    }

    @DeleteMapping("/{configId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a charging configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteChargingConfig(
            @PathVariable UUID modelId, @PathVariable UUID configId
    ) {
        chargingConfigService.deleteChargingConfig(modelId, configId);
        return ResponseEntity.noContent().build();
    }
}

