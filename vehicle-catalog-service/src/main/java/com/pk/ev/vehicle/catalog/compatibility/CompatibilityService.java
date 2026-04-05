package com.pk.ev.vehicle.catalog.compatibility;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;

import java.util.UUID;

public interface CompatibilityService {

    /**
     * GET /compatibility/vehicle/{variantListingId}/station/{stationId}
     * Can this specific vehicle variant charge at this station?
     */
    CompatibilityResult checkVariantAgainstStation(UUID variantListingId, UUID stationId);

    /**
     * GET /compatibility/vehicle/{variantListingId}/connector/{connectorType}
     * Can this variant charge using this connector type?
     * Optional stationMaxWattage query param narrows to a specific connector's output.
     */
    CompatibilityResult checkVariantAgainstConnector(
            UUID variantListingId, ConnectorType connectorType, Integer stationMaxWattage);

    /**
     * GET /compatibility/station/{stationId}/vehicles
     * Which vehicle variants can charge at this station?
     */
    StationCompatibleVariants getCompatibleVariantsForStation(UUID stationId);

    /**
     * GET /compatibility/connector/{connectorType}/vehicles
     * Which vehicle variants support this connector type?
     * Optional maxWattage param further filters by achievable wattage threshold.
     */
    ConnectorCompatibleVariants getCompatibleVariantsForConnector(
            ConnectorType connectorType, Integer maxWattage);

    /**
     * POST /compatibility/bulk-check
     * Batch check for multiple variant + station pairs (used internally by
     * Reservation and Session services).
     */
    BulkCompatibilityResponse bulkCheck(BulkCompatibilityRequest request);
}
