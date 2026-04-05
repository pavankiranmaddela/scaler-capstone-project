package com.pk.ev.vehicle.catalog.model.model;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.battery.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.model.enums.BodyType;
import com.pk.ev.vehicle.catalog.model.enums.DriveType;
import com.pk.ev.vehicle.catalog.model.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.make.model.VehicleMake;
import com.pk.ev.vehicle.catalog.variant.model.ModelTrim;
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

@Entity
@Table(
        name = "vehicle_models",
        indexes = {
                @Index(name = "idx_model_make_id",   columnList = "make_id"),
                @Index(name = "idx_model_status",    columnList = "status"),
                @Index(name = "idx_model_year",      columnList = "model_year"),
                @Index(name = "idx_model_body_type", columnList = "body_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ─── Relationships ───────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "make_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vehicle_model_make"))
    private VehicleMake make;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ModelImage> images = new ArrayList<>();


    // ─── Core fields ─────────────────────────────────────────────────────────

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "model_year", nullable = false)
    private Integer modelYear;

    // ─── Physical specs ──────────────────────────────────────────────────────

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type", length = 30)
    private BodyType bodyType;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "drive_type", length = 10)
    private DriveType driveType;

    // ─── Status ──────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ModelStatus status = ModelStatus.ACTIVE;

    // ─── Audit ───────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ModelTrim> trims = new ArrayList<>();

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BatteryPack> batteryPacks = new ArrayList<>();

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChargingConfiguration> chargingConfigurations = new ArrayList<>();

    @OneToMany(mappedBy = "model", fetch = FetchType.LAZY)
    @Builder.Default
    private List<VariantListing> variantListings = new ArrayList<>();

    // ─── Helpers ─────────────────────────────────────────────────────────────

    public void addImage(ModelImage image) {
        images.add(image);
        image.setModel(this);
    }

    public void removeImage(ModelImage image) {
        images.remove(image);
        image.setModel(null);
    }
}