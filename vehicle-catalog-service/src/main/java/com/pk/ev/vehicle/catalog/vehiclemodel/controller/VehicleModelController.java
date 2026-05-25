package com.pk.ev.vehicle.catalog.vehiclemodel.controller;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.vehiclemodel.dtos.VehicleModelDtos.*;
import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.vehiclemodel.service.VehicleModelService;
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
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicle-models")
@RequiredArgsConstructor
@Tag(name = "Vehicle Models", description = "Manage EV model catalog — Group 2")
public class VehicleModelController {

    private final VehicleModelService modelService;

    // ─── POST /vehicle-models ────────────────────────────────────────────────

    @Operation(
            summary = "Create a new vehicle model",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Model created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Make not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate model-year under same make")
    })
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModelResponse> createModel(
            @Valid @RequestBody CreateModelRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelService.createModel(request));
    }

    // ─── GET /vehicle-models ─────────────────────────────────────────────────

    @Operation(summary = "List vehicle models — paginated with filters")
    @GetMapping
    public ResponseEntity<PagedModelsResponse> getAllModels(
            @RequestParam(required = false) UUID makeId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String connectorType,
            @RequestParam(required = false) BigDecimal minBatteryKwh,
            @RequestParam(required = false) BigDecimal maxBatteryKwh,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        ConnectorType ct = null;
        ModelStatus ms = null;

        try {
            if (connectorType != null) {
                ct = ConnectorType.valueOf(connectorType.toUpperCase());
            }
            if (status != null) {
                ms = ModelStatus.valueOf(status.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        ModelFilterParams filters = new ModelFilterParams(
                makeId, year, ct, minBatteryKwh, maxBatteryKwh, ms, page, size, sort
        );
        return ResponseEntity.ok(modelService.getAllModels(filters));
    }

    // ─── GET /vehicle-models/search ──────────────────────────────────────────

    @Operation(summary = "Full-text search across make name, model name, and model year")
    @GetMapping("/search")
    public ResponseEntity<SearchModelsResponse> search(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String q,

            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size
    ) {
        return ResponseEntity.ok(
                modelService.search(q, status,
                        PageRequest.of(page, size, Sort.by("name").ascending()))
        );
    }

    // ─── GET /vehicle-models/{modelId} ───────────────────────────────────────

    @Operation(summary = "Get full detail for a vehicle model including charging specs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model found"),
            @ApiResponse(responseCode = "404", description = "Model not found")
    })
    @GetMapping("/{modelId}")
    public ResponseEntity<ModelResponse> getModelById(@PathVariable UUID modelId) {
        return ResponseEntity.ok(modelService.getModelById(modelId));
    }

    // ─── PUT /vehicle-models/{modelId} ───────────────────────────────────────

    @Operation(
            summary = "Update vehicle model specs",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{modelId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModelResponse> updateModel(
            @PathVariable UUID modelId,
            @Valid @RequestBody UpdateModelRequest request
    ) {
        return ResponseEntity.ok(modelService.updateModel(modelId, request));
    }

    // ─── DELETE /vehicle-models/{modelId} ────────────────────────────────────

    @Operation(
            summary = "Soft-delete a vehicle model (status → DISCONTINUED)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{modelId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteModel(@PathVariable UUID modelId) {
        modelService.deleteModel(modelId);
        return ResponseEntity.noContent().build();
    }

    // ─── PUT /vehicle-models/{modelId}/status ────────────────────────────────

    @Operation(
            summary = "Toggle model status — ACTIVE / INACTIVE / DISCONTINUED",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{modelId}/status")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModelResponse> updateModelStatus(
            @PathVariable UUID modelId,
            @Valid @RequestBody UpdateModelStatusRequest request
    ) {
        return ResponseEntity.ok(modelService.updateModelStatus(modelId, request));
    }

    // ─── POST /vehicle-models/{modelId}/images ───────────────────────────────

    @Operation(
            summary = "Upload a model image (CDN URL + angle + primary flag)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{modelId}/images")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModelImageResponse> addImage(
            @PathVariable UUID modelId,
            @Valid @RequestBody UploadImageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelService.addImage(modelId, request));
    }

    // ─── GET /vehicle-models/{modelId}/images ────────────────────────────────

    @Operation(summary = "List all images for a vehicle model")
    @GetMapping("/{modelId}/images")
    public ResponseEntity<List<ModelImageResponse>> getImages(@PathVariable UUID modelId) {
        return ResponseEntity.ok(modelService.getImages(modelId));
    }

    // ─── DELETE /vehicle-models/{modelId}/images/{imageId} ───────────────────

    @Operation(
            summary = "Remove a model image",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{modelId}/images/{imageId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID modelId,
            @PathVariable UUID imageId
    ) {
        modelService.deleteImage(modelId, imageId);
        return ResponseEntity.noContent().build();
    }
}
