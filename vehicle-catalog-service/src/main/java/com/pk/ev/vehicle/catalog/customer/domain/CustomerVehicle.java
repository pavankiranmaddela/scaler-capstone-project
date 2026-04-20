package com.pk.ev.vehicle.catalog.customer.domain;

import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a vehicle saved to a customer's personal garage.
 *
 * Links a userId (opaque UUID from the Auth/User service) to a
 * VariantListing in the catalog. Carries personal attributes the
 * customer adds on top of the catalog data.
 *
 * Business rules enforced by service:
 *   - One isPrimary=true per userId at any given time.
 *   - A user can register the same VariantListing more than once
 *     (e.g. two Tiago XZ+ — different registration numbers).
 *   - Only the owning customer can read/write their own garage entries,
 *     except Admin who can query by userId.
 */
@Entity
@Table(
        name = "customer_vehicles",
        indexes = {
                @Index(name = "idx_customer_vehicle_user_id",       columnList = "user_id"),
                @Index(name = "idx_customer_vehicle_variant_id",    columnList = "variant_listing_id"),
                @Index(name = "idx_customer_vehicle_primary",       columnList = "user_id, is_primary"),
                @Index(name = "idx_customer_vehicle_reg_number",    columnList = "registration_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Opaque FK → User service — not a JPA relationship intentionally */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * FK → VariantListing — the specific SKU (Trim + Battery + Charger) this
     * customer owns. We use a real JPA relationship here because the catalog
     * service owns both sides.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_listing_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_customer_vehicle_variant"))
    private VariantListing variantListing;

    /** Customer-defined label — e.g. "My White Tiago", "Office Car" */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /** Vehicle registration / license plate — e.g. "TS09EF1234" */
    @Column(name = "registration_number", length = 20)
    private String registrationNumber;

    /** Year the customer purchased this vehicle (can differ from modelYear) */
    @Column(name = "purchase_year")
    private Integer purchaseYear;

    /**
     * Default vehicle for charging sessions.
     * At most ONE record per userId may have isPrimary=true.
     * The service enforces this via clearPrimaryFlag() before setting.
     */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private Instant addedAt;
}
