package com.pk.ev.vehicle.catalog.user.service;

import com.pk.ev.vehicle.catalog.user.enums.EvUserRole;
import com.pk.ev.vehicle.catalog.user.model.EvUser;
import com.pk.ev.vehicle.catalog.user.repositoty.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<EvUser> getAllEvUsers() {
        return userRepository.findAll();
    }

    @Override
    public EvUser getEvUserById(String id) {
        Optional<EvUser> EvUser = userRepository.findById(id);
        return EvUser.orElse(null);
    }

    @Override
    public EvUser getEvUserById(UUID uuid) {
        Optional<EvUser> EvUser = userRepository.findById(uuid);
        return EvUser.orElse(null);
    }

    @Override
    @Transactional
    public EvUser addEvUser(EvUser EvUser) {
        boolean exists = userRepository.findAll().stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(EvUser.getEmail()));
        if (exists) {
            throw new IllegalArgumentException("Email already exists");
        }
        return userRepository.save(EvUser);
    }

    @Override
    @Transactional
    public EvUser updateEvUser(String id, EvUser evUser) {
        if (!userRepository.existsById(id)) {
            return null;
        }
        evUser.setId(UUID.fromString(id));
        return userRepository.save(evUser);
    }

    @Override
    public EvUser updateEvUser(UUID uuid, EvUser evUser) {
        return null;
    }

    @Override
    @Transactional
    public void deleteEvUser(String id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public EvUser updateEvUserWithSecurity(String id, EvUser evUser, String currentUserEmail) {
        EvUser currentUser = userRepository.findAll().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(currentUserEmail))
                .findFirst().orElse(null);
        if (currentUser == null) {
            throw new SecurityException("Unauthorized");
        }
        boolean isAdmin = currentUser.getRoles().contains(EvUserRole.ROLE_EV_APP_ADMIN);
        boolean isSelf = currentUser.getId().equals(id);
        if (!isAdmin && !isSelf) {
            throw new SecurityException("Forbidden");
        }
        if (!userRepository.existsById(id)) {
            return null;
        }
        evUser.setId(UUID.fromString(id));
        return userRepository.save(evUser);
    }

    @Override
    @Transactional
    public void deleteEvUserWithSecurity(String id, String currentUserEmail) {
        EvUser currentUser = userRepository.findAll().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(currentUserEmail))
                .findFirst().orElse(null);
        if (currentUser == null) {
            throw new SecurityException("Unauthorized");
        }
        boolean isAdmin = currentUser.getRoles().contains(EvUserRole.ROLE_EV_APP_ADMIN);
        boolean isSelf = currentUser.getId().equals(id);
        if (!isAdmin && !isSelf) {
            throw new SecurityException("Forbidden");
        }
        userRepository.deleteById(id);
    }
}
