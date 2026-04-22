package com.pk.ev.vehicle.catalog.vehiclemake.repository;

import com.pk.ev.vehicle.catalog.vehiclemake.enums.MakeStatus;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleMakeRepository extends JpaRepository<VehicleMake, UUID> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Optional<VehicleMake> findBySlug(String slug);

    // Paginated list filterable by status and/or country
    @Query("""
        SELECT m FROM VehicleMake m
        WHERE (:status IS NULL OR m.status = :status)
          AND (:country IS NULL OR m.countryOfOrigin = :country)
        """)
    Page<VehicleMake> findAllByFilters(
            @Param("status") MakeStatus status,
            @Param("country") String country,
            Pageable pageable
    );
}
