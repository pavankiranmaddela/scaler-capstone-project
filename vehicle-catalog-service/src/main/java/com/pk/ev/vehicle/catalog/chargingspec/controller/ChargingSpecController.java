package com.pk.ev.vehicle.catalog.chargingspec.controller;

import com.pk.ev.vehicle.catalog.chargingspec.dto.ChargingSpecDto;
import com.pk.ev.vehicle.catalog.chargingspec.service.ChargingSpecService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicle-models/{modelId}/charging-configurations")
@RequiredArgsConstructor
public class ChargingSpecController {
    // ─── Nested: Charging Specs under a Configuration ────────────────────────

    private final ChargingSpecService chargingSpecService;

    @PostMapping("/{configId}/specs")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a charging spec to a configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChargingSpecDto.ChargingSpecResponse> addChargingSpec(
            @PathVariable UUID modelId, @PathVariable UUID configId,
            @Valid @RequestBody ChargingSpecDto.CreateChargingSpecRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chargingSpecService.addChargingSpec(modelId, configId, request));
    }

    @GetMapping("/{configId}/specs")
    @Operation(summary = "List all specs for a charging configuration")
    public ResponseEntity<List<ChargingSpecDto.ChargingSpecResponse>> getChargingSpecs(
            @PathVariable UUID modelId, @PathVariable UUID configId
    ) {
        return ResponseEntity.ok(chargingSpecService.getChargingSpecs(modelId, configId));
    }

    @GetMapping("/{configId}/specs/summary")
    @Operation(summary = "Fastest AC + DC spec in a compact response")
    public ResponseEntity<ChargingSpecDto.ChargingSpecSummaryResponse> getChargingSpecSummary(
            @PathVariable UUID modelId, @PathVariable UUID configId
    ) {
        return ResponseEntity.ok(chargingSpecService.getChargingSpecSummary(modelId, configId));
    }

    @GetMapping("/{configId}/specs/{specId}")
    @Operation(summary = "Get a specific charging spec")
    public ResponseEntity<ChargingSpecDto.ChargingSpecResponse> getChargingSpecById(
            @PathVariable UUID modelId, @PathVariable UUID configId, @PathVariable UUID specId
    ) {
        return ResponseEntity.ok(chargingSpecService.getChargingSpecById(modelId, configId, specId));
    }

    @PutMapping("/{configId}/specs/{specId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a charging spec", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChargingSpecDto.ChargingSpecResponse> updateChargingSpec(
            @PathVariable UUID modelId, @PathVariable UUID configId, @PathVariable UUID specId,
            @Valid @RequestBody ChargingSpecDto.UpdateChargingSpecRequest request
    ) {
        return ResponseEntity.ok(chargingSpecService.updateChargingSpec(modelId, configId, specId, request));
    }

    @DeleteMapping("/{configId}/specs/{specId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a charging spec", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteChargingSpec(
            @PathVariable UUID modelId, @PathVariable UUID configId, @PathVariable UUID specId
    ) {
        chargingSpecService.deleteChargingSpec(modelId, configId, specId);
        return ResponseEntity.noContent().build();
    }
}
