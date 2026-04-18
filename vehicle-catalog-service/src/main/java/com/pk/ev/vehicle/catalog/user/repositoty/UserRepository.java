package com.pk.ev.vehicle.catalog.user.repositoty;

import com.pk.ev.vehicle.catalog.user.model.EvUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<EvUser, UUID> {
  Optional<EvUser> findByEmail(String email);
  Optional<EvUser> findById(String id);
  boolean existsById(String id);
  void deleteById(String userId);
}
