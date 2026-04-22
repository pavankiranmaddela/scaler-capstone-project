package com.pk.ev.vehicle.catalog.modeltrim.model;

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
 * Represents a trim grade under a VehicleModel.
 * Trim captures WHAT is in/on the car — features, ADAS, interior quality.
 * It says NOTHING about battery or charging.
 *
 * Example — Tiago EV:
 *   XE        → base trim, manual AC, basic infotainment
 *   XT        → auto AC, touchscreen
 *   XZ+       → sunroof, 360 camera, connected car
 *   XZ+ Tech LUX → leatherette seats, air purifier, JBL sound
 */
@Entity
@Table(
        name = "model_trims",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_trim_model_name",
                columnNames = {"model_id", "trim_name"}
        ),
        indexes = {
                @Index(name = "idx_model_trim_model_id", columnList = "model_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelTrim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_model_trim_model"))
    private VehicleModel model;

    @Column(name = "trim_name", nullable = false, length = 100)
    private String trimName;                      // e.g. "XE", "XT", "XZ+", "XZ+ Tech LUX"

    @Column(name = "description", length = 500)
    private String description;

    // Feature flags — extend as needed
    @Column(name = "has_sunroof")
    @Builder.Default
    private Boolean hasSunroof = false;

    @Column(name = "has_adas")
    @Builder.Default
    private Boolean hasAdas = false;

    @Column(name = "has_connected_car")
    @Builder.Default
    private Boolean hasConnectedCar = false;

    @Column(name = "infotainment_size_inches")
    private Integer infotainmentSizeInches;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;               // Lower = base trim

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "trim", fetch = FetchType.LAZY)
    @Builder.Default
    private List<VariantListing> variantListings = new ArrayList<>();
}
