package com.pk.ev.vehicle.catalog.chargingconfig.model;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the onboard charging hardware fitted to a vehicle.
 * VehicleChargingSpec hangs off this entity — NOT off VariantListing.
 *
 * Why: XZ+ 3.3kW and XZ+ Tech LUX 3.3kW share the exact same charging
 * behaviour. Duplicating VehicleChargingSpec per variant would violate DRY
 * and break the compatibility engine.
 *
 * Example — Tiago EV:
 *   ChargingConfig#1 → 3.3 kW AC, TYPE2 — used by 5 variant listings
 *   ChargingConfig#2 → 7.2 kW AC, TYPE2 — used by 2 variant listings
 */
@Entity
@Table(
        name = "charging_configurations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_charging_config_model_charger",
                columnNames = {"model_id", "onboard_charger_kw", "connector_type"}
        ),
        indexes = {
                @Index(name = "idx_charging_config_model_id",   columnList = "model_id"),
                @Index(name = "idx_charging_config_connector",  columnList = "connector_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_charging_config_model"))
    private VehicleModel model;

    // Human-readable label, e.g. "3.3 kW AC Standard", "7.2 kW AC Fast"
    @Column(name = "config_label", nullable = false, length = 100)
    private String configLabel;

    // The onboard charger capacity in kW — e.g. 3.3, 7.2, 11.0
    @Column(name = "onboard_charger_kw", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal onboardChargerKw;

    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", nullable = false, length = 20)
    private ConnectorType connectorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_type", nullable = false, length = 5)
    private CurrentType currentType;

    // Convenience: approx full charge time in minutes at rated wattage
    @Column(name = "charge_time_full_minutes")
    private Integer chargeTimeFullMinutes;

    // Convenience: 10→80% time in minutes (mainly relevant for DC fast config)
    @Column(name = "charge_time_10_to_80_minutes")
    private Integer chargeTime10To80Minutes;

    @Column(name = "cable_included", nullable = false)
    @Builder.Default
    private Boolean cableIncluded = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Full charging specs (connector details, max wattage, standards) hang here
    @OneToMany(mappedBy = "chargingConfiguration", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VehicleChargingSpec> chargingSpecs = new ArrayList<>();

    // Variant listings that use this charging configuration
    @OneToMany(mappedBy = "chargingConfiguration", fetch = FetchType.LAZY)
    @Builder.Default
    private List<VariantListing> variantListings = new ArrayList<>();
}
