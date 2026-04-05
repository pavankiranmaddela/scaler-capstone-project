package com.pk.ev.vehicle.catalog.customer.repository;

import com.pk.ev.vehicle.catalog.customer.domain.CustomerVehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerVehicleRepository extends JpaRepository<CustomerVehicle, UUID> {

    // ─── Customer-scoped queries ──────────────────────────────────────────────

    /** All vehicles in a customer's garage, newest first */
    List<CustomerVehicle> findByUserIdOrderByAddedAtDesc(UUID userId);

    /** Single vehicle — enforces ownership */
    Optional<CustomerVehicle> findByIdAndUserId(UUID id, UUID userId);

    /** The current primary vehicle for a user — at most one */
    Optional<CustomerVehicle> findByUserIdAndIsPrimaryTrue(UUID userId);

    /** Count vehicles in a user's garage — used for display badge */
    long countByUserId(UUID userId);

    // ─── Primary flag management ──────────────────────────────────────────────

    /**
     * Clears the isPrimary flag on all vehicles for a user before
     * setting a new primary. Ensures the one-primary-per-user invariant.
     */
    @Modifying
    @Query("UPDATE CustomerVehicle cv SET cv.isPrimary = false WHERE cv.userId = :userId")
    void clearPrimaryFlagForUser(@Param("userId") UUID userId);

    // ─── Admin queries ────────────────────────────────────────────────────────

    /**
     * Paginated list of all garage entries, optionally filtered by userId.
     * Admin-only — customers never hit this query path.
     */
    @Query("""
        SELECT cv FROM CustomerVehicle cv
        WHERE (:userId IS NULL OR cv.userId = :userId)
        ORDER BY cv.addedAt DESC
        """)
    Page<CustomerVehicle> findAllByOptionalUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    // ─── Compatible stations lookup support ───────────────────────────────────

    /**
     * Fetches a CustomerVehicle with its full VariantListing chain eagerly loaded.
     * Used by getCompatibleStations to avoid N+1 when the compatibility engine
     * needs ChargingConfiguration → ChargingSpecs.
     */
    @Query("""
        SELECT cv FROM CustomerVehicle cv
        JOIN FETCH cv.variantListing vl
        JOIN FETCH vl.chargingConfiguration cc
        LEFT JOIN FETCH cc.chargingSpecs
        JOIN FETCH vl.model m
        JOIN FETCH m.make
        WHERE cv.id = :id AND cv.userId = :userId
        """)
    Optional<CustomerVehicle> findByIdAndUserIdWithDetails(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );
}
