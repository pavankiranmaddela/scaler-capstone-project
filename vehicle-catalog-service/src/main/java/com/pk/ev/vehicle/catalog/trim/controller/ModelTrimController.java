package com.pk.ev.vehicle.catalog.trim.controller;

import com.pk.ev.vehicle.catalog.trim.dto.ModelTrimDto.*;
import com.pk.ev.vehicle.catalog.trim.service.ModelTrimService;
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
// Model Trim Controller
// Base: /vehicle-models/{modelId}/trims
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/vehicle-models/{modelId}/trims")
@RequiredArgsConstructor
@Tag(name = "Model Trims", description = "Manage trim grades per vehicle model")
class ModelTrimController {

    private final ModelTrimService modelTrimService;

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a trim grade", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ModelTrimResponse> addTrim(
            @PathVariable UUID modelId,
            @Valid @RequestBody CreateModelTrimRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelTrimService.addTrim(modelId, request));
    }

    @GetMapping
    @Operation(summary = "List trim grades for a model")
    public ResponseEntity<List<ModelTrimResponse>> getTrims(
            @PathVariable UUID modelId,
    @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(modelTrimService.getTrims(modelId, activeOnly));
    }

    @GetMapping("/{trimId}")
    @Operation(summary = "Get a trim by ID")
    public ResponseEntity<ModelTrimResponse> getTrimById(
            @PathVariable UUID modelId, @PathVariable UUID trimId
    ) {
        return ResponseEntity.ok(modelTrimService.getTrimById(modelId, trimId));
    }

    @PutMapping("/{trimId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a trim", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ModelTrimResponse> updateTrim(
            @PathVariable UUID modelId, @PathVariable UUID trimId,
            @Valid @RequestBody UpdateModelTrimRequest request
    ) {
        return ResponseEntity.ok(modelTrimService.updateTrim(modelId, trimId, request));
    }

    @DeleteMapping("/{trimId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a trim", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteTrim(
            @PathVariable UUID modelId, @PathVariable UUID trimId
    ) {
        modelTrimService.deleteTrim(modelId, trimId);
        return ResponseEntity.noContent().build();
    }
}

