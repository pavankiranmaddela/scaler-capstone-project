package com.pk.ev.vehicle.catalog.user.controller;

import com.pk.ev.vehicle.catalog.user.dtos.UserRegistrationDto;
import com.pk.ev.vehicle.catalog.user.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody UserRegistrationDto registrationDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(registrationDto.getEmail(), registrationDto.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String jwt = jwtUtil.generateToken(registrationDto.getEmail());
        return ResponseEntity.ok().body(jwt);
    }
}
