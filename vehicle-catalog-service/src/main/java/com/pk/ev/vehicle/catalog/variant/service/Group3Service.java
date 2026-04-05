package com.pk.ev.vehicle.catalog.variant.service;

import com.pk.ev.vehicle.catalog.variant.dto.Group3Dtos.*;

import java.util.List;
import java.util.UUID;

public interface Group3Service {

    // ─── Battery Pack ─────────────────────────────────────────────────────────
    BatteryPackResponse addBatteryPack(UUID modelId, CreateBatteryPackRequest request);
    List<BatteryPackResponse> getBatteryPacks(UUID modelId, boolean activeOnly);
    BatteryPackResponse getBatteryPackById(UUID modelId, UUID packId);
    BatteryPackResponse updateBatteryPack(UUID modelId, UUID packId, UpdateBatteryPackRequest request);
    void deleteBatteryPack(UUID modelId, UUID packId);

    // ─── Model Trim ───────────────────────────────────────────────────────────
    ModelTrimResponse addTrim(UUID modelId, CreateModelTrimRequest request);
    List<ModelTrimResponse> getTrims(UUID modelId, boolean activeOnly);
    ModelTrimResponse getTrimById(UUID modelId, UUID trimId);
    ModelTrimResponse updateTrim(UUID modelId, UUID trimId, UpdateModelTrimRequest request);
    void deleteTrim(UUID modelId, UUID trimId);

    // ─── Charging Configuration ───────────────────────────────────────────────
    ChargingConfigResponse addChargingConfig(UUID modelId, CreateChargingConfigRequest request);
    List<ChargingConfigResponse> getChargingConfigs(UUID modelId, boolean activeOnly);
    ChargingConfigResponse getChargingConfigById(UUID modelId, UUID configId);
    ChargingConfigResponse updateChargingConfig(UUID modelId, UUID configId, UpdateChargingConfigRequest request);
    void deleteChargingConfig(UUID modelId, UUID configId);

    // ─── Vehicle Charging Spec ────────────────────────────────────────────────
    ChargingSpecResponse addChargingSpec(UUID modelId, UUID configId, CreateChargingSpecRequest request);
    List<ChargingSpecResponse> getChargingSpecs(UUID modelId, UUID configId);
    ChargingSpecResponse getChargingSpecById(UUID modelId, UUID configId, UUID specId);
    ChargingSpecResponse updateChargingSpec(UUID modelId, UUID configId, UUID specId, UpdateChargingSpecRequest request);
    void deleteChargingSpec(UUID modelId, UUID configId, UUID specId);
    ChargingSpecSummaryResponse getChargingSpecSummary(UUID modelId, UUID configId);

    // ─── Variant Listing ──────────────────────────────────────────────────────
    VariantListingResponse createVariantListing(CreateVariantListingRequest request);
    PagedVariantResponse getVariantListings(VariantFilterParams filters);
    VariantListingResponse getVariantListingById(UUID variantId);
    List<VariantListingResponse> getVariantListingsByModel(UUID modelId, String status);
    VariantListingResponse updateVariantListing(UUID variantId, UpdateVariantListingRequest request);
    void deleteVariantListing(UUID variantId);
}
