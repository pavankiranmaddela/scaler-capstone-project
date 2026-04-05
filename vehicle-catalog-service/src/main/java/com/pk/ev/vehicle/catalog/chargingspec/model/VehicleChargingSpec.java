package com.pk.ev.vehicle.catalog.chargingspec.model;

import com.pk.ev.vehicle.catalog.battery.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailed charging spec for a specific ChargingConfiguration.
 * FK changed from modelId -> chargingConfigurationId (Group 3 refactor).
 *
 * One ChargingConfiguration can have multiple specs — e.g. a DC fast-charge
 * config might list both CCS2 (primary) and CHAdeMO (legacy adapter) specs.
 */
@Entity
@Table(
        name = "vehicle_charging_specs",
        indexes = {
                @Index(name = "idx_charging_spec_config_id",    columnList = "charging_configuration_id"),
                @Index(name = "idx_charging_spec_connector",    columnList = "connector_type"),
                @Index(name = "idx_charging_spec_current_type", columnList = "current_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleChargingSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "charging_configuration_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_charging_spec_config"))
    private ChargingConfiguration chargingConfiguration;

    @Column(name = "charging_standard_id")
    private UUID chargingStandardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", nullable = false, length = 20)
    private ConnectorType connectorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_type", nullable = false, length = 5)
    private CurrentType currentType;

    @Column(name = "max_accepted_wattage", nullable = false)
    private Integer maxAcceptedWattage;

    @Column(name = "onboard_charger_wattage")
    private Integer onboardChargerWattage;

    @Column(name = "charge_time_10_to_80_pct")
    private Integer chargeTime10To80Pct;

    @Column(name = "charge_time_to_full_minutes")
    private Integer chargeTimeToFullMinutes;

    @Column(name = "cable_included", nullable = false)
    @Builder.Default
    private Boolean cableIncluded = false;

    @Column(name = "notes", length = 300)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
