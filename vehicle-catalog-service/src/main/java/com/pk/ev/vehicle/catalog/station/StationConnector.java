package com.pk.ev.vehicle.catalog.station;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Lightweight stub representing a single physical connector at a charging station.
 *
 * The full Station entity lives in the Station Management service. This stub
 * exists in the Vehicle Catalog service ONLY to support the compatibility engine.
 * It is populated via an async event / sync call from Station Management
 * whenever a station's connector inventory changes.
 *
 * Fields kept intentionally minimal — only what the compatibility algorithm needs.
 * Do NOT add station business logic here.
 *
 * stationId        — FK to the station in Station Management (opaque UUID)
 * connectorType    — physical plug type (CCS2, TYPE2, CHAdeMO, etc.)
 * currentType      — AC or DC
 * maxWattage       — rated output of this specific connector in Watts
 * isOperational    — only operational connectors participate in compatibility checks
 */
@Entity
@Table(
        name = "station_connectors",
        indexes = {
                @Index(name = "idx_station_connector_station_id",   columnList = "station_id"),
                @Index(name = "idx_station_connector_type",         columnList = "connector_type"),
                @Index(name = "idx_station_connector_operational",  columnList = "is_operational")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationConnector {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "station_id", nullable = false)
    private UUID stationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", nullable = false, length = 20)
    private ConnectorType connectorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_type", nullable = false, length = 5)
    private CurrentType currentType;

    /** Rated max output of this connector in Watts. e.g. 22000, 50000, 150000 */
    @Column(name = "max_wattage", nullable = false)
    private Integer maxWattage;

    @Column(name = "is_operational", nullable = false)
    @Builder.Default
    private Boolean isOperational = true;
}
