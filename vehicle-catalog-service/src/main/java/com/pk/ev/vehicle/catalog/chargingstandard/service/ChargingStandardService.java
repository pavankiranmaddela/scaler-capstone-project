package com.pk.ev.vehicle.catalog.chargingstandard.service;

import com.pk.ev.vehicle.catalog.chargingstandard.dto.ChargingStandardDtos.*;

import java.util.List;
import java.util.UUID;

public interface ChargingStandardService {

    // POST /charging-standards
    ChargingStandardResponse createStandard(CreateChargingStandardRequest request);

    // GET /charging-standards
    PagedStandardsResponse getAllStandards(StandardFilterParams filters);

    // GET /charging-standards/{standardId}
    ChargingStandardResponse getStandardById(UUID standardId);

    // GET /charging-standards/by-short-code/{shortCode}
    ChargingStandardResponse getStandardByShortCode(String shortCode);

    // PUT /charging-standards/{standardId}
    ChargingStandardResponse updateStandard(UUID standardId, UpdateChargingStandardRequest request);

    // DELETE /charging-standards/{standardId}  — soft deprecate
    void deprecateStandard(UUID standardId);

    // GET /charging-standards/{standardId}/compatible-models
    List<CompatibleModelSummary> getCompatibleModels(UUID standardId);

    // GET /connector-types — flat enum list with metadata
    List<ConnectorTypeMetadata> getConnectorTypes();

    // GET /charging-standards/regions — distinct geographic region list
    List<String> getDistinctRegions();

    // GET /charging-standards/governing-bodies
    List<String> getDistinctGoverningBodies();
}
