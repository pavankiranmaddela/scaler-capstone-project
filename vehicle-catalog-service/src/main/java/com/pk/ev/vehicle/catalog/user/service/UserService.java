package com.pk.ev.vehicle.catalog.user.service;

import com.pk.ev.vehicle.catalog.user.model.EvUser;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<EvUser> getAllEvUsers();

    EvUser getEvUserById(String id);

    EvUser getEvUserById(UUID uuid);

    EvUser addEvUser(EvUser EvUser);

    EvUser updateEvUser(String id, EvUser evUser);

    EvUser updateEvUser(UUID uuid, EvUser evUser);

    void deleteEvUser(String id);

    EvUser updateEvUserWithSecurity(String id, EvUser evUser, String currentUserEmail);

    void deleteEvUserWithSecurity(String id, String currentUserEmail);
}
