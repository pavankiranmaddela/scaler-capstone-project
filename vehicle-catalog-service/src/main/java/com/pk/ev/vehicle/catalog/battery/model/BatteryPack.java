package com.pk.ev.vehicle.catalog.battery.model;

import com.pk.ev.vehicle.catalog.battery.enums.BatteryChemistry;
import com.pk.ev.vehicle.catalog.model.model.VehicleModel;
import com.pk.ev.vehicle.catalog.variant.model.VariantListing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a physical battery pack configuration.
 * One BatteryPack record is shared across all VariantListings that use it.
 *
 * Example — Tiago EV:
 *   BatteryPack#1 → 19.2 kWh (used by XE and XT trims)
 *   BatteryPack#2 → 24 kWh   (used by XT, XZ+, XZ+ Tech LUX trims)
 */
@Entity
@Table(
        name = "battery_packs",
        indexes = {
                @Index(name = "idx_battery_pack_model_id",  columnList = "model_id"),
                @Index(name = "idx_battery_pack_capacity",  columnList = "capacity_kwh")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryPack {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Scoped to a VehicleModel — 19.2 kWh on Tiago ≠ 19.2 kWh on Nexon
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_battery_pack_model"))
    private VehicleModel model;

    @Column(name = "pack_name", nullable = false, length = 100)
    private String packName;                      // e.g. "Medium Range", "Long Range"

    @Column(name = "capacity_kwh", nullable = false, precision = 6, scale = 2)
    private BigDecimal capacityKwh;               // Gross — e.g. 19.2, 24.0

    @Column(name = "usable_kwh", precision = 6, scale = 2)
    private BigDecimal usableKwh;                 // Net usable

    @Column(name = "range_km")
    private Integer rangeKm;                      // ARAI / WLTP certified

    @Enumerated(EnumType.STRING)
    @Column(name = "chemistry", length = 10)
    private BatteryChemistry chemistry;           // LFP, NMC, etc.

    @Column(name = "cells_configuration", length = 50)
    private String cellsConfiguration;            // e.g. "90S1P"

    @Column(name = "warranty_years")
    private Integer warrantyYears;

    @Column(name = "warranty_km")
    private Integer warrantyKm;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Reverse side — all variant listings using this pack
    @OneToMany(mappedBy = "batteryPack", fetch = FetchType.LAZY)
    @Builder.Default
    private List<VariantListing> variantListings = new ArrayList<>();
}
