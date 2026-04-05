package com.pk.ev.vehicle.catalog.chargingspec.repository;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleChargingSpecRepository extends JpaRepository<VehicleChargingSpec, UUID> {

    List<VehicleChargingSpec> findByChargingConfigurationId(UUID chargingConfigId);

    Optional<VehicleChargingSpec> findByIdAndChargingConfigurationId(UUID id, UUID chargingConfigId);

    boolean existsByChargingConfigurationIdAndConnectorType(UUID chargingConfigId, ConnectorType connectorType);

    // Summary: fastest AC and DC spec for a given charging config
    @Query("""
        SELECT s FROM VehicleChargingSpec s
        WHERE s.chargingConfiguration.id = :configId
          AND s.currentType = CurrentType.AC
        ORDER BY s.maxAcceptedWattage DESC
        LIMIT 1
        """)
    Optional<VehicleChargingSpec> findFastestAcSpec(@Param("configId") UUID configId);

    @Query("""
        SELECT s FROM VehicleChargingSpec s
        WHERE s.chargingConfiguration.id = :configId
          AND s.currentType = CurrentType.DC
        ORDER BY s.maxAcceptedWattage DESC
        LIMIT 1
        """)
    Optional<VehicleChargingSpec> findFastestDcSpec(@Param("configId") UUID configId);

    // Used by compatibility engine — all specs reachable from a variant listing
    @Query("""
        SELECT s FROM VehicleChargingSpec s
        WHERE s.chargingConfiguration.id IN (
            SELECT vl.chargingConfiguration.id
            FROM VariantListing vl WHERE vl.id = :variantListingId
        )
        """)
    List<VehicleChargingSpec> findByVariantListingId(@Param("variantListingId") UUID variantListingId);
}
