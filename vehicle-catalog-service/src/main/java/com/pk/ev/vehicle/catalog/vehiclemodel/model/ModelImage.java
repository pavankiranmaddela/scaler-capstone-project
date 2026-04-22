package com.pk.ev.vehicle.catalog.vehiclemodel.model;

import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ImageAngle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "model_images",
        indexes = {
                @Index(name = "idx_model_image_model_id", columnList = "model_id"),
                @Index(name = "idx_model_image_primary",  columnList = "model_id, is_primary")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_model_image_model"))
    private VehicleModel model;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "angle", length = 20)
    private ImageAngle angle;   // FRONT / SIDE / REAR / INTERIOR / CHARGING_PORT

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private Instant uploadedAt;
}
