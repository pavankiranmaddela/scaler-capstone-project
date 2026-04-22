package com.pk.ev.vehicle.catalog.vehiclemodel.repository;

import com.pk.ev.vehicle.catalog.vehiclemodel.enums.ModelStatus;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, UUID> {

    // ─── Paginated filtered list ─────────────────────────────────────────────
    @Query("""
        SELECT DISTINCT m FROM VehicleModel m
        WHERE (:makeId         IS NULL OR m.make.id            = :makeId)
          AND (:year           IS NULL OR m.modelYear          = :year)
          AND (:status         IS NULL OR m.status             = :status)
        """)
    Page<VehicleModel> findAllByFilters(
            @Param("makeId")        UUID makeId,
            @Param("year")          Integer year,
            @Param("status")        ModelStatus status,
            Pageable pageable
    );

    // ─── Full-text search across make name + model name + model year ─────────
    @Query("""
        SELECT m FROM VehicleModel m
        JOIN m.make mk
        WHERE LOWER(CONCAT(mk.name, ' ', m.name, ' ', m.modelYear, ' '))
              LIKE LOWER(CONCAT('%', :query, '%'))
          AND (:status IS NULL OR m.status = :status)
        """)
    Page<VehicleModel> searchByKeyword(
            @Param("query")  String query,
            @Param("status") ModelStatus status,
            Pageable pageable
    );

    // ─── Existence check (used for de-dup guard in tests / import) ───────────
    boolean existsByMakeIdAndNameAndModelYear(UUID makeId, String name, Integer modelYear);
}
