package com.pk.ev.vehicle.catalog.vehiclemake.repository;

import com.pk.ev.vehicle.catalog.vehiclemake.model.MakeRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MakeRegionRepository extends JpaRepository<MakeRegion, UUID> {

    List<MakeRegion> findByMakeId(UUID makeId);

    boolean existsByMakeIdAndRegionCode(UUID makeId, String regionCode);

    void deleteByMakeIdAndRegionCode(UUID makeId, String regionCode);
}
