package com.pk.ev.vehicle.catalog.variantlisting.model;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.trim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.trim.model.ModelTrim;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The sellable SKU — the intersection of Model + Trim + BatteryPack + ChargingConfiguration.
 *
 * Every row in the Tiago EV price sheet maps to exactly one VariantListing.
 * The unique constraint enforces that the same 4-way combination cannot be listed twice.
 *
 * Tiago EV examples:
 *   XE  + 19.2 kWh + 3.3 kW AC  → ₹8.49L
 *   XT  + 19.2 kWh + 3.3 kW AC  → ₹9.09L
 *   XT  + 24 kWh   + 3.3 kW AC  → ₹9.99L
 *   XZ+ + 24 kWh   + 3.3 kW AC  → ₹10.79L
 *   XZ+ + 24 kWh   + 7.2 kW AC  → ₹11.29L   ← same trim, different charger
 *   ...etc
 */
@Entity
@Table(
        name = "variant_listings",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_variant_listing_sku",
                columnNames = {"model_id", "trim_id", "battery_pack_id", "charging_configuration_id"}
        ),
        indexes = {
                @Index(name = "idx_variant_model_id",          columnList = "model_id"),
                @Index(name = "idx_variant_trim_id",           columnList = "trim_id"),
                @Index(name = "idx_variant_battery_pack_id",   columnList = "battery_pack_id"),
                @Index(name = "idx_variant_charging_config_id",columnList = "charging_configuration_id"),
                @Index(name = "idx_variant_status",            columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantListing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ─── The 4 axes ──────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_variant_model"))
    private VehicleModel model;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trim_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_variant_trim"))
    private ModelTrim trim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "battery_pack_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_variant_battery_pack"))
    private BatteryPack batteryPack;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "charging_configuration_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_variant_charging_config"))
    private ChargingConfiguration chargingConfiguration;

    // ─── Commercial attributes ────────────────────────────────────────────────

    // Human-friendly composite label auto-built by service, stored for search
    // e.g. "Tiago EV XZ+ 24 kWh 7.2 kW AC"
    @Column(name = "display_label", length = 200)
    private String displayLabel;

    @Column(name = "price_inr", precision = 12, scale = 2)
    private BigDecimal priceInr;                  // Ex-showroom, All India

    @Column(name = "launch_date")
    private LocalDate launchDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VariantStatus status = VariantStatus.ACTIVE;

    // Physical spec that can vary by variant even within same trim+battery
    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ─── Computed helper ──────────────────────────────────────────────────────

    /**
     * Builds a display label like "Tiago EV XZ+ 24 kWh 7.2 kW AC".
     * Called by service before persist.
     */
    public String buildDisplayLabel() {
        return String.format("%s %s %.0f kWh %.1f kW %s",
                model.getName(),
                trim.getTrimName(),
                batteryPack.getCapacityKwh(),
                chargingConfiguration.getOnboardChargerKw(),
                chargingConfiguration.getCurrentType().name()
        );
    }
}
