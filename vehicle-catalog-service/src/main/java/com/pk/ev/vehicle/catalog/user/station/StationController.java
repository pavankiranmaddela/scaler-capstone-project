package com.pk.ev.vehicle.catalog.user.station;

import com.pk.ev.vehicle.catalog.user.dtos.UserRegistrationDto;
import com.pk.ev.vehicle.catalog.user.dtos.UserDto;
import com.pk.ev.vehicle.catalog.user.model.EvUser;
import com.pk.ev.vehicle.catalog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stations")
@PreAuthorize("hasRole('ROLE_EV_STATION_ADMIN')")
public class StationController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserRegistrationDto registrationDto) {
        EvUser customer = UserRegistrationDto.toUser(registrationDto, passwordEncoder);
        EvUser created = userService.addEvUser(customer);
        return ResponseEntity.ok(UserDto.fromEvUser(created));
    }

    @PutMapping("/employee/update")
    public ResponseEntity<UserDto> updateEmployee(@RequestBody UserDto userDto) {
        EvUser existing = userService.getEvUserById(userDto.getId());
        EvUser evUser = userService.updateEvUser(existing.getId(), UserDto.toEvUser(userDto));

        return ResponseEntity.ok(UserDto.fromEvUser(evUser));
    }

    @DeleteMapping("/employee/delete")
    public ResponseEntity<Void> deleteEmployee(@RequestParam String id) {
        userService.deleteEvUser(id);
        return ResponseEntity.noContent().build();
    }

}
