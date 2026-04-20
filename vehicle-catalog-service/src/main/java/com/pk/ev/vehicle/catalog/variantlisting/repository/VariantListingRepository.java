package com.pk.ev.vehicle.catalog.variantlisting.repository;

import com.pk.ev.vehicle.catalog.trim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VariantListingRepository extends JpaRepository<VariantListing, UUID> {

    // All variants for a model — used by model detail page
    @Query("""
        SELECT vl FROM VariantListing vl
        JOIN FETCH vl.trim
        JOIN FETCH vl.batteryPack
        JOIN FETCH vl.chargingConfiguration
        WHERE vl.model.id = :modelId
          AND (:status IS NULL OR vl.status = :status)
        ORDER BY vl.sortOrder ASC, vl.priceInr ASC
        """)
    List<VariantListing> findByModelIdWithDetails(
            @Param("modelId") UUID modelId,
            @Param("status") VariantStatus status
    );

    // The 4-way unique key lookup — used for duplicate guard
    boolean existsByModelIdAndTrimIdAndBatteryPackIdAndChargingConfigurationId(
            UUID modelId, UUID trimId, UUID batteryPackId, UUID chargingConfigId
    );

    Optional<VariantListing> findByModelIdAndTrimIdAndBatteryPackIdAndChargingConfigurationId(
            UUID modelId, UUID trimId, UUID batteryPackId, UUID chargingConfigId
    );

    // Filterable paginated list across models
    @Query("""
        SELECT vl FROM VariantListing vl
        JOIN FETCH vl.trim t
        JOIN FETCH vl.batteryPack bp
        JOIN FETCH vl.chargingConfiguration cc
        JOIN vl.model m
        WHERE (:modelId       IS NULL OR m.id            = :modelId)
          AND (:trimId        IS NULL OR t.id             = :trimId)
          AND (:minPrice      IS NULL OR vl.priceInr     >= :minPrice)
          AND (:maxPrice      IS NULL OR vl.priceInr     <= :maxPrice)
          AND (:minBatteryKwh IS NULL OR bp.capacityKwh  >= :minBatteryKwh)
          AND (:status        IS NULL OR vl.status        = :status)
        """)
    Page<VariantListing> findAllByFilters(
            @Param("modelId")       UUID modelId,
            @Param("trimId")        UUID trimId,
            @Param("minPrice")      BigDecimal minPrice,
            @Param("maxPrice")      BigDecimal maxPrice,
            @Param("minBatteryKwh") BigDecimal minBatteryKwh,
            @Param("status")        VariantStatus status,
            Pageable pageable
    );

    // Fetch single variant with all associations for detail/compatibility
    @Query("""
        SELECT vl FROM VariantListing vl
        JOIN FETCH vl.model m
        JOIN FETCH vl.trim t
        JOIN FETCH vl.batteryPack bp
        JOIN FETCH vl.chargingConfiguration cc
        LEFT JOIN FETCH cc.chargingSpecs
        WHERE vl.id = :id
        """)
    Optional<VariantListing> findByIdWithAllDetails(@Param("id") UUID id);
}
