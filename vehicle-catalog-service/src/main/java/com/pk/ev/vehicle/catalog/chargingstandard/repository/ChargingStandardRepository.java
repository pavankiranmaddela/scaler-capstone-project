package com.pk.ev.vehicle.catalog.chargingstandard.repository;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingstandard.enums.ChargingStandardType;
import com.pk.ev.vehicle.catalog.chargingstandard.model.ChargingStandard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChargingStandardRepository extends JpaRepository<ChargingStandard, UUID> {

    Optional<ChargingStandard> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    boolean existsByName(String name);

    // Paginated list with optional filters — supports GET /charging-standards
    @Query("""
        SELECT s FROM ChargingStandard s
        WHERE (:region      IS NULL OR s.geographicRegion = :region)
          AND (:currentType IS NULL OR s.currentType      = :currentType)
          AND (:connector   IS NULL OR s.connectorType    = :connector)
          AND (:deprecated  IS NULL OR s.isDeprecated     = :deprecated)
        """)
    Page<ChargingStandard> findAllByFilters(
            @Param("region")      String region,
            @Param("currentType") ChargingStandardType currentType,
            @Param("connector") ConnectorType connector,
            @Param("deprecated")  Boolean deprecated,
            Pageable pageable
    );

    // All non-deprecated standards for a given connector type
    List<ChargingStandard> findByConnectorTypeAndIsDeprecatedFalse(ConnectorType connectorType);

    // All distinct geographic regions (for filter dropdowns)
    @Query("SELECT DISTINCT s.geographicRegion FROM ChargingStandard s WHERE s.geographicRegion IS NOT NULL ORDER BY s.geographicRegion")
    List<String> findDistinctRegions();

    // All distinct governing bodies
    @Query("SELECT DISTINCT s.governingBody FROM ChargingStandard s WHERE s.governingBody IS NOT NULL ORDER BY s.governingBody")
    List<String> findDistinctGoverningBodies();

    // Standards compatible with a given vehicle (via VehicleChargingSpec → ChargingConfiguration)
    @Query("""
        SELECT DISTINCT std FROM ChargingStandard std
        WHERE std.id IN (
            SELECT DISTINCT vcs.chargingStandardId
            FROM VehicleChargingSpec vcs
            JOIN vcs.chargingConfiguration cc
            JOIN VariantListing vl ON vl.chargingConfiguration.id = cc.id
            WHERE vl.model.id = :modelId
              AND std.isDeprecated = false
        )
        """)
    List<ChargingStandard> findCompatibleStandardsByModelId(@Param("modelId") UUID modelId);
}
