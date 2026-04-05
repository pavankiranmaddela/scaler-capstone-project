package com.pk.ev.vehicle.catalog.chargingstandard.model;

import com.pk.ev.vehicle.catalog.chargingstandard.enums.ChargingStandardType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Static reference catalog of every charging protocol the platform knows about.
 * Seeded once by Admin; read by everyone.
 *
 * Relationship to the rest of the model:
 *   ChargingStandard ← (referenced by UUID FK) ─ VehicleChargingSpec.chargingStandardId
 *
 * We use a loose UUID FK rather than a JPA @ManyToOne so that the catalog
 * service can be deployed / seeded independently without coupling migrations.
 * The compatibility engine resolves the standard at query time.
 *
 * Seed examples:
 *   CCS2         | CCS Combo 2        | DC   | 350,000 W | Europe/India | IEC
 *   TYPE2        | IEC 62196 Type 2   | AC   |  22,000 W | Europe/India | IEC
 *   CHAdeMO      | CHAdeMO 2.0        | DC   | 400,000 W | Global       | CHAdeMO
 *   TESLA_NACS   | Tesla NACS         | BOTH | 250,000 W | USA          | Tesla/SAE
 *   GBT_AC       | GB/T 20234.2       | AC   |  43,000 W | China        | GB/T
 *   GBT_DC       | GB/T 20234.3       | DC   | 237,500 W | China        | GB/T
 *   BDC          | Bharat DC-001      | DC   |  15,000 W | India        | BIS
 *   BAC          | Bharat AC-001      | AC   |  10,000 W | India        | BIS
 */
@Entity
@Table(
        name = "charging_standards",
        indexes = {
                @Index(name = "idx_charging_std_short_code",    columnList = "short_code", unique = true),
                @Index(name = "idx_charging_std_connector",     columnList = "connector_type"),
                @Index(name = "idx_charging_std_region",        columnList = "geographic_region"),
                @Index(name = "idx_charging_std_deprecated",    columnList = "is_deprecated")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Full name — e.g. "CCS Combo 2", "Bharat DC-001"
    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    // Short code used in APIs and UI — e.g. "CCS2", "BDC", "TYPE2"
    @Column(name = "short_code", nullable = false, unique = true, length = 30)
    private String shortCode;

    // Maps to our ConnectorType enum so we can join with VehicleChargingSpec
    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", nullable = false, length = 20)
    private ConnectorType connectorType;

    // Whether this standard is AC, DC, or both (e.g. CCS)
    @Enumerated(EnumType.STRING)
    @Column(name = "current_type", nullable = false, length = 10)
    private ChargingStandardType currentType;

    // Protocol-defined ceiling in Watts — e.g. 350000 for CCS2 350 kW
    @Column(name = "max_wattage", nullable = false)
    private Integer maxWattage;

    // Primary geographic market — "India", "Europe", "Global", "China", "USA"
    @Column(name = "geographic_region", length = 100)
    private String geographicRegion;

    // Standards body — "IEC", "SAE", "BIS", "CHAdeMO Association", "GB/T"
    @Column(name = "governing_body", length = 100)
    private String governingBody;

    // Version/revision string — e.g. "2.0", "3.0", "2020"
    @Column(name = "version", length = 20)
    private String version;

    // Short description or notes for admin UI
    @Column(name = "description", length = 500)
    private String description;

    // CDN URL for connector icon used in frontend
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    // Soft deprecation — old standards stay queryable, just flagged
    @Column(name = "is_deprecated", nullable = false)
    @Builder.Default
    private Boolean isDeprecated = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
