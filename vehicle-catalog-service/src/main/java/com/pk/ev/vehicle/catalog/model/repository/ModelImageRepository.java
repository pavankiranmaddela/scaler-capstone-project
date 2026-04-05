package com.pk.ev.vehicle.catalog.model.repository;

import com.pk.ev.vehicle.catalog.model.model.ModelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelImageRepository extends JpaRepository<ModelImage, UUID> {

    List<ModelImage> findByModelId(UUID modelId);

    Optional<ModelImage> findByIdAndModelId(UUID imageId, UUID modelId);

    boolean existsByModelIdAndIsPrimaryTrue(UUID modelId);

    // Clear primary flag on all images for a model before setting a new one
    @Modifying
    @Query("UPDATE ModelImage i SET i.isPrimary = false WHERE i.model.id = :modelId")
    void clearPrimaryFlag(@Param("modelId") UUID modelId);
}
