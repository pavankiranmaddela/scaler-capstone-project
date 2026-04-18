package com.pk.ev.vehicle.catalog.user.customer;

import com.pk.ev.vehicle.catalog.user.dtos.UserDto;
import com.pk.ev.vehicle.catalog.user.dtos.UserRegistrationDto;
import com.pk.ev.vehicle.catalog.user.enums.EvUserRole;
import com.pk.ev.vehicle.catalog.user.model.EvUser;
import com.pk.ev.vehicle.catalog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/customers/register")
    public ResponseEntity<UserDto> register(@RequestBody UserRegistrationDto registrationDto) {
        EvUser customer = UserRegistrationDto.toUser(registrationDto, passwordEncoder);
        customer.getRoles().add(EvUserRole.ROLE_CUSTOMER);
        EvUser created = userService.addEvUser(customer);
        return ResponseEntity.ok(UserDto.fromEvUser(created));
    }
}
