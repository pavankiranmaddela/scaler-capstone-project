package com.pk.ev.vehicle.catalog.user.admin;

import com.pk.ev.vehicle.catalog.user.dtos.UserDto;
import com.pk.ev.vehicle.catalog.user.dtos.UserRegistrationDto;
import com.pk.ev.vehicle.catalog.user.enums.EvUserRole;
import com.pk.ev.vehicle.catalog.user.model.EvUser;
import com.pk.ev.vehicle.catalog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/admin")
@PreAuthorize("hasRole('ROLE_EV_APP_ADMIN')")
public class EvAdminController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> dtos = userService.getAllEvUsers().stream()
                .map(UserDto::fromEvUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        EvUser evUser = userService.getEvUserById(id);
        if (evUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserDto.fromEvUser(evUser));
    }

    @PostMapping("/createStationAdmin")
    public ResponseEntity<UserDto> addStationAdmin(UserRegistrationDto registrationDto) {
        EvUser stationAdmin = UserRegistrationDto.toUser(registrationDto, passwordEncoder);

        EvUser created = userService.addEvUser(stationAdmin);
        return ResponseEntity.ok(UserDto.fromEvUser(created));
    }

    @PostMapping("/createAdmin")
    public ResponseEntity<UserDto> addAdmin(UserRegistrationDto registrationDto) {
        EvUser evAdmin = UserRegistrationDto.toUser(registrationDto, passwordEncoder);
        evAdmin.getRoles().add(EvUserRole.ROLE_EV_APP_ADMIN);
        EvUser created = userService.addEvUser(evAdmin);
        return ResponseEntity.ok(UserDto.fromEvUser(created));
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String id, @RequestBody UserDto UserDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        try {
            EvUser updated = userService.updateEvUserWithSecurity(id, UserDto.toEvUser(UserDto), currentUserEmail);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(UserDto.fromEvUser(updated));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        try {
            userService.deleteEvUserWithSecurity(id, currentUserEmail);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
