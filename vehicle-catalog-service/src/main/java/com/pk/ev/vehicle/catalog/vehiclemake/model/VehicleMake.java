package com.pk.ev.vehicle.catalog.vehiclemake.model;

import com.pk.ev.vehicle.catalog.vehiclemake.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "vehicle_makes",
        indexes = {
                @Index(name = "idx_make_slug", columnList = "slug", unique = true),
                @Index(name = "idx_make_status", columnList = "status"),
                @Index(name = "idx_make_country", columnList = "country_of_origin")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleMake {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "country_of_origin", nullable = false, length = 2)
    private String countryOfOrigin;  // ISO 3166-1 alpha-2

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website_url", length = 300)
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MakeStatus status = MakeStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "make", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VehicleModel> models = new ArrayList<>();

    @OneToMany(mappedBy = "make", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MakeRegion> regions = new ArrayList<>();

    // Lifecycle hook — auto-generate slug if not set
    @PrePersist
    @PreUpdate
    public void generateSlug() {
        if (this.name != null) {
            this.slug = this.name.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("[\\s]+", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
        }
    }
}
