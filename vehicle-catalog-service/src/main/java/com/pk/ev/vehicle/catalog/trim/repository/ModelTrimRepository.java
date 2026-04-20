package com.pk.ev.vehicle.catalog.trim.repository;

import com.pk.ev.vehicle.catalog.trim.model.ModelTrim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelTrimRepository extends JpaRepository<ModelTrim, UUID> {

    List<ModelTrim> findByModelIdAndIsActiveTrueOrderBySortOrderAsc(UUID modelId);

    List<ModelTrim> findByModelIdOrderBySortOrderAsc(UUID modelId);

    Optional<ModelTrim> findByIdAndModelId(UUID id, UUID modelId);

    boolean existsByModelIdAndTrimName(UUID modelId, String trimName);
}
