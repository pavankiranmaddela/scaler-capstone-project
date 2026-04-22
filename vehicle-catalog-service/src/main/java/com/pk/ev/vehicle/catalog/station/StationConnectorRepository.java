package com.pk.ev.vehicle.catalog.station;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StationConnectorRepository extends JpaRepository<StationConnector, UUID> {

    /** All operational connectors at a given station */
    List<StationConnector> findByStationIdAndIsOperationalTrue(UUID stationId);

    /** Distinct station IDs that have at least one operational connector of a given type */
    @Query("""
        SELECT DISTINCT sc.stationId FROM StationConnector sc
        WHERE sc.connectorType = :connectorType
          AND sc.isOperational = true
        """)
    List<UUID> findStationIdsByConnectorType(@Param("connectorType") ConnectorType connectorType);

    /** All connectors of a specific type at a station (may be multiple bays) */
    List<StationConnector> findByStationIdAndConnectorTypeAndIsOperationalTrue(
            UUID stationId, ConnectorType connectorType);

    /** Delete all connectors for a station — called when station sync removes a station */
    void deleteByStationId(UUID stationId);
}
