package com.pk.ev.vehicle.catalog.chargingconfig.repository;

import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChargingConfigurationRepository extends JpaRepository<ChargingConfiguration, UUID> {

    List<ChargingConfiguration> findByModelIdAndIsActiveTrue(UUID modelId);

    List<ChargingConfiguration> findByModelId(UUID modelId);

    Optional<ChargingConfiguration> findByIdAndModelId(UUID id, UUID modelId);

    boolean existsByModelIdAndOnboardChargerKwAndConnectorType(
            UUID modelId, BigDecimal onboardChargerKw, ConnectorType connectorType);

    // Fetch config with its specs eagerly — used by compatibility engine
    @Query("""
        SELECT c FROM ChargingConfiguration c
        LEFT JOIN FETCH c.chargingSpecs
        WHERE c.id = :id
        """)
    Optional<ChargingConfiguration> findByIdWithSpecs(@Param("id") UUID id);

    // All active configs for a model, with specs — used by variant listing detail
    @Query("""
        SELECT DISTINCT c FROM ChargingConfiguration c
        LEFT JOIN FETCH c.chargingSpecs
        WHERE c.model.id = :modelId AND c.isActive = true
        """)
    List<ChargingConfiguration> findByModelIdWithSpecs(@Param("modelId") UUID modelId);
}
