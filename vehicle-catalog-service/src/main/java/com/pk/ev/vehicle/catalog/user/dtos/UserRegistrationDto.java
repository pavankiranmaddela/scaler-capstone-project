package com.pk.ev.vehicle.catalog.user.dtos;

import com.pk.ev.vehicle.catalog.user.model.EvUser;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {
    private String email;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    // Add other registration fields as needed

    public static EvUser toUser(UserRegistrationDto dto, PasswordEncoder encoder) {
        EvUser customer = new EvUser();
        customer.setEmail(dto.getEmail());
        customer.setPassword(encoder.encode(dto.getPassword()));
        customer.setFirstName(dto.getFirstName());
        customer.setMiddleName(dto.getMiddleName());
        customer.setLastName(dto.getLastName());
        customer.setFullName(dto.getFullName());
        // Set other fields as needed
        return customer;
    }
}
