package com.pk.ev.vehicle.catalog.battery.repository;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatteryPackRepository extends JpaRepository<BatteryPack, UUID> {

    List<BatteryPack> findByModelIdAndIsActiveTrue(UUID modelId);

    List<BatteryPack> findByModelId(UUID modelId);

    Optional<BatteryPack> findByIdAndModelId(UUID id, UUID modelId);

    boolean existsByModelIdAndCapacityKwh(UUID modelId, BigDecimal capacityKwh);
}
