package com.pk.ev.vehicle.catalog.make.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "make_regions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_make_region",
                columnNames = {"make_id", "region_code"}
        ),
        indexes = {
                @Index(name = "idx_make_region_make_id", columnList = "make_id"),
                @Index(name = "idx_make_region_code", columnList = "region_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MakeRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "make_id", nullable = false, foreignKey = @ForeignKey(name = "fk_make_region_make"))
    private VehicleMake make;

    @Column(name = "region_code", nullable = false, length = 2)
    private String regionCode;  // ISO 3166-1 alpha-2

    @Column(name = "launch_year")
    private Integer launchYear;
}
